package com.pavlakosilias.musicstreamingapp;

import com.pavlakosilias.musicstreamingapp.Objects.MusicFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class IOHelper {
    private static IOHelper instance;
    private File cachedir;

    //default cachedir is the one provided by the VM; client contexts should overrride this option
    private IOHelper() {
        String cachePath = System.getProperty("deployment.user.cachedir");
        if (cachePath != null) {
            File cachedir = new File(cachePath);
            if (cachedir.isDirectory()) {
                this.cachedir = cachedir;
            }
        }
    }

    synchronized public static IOHelper getInstance() {
        if (instance == null) {
            instance = new IOHelper();
        }
        return instance;
    }

    public File createIntermediateDirectoriesAndFileInCache(String suffix, String... args) throws IOException {
        return createIntermediateDirectoriesAndFile(cachedir, suffix, args);
    }

    public File createIntermediateDirectoriesAndFileInRoot(String suffix, String... args) throws IOException {
        return createIntermediateDirectoriesAndFile(new File("/"), suffix, args);
    }

    public File createIntermediateDirectoriesAndFile(File sup, String suffix, String... args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            if (sup.isDirectory()) {
                File sub = null;
                if (i == args.length - 1) {
                    sub = new File(sup, args[i] + suffix);
                } else {
                    sub = new File(sup, args[i]);
                    if (!sub.exists()) {
                        sub.mkdir();
                    }
                }
                sup = sub;
            }
        }
        return sup;
    }

    public File getFileForTrack(String... args) throws IOException {
        return createIntermediateDirectoriesAndFileInCache(".mp3", args);
    }

    public File getFileForChunk(int chunkid, String... args) throws IOException {
        return createIntermediateDirectoriesAndFileInCache("-chunk-" + chunkid + ".mp3", args);
    }

    public File getFileForTrack(MusicFile song) throws IOException {
        return getFileForTrack(song.artistName, song.trackName);
    }

    public File getFileForChunk(int chunkid, MusicFile chunk) throws IOException {
        return getFileForChunk(chunkid, chunk.artistName, chunk.trackName);
    }

    public FileInputStream getFileInputStreamForTrack(String... args) throws IOException {
        return new FileInputStream(getFileForTrack(args));
    }

    public FileOutputStream getFileOutputStreamForTrack(String... args) throws IOException {
        return new FileOutputStream(getFileForTrack(args));
    }

    public FileInputStream getFileInputStreamForTrack(MusicFile song) throws IOException {
        return new FileInputStream(getFileForTrack(song));
    }

    public FileOutputStream getFileOutputStreamForTrack(MusicFile song) throws IOException {
        return new FileOutputStream(getFileForTrack(song));
    }

    public FileOutputStream getFileOutputStreamForChunk(int chunkid, MusicFile chunk) throws IOException {
        return new FileOutputStream(getFileForChunk(chunkid, chunk));
    }

    public File getCachedir() {
        return cachedir;
    }

    public void setCachedir(File cachedir) {
        if (cachedir.isDirectory()) {
            this.cachedir = cachedir;
        }
    }

    public boolean isReadable(File file) {
        boolean ret = false;
        if (file != null && file.exists() && file.isFile()) {
            try {
                FileReader fileReader = new FileReader(file.getAbsolutePath());
                fileReader.read();
                fileReader.close();
                ret = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public void copyX(File from, File to) throws IOException {
        try {
            BufferedInputStream source = new BufferedInputStream(new FileInputStream(from));
            try {
                BufferedOutputStream sink = new BufferedOutputStream(new FileOutputStream(to));
                byte[] buffer = new byte[512];
                int length;
                while (true) {
                    try {
                        if (!((length = source.read(buffer)) > 0)) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new IOException("Could not read from source for: " + from.getAbsolutePath());
                    }
                    try {
                        sink.write(buffer, 0, length);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new IOException("Could not write to sink for: " + to.getAbsolutePath());
                    }
                }
                try {
                    source.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new IOException("Could not close source for: " + from.getAbsolutePath());
                }
                try {
                    sink.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new IOException("Could not close sink for: " + to.getAbsolutePath());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new FileNotFoundException("Could not find target file: " + to.getAbsolutePath());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new FileNotFoundException("Could not find source file: " + from.getAbsolutePath());
        }
    }
}
