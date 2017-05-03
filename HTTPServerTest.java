package HTTPServer;

import junit.framework.TestCase;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

/**
 * Created by ytseitkin on 5/2/2017.
 */
public class HTTPServerTest extends TestCase {
    public void testIsInt() throws Exception {

        assertTrue(HTTPServer.isInt("5"));

        assertTrue(HTTPServer.isInt("5436"));

        assertFalse(HTTPServer.isInt("asdg"));

        assertFalse(HTTPServer.isInt("6yhfg4"));
    }

    public void testListen() throws Exception {

        Properties prop = new Properties();

        prop.setProperty("cacheSlots","5");

        prop.setProperty("port","8080");

        prop.setProperty("number_of_threads","100");

        prop.setProperty("freshTime","9999999");

        prop.setProperty("fileBase","C:\\Users\\ytseitkin");

        HTTPServer httpServer = new HTTPServer(prop);

        Runnable runnable = () -> {
            httpServer.listen();
        };
        Thread serverThread = new Thread(runnable);
        serverThread.start();

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        String request = "GET /web/interface/index HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "Accept-Encoding: gzip, deflate\r\n" +
                "Connection: keep-alive\r\n" +
                "Upgrade-Insecure-Requests: 1\r\n" +
                "\r\n";

        outputStream.write(request.getBytes());

        Thread.sleep(100);

        InputStream inputStream = socket.getInputStream();

        //The server responded
        assertTrue(inputStream.available()>0);

        socket.close();

        //Kill the server
        serverThread.interrupt();

    }

}