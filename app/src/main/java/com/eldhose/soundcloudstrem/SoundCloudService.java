package com.eldhose.soundcloudstrem;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by ELDHOSE on 2016-08-18.
 */
public interface SoundCloudService {
    @GET("tracks")
    Call<List<Track>> searchTracks(@Query("q") String query);
    @GET("tracks") Call<List<Track>> searchTracks(@QueryMap HashMap<String, String> queries);
    @GET("tracks/{id}") Call<Track> getTrack(@Path("id") String trackId);
    @GET("users") Call<List<User>> searchUsers(@Query("q") String query);
    @GET("users/{id}") Call<User> getUser(@Path("id") String userId);
    @GET("users/{id}/tracks") Call<List<Track>> getUserTracks(@Path("id") String userId);
    @GET("me") Call<User> getMe();
    @GET("me/tracks") Call<List<Track>> getMyTracks();
}
