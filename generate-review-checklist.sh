#!/usr/bin/env bash

# Generates `.github/instructions/review.instructions.md` from the <!-- check:
# ... --> annotations in other instruction files.
#
# Each heading in the source instruction files produces a checklist item unless
# annotated with <!-- check: skip -->. Authors can override the default
# (heading text) with <!-- check: Custom text --> or produce multiple items
# with multiple <!-- check: ... --> lines.
#
# The output file should be committed to the repository. CI validates that it
# matches the regenerated output.

set -e -u -o pipefail

if ! command -v pandoc >/dev/null 2>&1; then
  echo 'This script requires `pandoc`; please install it.'
  exit 1
fi

PROJECT_ROOT="$(dirname "${0}")"
INSTRUCTIONS_DIR="${PROJECT_ROOT}/.github/instructions"
OUTPUT_FILE="${INSTRUCTIONS_DIR}/review.instructions.md"

LUA_FILTER=$(mktemp)
trap 'rm -f "${LUA_FILTER}"' INT TERM HUP EXIT

# A Pandoc Lua filter that extracts review checklist items from an instruction
# file's headings and <!-- check: ... --> annotations.
cat > "${LUA_FILTER}" << 'EOF'
local function inlines_to_text(inlines)
  local patched = inlines:walk {
    Code = function(el)
      return pandoc.Str('`' .. el.text .. '`')
    end,
  }
  return pandoc.utils.stringify(patched)
end

function Pandoc(doc)
  local file = doc.meta.file and pandoc.utils.stringify(doc.meta.file) or '?'
  local title
  local section = ''
  local pending = nil -- {level: integer, text: string}?
  local checks = {}
  local items = {}    -- {{section: string, text: string}, ...}

  local function flush()
    if not pending then return end

    -- 'skip' dominates: discard this heading and all its annotations.
    for _, c in ipairs(checks) do
      if c == 'skip' then
        pending = nil
        checks = {}
        return
      end
    end

    if #checks > 0 then
      for _, c in ipairs(checks) do
        items[#items + 1] = {section = section, text = c}
      end
    elseif pending.level >= 3 and pending.text ~= '' then
      items[#items + 1] = {section = section, text = pending.text}
    end

    pending = nil
    checks = {}
  end

  for _, block in ipairs(doc.blocks) do
    if block.t == 'Header' then
      flush()
      local text = inlines_to_text(block.content)
      local level = block.level

      if level == 1 then
        title = title or text
      else
        -- Only ## headings update the current section label.
        if level == 2 then
          section = text
        end
        pending = {level = level, text = text}
      end
    elseif block.t == 'RawBlock' and block.format == 'html' then
      -- Annotations must be single-line: <!-- check: text -->
      local val = block.text:match('^%s*<!%-%-%s*check:%s*(.-)%s*%-%->%s*$')
      if val then
        checks[#checks + 1] = val
      end
    end
  end
  flush()

  title = title or file
  if #items == 0 then return pandoc.Pandoc({}) end

  local out = '\n## ' .. title .. ' (`' .. file .. '`)\n'
  local cur_sec = nil
  for _, item in ipairs(items) do
    if item.section ~= cur_sec then
      cur_sec = item.section
      if cur_sec ~= '' then
        out = out .. '\n### ' .. cur_sec .. '\n\n'
      else
        out = out .. '\n'
      end
    end
    out = out .. '- [ ] ' .. item.text .. '\n'
  end

  return pandoc.Pandoc({pandoc.RawBlock('markdown', out)})
end
EOF

cat > "$OUTPUT_FILE" << 'EOF'
---
description: >
  Auto-generated review checklist derived from the project's instruction files.
  Do not edit manually; edit the source instruction files instead and then run
  `./generate-review-checklist.sh `to regenerate.
---

# Review Checklist

Before committing, or any time you are asked to review some aspect of this
project, verify each applicable item. Items are grouped by source instruction
file. Not all sections apply to every change; focus on the sections relevant to
the files you modified.
EOF

find "${INSTRUCTIONS_DIR}" \
    -name '*.instructions.md' \
    -not -name 'review.instructions.md' \
  | sort \
  | while read -r f; do
      echo "Processing ${f}..."
      pandoc \
        -f markdown+raw_html-smart \
        -t markdown-smart \
        --lua-filter "${LUA_FILTER}" \
        -M "file=$(basename "${f}")" \
        "${f}" >> "${OUTPUT_FILE}"
    done

echo "Generated ${OUTPUT_FILE}"
