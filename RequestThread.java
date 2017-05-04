package HTTPServer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by ytseitkin on 3/16/2017.
 */
public class RequestThread implements Runnable {

    private Socket socket;
    protected HTTPRequest httpRequest;
    private FileManager fileManager;
    protected HTTPResponse httpResponse;

    public RequestThread(Socket socket, FileManager fileManager,int ID){
        httpRequest = new HTTPRequest(ID);
        this.socket = socket;
        this.fileManager = fileManager;
        httpResponse = new HTTPResponse();
    }

    protected boolean parseRequest(){

        ArrayList<Byte> request = new ArrayList<>();

        try{
            byte[] buffer = new byte[1024];
            int read;
            InputStream is = new DataInputStream(socket.getInputStream());

            int fails = 0;

            while(is.available()==0 && fails<10){
                fails++;
                System.out.println("Failed to read from socket again");
                Thread.sleep(250);
            }

            while(is.available() > 0) {
                read = is.read(buffer);

                for(int i=0;i<read;i++)
                    request.add(buffer[i]);

            }

        } catch (Exception e){
            return false;
        }

        byte [] arr = new byte[request.size()];

        for(int i=0;i<arr.length;i++)
            arr[i] = request.get(i);

        if(!httpRequest.parseRequest(arr)){
            return false;
        }

        return true;
    }

    protected void executeRequest(){

        FileManagerResponse fileManagerResponse;

        switch (httpRequest.getVerb()){

            case GET:
                fileManagerResponse = fileManager.get(httpRequest);

                if(fileManagerResponse.getStat() == FileManagerResponse.fileStatus.SUCCESS){

                    httpResponse.addMessage(fileManagerResponse.getContent());
                    httpResponse.addHeader("Content-Type",fileManagerResponse.getFormat());
                    httpResponse.addHeader("Content-Language",fileManagerResponse.getLang());
                    httpResponse.addHeader("Content-Length",Integer.toString(fileManagerResponse.getContent().length));
                    httpResponse.setStatus(200);

                } else{
                    getErrorResponse(fileManagerResponse);
                }

                break;


            case HEAD:

                fileManagerResponse = fileManager.get(httpRequest);

                if(fileManagerResponse.getStat() == FileManagerResponse.fileStatus.SUCCESS){

                    httpResponse.addMessage(new byte[0]);
                    httpResponse.addHeader("Content-Type",fileManagerResponse.getFormat());
                    httpResponse.addHeader("Content-Language",fileManagerResponse.getLang());
                    httpResponse.addHeader("Content-Length",Integer.toString(fileManagerResponse.getContent().length));

                } else{
                    getErrorResponse(fileManagerResponse);
                }

                break;

            case POST:
                fileManagerResponse = fileManager.post(httpRequest);

                getErrorResponse(fileManagerResponse);
                break;

            case PUT:

                fileManagerResponse = fileManager.put(httpRequest);

                getErrorResponse(fileManagerResponse);

                break;

            case DELETE:
                fileManagerResponse = fileManager.delete(httpRequest);

                getErrorResponse(fileManagerResponse);
                break;
        }

        httpResponse.addHeader("Connection", "Closed");

    }

    protected void getErrorResponse(FileManagerResponse fileManagerResponse){

        switch (fileManagerResponse.getStat()){
            case SUCCESS:
                httpResponse.addMessage(ErrorMessages.SUCCESS.getBytes());
                httpResponse.setStatus(200);
                break;

            case CONFLICT:
                httpResponse.addMessage(ErrorMessages.CONFLICT.getBytes());
                httpResponse.setStatus(409);
                break;

            case SERVICE_UNAVAILABLE:
                httpResponse.addMessage(ErrorMessages.SERVICE_UNAVAILABLE.getBytes());
                httpResponse.setStatus(503);
                break;

            case NOT_FOUND:
                httpResponse.addMessage(ErrorMessages.NOT_FOUND.getBytes());
                httpResponse.setStatus(404);
                break;

            case INTERNAL_ERROR:
                httpResponse.addMessage(ErrorMessages.INTERNAL_ERROR.getBytes());
                httpResponse.setStatus(500);
                break;

            case CREATED:
                httpResponse.addMessage(ErrorMessages.CREATED.getBytes());
                httpResponse.setStatus(201);
                break;

            case NOT_ACCEPTABLE:
                httpResponse.addMessage(ErrorMessages.NOT_ACCEPTABLE.getBytes());
                httpResponse.setStatus(406);
                break;

            case METHOD_NOT_ALLOWED:
                httpResponse.addMessage(ErrorMessages.NOT_ACCEPTABLE.getBytes());
                httpResponse.setStatus(406);
                break;
        }

        httpResponse.addHeader("Content-Type",fileManagerResponse.getFormat());
        httpResponse.addHeader("Content-Language",fileManagerResponse.getLang());
        httpResponse.addHeader("Content-Length",Integer.toString(httpResponse.getMessage().length));
    }

    private void respond () throws IOException{
        OutputStream out = socket.getOutputStream();

        out.write(httpResponse.toByte());
        out.flush();
        socket.close();
        out.close();
    }

    @Override
    public void run() {

        if(!parseRequest()){

            if(httpRequest.getMessageBody() == null){
                httpResponse = new HTTPResponse(400);
                httpResponse.addMessage(ErrorMessages.BAD_REQUEST.getBytes());
                httpResponse.addHeader("Content-Type","text/html");
                httpResponse.addHeader("Content-Language","en");
            }else{
                httpResponse = new HTTPResponse(405);
                httpResponse.addMessage(ErrorMessages.METHOD_NOT_ALLOWED.getBytes());
                httpResponse.addHeader("Content-Type","text/html");
                httpResponse.addHeader("Content-Language","en");
            }


        }
        else{
            System.out.println("Request processed");
            executeRequest();
        }


        try {
            respond();
        } catch (IOException e) {
            System.out.println("An error has occurred in the request thread");
        }
    }
}

