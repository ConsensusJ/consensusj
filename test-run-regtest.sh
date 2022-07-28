#!/bin/bash
set -x

function cleanup {
    kill -15 $BTCPID
}
trap cleanup EXIT

# We are currently using Omni Core since it a superset of Bitcoin Core
BITCOIND=copied-artifacts/src/omnicored
DATADIR=build/regtest-datadir
LOGDIR=logs
OMNILOG=/tmp/omnicore.log

# Assume bitcoind built elsewhere and copied without x permission
chmod +x $BITCOIND

# Setup bitcoin conf and data dir
mkdir -p $DATADIR
cp -n bitcoin.conf $DATADIR

# setup logging
mkdir -p $LOGDIR
touch $OMNILOG
ln -sf $OMNILOG $LOGDIR/omnicore.log

# Remove all regtest data
rm -rf $DATADIR/regtest

# Run bitcoind in regtest mode
$BITCOIND -regtest -datadir=$DATADIR \
  -addresstype=legacy -experimental-btc-balances=1 \
  -peerbloomfilters \
  -paytxfee=0.0001 -minrelaytxfee=0.00001 \
  -limitancestorcount=750 -limitdescendantcount=750 > $LOGDIR/bitcoin.log &
BTCSTATUS=$?
BTCPID=$!

echo $BITCOIND started return code $BTCSTATUS pid $BTCPID

# Give server some time to start
# sleep 30

# Run integration tests
echo "Running Bitcoin Core RPC integration tests in RegTest mode..."
./gradlew regTest --scan --info --stacktrace
GRADLESTATUS=$?

exit $GRADLESTATUS
