#!/usr/bin/env bash

# Validates the structure and references of all agent skills in this project.
# The validation uses only tools available on the Ubuntu GitHub Actions runner.

set -e -u -o pipefail

if [ "${#}" -ne 0 ]; then
  echo "Usage: ${0}" >&2
  exit 1
fi

REQUIRED_COMMANDS=(
  awk
  basename
  dirname
  find
  grep
  git
  head
  mktemp
  sed
  sort
  tr
  tsort
)

for command_name in "${REQUIRED_COMMANDS[@]}"; do
  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "This script requires \`${command_name}\`; please install it." >&2
    exit 1
  fi
done

PROJECT_ROOT="$(git rev-parse --show-toplevel)"
SKILLS_DIR="${PROJECT_ROOT}/.agents/skills"
MAX_SKILL_LINES=49

tmp_dir="$(mktemp -d)"
trap 'rm -rf -- "${tmp_dir}"' INT TERM HUP EXIT
EDGES_FILE="${tmp_dir}/skill-edges.txt"
touch "${EDGES_FILE}"

has_errors=false

function report_error() {
  local skill_file="${1}"
  local message="${2}"

  echo "ERROR: ${skill_file}: ${message}" >&2
  has_errors=true
}

function check_frontmatter() {
  local skill_file="${1}"
  local relative_file="${skill_file#"${PROJECT_ROOT}/"}"
  local first_line
  local closing_line
  local invalid_line
  local name_count
  local name
  local description_count
  local description

  first_line="$(head -n 1 "${skill_file}")"
  if [ "${first_line}" != '---' ]; then
    report_error "${relative_file}" 'frontmatter must start on the first line'
    return 1
  fi

  closing_line="$(
    awk 'NR > 1 && $0 == "---" { print NR; exit }' "${skill_file}"
  )"
  if [ -z "${closing_line}" ]; then
    report_error "${relative_file}" 'frontmatter has no closing delimiter'
    return 1
  fi

  # Skills use a strict YAML subset: name plus a folded or literal description.
  invalid_line="$(
    awk -v end="${closing_line}" '
      NR <= 1 || NR >= end { next }
      /\t/ { print NR; exit }
      /^[[:space:]]*$/ || /^#/ { next }
      /^name:[[:space:]]+[^[:space:]].*$/ {
        in_description = 0
        next
      }
      /^description:[[:space:]]*[>|][+-]?[[:space:]]*$/ {
        in_description = 1
        next
      }
      in_description && /^  / { next }
      { print NR; exit }
    ' "${skill_file}"
  )"
  if [ -n "${invalid_line}" ]; then
    report_error \
      "${relative_file}" \
      "frontmatter violates the project YAML subset on line ${invalid_line}"
    return 1
  fi

  name_count="$(
    awk -v end="${closing_line}" \
      'NR > 1 && NR < end && /^name:[[:space:]]*/ { count++ }
       END { print count + 0 }' \
      "${skill_file}"
  )"
  if [ "${name_count}" -ne 1 ]; then
    report_error \
      "${relative_file}" \
      "frontmatter must contain exactly one name field; found ${name_count}"
    return 1
  fi

  name="$(
    awk -v end="${closing_line}" '
      NR > 1 && NR < end && /^name:[[:space:]]*/ {
        sub(/^name:[[:space:]]*/, "")
        print
        exit
      }
    ' "${skill_file}"
  )"
  case "${name}" in
    '' | -* | *- | *--* | *[!a-z0-9-]*)
      report_error \
        "${relative_file}" \
        "frontmatter name \`${name}\` is not lowercase kebab-case"
      return 1
      ;;
  esac

  description_count="$(
    awk -v end="${closing_line}" \
      'NR > 1 && NR < end && /^description:[[:space:]]*/ { count++ }
       END { print count + 0 }' \
      "${skill_file}"
  )"
  if [ "${description_count}" -ne 1 ]; then
    report_error \
      "${relative_file}" \
      "frontmatter must contain exactly one description field;" \
      "found ${description_count}"
    return 1
  fi

  description="$(
    awk -v end="${closing_line}" '
      NR > 1 && NR < end && /^description:[[:space:]]*/ {
        reading = 1
        sub(/^description:[[:space:]]*/, "")
        if ($0 !~ /^[>|][+-]?$/) {
          printf "%s", $0
          separator = " "
        }
        next
      }
      NR > 1 && NR < end && reading {
        if (/^#/) { next }
        if (/^[^[:space:]][^:]*:/) {
          exit
        }
        sub(/^[[:space:]]+/, "")
        if ($0 != "") {
          printf "%s%s", separator, $0
          separator = " "
        }
      }
    ' "${skill_file}"
  )"
  case "${description}" in
    'Use this skill '*) ;;
    *)
      report_error \
        "${relative_file}" \
        'description must start with `Use this skill`'
      return 1
      ;;
  esac
}

function extract_name() {
  local skill_file="${1}"

  awk '/^name:[[:space:]]*/ {
    sub(/^name:[[:space:]]*/, "")
    print
    exit
  }' "${skill_file}"
}

function check_name_and_size() {
  local skill_file="${1}"
  local name="${2}"
  local relative_file="${skill_file#"${PROJECT_ROOT}/"}"
  local directory_name
  local heading
  local heading_slug
  local line_count

  directory_name="$(basename "$(dirname "${skill_file}")")"
  if [ "${directory_name}" != "${name}" ]; then
    report_error \
      "${relative_file}" \
      "directory name \`${directory_name}\` does not match" \
      "skill name \`${name}\`"
  fi

  heading="$(
    awk '/^# / { sub(/^# /, ""); print; exit }' "${skill_file}"
  )"
  heading_slug="$(
    printf '%s' "${heading}" \
      | tr '[:upper:]' '[:lower:]' \
      | sed -E 's/[^a-z0-9]+/-/g; s/^-//; s/-$//'
  )"
  if [ -z "${heading}" ]; then
    report_error "${relative_file}" 'skill must contain a level-one heading'
  elif [ "${heading_slug}" != "${name}" ]; then
    report_error \
      "${relative_file}" \
      "heading slug \`${heading_slug}\` does not match skill name \`${name}\`"
  fi

  line_count="$(awk 'END { print NR }' "${skill_file}")"
  if [ "${line_count}" -gt "${MAX_SKILL_LINES}" ]; then
    report_error \
      "${relative_file}" \
      "skill has ${line_count} lines; expected fewer than 50"
  fi
}

function check_references() {
  local skill_file="${1}"
  local name="${2}"
  local relative_file="${skill_file#"${PROJECT_ROOT}/"}"
  local skill_dir
  local target
  local target_path
  local invoked_skill
  local script

  skill_dir="$(dirname "${skill_file}")"
  while IFS= read -r target; do
    case "${target}" in
      \#* | http://* | https://* | mailto:*) continue ;;
    esac

    target_path="${target%%#*}"
    if [ ! -e "${skill_dir}/${target_path}" ]; then
      report_error \
        "${relative_file}" \
        "relative link target \`${target}\` does not exist"
      continue
    fi

  done < <(
    sed -n -E \
      's/^\[[^]]+\]:[[:space:]]+([^[:space:]]+).*$/\1/p' \
      "${skill_file}"
  )

  while IFS= read -r invoked_skill; do
    if [ ! -f "${SKILLS_DIR}/${invoked_skill}/SKILL.md" ]; then
      report_error \
        "${relative_file}" \
        "invoked skill \`/${invoked_skill}\` does not exist"
      continue
    fi
    printf '%s %s\n' "${name}" "${invoked_skill}" >> "${EDGES_FILE}"
  done < <(
    grep -oE '`/[a-z0-9]+(-[a-z0-9]+)*`' "${skill_file}" \
      | sed -E 's#^`/##; s#`$##' \
      | sort -u \
      || true
  )

  while IFS= read -r script; do
    if [ ! -f "${PROJECT_ROOT}/${script#./}" ]; then
      report_error \
        "${relative_file}" \
        "referenced script \`${script}\` does not exist"
    fi
  done < <(
    grep -oE '`[^`]+`' "${skill_file}" \
      | grep -oE \
        '(^|[[:space:]])\./[^`[:space:]]+\.sh([[:space:]]|$)' \
      | sed -E 's/^[[:space:]]*//; s/[[:space:]]*$//' \
      | sort -u || true
  )
}

function check_cycles() {
  local cycle_output="${tmp_dir}/cycles.txt"

  if ! tsort "${EDGES_FILE}" >/dev/null 2> "${cycle_output}"; then
    report_error 'skill invocation graph' 'contains a cycle'
    sed 's/^/  /' "${cycle_output}" >&2
  fi
}

function main() {
  local skill_count=0
  local skill_file
  local name

  if [ ! -d "${SKILLS_DIR}" ]; then
    echo "ERROR: skill directory not found: ${SKILLS_DIR}" >&2
    exit 1
  fi

  while IFS= read -r skill_file; do
    if check_frontmatter "${skill_file}"; then
      name="$(extract_name "${skill_file}")"
      check_name_and_size "${skill_file}" "${name}"
      check_references "${skill_file}" "${name}"
    fi
    skill_count=$((skill_count + 1))
  done < <(
    find "${SKILLS_DIR}" -mindepth 2 -maxdepth 2 -name SKILL.md | sort
  )

  if [ "${skill_count}" -eq 0 ]; then
    report_error '.agents/skills' 'contains no skills'
  fi

  check_cycles

  if [ "${has_errors}" = true ]; then
    exit 1
  fi

  echo "Validated ${skill_count} agent skills."
}

main
