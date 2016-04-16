package com.msgilligan.currency.exchanges;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A partial implementation of the Bitfinex API (Pull Requests Welcome)
 */
public class BitfinexClient {
    static final String baseUrl = "https://api.bitfinex.com";
    static final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    static final int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s
    private Retrofit restAdapter;
    private BitfinexService service;

    interface BitfinexService {
        @GET("/v1/pubticker/{pair}")
        Call<Map<String, Object>> ticker(@Path("pair") String pair);
    }

    public BitfinexClient() {
        OkHttpClient client = initClient();

        restAdapter = new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        service = restAdapter.create(BitfinexService.class);
    }

    public BigDecimal getPrice() {
        Map<String, Object> ticker;
        try {
            ticker = service.ticker("BTCUSD").execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String price = (String) ticker.get("last_price");
        return new BigDecimal(price);
    }

    private OkHttpClient initClient() {
        boolean debug = false;

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        if (debug) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }

        return builder.build();
    }
}
