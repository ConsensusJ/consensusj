#!/bin/sh
# Build all release artifacts and copies them to a writable directory for signing (with JReleaser)
# This should be run from the project root directory and inside the `nix develop` shell
# Run this inside the `nix develop` shell
set -e -x
gradle publish

# In the future `ARTIFACT_REPO` will be read-only.
# Copy to a writable staging dir (`SIGNED_REPO`) for JReleaser to do signing in:
ARTIFACT_REPO="build/repo"
SIGNED_REPO="build/repo-signed"
rm -rf "$SIGNED_REPO"
cp -rL "$ARTIFACT_REPO" "$SIGNED_REPO" # -L dereferences any symlinks
chmod -R u+w "$SIGNED_REPO"            # /nix/store files are read-only; make the copy writable
