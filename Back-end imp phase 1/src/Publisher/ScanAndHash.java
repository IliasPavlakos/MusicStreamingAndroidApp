//Implementation: Ηλίας Παυλάκος
package Publisher;

import com.pavlakosilias.musicstreamingapp.Objects.ArtistName;
import com.pavlakosilias.musicstreamingapp.Objects.MusicFile;
import com.mpatric.mp3agic.*;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScanAndHash {

    /* This class is responsible of scanning the system path given upon a Publisher's creation and returning
     * the appropriate data structures in order to initialize the publisher.
     */
    public static class mp3DataExtractor{

        /* Scan a path and build the artistList and albumList.
         * artistList contains all the artists this publisher is responsible for.
         * Each cell of albumList contains a list of songs of an artist.
         * If we want to access songs of the artist at index 93 of the artistList, then we just get the list
         * from cell 93 of the albumList.
         */
        public static void scanDataSet(char from, char to, String path, ArrayList<ArtistName> artistList, ArrayList<ArrayList<MusicFile>> albumList) {
            System.out.print("Scanning directory...");
            File folder = new File(path);
            if (!folder.canRead()) {
                System.out.println("Error: Path to dataSet is incorrect.");
            } else {

                //Extract all the mp3 files, build them into a MusicFile object & create an array of them.
                ArrayList<MusicFile> musicFiles = new ArrayList<MusicFile>();
                extractMusicFiles(folder, musicFiles);

                /* Create a list containing all the artists and one with all songs for each artist.
                 * If for example artist "Travis Scott" is at index 92 in the artistList, then all his songs
                 * can be found in the list of the index 92 of the albumList.
                 */
                for (MusicFile song : musicFiles) {
                    //Check if mp3 has a tag, if not skip it.
                    ArtistName artistName = new ArtistName(song.artistName);
                    String str = song.artistName;

                    //Filter the songs artist based on the 'from' and 'to' chars.
                    if (str != null) {
                        if (!str.equals(" ") && str.length() > 1) {
                            if (Character.toLowerCase(str.charAt(0)) >= from && Character.toLowerCase(str.charAt(0)) <= to) {

                                //If the artist is new, create a new album.
                                if (!containsArtist(artistName, artistList)) {
                                    //System.out.println("new album from: " + str);
                                    artistList.add(artistName);
                                    albumList.add(new ArrayList<MusicFile>());
                                }

                                albumList.get(indexOfArtist(artistName, artistList)).add(song);
                            }
                        }
                    }

                }

                //Print confirmation message on console.
                int sum = 0;
                for (ArrayList<MusicFile> album : albumList) {
                    for (MusicFile song : album) sum++;
                }
                System.out.println(".........Done! Found " + albumList.size() + " albums with a total of " + sum  + " songs.");
            }
        }

        //Returns an array of mp3 files from a given folder and of its sub-folders.
        private static void extractMusicFiles(final File folder, ArrayList<MusicFile> result) {
            //for each file inside the folder.
            for (final File f : Objects.requireNonNull(folder.listFiles())) {
                //If it is a folder, search inside it.
                if (f.isDirectory()) {
                    extractMusicFiles(f, result);
                }
                //If it is a file, check if it is an mp3.
                if (f.isFile()) {
                    if (f.getName().matches("(.*)\\.mp3") && !f.getName().matches("._(.*)")) {
                        //Build mp3
                        MusicFile musicFile = buildMusicFile(f);
                        //Filter
                        if(musicFile.trackName != null && !musicFile.trackName.equals(" ") && musicFile.trackName.length() > 2)
                            result.add(musicFile);
                    }
                }//End of if

            }//End of for
        }

        //Returns a MusicFile object (containing only the metaData) from the input file.
        public static MusicFile buildMusicFile(final File f){
            String     trackName  = null;
            String     artistName = null;
            String     albumInfo  = null;
            String     genre      = null;
            List<Byte> musicFileExtract = new ArrayList<>();
            String mp3Path = f.getAbsolutePath();

            try {
                Mp3File mp3 = new Mp3File(mp3Path);

                if (mp3.hasId3v1Tag()) {
                    ID3v1 id3v1Tag = mp3.getId3v1Tag();
                    trackName = id3v1Tag.getTitle();
                    artistName = id3v1Tag.getArtist();
                    albumInfo = id3v1Tag.getAlbum();
                    genre = id3v1Tag.getGenreDescription();
                } else if (mp3.hasId3v2Tag()) {
                    ID3v2 id3v2Tag = mp3.getId3v2Tag();
                    trackName = id3v2Tag.getTitle();
                    artistName = id3v2Tag.getArtist();
                    albumInfo = id3v2Tag.getAlbum();
                    genre = id3v2Tag.getGenreDescription();
                }

                return new MusicFile(trackName,artistName,albumInfo,genre, mp3Path);
            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
            }
            return null;
        }

        //Returns if this publisher is in charge for this artist.
        private static boolean containsArtist(ArtistName key, ArrayList<ArtistName> list){
            for (ArtistName artistName : list ) {
                String name = artistName.getArtistName();
                if(name.equals(key.getArtistName())) return true;
            }
            return false;
        }

        //Returns the index of the given artist in the artistList or -1 if not found.
        private static int indexOfArtist(ArtistName key, ArrayList<ArtistName> list){
            int pos = 0;
            for(ArtistName artistName : list){
                String name = artistName.getArtistName();
                if(name.equals(key.getArtistName())) return pos;
                else pos++;
            }
            return -1;
        }

    }//End of mp3DataExtractor

    /* This class is responsible for calculating the hash code for each artistName object a publisher
     * is responsible for. Hashing is done using the MD5 hashing function.
     */
    public static class ArtistHasher{

        //Generates a hash for all the artistName objects inside the input list.
        public static void assignHashes(ArrayList<ArtistName> list){
            System.out.print("Hashing artists...");
            if(list.isEmpty()) System.out.println("Warning at Publisher.assignHashes():" +
                                                  " Tried to assign hashes on an empty list");
            else {
                for (ArtistName artistName : list) {
                    //Get md5 hash code from getMd5()
                    BigInteger MD5hash = getMd5(artistName.getArtistName());

                    //Calculate the final hash code of the artist
                    int finalHash = MD5hash.intValue();
                    if (finalHash < 0) finalHash = -1 * finalHash;
                    finalHash = finalHash % 17;


                    //Set the filed hash in the artistName object to the final hash.
                    artistName.setArtistKey(finalHash);
                }
            }
            System.out.println("............Done!");
        }

        //Returns a MD5 hash value based on the input string.
        public static BigInteger getMd5(String input) {
            BigInteger no = null;
            try {
                //Static getInstance method is called with hashing MD5
                MessageDigest md = MessageDigest.getInstance("MD5");

                //digest() method is called to calculate message digest of an input, digest() return array of byte
                byte[] messageDigest = md.digest(input.getBytes());

                // Convert byte array into signum representation
                no = new BigInteger(1, messageDigest);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            return no;
        }

    }//End of ArtistHasher

}//End of ScanAndHash
