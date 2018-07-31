#!/bin/bash
#
# This script is intended to run on Jenkins server to test `bitcoind` in RegTest mode
# using the bitcoinj-rpcclient.
#
# WARNING: Read this script carefully before running or using. Note the `rm -rf` command.
#
set -x

function cleanup {
    kill -15 $BTCPID
}
trap cleanup EXIT

BTCD=copied-artifacts/src/bitcoind
DATADIR=$HOME/.bitcoin
LOGDIR=logs
#MSCLOG=/tmp/mastercore.log

if [ ! -f "$BTCD" ]
then
  echo "$BTCD doesn't exist - check your Jenkins test setup."
  exit 1
fi

# Assume bitcoind built elsewhere and copied by Jenkins Copy Artifact plugin
chmod +x $BTCD

# Setup bitcoin conf and data dir
mkdir -p $DATADIR
cp -n bitcoinj-rpcclient/bitcoin.conf $DATADIR

# Setup logging
mkdir -p $LOGDIR
#touch $MSCLOG
#ln -sf $MSCLOG $LOGDIR/mastercore.log

# Remove all regtest data
rm -rf $DATADIR/regtest

# Run bitcoind in regtest mode
$BTCD -regtest -datadir=$DATADIR > $LOGDIR/bitcoin.log &
BTCSTATUS=$?
BTCPID=$!

# Run integration tests
echo "Running Bitcoin RPC integration tests in regtest mode..."
./gradlew clean :bitcoin-rpcclient:regTest :cj-btc-cli:regTest
GRADLESTATUS=$?

exit $GRADLESTATUS
