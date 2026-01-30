/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
