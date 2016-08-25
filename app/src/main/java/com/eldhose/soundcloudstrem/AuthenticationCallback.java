package com.eldhose.soundcloudstrem;

/**
 * Created by ELDHOSE on 2016-08-18.
 */
public interface AuthenticationCallback {
    void onReadyToAuthenticate();
    void onAuthenticationEnded();
}
