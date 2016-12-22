package com.msgilligan.bitcoinj.proxy.authext;

import com.msgilligan.bitcoinj.rpc.util.Base64;
import okhttp3.Call;
import okhttp3.Request;
import ratpack.retrofit.internal.RatpackCallFactory;

/**
 * Workaround solution for adding Basic Auth Header
 * Ratpack 1.5 will have a proper mechanism for doing this.
 */
public class BasicAuthCallFactory implements okhttp3.Call.Factory {
    private final String authString;

    public BasicAuthCallFactory(String user, String password) {
        this.authString = authString(user, password);
    }

    @Override
    public Call newCall(Request request) {
        return RatpackCallFactory.INSTANCE.newCall(request
                .newBuilder()
                .header("Authorization", authString)
                .build());
    }

    private static String authString(String username, String password) {
        String auth = username + ":" + password;
        return "Basic " + Base64.encodeToString(auth.getBytes(),Base64.DEFAULT).trim();
    }
}
