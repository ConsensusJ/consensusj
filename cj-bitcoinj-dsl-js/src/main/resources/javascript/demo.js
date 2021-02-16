// A simple demo of ConsensusJ ScriptRunner capabilities

// Call a JSON-RPC method with the `client` object
var blockheight = client.getBlockCount();
print("blockheight = ${blockheight}");

// Use utility functions to create `Coin` objects for various amounts
var satoshiAmount = satoshi(1);
print("satoshiAmount = ${satoshiAmount.toFriendlyString()}");

var btcAmount = btc(2);
print("btcAmount = ${btcAmount.toFriendlyString()}");

var coinAmount = coin(2, 50);
print("coinAmount = ${coinAmount.toFriendlyString()}");
