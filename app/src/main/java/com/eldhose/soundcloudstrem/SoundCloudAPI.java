package com.eldhose.soundcloudstrem;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import java.io.IOException;
import java.util.Date;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ELDHOSE on 2016-08-18.
 */
public class SoundCloudAPI {
    public static final String SOUNDCLOUD_API_ENDPOINT = "https://api.soundcloud.com/";

    private final SoundCloudService service;

    private final String clientId;
    private String token;


    public SoundCloudAPI(String clientId) {
        this.clientId = clientId;

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new SoundCloudInterceptor())
                .build();

        Retrofit adapter = new Retrofit.Builder()
                .client(client)
                .baseUrl(SOUNDCLOUD_API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        service = adapter.create(SoundCloudService.class);
    }


    public SoundCloudService getService() {
        return service;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private class SoundCloudInterceptor implements Interceptor {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {

            Request request = chain.request();

            HttpUrl.Builder urlBuilder = request.url().newBuilder();

            urlBuilder.addEncodedQueryParameter("client_id", clientId);
            if (token != null) {
                urlBuilder.addEncodedQueryParameter("oauth_token", token);
            }

            Request newRequest = request.newBuilder()
                    .url(urlBuilder.build())
                    .build();

            return chain.proceed(newRequest);
        }
    }
}
