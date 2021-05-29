//Implementation: Ηλίας Παυλάκος
package Run;

import Broker.Broker;

import static com.pavlakosilias.musicstreamingapp.Objects.Helpers.getSystemIP;

public class Broker2 {

    public static final String  IP         = getSystemIP(); //If this doesn't work, put your IP manually.
    public static final int     serverPort = 5002;

    public static void main(String[] args){
        //Create broker 2
        Broker b1 = new Broker(2, args);




        //Start the server
        b1.openServer(IP, serverPort);
    }


}
