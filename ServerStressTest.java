package HTTPServer;

import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * Created by ytseitkin on 4/29/2017.
 */
public class ServerStressTest {

    private static String fileRoot = "C:\\Users\\ytseitkin";

    @Test
    public void testStressTestServer() throws Exception{

        Properties prop = new Properties();

        prop.setProperty("cacheSlots","5");

        prop.setProperty("port","8080");

        prop.setProperty("number_of_threads","100");

        prop.setProperty("freshTime","9999999");

        prop.setProperty("fileRoot",fileRoot);

        HTTPServer server = new HTTPServer(prop);

        File file = new File(fileRoot + "\\web\\interface\\text#html\\en\\testStress");

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            writer.write("<html>This is the test file</html>");
        }

        Runnable runnable = server::listen;
        Thread serverThread = new Thread(runnable);
        serverThread.start();

        Thread.sleep(3000);
        ArrayList <Socket> sockets = new ArrayList<>();

        int success=0,fail=0;

        String request = "GET /web/interface/testStress HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "\r\n\r\n";


        for(int i=0;i<100;i++){

            try{
                Socket socket = new Socket("localhost",8080);
                sockets.add(socket);
                OutputStream out= socket.getOutputStream();
                out.write(request.getBytes());
            } catch(IOException e){
               System.out.println("The server is not up. Please Start the server first");
               System.exit(1);
            }

        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(Socket socket:sockets){
            try {
                StringBuilder requestString = new StringBuilder();
                byte[] buffer = new byte[1024];
                int read;
                InputStream is = socket.getInputStream();

                if(is.available()==0)
                    fail++;
                else{

                    while(is.available()>0){
                        read = is.read(buffer);
                        String output = new String(buffer, 0, read);
                        requestString.append(output);
                    }

                    if(requestString.toString().contains("200"))
                        success++;
                }

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n\n\nWorked: " + success + "\nFailed: " + fail);

        serverThread.stop();

        Thread.sleep(3000);

        assertTrue(fail<50);

        assertTrue(file.delete());
    }

}
