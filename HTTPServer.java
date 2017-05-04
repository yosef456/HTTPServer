package HTTPServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ytseitkin on 3/16/2017.
 */
public class HTTPServer {

    private Properties prop;

    private FileManager fileManager;

    private ServerSocket serverSocket;

    private ExecutorService executor;

    private String fileRoot;

    private int requestID;

    public HTTPServer(String propertiesFile){

        requestID = 0;

        prop = new Properties();

        if(!readFromPropFile(propertiesFile) || !prop.containsKey("fileRoot")){
            System.out.println("An error has occurred while reading from properties file, please make sure the path is correct" +
                    "and that all the properties are correct and included. Please make sure to include:\n" +
                    "fileRoot (location of the web directory: Must come from properties)\n" +
                    "cacheSlots (number of slots in the cache: Default 100 )\n" +
                    "freshTime (Amount in milliseconds of freshness of the cache copy: Default 99999)\n" +
                    "number_of_threads (number of threads in thread pool: Default 10)\n" +
                    "port (port the server will listening:D Default 8080)");
            System.exit(1);
        }

        setDefaultPropValues();

        fileRoot = prop.getProperty("fileRoot");

        fileManager = new FileManager(Integer.parseInt(prop.getProperty("cacheSlots")), Long.parseLong(prop.getProperty("freshTime")),fileRoot);

        rollBackNotCompletedRequests();

        try{
            serverSocket = new ServerSocket(Integer.parseInt(prop.getProperty("port")));
            executor = Executors.newFixedThreadPool(Integer.parseInt(prop.getProperty("number_of_threads")));
        } catch(Exception e){
            System.out.println("An error has occurred, shutting down server");
        }

        String slash = "\\";

        if(fileRoot.contains("/"))
            slash = "/";

        if(!(new File(fileRoot  + slash + "temp")).isDirectory())
            (new File(fileRoot  + slash + "temp")).mkdirs();

    }

    public HTTPServer(Properties propertiesFile){
        prop = propertiesFile;

        if(!prop.containsKey("fileRoot")){
            System.out.println("An error has occurred while reading from properties file, please make sure the path is correct" +
                    "and that all the properties are correct and included. Please make sure to include:\n" +
                    "fileRoot (location of the web directory: Must come from properties)\n" +
                    "cacheSlots (number of slots in the cache: Default 100 )\n" +
                    "freshTime (Amount in milliseconds of freshness of the cache copy: Default 99999)\n" +
                    "number_of_threads (number of threads in thread pool: Default 10)\n" +
                    "port (port the server will listening:D Default 8080)");
            System.exit(1);
        }

        setDefaultPropValues();

        fileRoot = prop.getProperty("fileRoot");

        fileManager = new FileManager(Integer.parseInt(prop.getProperty("cacheSlots")), Long.parseLong(prop.getProperty("freshTime")),fileRoot);
        try{
            serverSocket = new ServerSocket(Integer.parseInt(prop.getProperty("port")));
            executor = Executors.newFixedThreadPool(Integer.parseInt(prop.getProperty("number_of_threads")));
        } catch(IOException e){
            System.out.println("An error has occurred, shutting down server");
        }

        rollBackNotCompletedRequests();

    }

    private void rollBackNotCompletedRequests(){

        String slash = "\\";

        if(fileRoot.contains("/"))
            slash = "/";

        File tempDir = new File(fileRoot + slash + "web" + slash + "temp");

        //There are no files to return
        if(!tempDir.isDirectory())
            return;

        File [] tempFiles = tempDir.listFiles();

        if(tempFiles == null || tempFiles.length == 0)
            return;

        for(File file : tempFiles){
            try(BufferedReader reader = new BufferedReader(new FileReader(file))) {


                /*
                Formats of temp Files would be as follows:
                In case of PUT:
                POST
                PATH

                In case of DELETE:
                DELETE
                PATH
                PATH
                ...

                In case of Post:
                POST
                PATH
                Length of original file
                (More Paths in case of delete)
                 */

                String line = reader.readLine();

                RequestVerb verb = RequestVerb.valueOf(line);

                switch (verb){

                    //Delete the file we started creating
                    case PUT:
                        (new File(reader.readLine())).delete();
                        break;

                        //
                    case DELETE:
                        ArrayList<String> paths = new ArrayList<>();
                        while((line = reader.readLine())!=null){
                            paths.add(line);
                        }

                        for(String path:paths){
                            (new File(path)).delete();
                        }
                        break;

                        //delete anything we have added from the file
                    case POST:
                        String path = reader.readLine();
                        int bytes = Integer.parseInt(reader.readLine());

                        File target = new File(path);

                        if(!target.exists())
                            continue;

                        try(RandomAccessFile postedFile = new RandomAccessFile(target,"rwd")){
                            postedFile.setLength(bytes);
                        }

                }

            } catch (IOException e) {
                System.out.println("Could not roll back requests");
            }

            file.delete();
        }
    }

    //If the value is missing or it is invalid, it will set a default value
    private void setDefaultPropValues(){

        if(!prop.containsKey("cacheSlots") || !isInt(prop.getProperty("cacheSlots"))){
            prop.setProperty("cacheSlots","100");
        }


        if(!prop.containsKey("port") || !isInt(prop.getProperty("port"))){
            prop.setProperty("port","8080");
        }

        if(!prop.containsKey("number_of_threads") || !isInt(prop.getProperty("number_of_threads"))){
            prop.setProperty("number_of_threads","10");
        }


        if(!prop.containsKey("freshTime") || !isInt(prop.getProperty("freshTime"))){
            prop.setProperty("freshTime","99999");
        }


    }

    protected static boolean isInt(String num){

        try{
            Integer.parseInt(num);
        }catch (Exception e){
            return false;
        }

        return true;
    }

    protected boolean readFromPropFile(String propertiesFile){

        try (InputStream input = new FileInputStream(propertiesFile)){

            prop.load(input);

        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    public void listen(){

        try{
            System.out.println("Starting to listen");
            while(true){
                Socket client= serverSocket.accept();
                System.out.println("getting request");
                newRequest(client);

            }
        } catch (Exception e){
            System.out.println("An error occurred, shutting down the server");
        } finally {
            try {
                if(serverSocket!=null)
                    serverSocket.close();
            } catch (IOException e) {
                System.out.println("Could not close server socket");
            }
        }

    }

    private void newRequest(Socket socket){
        System.out.println("accepting Request");
        requestID++;
        Thread th = new Thread (new RequestThread(socket, fileManager,requestID));

        executor.execute(th);
    }


}
