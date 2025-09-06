///usr/bin/env jbang "$0" "$@" ; exit $? 
//JAVA 25+
//REPOS mavenCentral
//REPOS https://gitlab.com/api/v4/projects/8482916/packages/maven
//DEPS com.msgilligan:cj-btc-jsonrpc:0.7.0-alpha3
//DEPS org.slf4j:slf4j-jdk14:2.0.17

import org.consensusj.bitcoin.jsonrpc.BitcoinClient;

void main(String[] args) throws Exception {
    if (args.length < 3) {
      IO.println("Usage: getblockcount url username password");
      System.exit(1);
    }
    var client = new BitcoinClient(URI.create(args[0]), args[1], args[2]);
    int count = client.getBlockCount();
    IO.println(count);
}
