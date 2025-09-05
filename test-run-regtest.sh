#!/bin/bash
set -x

function cleanup {
    kill -15 $BTCPID
}
trap cleanup EXIT

# Use `bitcoind` in current PATH
BITCOIND=bitcoind
DATADIR=build/regtest-datadir
LOGDIR=logs

# Setup bitcoin conf and data dir
mkdir -p $DATADIR
cp -n bitcoin.conf $DATADIR

# setup logging
mkdir -p $LOGDIR

# Remove all regtest data
rm -rf $DATADIR/regtest

# Run bitcoind in regtest mode
$BITCOIND -regtest -datadir=$DATADIR \
  -addresstype=legacy \
  -peerbloomfilters \
  -deprecatedrpc=create_bdb \
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
