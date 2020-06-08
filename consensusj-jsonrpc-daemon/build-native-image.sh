#!/usr/bin/env bash
VERSION=0.5.1-SNAPSHOT
../gradlew shadowJar
native-image --no-server -jar build/libs/consensusj-jsonrpc-daemon-${VERSION}.jar build/jsonrpcdaemon

