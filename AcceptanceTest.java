package HTTPServer;

import junit.framework.TestCase;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

/**
 * Created by ytseitkin on 5/2/2017.
 */
public class AcceptanceTest extends TestCase {

    HTTPServer server;

    Properties prop;

    Thread serverThread;

    File file;

    public void setUp() throws Exception {
        super.setUp();

        prop = new Properties();

        prop.setProperty("cacheSlots","5");

        prop.setProperty("port","8080");

        prop.setProperty("number_of_threads","100");

        prop.setProperty("freshTime","9999999");

        prop.setProperty("fileBase","C:\\Users\\ytseitkin");

        server = new HTTPServer(prop);

        Runnable runnable = () -> server.listen();
        serverThread = new Thread(runnable);
        serverThread.start();

        file = new File("C:\\Users\\ytseitkin\\web\\interface\\text#html\\en\\test");

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            writer.write("<html>This is the test file</html>");
        }
    }

    public void tearDown() throws Exception {
        //super.tearDown();

        serverThread.interrupt();
    }

    public void testGet() throws Exception {

        String request = "GET /web/interface/test HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(50);

        checkIfResponse(socket,"200");

        socket.close();
    }

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

        Thread.sleep(50);

        checkIfResponse(socket,"200");

        socket.close();
    }

    public void testPut() throws Exception {

        String request = "PUT /web/interface/test1 HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Type: text/html;charset=utf8\r\n" +
                "Content-Language: en\r\n" +
                "\r\n" +
                "<h1>This is the content of the file</h1>";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(50);

        checkIfResponse(socket,"201");

        File createdFile = new File("C:\\Users\\ytseitkin\\web\\interface\\text#html\\en\\test1");

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

    public void testDelete() throws Exception {

        file = new File("C:\\Users\\ytseitkin\\web\\interface\\text#html\\en\\testDelete");

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            writer.write("<html>This is the DeleteFile</html>");
        }

        String request = "DELETE /web/interface/testDelete HTTP/1.1\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(50);

        checkIfResponse(socket,"200");

        assertFalse(file.exists());

        socket.close();
    }

    public void testConnect() throws Exception {

        String request = "CONNECT /web/interface/testDelete HTTP/1.1\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(50);

        checkIfResponse(socket,"405");

        socket.close();
    }

    public void testGetNotFound() throws Exception {

        String request = "GET /web/interface/blahblahblah HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(50);

        checkIfResponse(socket,"404");

        socket.close();
    }

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

        Thread.sleep(50);

        checkIfResponse(socket,"200");
    }

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

        Thread.sleep(50);

        checkIfResponse(socket,"201");

        File createdFile = new File("C:\\Users\\ytseitkin\\web\\interface\\text#html\\en\\postTest");

        assertTrue(createdFile.delete());

        socket.close();
    }

    public void testDeleteDoesntExist() throws Exception {

        String request = "DELETE /web/sdmnvdfkj HTTP/1.1\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(50);

        checkIfResponse(socket,"404");

    }

    public void testInvalidRequestLine() throws Exception {

        String request = "GET sdvs blahb blah HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(50);

        checkIfResponse(socket,"400");
    }

    public void testInvalidHeader() throws Exception {

        String request = "GET /web/interface/test HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: blah blah\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "\r\n";

        Socket socket = new Socket("localhost",8080);

        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());

        Thread.sleep(50);

        checkIfResponse(socket,"400");
    }

    public void checkIfResponse(Socket socket, String response ) throws Exception{
        try(BufferedReader read = new BufferedReader (new InputStreamReader (socket.getInputStream()))){
            String[] responseLine = read.readLine().split(" ");

            assertTrue(responseLine.length>=3);

            assertEquals(responseLine[1],response);
        }
    }

}