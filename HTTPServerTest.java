package HTTPServer;

import org.junit.Test;

import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by ytseitkin on 5/2/2017.
 */
public class HTTPServerTest {

    private String fileRoot = "C:\\Users\\ytseitkin";

    @Test
    public void testIsInt() throws Exception {

        assertTrue(HTTPServer.isInt("5"));

        assertTrue(HTTPServer.isInt("5436"));

        assertFalse(HTTPServer.isInt("asdg"));

        assertFalse(HTTPServer.isInt("6yhfg4"));
    }

    @Test
    public void testListen() throws Exception {

        Properties prop = new Properties();

        prop.setProperty("cacheSlots","5");

        prop.setProperty("port","8080");

        prop.setProperty("number_of_threads","100");

        prop.setProperty("freshTime","9999999");

        prop.setProperty("fileRoot",fileRoot);

        HTTPServer httpServer = new HTTPServer(prop);

        Runnable runnable = () -> {
            httpServer.listen();
        };
        Thread serverThread = new Thread(runnable);
        serverThread.start();

        Thread.sleep(3000);

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

        Thread.sleep(500);

        //Meaning we got the something otherwise the socket wouldn't have connected
        socket.close();

        //Kill the server
        serverThread.stop();

        Thread.sleep(3000);

    }

}