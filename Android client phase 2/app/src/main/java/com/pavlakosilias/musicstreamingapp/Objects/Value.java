//Implementation: Ηλίας Παυλάκος
package com.pavlakosilias.musicstreamingapp.Objects;
import java.io.Serializable;

/**
 * This class represents a serialized object.
 * The type Value.
 */
public class Value implements Serializable {
    //fields----------------------------------------------------------------------------<
    private MusicFile musicFile;

    //Constructors----------------------------------------------------------------------<

    /**
     * Default Constructor
     * Instantiates a new Value.
     */

    public Value(){}

    /**
     * Raw data Constructor
     * Instantiates a new Value.
     *
     * @param trackName  the track name
     * @param artistName the artist name
     * @param albumInfo  the album info
     * @param genre      the genre
     * @param path       the path
     */
    public Value(String trackName,String artistName,String albumInfo,String genre,String path){
        this.musicFile = new MusicFile(trackName,artistName,albumInfo,genre, path);
    }

    /**
     * High level Constructor
     * Instantiates a new Value.
     *
     * @param musicFile the music file
     */
    public Value(MusicFile musicFile){
        this.musicFile = musicFile;
    }

    //Setters & Getters-----------------------------------------------------------------<
    /**
     * Sets music file.
     *
     * @param musicFile the music file
     */
    public void setMusicFile(MusicFile musicFile) {
        this.musicFile = musicFile;
    }

    /**
     * Gets music file.
     *
     * @return the music file
     */
    public MusicFile getMusicFile() {
        return musicFile;
    }
}
