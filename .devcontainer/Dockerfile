# A sandboxed development environment for running Claude Code with
# `--dangerously-skip-permissions`.

FROM library/node:25.9.0-trixie@sha256:6caf08ae7d5c8b8c3455161bee6ab84e658458cb0855f47172ccbe22c8be54ba

# Install additional Debian packages. Recent releases of `gh` are available
# only from the GitHub CLI marketing site.
# XXX: Extend this list if Claude Code is observed trying to use missing
# commands.
# XXX: Drop `pandoc` installation if `./generate-review-checklist.sh` is
# migrated to Java.
RUN curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg \
    -o /etc/apt/keyrings/githubcli-archive-keyring.gpg \
  && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" \
    > /etc/apt/sources.list.d/github-cli.list \
  && apt-get update \
  && apt-get install -y --no-install-recommends \
    fzf \
    gh \
    jq \
    less \
    man-db \
    pandoc \
    python3 \
    vim \
    xxd \
    zip \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

ARG USERNAME=node
# Rename the `node` user/group to `${USERNAME}` (keeping UID/GID 1000) so that
# the container identity matches the host user. This is a workaround for
# https://github.com/anthropics/claude-code/issues/15717.
RUN if [ "${USERNAME}" != "node" ]; then \
    groupmod -n "${USERNAME}" node \
      && usermod -l "${USERNAME}" -d "/home/${USERNAME}" -m node; \
    fi
USER ${USERNAME}
WORKDIR /home/${USERNAME}

# Pre-create directories that will receive bind mounts or be written to by
# tools at runtime. This ensures correct ownership and prevents Docker from
# creating parent directories as `root`.
RUN mkdir -p \
  .config \
  .m2/repository \
  .ssh

# Install Claude Code using the native installer.
RUN curl -fsSL https://claude.ai/install.sh | bash

# Install SDKMAN! and the tools declared in `.sdkmanrc`. Also configure Maven
# Toolchains support for JDK 21, as required by `./run-full-build.sh`.
# Note: the `echo >> .sdkmanrc` intentionally creates a duplicate `java=` entry,
# so that `sdk env install` installs both the default JDK (listed first) and
# `${TARGET_JDK}`.
ARG TARGET_JDK=21.0.10-tem
COPY --chown="${USERNAME}:${USERNAME}" .sdkmanrc .sdkmanrc
SHELL ["/bin/bash", "-c"]
RUN curl -fsSL https://get.sdkman.io | bash \
  && source "${HOME}/.sdkman/bin/sdkman-init.sh" \
  && echo "java=${TARGET_JDK}" >> .sdkmanrc \
  && sdk env install \
  && rm .sdkmanrc \
  && printf '%s\n' \
    '<toolchains xmlns="https://maven.apache.org/TOOLCHAINS/1.1.0"' \
    '    xmlns:xsi="https://www.w3.org/2001/XMLSchema-instance"' \
    '    xsi:schemaLocation="http://maven.apache.org/TOOLCHAINS/1.1.0 https://maven.apache.org/xsd/toolchains-1.1.0.xsd">' \
    '  <toolchain>' \
    '    <type>jdk</type>' \
    '    <provides>' \
    '      <version>21</version>' \
    '    </provides>' \
    '    <configuration>' \
    "      <jdkHome>$(sdk home java "${TARGET_JDK}")</jdkHome>" \
    '    </configuration>' \
    '  </toolchain>' \
    '</toolchains>' \
    > "${HOME}/.m2/toolchains.xml"

# Ensure that SDKMAN!-managed tools are on the `${PATH}` for both interactive
# and non-interactive shells. The former load `.bashrc`, while the latter use
# `${BASH_ENV}`.
RUN echo 'source "${HOME}/.sdkman/bin/sdkman-init.sh"' \
  | tee "${HOME}/.bashrc" "${HOME}/.bash-env"
ENV BASH_ENV=/home/${USERNAME}/.bash-env
