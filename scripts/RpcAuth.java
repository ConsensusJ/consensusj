///usr/bin/env java "$0" "$@" ; exit $?
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

// Generates a Bitcoin Core-style rpcauth HMAC
// Similar to https://github.com/bitcoin/bitcoin/tree/master/share/rpcauth/rpcauth.py
//
// To run, with `java` JDK 25+ installed:
//   ./RpcAuth.java <username> [password]
// If the password is omitted, it is read from the console without echoing.

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

static final HexFormat HEX = HexFormat.of();

/// @return 16 random bytes, hex-encoded (32 chars)
String generateSalt() {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return HEX.formatHex(salt);
}

/// @param password provided password
/// @return <SALT-HEX>$<HMAC-HEX>
String generateAuth(byte[] password) throws GeneralSecurityException {
    String salt = generateSalt();
    return salt + "$" + hmacSha256(salt, password);
}

/// @return HMAC-SHA256(salt, password)
String hmacSha256(String salt, byte[] password) throws GeneralSecurityException {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(salt.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    return HEX.formatHex(mac.doFinal(password));
}

void main(String[] args) throws GeneralSecurityException {
    if (args.length < 1 || args.length > 2) {
        System.err.println("Usage: java RpcAuth.java <username> [password]");
        System.exit(1);
    }
    String username = args[0];

    byte[] password;
    if (args.length == 2) {
        password = args[1].getBytes(StandardCharsets.UTF_8);
    } else {
        Console console = System.console();
        if (console == null) {
            System.err.println("No console available; pass the password as an argument.");
            System.exit(1);
        }
        char[] chars = console.readPassword("Password for %s: ", username);
        password = new String(chars).getBytes(StandardCharsets.UTF_8);
        Arrays.fill(chars, '\0');
    }

    String passwordHmac = generateAuth(password);
    Arrays.fill(password, (byte) 0);

    IO.println("passwordHMAC: " + passwordHmac);
    IO.println("rpcauth=" + username + ":" + passwordHmac);
}
