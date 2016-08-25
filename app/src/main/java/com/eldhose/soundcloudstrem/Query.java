package com.eldhose.soundcloudstrem;

import java.util.HashMap;

/**
 * Created by ELDHOSE on 2016-08-19.
 */
public abstract class Query {

    protected int limit = 50;
    protected int offset = 50;

    /**
     * Creates a map that can be used as a QueryMap when querying the SoundCloud web API.
     *
     * @return a map of query parameters and values.
     */
    public abstract HashMap<String, String> createMap();
}

