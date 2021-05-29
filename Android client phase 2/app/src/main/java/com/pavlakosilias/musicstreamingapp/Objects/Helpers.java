//Implementation: Ηλίας Παυλάκος
package com.pavlakosilias.musicstreamingapp.Objects;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * The type Helpers.
 */
public class Helpers implements Serializable {

    /**
     * Returns the IPv4 address of this computer.
     * Get system ip string.
     *
     * @return the string
     */
    //WARNING: ONLY FOR USE IN RUN PACKAGE
    public static String getSystemIP(){
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost(); //Get localhost
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (inetAddress != null) {
            return(inetAddress.getHostAddress()); //Ping localhost to get HostAddress and return it
        }
        return null;
    }

    /**
     * Prints the input album's tracks in console with a nice formatting.
     *
     * @param album the album
     */
    public static void printAlbumInConsole(ArrayList<MusicFile> album){
        System.out.println('\n' + "-----------------------------" + "Tracks by " +
                           album.get(0).artistName + " album-----------------------------");
        int count = 1;
        for(MusicFile track : album){
            if(count < 10) System.out.println(count + ".  " + track.trackName);
            else System.out.println(count + ". " + track.trackName);
            ++count;
        }
        System.out.println("-----------------------------------------------" +
                           "----------------------------------------" + '\n');
    }


}
