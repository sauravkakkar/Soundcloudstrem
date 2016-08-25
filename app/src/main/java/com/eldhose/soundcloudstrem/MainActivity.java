package com.eldhose.soundcloudstrem;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import static com.eldhose.soundcloudstrem.Constants.CLIENT_ID;
import static com.eldhose.soundcloudstrem.Constants.REDIRECT;
import static com.eldhose.soundcloudstrem.Constants.CLIENT_SECRET;
import static com.eldhose.soundcloudstrem.Constants.AUTH_TOKEN_KEY;
import static com.eldhose.soundcloudstrem.Constants.PREFS_NAME;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BrowserSoundCloudAuthenticator browserAuthenticator;
    private ChromeTabsSoundCloudAuthenticator tabsAuthenticator;
    private boolean tabsDidConnect = false;

    private Button chromeAuthButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button browserAuthButton = (Button) findViewById(R.id.btn_browser_auth);
        chromeAuthButton = (Button) findViewById(R.id.btn_chrome_auth);
        chromeAuthButton.setEnabled(false);

        // Prepare auth methods
        browserAuthenticator = new BrowserSoundCloudAuthenticator(CLIENT_ID, REDIRECT, this);

        browserAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browserAuthenticator.launchAuthenticationFlow();
            }
        });
        AuthTabServiceConnection serviceConnection = new AuthTabServiceConnection(new AuthenticationCallback() {
            @Override
            public void onReadyToAuthenticate() {

                // Customize Chrome Tabs
                CustomTabsIntent.Builder builder = tabsAuthenticator.newTabsIntentBuilder()
                        .setToolbarColor(getColorCompat(R.color.colorPrimary))
                        .setSecondaryToolbarColor(getColorCompat(R.color.colorAccent));

                tabsAuthenticator.setTabsIntentBuilder(builder);

                if(chromeAuthButton != null) {
                    chromeAuthButton.setEnabled(true);
                }
            }

            @Override
            public void onAuthenticationEnded() {
                Log.i(TAG, "Auth ended.");
            }
        });

        tabsAuthenticator = new ChromeTabsSoundCloudAuthenticator(CLIENT_ID, REDIRECT, this, serviceConnection);
        chromeAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tabsAuthenticator.launchAuthenticationFlow();
            }
        });
    }
     protected void onStart() {
        super.onStart();

        if(tabsAuthenticator != null) {
            tabsDidConnect = tabsAuthenticator.prepareAuthenticationFlow();

            Log.i(TAG, "Tab auth did connect: " + tabsDidConnect);
        }
    }

    @Override protected void onResume() {
        super.onResume();

        Intent intent = getIntent();

        String intentInfo = intent != null ? intent.getDataString() : "Null intent.";

        Log.i(TAG, "Lifecycle method onResume with intent info: " + intentInfo);

        if(intent != null && intent.getDataString() != null && intent.getDataString().contains(REDIRECT)) {
            HashMap<String, String> authMap = SoundCloudAuthenticator.handleResponse(intent, REDIRECT, CLIENT_ID, CLIENT_SECRET);

            SoundCloudAuthenticator.AuthService service = tabsAuthenticator.getAuthService(); // Method is final, varies only with clientId used to construct the authenticator
            service.authorize(authMap).enqueue(new Callback<AuthenticationResponse>() {
                @Override
                public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
                    Log.i(TAG, "Response was: " + response.raw().toString());

                    AuthenticationResponse authResponse = response.body();

                    if(authResponse != null) {
                        Log.i(TAG, "Auth success -  " + authResponse.access_token);

                        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        preferences.edit().putString(AUTH_TOKEN_KEY, authResponse.access_token).apply();

                        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                }

                @Override
                public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                    Log.e(TAG, "Auth failure - " + t.getMessage());
                }
            });
        } else {
            Log.w(TAG, "Other new intent not handled.");
        }


    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
    }

    @Override public void onDestroy() {
        Log.i(TAG, "OnDestroy");

        if(tabsAuthenticator != null && tabsDidConnect) {
            tabsAuthenticator.unbindService();
        }

        super.onDestroy();
    }

    @SuppressLint("deprecation")
    private int getColorCompat(@ColorRes int color) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return getColor(color);
        } else {
            return getResources().getColor(color);
        }
    }


}
