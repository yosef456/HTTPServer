package HTTPServer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Created by ytseitkin on 5/2/2017.
 */
public class AcceptanceTest {

    private static Thread serverThread;

    private static final String fileRoot = "C:\\Users\\ytseitkin";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        Properties prop = new Properties();

        prop.setProperty("cacheSlots","5");

        prop.setProperty("port","8080");

        prop.setProperty("number_of_threads","100");

        prop.setProperty("freshTime","9999999");

        prop.setProperty("fileRoot",fileRoot);

        HTTPServer server = new HTTPServer(prop);

        Runnable runnable = () -> server.listen();


        serverThread = new Thread(runnable);
        serverThread.start();

        //Thread.sleep(3000);

        File file = new File(fileRoot + "\\web\\interface\\text#html\\en\\test");

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            writer.write("<html>This is the test file</html>");
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        //Just for testing
        serverThread.stop();
        Thread.sleep(3000);
    }

    @Test
    public void testGet() throws Exception {

        String request = "GET /web/interface/test HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(500);

        checkIfResponse(socket,"200");

        socket.close();
    }

    @Test
    public void testPost() throws Exception {

        String request = "POST /web/interface/test HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Type: text/html;charset=utf8\r\n" +
                "Content-Language: en\r\n" +
                "\r\n" +
                "<h1>This is the addition</h1>";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(500);

        checkIfResponse(socket,"200");

        socket.close();
    }

    @Test
    public void testPut() throws Exception {

        File createdFile = new File(fileRoot + "\\web\\interface\\text#html\\en\\test1");

        if(createdFile.exists())
            assertTrue(createdFile.delete());

        String request = "PUT /web/interface/test1 HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Type: text/html;charset=utf8\r\n" +
                "Content-Language: en\r\n" +
                "\r\n" +
                "<h1>This is the content of the file</h1>";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(500);

        checkIfResponse(socket,"201");

        StringBuilder fileContent = new StringBuilder();

        try(BufferedReader read = new BufferedReader (new FileReader (createdFile))){

            String line;

            while((line = read.readLine())!= null)
                fileContent.append(line);
        }

        assertEquals("<h1>This is the content of the file</h1>",fileContent.toString());

        socket.close();

        assertTrue(createdFile.delete());

    }

    @Test
    public void testDelete() throws Exception {

        File file = new File(fileRoot + "\\web\\interface\\text#html\\en\\testDelete");

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            writer.write("<html>This is the DeleteFile</html>");
        }

        String request = "DELETE /web/interface/testDelete HTTP/1.1\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(500);

        checkIfResponse(socket,"200");

        assertFalse(file.exists());

        socket.close();
    }

    @Test
    public void testConnect() throws Exception {

        String request = "CONNECT /web/interface/testDelete HTTP/1.1\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(500);

        checkIfResponse(socket,"405");

        socket.close();
    }

    @Test
    public void testGetNotFound() throws Exception {

        String request = "GET /web/interface/blahblahblah HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(500);

        checkIfResponse(socket,"404");

        socket.close();
    }

    @Test
    public void testPutExistsAlready() throws Exception {

        String request = "PUT /web/interface/test HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Type: text/html;charset=utf8\r\n" +
                "Content-Language: en\r\n" +
                "\r\n" +
                "<html>This is the test file</html>";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(500);

        checkIfResponse(socket,"200");
    }

    @Test
    public void testPostDoesntExistAlready() throws Exception {

        String request = "POST /web/interface/postTest HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Type: text/html;charset=utf8\r\n" +
                "Content-Language: en\r\n" +
                "\r\n" +
                "<h1>This is the addition</h1>";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(500);

        checkIfResponse(socket,"201");

        File createdFile = new File(fileRoot + "\\web\\interface\\text#html\\en\\postTest");

        assertTrue(createdFile.delete());

        socket.close();
    }

    @Test
    public void testDeleteDoesntExist() throws Exception {

        String request = "DELETE /web/sdmnvdfkj HTTP/1.1\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(500);

        checkIfResponse(socket,"404");

    }

    @Test
    public void testInvalidRequestLine() throws Exception {

        String request = "GET sdvs blahb blah HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(500);

        checkIfResponse(socket,"400");
    }

    @Test
    public void testInvalidHeader() throws Exception {

        String request = "GET /web/interface/test HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: blah blah\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(500);

        checkIfResponse(socket,"400");
    }

    public void checkIfResponse(Socket socket, String response ) throws Exception{
        try(BufferedReader read = new BufferedReader (new InputStreamReader (socket.getInputStream()))){

            assertTrue(socket.getInputStream().available()>0);

            String[] responseLine = read.readLine().split(" ");

            assertTrue(responseLine.length>=3);

            assertEquals(responseLine[1],response);
        }
    }

}