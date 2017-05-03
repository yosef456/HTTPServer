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

    private String fileBase;

    private int requestID;

    public HTTPServer(String propertiesFile){

        requestID = 0;

        prop = new Properties();

        if(!readFromPropFile(propertiesFile) || !prop.containsKey("fileBase")){
            System.out.println("An error has occurred while reading from properties file, please make sure the path is correct" +
                    "and that all the properties are correct and included. Please make sure to include:\n" +
                    "fileBase (location of the web directory)\n" +
                    "cacheSlots (number of slots in the cache)\n" +
                    "freshTime (Amount in milliseconds of freshness of the cache copy)\n" +
                    "number_of_threads (number of threads in thread pool)\n" +
                    "port (port the server will listening)");
            System.exit(1);
        }

        setDefaultPropValues();

        fileBase = prop.getProperty("fileBase");

        fileManager = new FileManager(Integer.parseInt(prop.getProperty("cacheSlots")), Long.parseLong(prop.getProperty("freshTime")),fileBase);

        rollBackNotCompletedRequests();

        try{
            serverSocket = new ServerSocket(Integer.parseInt(prop.getProperty("port")));
            executor = Executors.newFixedThreadPool(Integer.parseInt(prop.getProperty("number_of_threads")));
        } catch(IOException e){
            e.printStackTrace();
        }

        String slash = "\\";

        if(fileBase.contains("/"))
            slash = "/";

        if(!(new File(fileBase  + slash + "temp")).isDirectory())
            (new File(fileBase  + slash + "temp")).mkdirs();

    }

    public HTTPServer(Properties propertiesFile){
        prop = propertiesFile;

        fileBase = prop.getProperty("fileBase");

        fileManager = new FileManager(Integer.parseInt(prop.getProperty("cacheSlots")), Long.parseLong(prop.getProperty("freshTime")),fileBase);
        try{
            serverSocket = new ServerSocket(Integer.parseInt(prop.getProperty("port")));
            executor = Executors.newFixedThreadPool(Integer.parseInt(prop.getProperty("number_of_threads")));
        } catch(IOException e){
            e.printStackTrace();
        }

    }

    private void rollBackNotCompletedRequests(){

        String slash = "\\";

        if(fileBase.contains("/"))
            slash = "/";

        File tempDir = new File(fileBase + slash + "temp");

        //There are no files to return
        if(!tempDir.isDirectory())
            return;

        File [] tempFiles = tempDir.listFiles();

        if(tempFiles == null || tempFiles.length == 0)
            return;

        for(File file : tempFiles){
            try {


                /*
                Formats of temp Files would be as follows:
                In case of POST:
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
                Place where we started to add stuff
                (More Paths in case of delete)
                 */
                BufferedReader reader = new BufferedReader(new FileReader(file));
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

                        RandomAccessFile postedFile = new RandomAccessFile(target,"rwd");

                        postedFile.setLength(bytes);
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
            prop.setProperty("number_of_threads","5");
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
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                if(serverSocket!=null)
                    serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
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
