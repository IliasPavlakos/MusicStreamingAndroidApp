//Implementation: Ηλίας Παυλάκος
package com.pavlakosilias.musicstreamingapp.Objects;
import java.io.Serializable;

//This class represents a serialized object.
public class Value implements Serializable {
    //fields----------------------------------------------------------------------------<
    private MusicFile musicFile;

    //Constructors----------------------------------------------------------------------<

    //Default Constructor
    public Value(){}

    //Raw data Constructor
    public Value(String trackName,String artistName,String albumInfo,String genre,String path){
        this.musicFile = new MusicFile(trackName,artistName,albumInfo,genre, path);
    }

    //High level Constructor
    public Value(MusicFile musicFile){
        this.musicFile = musicFile;
    }

    //Setters & Getters-----------------------------------------------------------------<
    public void setMusicFile(MusicFile musicFile) {
        this.musicFile = musicFile;
    }
    public MusicFile getMusicFile() {
        return musicFile;
    }
}
