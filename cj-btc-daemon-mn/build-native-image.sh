#!/usr/bin/env bash
VERSION=0.5.3-SNAPSHOT
../gradlew shadowJar
native-image --no-server --no-fallback --report-unsupported-elements-at-runtime \
    -jar build/libs/cj-btc-daemon-mn-${VERSION}.jar build/cjbitcoind
