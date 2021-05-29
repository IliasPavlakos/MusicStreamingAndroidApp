//Implementation: Ηλίας Παυλάκος
package Run;
import Publisher.Publisher;

import static com.pavlakosilias.musicstreamingapp.Objects.Helpers.getSystemIP;

/* pathToDataSet = system path to the folder where the dataSet is stored.
 * from and to represents for what artists this publisher will be responsible for.
 * for example, if from = A and to = F then the publisher will be responsible for all the artists whose name
 * is starting with an A, B, C, ...., F.
 */
public class Publisher3 {

    public static final int    id             = 3;
    public static final String IP             = getSystemIP(); //TODO: If this doesn't work, put your IP manually.
    public static final int    port           = 4000 + id; //This publisher's port
    public static final int    portToBroker1  = 5001;
    public static final int    portToBroker2  = 5002;
    public static final int    portToBroker3  = 5003;
    public static final String pathToDataSet  = "C:\\Users\\paula\\Desktop\\dataset1"; //Your dataSet path here.
    public static final char   from           = 'O';
    public static final char   to             = 'Z';

    public static void main(String[] args){
        //Create the publisher.
        Publisher p3 = new Publisher(id, IP, port, from, to, pathToDataSet);

        //Connect to a brokers
        p3.register(IP, portToBroker1);
        p3.register(IP, portToBroker2);
        p3.register(IP, portToBroker3);



        //Start the server.
        p3.openServer(IP, port);
    }

}