package HTTPServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by ytseitkin on 3/16/2017.
 */
public class HTTPRequest {

    private HashMap<String, ArrayList<HeaderValue>> headers;
    private RequestVerb verb;
    private String httpVersion;
    private String url;
    private String messageBody;
    private int ID;

    public HTTPRequest(int ID){
        this.ID = ID;
        headers = new HashMap<>();
    }


    public static boolean checkIfFormatted(String request){

        String [] lines = request.split("\r\n");

        if(lines.length<2)
            return false;

        if(!lines[0].matches(HTTPRequestRegex.requestLine))
            return false;

        String headerValue = "(" + HTTPRequestRegex.token + "+|\\*{1})" + "+" + "(;" + HTTPRequestRegex.token
                + "+=(([1]|[0]|0\\.[0-9])|" + HTTPRequestRegex.token + "+){1})?";


        String values = headerValue + "(," + headerValue +  ")*";

        for(int i=2; !lines[i].equals(""); i++){
            if (!lines[i].startsWith("User-Agent:") && !lines[i].matches(HTTPRequestRegex.fieldName + ": "+ values))
                return false;
        }

        return true;
    }
    public boolean parseRequest(String request){

        String [] parts = request.split("\r\n\r\n");

        if(parts.length>1)
            messageBody = parts[1];
        else
            messageBody = "";

        String[] headersString = parts[0].split("\r\n");

        if(!headersString[0].matches(HTTPRequestRegex.requestLine)){
            messageBody = null;
            return false;
        }

        String [] firstLine = headersString[0].split(" ");

        if(!Arrays.stream(RequestVerb.values()).anyMatch(VRequest -> VRequest.toString().equals(firstLine[0]))){
            messageBody = firstLine[0];
            return false;
        }

        try{
            verb = RequestVerb.valueOf(firstLine[0]);

            url = firstLine[1];

            httpVersion = firstLine[2].trim();

            for(int i=1;i<headersString.length;i++){

                //TODO check if the line fits the grammar

                //If we are done with the headers
                if(headersString[i].trim().equals(""))
                    break;

                //Don't bother parsing User-Agent and Host since we don't use them for anything anyways
                if(headersString[i].contains("User-Agent") || headersString[i].contains("Host"))
                    continue;

                String[] header = headersString[i].split(":",2);

                String []args = header[1].split(",");

                ArrayList <HeaderValue> argsList = new ArrayList<>();

                for(String arg: args){


                    if (arg.contains(";") && arg.split(";",2)[1].contains("=") && arg.split(";")[1].split("=")[0].trim().equals("q") ){

                        argsList.add(new HeaderValue(arg.split(";")[0].trim(),Double.parseDouble(arg.split(";")[1].split("=")[1].trim())));

                    } else if(arg.contains(";") && arg.split(";",2)[1].contains("=")) {
                        argsList.add(new HeaderValue(arg.split(";")[0].trim(),arg.split(";")[1].split("=")[1].trim()));

                    } else {
                        argsList.add(new HeaderValue(arg.trim(),1));
                    }
                }

                Collections.sort(argsList);

                headers.put(header[0],argsList);
            }

        } catch (Exception e){
            return false;
        }

        return true;
    }


    public boolean containsHeader(String header){ return headers.containsKey(header);}

    public RequestVerb getVerb(){
        return verb;
    }

    public String getUrl(){
        return url;
    }

    public int getID(){ return ID;}

    public String getMessageBody(){ return messageBody;}

    public ArrayList<HeaderValue> getHeader(String header){
        return headers.get(header);
    }

    public int hashCode(){
        return url.hashCode();
    }

    public boolean equals(Object o){
        return ((HTTPRequest) o ).getUrl().equals(this.url);
    }

}
