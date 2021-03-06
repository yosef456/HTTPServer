package HTTPServer;

import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.Assert.*;

/**
 * Created by ytseitkin on 5/1/2017.
 */
public class FileManagerTest {

    private String fileRoot = "C:\\Users\\ytseitkin";

    private FileManager fileManager = new FileManager(5, 999999,fileRoot );

    private HTTPRequest httpRequest = new HTTPRequest(1);

    @Test
    public void testGetLockForFile() throws Exception {

        ReentrantReadWriteLock lock = fileManager.getLockForFile(fileRoot + "\\web" );

        assertNotNull(lock);
    }

    @Test
    public void testGet() throws Exception {

        File dir = new File(fileRoot + "/web/interface/text#html/en");

        if(!dir.isDirectory())
            dir.mkdirs();

        File file = new File(fileRoot + "/web/interface/text#html/en/test");

        if(!file.exists()){
            try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))){
                bufferedWriter.write("<html>THis is a test</html>");
            }
        }

        String request = "GET /web/interface/test HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: text/html\r\n" +
                "Accept-Language: en\r\n" +
                "\r\n";

        assertTrue(httpRequest.parseRequest(request.getBytes()));

        FileManagerResponse fileManagerResponse = fileManager.get(httpRequest);
        assertEquals(fileManagerResponse.getFormat(),"text/html");

        if(new File(fileRoot + "/web/interface/text#html/en/index").exists())
            assertEquals(fileManagerResponse.getStat(),FileManagerResponse.fileStatus.SUCCESS);
        else
            assertEquals(fileManagerResponse.getStat(),FileManagerResponse.fileStatus.NOT_FOUND);

    }

    @Test
    public void testPost() throws Exception {

        File dir = new File(fileRoot + "/web/interface/text#txt/en");

        if(!dir.isDirectory())
            dir.mkdirs();

        File file = new File(fileRoot + "/web/interface/text#txt/en/test");

        if(file.exists())
            file.delete();

        //Make sure that the file is not empty
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

        String content = "blahblahblah";

        bufferedWriter.write(content);

        bufferedWriter.close();

        String request = "POST /web/interface/test HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Type: text/txt;charset=utf8\r\n" +
                "Content-Language: en\r\n" +
                "\r\n" +
                "This is the addition";

        HTTPRequest httpRequest = new HTTPRequest(1);

        assertTrue(httpRequest.parseRequest(request.getBytes()));

        FileManagerResponse fileManagerResponse = fileManager.post(httpRequest);

        assertEquals(fileManagerResponse.getFormat(),"text/html");
        assertEquals(fileManagerResponse.getStat(),FileManagerResponse.fileStatus.SUCCESS);

        BufferedReader inputStream = new BufferedReader(new FileReader(file));
        String fileContent = inputStream.readLine();

        inputStream.close();

        assertEquals(fileContent,content + "This is the addition");

        assertTrue(file.delete());
    }

    @Test
    public void testPut() throws Exception {

        String request = "PUT /web/interface/test HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Type: text/txt;charset=utf8\r\n" +
                "Content-Language: en\r\n" +
                "\r\n" +
                "This is the creation";

        assertTrue(httpRequest.parseRequest(request.getBytes()));

        File file = new File(fileRoot + "/web/interface/text#txt/en/test");

        if(file.exists())
            assertTrue(file.delete());

        FileManagerResponse fileManagerResponse = fileManager.put(httpRequest);

        assertEquals(fileManagerResponse.getStat(),FileManagerResponse.fileStatus.CREATED);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        String content = bufferedReader.readLine();

        bufferedReader.close();

        assertEquals(content,"This is the creation");

        assertTrue(file.delete());

    }

    @Test
    public void testDelete() throws Exception {
        String request = "DELETE /web/interface/test HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        assertTrue(httpRequest.parseRequest(request.getBytes()));

        File dir = new File(fileRoot + "/web/interface/text#txt/en");

        if(!dir.isDirectory())
            dir.mkdirs();

        File file = new File(fileRoot + "/web/interface/text#txt/en/test");

        if(!file.exists()){
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Just Random Stuff");
            bufferedWriter.close();
        }

        FileManagerResponse fileManagerResponse = fileManager.delete(httpRequest);

        assertEquals(fileManagerResponse.getStat(),FileManagerResponse.fileStatus.SUCCESS);

        assertFalse(file.exists());
    }

    @Test
    public void testReadFromFile() throws Exception {

        File dir = new File(fileRoot + "/web/interface/text#txt/en");

        if(!dir.isDirectory())
            dir.mkdirs();

        File file = new File(fileRoot + "/web/interface/text#txt/en/test");

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        bufferedWriter.write("Just Random Stuff");
        bufferedWriter.close();


        byte [] content = fileManager.readFromFile(file);

        assertEquals(new String(content),"Just Random Stuff");

        assertTrue(file.delete());
    }

    @Test
    public void testFindAllFiles() throws Exception {

        File dir = new File(fileRoot + "/web/interface/text#txt/en");

        if(!dir.isDirectory())
            dir.mkdirs();

        File file = new File(fileRoot + "/web/interface/text#txt/en/test");

        if(!file.exists()){
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Just Random Stuff");
            bufferedWriter.close();
        }

        ArrayList<File >files = new ArrayList<>();
        fileManager.findAllFiles(dir.getAbsolutePath(), "test",files);

        assertEquals(files.size(),1);
        assertEquals(files.get(0).getAbsoluteFile(),file.getAbsoluteFile());
    }

    @Test
    public void testFindPerfectFileVersion() throws Exception {

        File dir = new File(fileRoot + "/web/interface/text#txt/en");

        if(!dir.isDirectory())
            dir.mkdirs();

        File file = new File(fileRoot + "/web/interface/text#txt/en/test");

        if(!file.exists()){
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Just Random Stuff");
            bufferedWriter.close();
        }

        dir =  new File(fileRoot + "/web/interface");

        String request = "GET /web/interface/test HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: text/txt\r\n" +
                "Accept-Language: en\r\n" +
                "\r\n";

        assertTrue(httpRequest.parseRequest(request.getBytes()));

        ArrayList<File >files = new ArrayList<>();
        fileManager.findAllFiles(dir.getAbsolutePath(), "test",files);

        File perfectFileVersion = fileManager.findPerfectFileVersion(httpRequest,"test",dir.getAbsolutePath(),"\\",files);

        assertEquals(perfectFileVersion.getAbsoluteFile(),file.getAbsoluteFile());

        assertTrue(file.delete());
    }

    @Test
    public void testCreateTempFile() throws Exception {

        File file = new File(fileRoot + "/web/interface/text#txt/en/test");

        File temp = fileManager.createTempFile(file,RequestVerb.POST,1);

        assertTrue(temp.exists());

        BufferedReader bufferedReader = new BufferedReader(new FileReader(temp));

        String verb = bufferedReader.readLine();

        bufferedReader.close();

        assertEquals(verb,"POST");

    }

}