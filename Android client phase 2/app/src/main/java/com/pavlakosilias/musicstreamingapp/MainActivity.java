package com.pavlakosilias.musicstreamingapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pavlakosilias.musicstreamingapp.Consumer.Consumer;
import com.pavlakosilias.musicstreamingapp.Objects.ArtistName;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity {

    //fields----------------------------------------------------------------------------<

    //finals
    public static final int id = new Random().nextInt(999) + 1;
    public static final Consumer c = new Consumer(id);

    //MainActivity UI
    public ListView artistsListView;

    //Others
    public ArrayList<String> artistList;
    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.emptyTextView).setVisibility(View.INVISIBLE);

        //Initialize consumer.
        new RegisterTask(this).execute();

        //Start the timer
        new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Ticks are expensive, don't write code here
            }
            @Override
            public void onFinish() {
                if (!isConnected) {
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    findViewById(R.id.emptyTextView).setVisibility(View.VISIBLE);
                }
            }
        }.start();

        //Set overhead
        setTitle("Artists A-Z");
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Initialize artist list view
        new GetArtistListTask(this).execute();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //Set listView content to the artistList
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, artistList);
        artistsListView.setAdapter(arrayAdapter);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //Set list click listener
        artistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get object clicked by position
                String artistClicked = artistList.get(position);

                //Show toast
                Toast.makeText(getApplicationContext(), artistClicked, Toast.LENGTH_SHORT).show();

                //Declare Intent
                Intent intent = new Intent(MainActivity.this, TrackSelectorActivity.class);

                //Hook extras
                intent.putExtra("ARTIST_CLICKED", artistClicked);

                //Start track selector
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    //Sub-classes----------------------------------------------------------------------------------<

    //Get list of available artists
    public static class GetArtistListTask extends AsyncTask<Void, Void , ArrayList<String>> {
        private WeakReference<MainActivity> activityReference;
        private ArrayList<String> artistList;

        GetArtistListTask(MainActivity context){
            activityReference = new WeakReference<>(context);
            artistList = new ArrayList<String>();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Get reference
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            //Get a handle to the artistsListView
            activity.artistsListView = activity.findViewById(R.id.artistsListView);
        }


        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            //Get reference
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            //Get raw list
            ArrayList<ArtistName> artistListRaw = c.getArtists();

            //Build string list
            for (ArtistName artist : artistListRaw) {
                artistList.add(artist.getArtistName());
            }

            //Sort output list alphabetically
            Collections.sort(artistList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            });

            //Return it onPost
            return artistList;
        }

        @Override
        protected void onPostExecute(final ArrayList<String> artistList) {
            super.onPostExecute(artistList);

            //Get reference
            final MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            //Set activity ref data
            activity.artistList = artistList;

            //Disable loading gui
            activity.findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            //Set listView content to the artistList
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, artistList);
            activity.artistsListView.setAdapter(arrayAdapter);
        }

    }

    public static class RegisterTask extends AsyncTask<Void, Void, Boolean> {
        private WeakReference<MainActivity> activityReference;

        RegisterTask(MainActivity context){
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //Get reference
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return false;

            boolean connected = false;
            out:
            while (true) {
                int port = Integer.valueOf(PrefMan.getInstance().get("port"));
                while (true) {
                    String broker = PrefMan.getInstance().get("broker");
                    connected = c.register(broker, port);
                    if (!connected) {
                        try {
                            sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        break out;
                    }
                }
            }
            activity.isConnected = connected;
            return connected;
        }

        @Override
        protected void onPostExecute(Boolean connected) {
            super.onPostExecute(connected);

            //Get reference
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            //Show toast
            if (connected) {
                activity.findViewById(R.id.emptyTextView).setVisibility(View.GONE);
                Toast.makeText(activity.getApplicationContext(), "You are Connected :)", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(activity.getApplicationContext(), "Unable to find server :(", Toast.LENGTH_SHORT).show();
        }
    }


}
