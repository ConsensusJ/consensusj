var blockheight = client.getBlockCount();
print("blockheight = ${blockheight}");

var satoshiAmount = satoshi(1);
print("satoshiAmount = ${satoshiAmount.toFriendlyString()}");

var btcAmount = btc(2);
print("btcAmount = ${btcAmount.toFriendlyString()}");

var coinAmount = coin(2, 50);
print("coinAmount = ${coinAmount.toFriendlyString()}");
