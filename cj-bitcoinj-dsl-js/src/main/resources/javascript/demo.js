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

print("blockheight (via lambda) = ${getBlockCount()}");
