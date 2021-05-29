package com.pavlakosilias.musicstreamingapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.pavlakosilias.musicstreamingapp.Objects.MusicFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.lang.Thread.sleep;

public class TrackSelectorActivity extends AppCompatActivity {

    public ArrayList<MusicFile> albumMetaData = null;
    public static ArrayList<String> trackListSave = null;
    public String artistClicked;
    public ListView tracksListView;
    private boolean showLoadingView;
    private boolean showTimeOut;
    AsyncTask thisTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_selector);
        findViewById(R.id.emptyTextView).setVisibility(View.INVISIBLE);
        findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);

        //Get intent
        Intent intent = getIntent();

        //Get clicked artist
        artistClicked = intent.getExtras().getString("ARTIST_CLICKED");

        //Set overhead
        setTitle("Tracks by " + artistClicked);

        //Init track list
        thisTask = new GetTrackListTask(this, artistClicked).execute();

        //Set list click listener
        tracksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get object clicked by position
                String trackClicked = trackListSave.get(position);

                //Declare Intent
                Intent intent = new Intent(getApplicationContext(), AudioPlayerActivity.class);

                //Get meta data
                MusicFile mfClicked = null;
                for(MusicFile mf : albumMetaData){
                    if(mf.trackName.equals(trackClicked)){
                        mfClicked = mf;
                        break;
                    }
                }

                //Hook extras
                if (mfClicked != null) {
                    intent.putExtra("trackName", trackClicked);
                    intent.putExtra("artistName", mfClicked.artistName);
                    intent.putExtra("albumInfo",mfClicked.albumInfo);
                    intent.putExtra("genre", mfClicked.genre);
                    intent.putExtra("path", mfClicked.path);
                }else{
                    //crash
                    finish();
                }

                //Start track selector
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
            //Start the timer
            new CountDownTimer(6000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (showLoadingView) {
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFinish() {
                    if (showTimeOut) {
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        findViewById(R.id.emptyTextView).setVisibility(View.VISIBLE);
                    }
                }
            }.start();
        }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        finish();
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        //Set listView content to the artistList
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1, trackListSave);
        tracksListView.setAdapter(arrayAdapter);
    }



    //Retrieves track-list from the server
    public static class GetTrackListTask extends AsyncTask<String, Void, ArrayList<String>> {
        private WeakReference<TrackSelectorActivity> activityReference;
        private ArrayList<String> trackList;
        private String artistClicked;

        GetTrackListTask(TrackSelectorActivity context, String artistClickedExtra){
            activityReference = new WeakReference<>(context);
            trackList = new ArrayList<String>();
            artistClicked = artistClickedExtra;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Get reference
            final TrackSelectorActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            //Get a handle to the tracksListView
            activity.tracksListView = activity.findViewById(R.id.tracksListView);
        }

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            //Get reference
            final TrackSelectorActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            int tries = 0;
            while(activity.albumMetaData == null && tries < 3) {
                //Get meta data
                activity.albumMetaData = MainActivity.c.getAlbum(artistClicked);
                if(activity.albumMetaData == null) activity.showLoadingView = true;
                if(activity.albumMetaData != null) break;
                tries++;
                //Set time out
                if(tries >= 3){
                    activity.showLoadingView = false;
                    activity.showTimeOut = true;
                    break;
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(activity.albumMetaData != null) {

                //Export track Names
                for (MusicFile track : activity.albumMetaData) trackList.add(track.trackName);

                //Sort output list alphabetically
                Collections.sort(trackList, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareToIgnoreCase(o2);
                    }
                });

                //Set data
                activity.trackListSave = trackList;
            }

            //return result
            return trackList;
        }

        @Override
        protected void onPostExecute(final ArrayList<String> trackList) {
            super.onPostExecute(trackList);

            //Get reference
            final TrackSelectorActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            //Set listView content to the trackList
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(activity.getApplicationContext(), android.R.layout.simple_list_item_1, trackList);
            activity.tracksListView.setAdapter(arrayAdapter);

            if(!trackList.isEmpty()) {
                activity.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                activity.findViewById(R.id.emptyTextView).setVisibility(View.GONE);
            }else{
                activity.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                activity.findViewById(R.id.emptyTextView).setVisibility(View.VISIBLE);
            }

        }
    }
}
