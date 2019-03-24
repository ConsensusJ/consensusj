#!/usr/bin/env bash
VERSION=0.3.2-SNAPSHOT
../gradlew build
native-image --no-server --class-path build/libs/consensusj-jsonrpc-daemon-${VERSION}-all.jar \
    org.consensusj.jsonrpc.daemon.Application build/jsonrpcdaemon

