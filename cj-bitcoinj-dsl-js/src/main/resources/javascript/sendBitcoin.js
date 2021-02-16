print("blockheight = ${getBlockCount()}");

var amount = btc(2);
print("amount = ${amount.toFriendlyString()}");
var address = funder.createFundedAddress(amount);
env.waitForBlock();

var balance = client.getBitcoinBalance(address);
print("balance = ${balance.toFriendlyString()}");

var destAddress = client.getNewAddress();

var sendAmount = coin(1, 50);
print("sendAmount = ${sendAmount.toFriendlyString()}");

var sendtxid = client.sendToAddress(destAddress, sendAmount);
env.waitForBlock();

var destBalance = client.getBitcoinBalance(destAddress);
print("destBalance = ${destBalance.toFriendlyString()}");

print("blockheight = ${getBlockCount()}");
