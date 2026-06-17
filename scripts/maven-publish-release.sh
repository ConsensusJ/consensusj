#!/bin/sh
# Validate and publish release artifacts to our GitLab Maven artifact rpo
# This should be run from the project root directory and inside the `nix develop` shell
# JRELEASER_GITLAB_CONSENSUSJ_TOKEN should be set via `~/.jreleaser/config.properties` or env variable
if [[ $1 != "--dry-run" && $1 != "" ]]; then
    echo "Usage: $0 [--dry-run]"
    exit 1
fi
set -x -e
OPTIONS=${1:-""}
# Parse REVISION from `gradle.properties`
export REVISION=`grep consensusjVersion gradle.properties | cut -d'=' -f2 | xargs`

export JRELEASER_OUTPUT_DIRECTORY="build"
export JRELEASER_PROJECT_VERSION="$REVISION"
jreleaser-cli deploy $OPTIONS
