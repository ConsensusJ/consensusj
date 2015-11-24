var blockheight = client.getBlockCount();
print("blockheight = ${blockheight}");

var block = env.waitForBlock()[0];
print("new block = ${block}");

blockheight = client.getBlockCount();
print("blockheight = ${blockheight}");
