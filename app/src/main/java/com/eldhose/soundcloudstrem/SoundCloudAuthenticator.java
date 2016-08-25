package com.eldhose.soundcloudstrem;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by ELDHOSE on 2016-08-18.
 */
public abstract class SoundCloudAuthenticator {
    private AuthService service;

    private static final String RESPONSE_TYPE = "code";
    private static final String SCOPE = "non-expiring";
    private static final String DISPLAY = "popup";
    private static final String STATE = "asdf";

    private final String clientId;
    private final String redirectUri;


    public SoundCloudAuthenticator(String clientId, String redirectUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }


    public abstract boolean prepareAuthenticationFlow();


    public abstract void launchAuthenticationFlow();

    protected final String loginUrl() {
        return  "https://www.soundcloud.com/connect?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=" + RESPONSE_TYPE +
                "&scope=" + SCOPE +
                "&display=" + DISPLAY +
                "&state=" + STATE;
    }

    protected final void addReferrerToIntent(Intent intent, String packageName) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            String referrer = Intent.URI_ANDROID_APP_SCHEME + "//" + packageName;
            intent.putExtra(Intent.EXTRA_REFERRER_NAME, referrer);
        }
    }



    public static HashMap<String, String> handleResponse(Intent intent, String redirectUri, String clientId,
                                                         String clientSecret) {
        String uri = intent.getDataString();
        String code = Uri.parse(uri).getQueryParameter(RESPONSE_TYPE);

        if (code != null) {
            HashMap<String, String> fieldMap = new HashMap<>();

            fieldMap.put("client_id", clientId);
            fieldMap.put("client_secret", clientSecret);
            fieldMap.put("code", code);
            fieldMap.put("grant_type", GrantType.AUTH_CODE);
            fieldMap.put("redirect_uri", redirectUri);

            return fieldMap;
        } else {
            return null;
        }
    }



    public class GrantType {
        public static final String AUTH_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String PASSWORD = "password";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String OAUTH1_TOKEN = "oauth1_token";
    }

    /**
     * Retrofit interface built solely to authenticate SoundCloud.
     */
    public interface AuthService {


        @FormUrlEncoded
        @POST("oauth2/token") Call<AuthenticationResponse> authorize(@FieldMap Map<String, String> authMap);
    }


    public final AuthService getAuthService() {
        if (service == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor())
                    .build();

            Retrofit adapter = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(SoundCloudAPI.SOUNDCLOUD_API_ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = adapter.create(AuthService.class);
        }

        return service;
    }

    protected class AuthInterceptor implements Interceptor {
        @Override public Response intercept(Chain chain) throws IOException {

            Request request = chain.request();

            HttpUrl url = request.url()
                    .newBuilder()
                    .addEncodedQueryParameter("client_id", clientId)
                    .build();

            Request newRequest = request.newBuilder()
                    .url(url)
                    .build();

            return chain.proceed(newRequest);
        }
    }
}
