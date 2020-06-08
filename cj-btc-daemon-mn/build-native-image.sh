#!/usr/bin/env bash
VERSION=0.5.1-SNAPSHOT
../gradlew shadowJar
native-image --no-server -jar build/libs/cj-btc-daemon-mn-${VERSION}.jar build/cjbitcoind
