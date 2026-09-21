# Pull Request Conventions

This document describes the conventions for creating and formatting pull
requests in this repository. For commit message conventions (which determine
the PR title and suggested commit message), see
[`commit-message.instructions.md`][commit-message].

## PR title
<!-- check: PR title matches the commit message summary (without `(#N)`) -->

The PR title is exactly the commit message summary line, without the `(#N)`
suffix. GitHub appends the PR number automatically on squash-merge.

If this PR comprises multiple commits, generate a new commit message based on
the full diff, as if all changes were part of a single commit.

## Draft status
<!-- check: PR opened by an AI agent is created as a draft, not marked ready for review -->

When an AI agent opens the pull request, open it as a draft (e.g. `gh pr create
--draft`). The agent's owner reviews the changes and confirms that all checks
pass, then marks the PR ready for review. Only at that point are reviewers
requested (see [Reviewers](#reviewers)) and maintainers expected to take a
look.

## Label
<!-- check: PR has exactly one label from `.github/release.yml` -->

Assign exactly one label from [`.github/release.yml`][github-release]. Read
that file for the available labels and their release note categories.

## Milestone
<!-- check: PR targets the nearest upcoming milestone -->

Assign the nearest upcoming open milestone. For dependency upgrade PRs with the
`dependencies` label, this is handled automatically by
[`.github/workflows/assign-milestone.yml`][assign-milestone-workflow].

## Reviewers
<!-- check: PR does not manually assign reviewers -->

Do not assign reviewers manually. [`CODEOWNERS`][codeowners] causes GitHub to
automatically request a review from the relevant owners once the PR is marked
ready for review (see [Draft status](#draft-status)).

## Description format
<!-- check: PR description contains a suggested commit message in a fenced code block -->

Use the following format:

~~~
Suggested commit message:
```
{full commit message, initially without the (#N) suffix}
```

{Optional additional context not covered by the commit message.}
~~~

After the PR is created and the PR number is known, edit the description to add
the ` (#N)` suffix to the summary line inside the code block.

[assign-milestone-workflow]: ../workflows/assign-milestone.yml
[codeowners]: ../CODEOWNERS
[commit-message]: commit-message.instructions.md
[github-release]: ../release.yml
