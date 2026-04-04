---
applyTo: "**/*.sh"
---

# Shell Script Conventions

This document describes the conventions used by shell scripts in this project.
All scripts are written in Bash.

## Structure
<!-- check: skip -->

### Shebang and safety flags
<!-- check: Script starts with `#!/usr/bin/env bash` and `set -e -u -o pipefail` -->

Every script must start with the portable shebang and strict error handling
flags:

```sh
#!/usr/bin/env bash

set -e -u -o pipefail
```

- `-e`: exit immediately on command failure.
- `-u`: treat unset variables as errors.
- `-o pipefail`: propagate failures through pipes.

### Header comment
<!-- check: Script has a header comment explaining its purpose -->

After the shebang and before `set`, include a comment block explaining what the
script does and any prerequisites or constraints:

```sh
#!/usr/bin/env bash

# Compiles the code using Error Prone and applies its suggestions. The set of
# checks applied can optionally be restricted by name.
#
# As this script may modify the project's code, it is important to execute it
# in a clean Git working directory.

set -e -u -o pipefail
```

Use `#` on each line. Explain purpose, prerequisites, and constraints. Comments
explain _why_, not _what_.

## Variables
<!-- check: skip -->

### Naming
<!-- check: Variables use the correct casing convention -->

Use `UPPER_CASE` for constants and script-level configuration. Use `lower_case`
(snake_case) for variables local to functions:

```sh
# Script-level constants.
PROJECT_ROOT="$(dirname "${0}")"
INSTRUCTIONS_DIR="${PROJECT_ROOT}/.github/instructions"

function do_work() {
  local src="${1}"
  local output_file="${PROJECT_ROOT}/out.txt"
}
```

### Quoting
<!-- check: All variable expansions are quoted with `"${var}"` -->

Always quote variable expansions using the `"${var}"` form. This protects
against word splitting and globbing:

```sh
# Do:
echo "${PROJECT_ROOT}/file.txt"
if [ "${#}" -gt 1 ]; then

# Don't:
echo $PROJECT_ROOT/file.txt
if [ $# -gt 1 ]; then
```

### Command substitution
<!-- check: Command substitutions use `$(...)` syntax -->

Always use `$(...)` for command substitution. Never use backticks:

```sh
# Do:
root="$(git rev-parse --show-toplevel)"

# Don't:
root=`git rev-parse --show-toplevel`
```

## Functions
<!-- check: skip -->

### Declaration
<!-- check: Functions use `function name() {` syntax with `local` for all variables -->

Use the `function` keyword. Declare all function variables with `local`:

```sh
function patch() {
  local current_diff="${1}"
  local new_diff
  new_diff="$(git diff)"
}
```

When a variable's value comes from a command substitution that may fail, split
the declaration and assignment so that `set -e` can catch the failure:

```sh
# Do:
local new_diff
new_diff="$(might_fail)"

# Don't (masks non-zero exit):
local new_diff="$(might_fail)"
```

### Naming
<!-- check: skip -->

Use `snake_case` for function names.

## Conditionals and tests
<!-- check: Conditionals use `[ ]` (not `[[ ]]`) -->

Use `[ ]` (POSIX test) for conditionals. Reserve `[[ ]]` for cases that
genuinely require pattern matching or regex:

```sh
if [ "${#}" -gt 1 ]; then
  echo "Usage: ${0} [PatchChecks]" >&2
  exit 1
fi
```

<!-- XXX: Consider consistently using safer Bash `[[ ]]` variant instead. -->

## Error handling
<!-- check: skip -->

### Argument validation
<!-- check: Optional arguments use `${1:-}` and required arguments use `${1:?message}` -->

Validate argument counts early with a usage message:

```sh
if [ "${#}" -gt 1 ]; then
  echo "Usage: ${0} [PatchChecks]" >&2
  exit 1
fi
```

Use `${1:-}` for optional arguments and `${1:?Specify a branch}` for required
arguments.

### Prerequisite checks
<!-- check: skip -->

Check for required tools before doing work:

```sh
if ! command -v pandoc >/dev/null 2>&1; then
  echo 'This script requires `pandoc`; please install it.' >&2
  exit 1
fi
```

### Output to stderr
<!-- check: Error and usage messages use `echo "..." >&2` -->

Send error and usage messages to stderr:

```sh
echo "Error: file not found." >&2
```

Use the `echo "..." >&2` form consistently (not `>&2 echo` or `1>&2`).

### Exit codes
<!-- check: skip -->

Use `exit 1` for errors. Rely on implicit `exit 0` for success (do not write an
explicit `exit 0`).

## Temporary files
<!-- check: Temporary files use `mktemp -d` with a `trap` cleanup handler -->

Use `mktemp -d` for temporary directories (or `mktemp` for temporary files) and
register a `trap` for cleanup immediately:

```sh
tmp_dir="$(mktemp -d)"
trap 'rm -rf -- "${tmp_dir}"' INT TERM HUP EXIT
```

Always trap all four signals: `INT` (Ctrl+C), `TERM` (kill), `HUP` (hangup),
and `EXIT` (normal exit). Always include `--` in the `rm` command to prevent
filename interpretation.

## Path handling
<!-- check: skip -->

Prefer `git rev-parse --show-toplevel` for the repository root and `dirname
"${0}"` for paths relative to the script. Use `git -C` when operating on a Git
repository in another directory. Avoid bare `cd` in the main script flow; use
`pushd`/`popd` for temporary directory changes.

## Formatting
<!-- check: skip -->

### Indentation
<!-- check: Two-space indentation (no tabs) -->

Use two spaces. Never use tabs.

### Line continuation
<!-- check: skip -->

Break long commands with backslash continuation. Place each flag/argument on
its own line, indented:

```sh
mvn ${shared_build_flags} \
  -Derror-prone.configuration-args="${error_prone_validation_flags}" \
  ${validation_build_flags}
```

### Pipe chains
<!-- check: skip -->

For multi-line pipe chains, place `|` at the start of each line:

```sh
find "${INSTRUCTIONS_DIR}" \
    -name '*.instructions.md' \
    -not -name 'review.instructions.md' \
  | sort \
  | while read -r src; do
      process "${src}"
    done
```

### Heredocs
<!-- check: skip -->

Use `<< 'EOF'` (single-quoted) when no variable expansion is needed and `<<
EOF` when expansion is required. Always use `EOF` as the delimiter.
