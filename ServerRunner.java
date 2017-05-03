package HTTPServer;

import java.util.Properties;

/**
 * Created by ytseitkin on 3/19/2017.
 */
public class ServerRunner {

    public static void main(String []args){

        Properties prop = new Properties();

        prop.setProperty("cacheSlots","5");

        prop.setProperty("port","8080");

        prop.setProperty("number_of_threads","10");

        prop.setProperty("freshTime","9999999");

        prop.setProperty("fileBase","C:\\Users\\ytseitkin");

//        try {
//            OutputStream outputStream = new FileOutputStream(new File("C:\\Users\\ytseitkin\\Desktop\\text.txt"));
//            prop.store(outputStream,"this is my prop files");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }



        HTTPServer server = new HTTPServer(prop);

        server.listen();
    }
}
