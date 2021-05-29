package com.pavlakosilias.musicstreamingapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.pavlakosilias.musicstreamingapp.Consumer.Consumer;
import com.pavlakosilias.musicstreamingapp.Objects.MusicFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class AudioPlayerActivity extends AppCompatActivity {
    //fields---------------------------------------------------------------------------------------<

    //main fields
    protected MusicFile musicFileClicked;
    protected Consumer c = MainActivity.c;

    //Buttons
    Button playButton;
    Button returnButton;
    Button cloudDownloadButton;

    //SeekBar specifics
    ScheduledFuture<?> seekBarUpdater;
    SeekBar positionBar;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;
    public static int totalTime;
    //MediaPlayer
    MediaPlayer mediaPlayer = null;
    //unordered
    private String trackName;
    private String artistName;
    private String albumInfo;
    private String genre;
    private String path;
    private String targetPath;
    private ScheduledFuture<?> playback;
    private ScheduledFuture<?> downloader;
    private ScheduledFuture<?> saver;
    private BufferedOutputStream bos = null;
    private File cachedFile;
    private ScheduledExecutorService ses = Executors.newScheduledThreadPool(4);
    private int crossPlayerProgress;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            int currentPosition = msg.what;

            //Update positionBar
            positionBar.setProgress(currentPosition);

            //Update labels
            String elapsedTime = createTimeLabel(currentPosition);
            elapsedTimeLabel.setText(elapsedTime);

            String remainingTime = createTimeLabel(totalTime - currentPosition);
            remainingTimeLabel.setText(remainingTime);
        }
    };

    //methods--------------------------------------------------------------------------------------<
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        //use this context's cachedir, instead of the VM's
        IOHelper.getInstance().setCachedir(getCacheDir());

        //Get intent extras
        Intent intent = getIntent();
        trackName = intent.getStringExtra("trackName");
        artistName = intent.getStringExtra("artistName");
        albumInfo = intent.getStringExtra("albumInfo");
        genre = intent.getStringExtra("genre");
        path = intent.getStringExtra("path");

        //Rebuild track clicked metadata
        musicFileClicked = new MusicFile(trackName, artistName, albumInfo, genre, path);

        //Set overhead
        setTitle(trackName);

        //Get UI build time ref
        playButton = findViewById(R.id.playButton);
        returnButton = findViewById(R.id.returnButton);
        cloudDownloadButton = findViewById(R.id.cloudDownloadButton);
        elapsedTimeLabel = findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = findViewById(R.id.remainingTimeLabel);

        //Init play/pause button sprite
        playButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_24dp);

        Thread downloaderT = new Thread(new Runnable() {
            int runCount = 0;

            @Override
            public void run() {
                System.err.println("Running downloader: " + runCount++);
                if (runCount < 2) {
                    //Start downloading the track
                    Log.d("myDebug", "Downloading chunks");
                    ArrayList<MusicFile> chunks = c.getSong(artistName, musicFileClicked);
                    if (!chunks.isEmpty()) {
                        Log.d("myDebug", "Chunks retrieved");
                    }
                } else {
                    downloader.cancel(false);
                }
            }
        });
        Thread saverT = new Thread(new Runnable() {
            int runCount = 0;

            @Override
            public void run() {
                System.err.println("Running saver: " + runCount++);
                try {
                    MusicFile chunk = c.getChuckFromQueue();
                    if (bos == null) {
                        bos = new BufferedOutputStream(IOHelper.getInstance().getFileOutputStreamForTrack(chunk));
                        cachedFile = IOHelper.getInstance().getFileForTrack(chunk);
                    }
                    bos.write(chunk.musicFileExtract);
                    bos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        Thread playbackT = new Thread(new Runnable() {
            int runCount = 0;

            @Override
            public void run() {
                System.err.println("Running playback: " + runCount++);
                if (mediaPlayer == null && IOHelper.getInstance().isReadable(cachedFile)) {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse("file://" + cachedFile.getAbsolutePath()));
                    playback.cancel(false);
                    mediaPlayer.seekTo(0);
                    mediaPlayer.setVolume(0.5f, 0.5f);
                    AudioPlayerActivity.totalTime = mediaPlayer.getDuration();
                    mediaPlayer.start();
                }
            }
        });


        //Seek bar
        positionBar = findViewById(R.id.positionBar);
        positionBar.setMax(totalTime);

        //Activated when the user touch the seek bar
        positionBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    positionBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Thread that updates the seek bar
        Thread seekBarUpdaterT = new Thread(new Runnable() {
            int runCount = 0;

            @Override
            public void run() {
                System.err.println("Running seekBarUpdater: " + runCount++);
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    positionBar.setMax(totalTime);
                    Message msg = new Message();
                    msg.what = mediaPlayer.getCurrentPosition();
                    crossPlayerProgress = msg.what;
                    handler.sendMessage(msg);
                }
            }
        });

        downloader = ses.scheduleWithFixedDelay(downloaderT, 0, 100, TimeUnit.MILLISECONDS);
        saver = ses.scheduleWithFixedDelay(saverT, 100, 100, TimeUnit.MILLISECONDS);
        playback = ses.scheduleWithFixedDelay(playbackT, 1000, 1000, TimeUnit.MILLISECONDS);
        seekBarUpdater = ses.scheduleWithFixedDelay(seekBarUpdaterT, 1, 1, TimeUnit.SECONDS);

    }//End of onCreate


    //Player button actions------------------------------------------------------------------------<

    public String createTimeLabel(int time) {

        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    @Override
    public void onBackPressed() {
        seekBarUpdater.cancel(true);
        saver.cancel(true);
        downloader.cancel(true);
        playback.cancel(true);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    //play - pause
    public void onPlayButtonClick(View view) {
        if (!mediaPlayer.isPlaying()) {
            //Play track
            mediaPlayer.start();

            //Enable pause
            playButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_24dp);
        } else {
            //Pause track
            mediaPlayer.pause();

            //Enable play
            playButton.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp);
        }
    }

    //return
    public void onReturnButtonClick(View view) {
        onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    //download
    public void onCloudDownloadButtonClick(View view) {
        try {
            targetPath = String.format("%s/%s/%s",
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    cachedFile.getParentFile().getName(),
                    cachedFile.getName()
            );

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Where do you want to save it?");
            downloader.cancel(true);
            final EditText input = new EditText(this);
            input.setText(targetPath);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    targetPath = input.getText().toString();
                    //File::renameTo does not cross fs boundaries, so we need to read the file from cache to write it externally
                    try {
                        IOHelper.getInstance().copyX(
                                cachedFile,
                                IOHelper.getInstance().createIntermediateDirectoriesAndFileInRoot("", targetPath.split(Pattern.quote(File.separator)))
                        );
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
