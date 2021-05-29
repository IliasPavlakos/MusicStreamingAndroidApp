//Implementation: Ηλίας Παυλάκος
package com.pavlakosilias.musicstreamingapp.Objects;
import java.io.Serializable;

//This class represents a generic music file and all of its properties.
public class MusicFile implements Serializable {
    //fields----------------------------------------------------------------------------<
    public String     trackName;
    public String     artistName;
    public String     albumInfo;
    public String     genre;
    public String     path;
    public byte[]     musicFileExtract;

    //Constructors----------------------------------------------------------------------<
    public MusicFile(String trackName,String artistName,String albumInfo,String genre,String path){
        this.trackName = trackName;
        this.artistName = artistName;
        this.albumInfo = albumInfo;
        this.genre = genre;
        this.path = path;
        this.musicFileExtract = null;
    }

    public MusicFile(MusicFile musicFile, byte[] musicFileExtract){
        this.trackName = musicFile.trackName;
        this.artistName = musicFile.artistName;
        this.albumInfo = musicFile.albumInfo;
        this.genre = musicFile.genre;
        this.path = musicFile.path;
        this.musicFileExtract = musicFileExtract;
    }


}

