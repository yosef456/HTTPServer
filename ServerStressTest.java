package HTTPServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by ytseitkin on 4/29/2017.
 */
public class ServerStressTest {

    public static void main(String [] args){

        ArrayList <Socket> sockets = new ArrayList<>();

        int success=0,fail=0;

        String request = "GET /web/interface/index HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "Accept-Encoding: gzip, deflate\r\n" +
                "Connection: keep-alive\r\n" +
                "Upgrade-Insecure-Requests: 1\r\n" +
                "\r\n\r\n";


        for(int i=0;i<1000;i++){

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

        System.out.println("Worked: " + success + "\nFailed: " + fail);
    }

}
