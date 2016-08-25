package com.eldhose.soundcloudstrem;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static com.eldhose.soundcloudstrem.Constants.CLIENT_ID;

import static com.eldhose.soundcloudstrem.Constants.AUTH_TOKEN_KEY;
import static com.eldhose.soundcloudstrem.Constants.PREFS_NAME;
/**
 * Created by ELDHOSE on 2016-08-18.
 */
public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "PlayerActivity";

    private SoundCloudService soundcloud;
    private List<Track> tracks;

    private String searchString;
    private final ArrayList<String> trackTitles = new ArrayList<>();
    private final ArrayList<String> trackUrls = new ArrayList<>();
    private ArrayAdapter<String> songsListAdapter;
    private int currentTrack = -1;

    private final MediaPlayer player = new MediaPlayer();

    private ImageButton playPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_layout);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        final String token = preferences.getString(AUTH_TOKEN_KEY, null);

        if (token != null) {
            SoundCloudAPI api = new SoundCloudAPI(CLIENT_ID);
            api.setToken(token);

            soundcloud = api.getService();
        } else {
            finish();

            return;
        }

        EditText searchBox = (EditText) findViewById(R.id.search_box);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchString = s.toString();
            }
        });

        ListView songsList = (ListView) findViewById(R.id.song_list);
        songsListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, trackTitles);
        songsList.setAdapter(songsListAdapter);
        songsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, trackUrls.get(position));

                currentTrack = position;

                playTrack(trackUrls.get(position));
            }
        });

        playPauseButton = (ImageButton) findViewById(R.id.play_pause);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                togglePlayPause();
            }
        });
    }

    private void createSongList() {
        trackTitles.clear();
        trackUrls.clear();

        if(tracks != null) {
            for (Track track : tracks) {
                if (track.title != null && !track.title.isEmpty()) {
                    if (track.is_streamable) {
                        trackTitles.add(track.title);
                        trackUrls.add(track.stream_url);
                    } else {
                        Log.w(TAG, "Error getting track title.", new IllegalStateException());
                    }
                } else {
                    Log.w(TAG, "Error getting track title.", new IllegalStateException());
                }
            }
        }

        songsListAdapter.notifyDataSetChanged();
    }

    private void playTrack(String trackUrl) {
        player.reset();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (!player.isPlaying()) {
                    player.start();
                    playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                currentTrack++;
                if (currentTrack > trackTitles.size() - 1) {
                    currentTrack = -1;
                } else {
                    playTrack(trackUrls.get(currentTrack));
                }
            }
        });
        String dataString = trackUrl + "?client_id=" + CLIENT_ID;
        try {
            player.setDataSource(dataString);
        } catch (IOException e) {
            Log.e(TAG, "Couldn't parse URI.", e);
        }
        player.prepareAsync();
    }

    private void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
            playPauseButton.setImageResource(android.R.drawable.ic_media_play);
        } else if (trackUrls.size() > 0 && !player.isPlaying() && currentTrack == -1) {
            playTrack(trackUrls.get(0));
        } else {
            player.start();

            if(player.isPlaying()) {
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            }
        }
    }

    public void searchTracks(View view) {
        TrackQuery query = new TrackQuery.Builder()
                .setQuery(searchString)
                .build();

        soundcloud.searchTracks(query.createMap()).enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {
                tracks = response.body();

                Log.i(TAG, response.raw().toString());

                createSongList();
            }

            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                Log.e(TAG, "Failed to load tracks.", t);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }
}
