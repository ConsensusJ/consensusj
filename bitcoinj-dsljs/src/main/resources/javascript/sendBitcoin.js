var Coin = Java.type('org.bitcoinj.core.Coin');

var blockheight = getBlockCount();
print("blockheight = ${blockheight}");

var coin = Coin.valueOf(2, 50); //btc(2);
print("coin = ${coin.toFriendlyString()}");
var address = funder.createFundedAddress(coin);
env.waitForBlock();

var balance = client.getBitcoinBalance(address);
print("balance = ${balance.toFriendlyString()}");

var destAddress = client.getNewAddress();

var sendAmount = btc(1);
print("sendAmount = ${sendAmount.toFriendlyString()}");

var sendtxid = client.sendToAddress(destAddress, sendAmount);
env.waitForBlock();

var destBalance = client.getBitcoinBalance(destAddress);
print("destBalance = ${destBalance.toFriendlyString()}");


blockheight = client.getBlockCount();
print("blockheight = ${blockheight}");
