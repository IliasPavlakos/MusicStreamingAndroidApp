//Implementation: Ηλίας Παυλάκος
package com.pavlakosilias.musicstreamingapp.Objects;
import java.io.Serializable;

//This class represents an Artist.
public class ArtistName implements Serializable {
    //fields----------------------------------------------------------------------------<
    private String artistName;
    private int    artistKey;

    //Constructors----------------------------------------------------------------------<

    //Default Constructor
    public ArtistName(){}

    //Constructor
    public ArtistName(String artistName){
        this.artistName = artistName;
        this.artistKey = 0;
    }

    //Constructor
    public ArtistName(String artistName, int artistKey){
        this.artistName = artistName;
        this.artistKey = artistKey;
    }

    //Setters & Getters-----------------------------------------------------------------<
    public String getArtistName() {
        return artistName;
    }
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
    public void setArtistKey(int artistKey) {
        this.artistKey = artistKey;
    }
    public int getArtistKey() {
        return artistKey;
    }


}
