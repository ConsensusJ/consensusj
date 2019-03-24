#!/usr/bin/env bash
VERSION=0.3.2-SNAPSHOT
../gradlew build
native-image --no-server -cp build/libs/cj-btc-daemon-mn-${VERSION}-all.jar
