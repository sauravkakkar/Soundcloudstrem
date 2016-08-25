package com.eldhose.soundcloudstrem;

import android.content.ComponentName;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import java.lang.ref.WeakReference;

/**
 * Created by ELDHOSE on 2016-08-18.
 */
public class AuthTabServiceConnection extends CustomTabsServiceConnection {
    private final WeakReference<AuthenticationCallback> authCallbackReference;
    private final WeakReference<CustomTabsCallback> navCallbackReference;
    private CustomTabsClient tabsClient;
    private CustomTabsSession tabsSession;
    private String clientAuthUrl;

    public AuthTabServiceConnection(@NonNull AuthenticationCallback callback) {
        this.authCallbackReference = new WeakReference<>(callback);
        this.navCallbackReference = new WeakReference<>(null);
    }

    public AuthTabServiceConnection(@NonNull AuthenticationCallback authenticationCallback,
                                    @Nullable CustomTabsCallback navigationCallback) {
        this.authCallbackReference = new WeakReference<>(authenticationCallback);
        this.navCallbackReference = new WeakReference<>(navigationCallback);
    }

     public void onCustomTabsServiceConnected(ComponentName componentName,
                                                       CustomTabsClient customTabsClient) {

        tabsClient = customTabsClient;
        tabsClient.warmup(0);

        tabsSession = tabsClient.newSession(navCallbackReference.get());

        if (tabsSession != null && clientAuthUrl != null) {
            tabsSession.mayLaunchUrl(Uri.parse(clientAuthUrl), null, null);
        }

        AuthenticationCallback callback = authCallbackReference.get();
        if(callback != null) {
            callback.onReadyToAuthenticate();
        }
    }

     public void onServiceDisconnected(ComponentName componentName) {
        tabsClient = null;
        tabsSession = null;

        AuthenticationCallback callback = authCallbackReference.get();
        if(callback != null) {
            callback.onAuthenticationEnded();
        }
    }

    public void setClientAuthUrl(String authUrl) {
        this.clientAuthUrl = authUrl;
    }

    public CustomTabsSession getSession() {
        return tabsSession;
    }
}
