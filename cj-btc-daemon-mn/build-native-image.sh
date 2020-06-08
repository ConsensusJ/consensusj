#!/usr/bin/env bash
VERSION=0.5.1-SNAPSHOT
../gradlew build
native-image --no-server -cp build/libs/cj-btc-daemon-mn-${VERSION}-all.jar
