package com.eldhose.soundcloudstrem;

/**
 * Created by ELDHOSE on 2016-08-18.
 */
public class AuthenticationResponse {
    public static final String TOKEN = "access_token";
    public static final String ERROR = "error";
    public static final String UNKNOWN = "unknown";

    public String access_token;

    public String scope;

    public String error;

    /**
     * Helper method to determine authentication response type.
     *
     * @return The type of the authentication response.
     */
    public String getType() {
        if (access_token != null) {
            return TOKEN;
        }

        if (error != null) {
            return ERROR;
        }

        return UNKNOWN;
    }
}
