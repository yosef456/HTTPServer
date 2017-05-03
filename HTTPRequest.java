package HTTPServer;

import java.util.*;

/**
 * Created by ytseitkin on 3/16/2017.
 */
public class HTTPRequest {

    private HashMap<String, ArrayList<HeaderValue>> headers;
    private RequestVerb verb;
    private String httpVersion;
    private String url;
    private byte[] messageBody;
    private int ID;

    public HTTPRequest(int ID){
        this.ID = ID;
        headers = new HashMap<>();
    }

//
//    public static boolean checkIfFormatted(String request){
//
//        String [] lines = request.split("\r\n");
//
//        if(lines.length<2)
//            return false;
//
//        if(!lines[0].matches(HTTPRequestRegex.requestLine))
//            return false;
//
//        String headerValue = "(" + HTTPRequestRegex.token + "+|\\*{1})" + "+" + "(;" + HTTPRequestRegex.token
//                + "+=(([1]|[0]|0\\.[0-9])|" + HTTPRequestRegex.token + "+){1})?";
//
//
//        String values = headerValue + "(," + headerValue +  ")*";
//
//        for(int i=2; !lines[i].equals(""); i++){
//            if (!lines[i].startsWith("User-Agent:") && !lines[i].matches(HTTPRequestRegex.fieldName + ": "+ values))
//                return false;
//        }
//
//        return true;
//    }
    public boolean parseRequest(byte[] request){

        int indexOfMessage = 0;

        for(int i=0;i<request.length-3;i++){
            if(request[i]=='\r' && request[i+1] == '\n' && request[i+2]=='\r' && request[i+3] == '\n'){
                indexOfMessage = i;
                break;
            }
        }

        if(indexOfMessage==0){
            return false;
        }

        byte[] headersBytes  = new byte[indexOfMessage];

        byte[] message = new byte[request.length-indexOfMessage -4];

        for(int i=0;i<indexOfMessage;i++)
            headersBytes[i] = request[i];

        for(int i=0;i<message.length;i++)
            message[i] = request[i + 4 + indexOfMessage];

        messageBody = message;

        String[] headersString = (new String(headersBytes)).split("\r\n");

        if(!headersString[0].matches(HTTPRequestRegex.requestLine)){
            messageBody = null;
            return false;
        }

        String [] firstLine = headersString[0].split(" ");

        if(!Arrays.stream(RequestVerb.values()).anyMatch(VRequest -> VRequest.toString().equals(firstLine[0]))){
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

                //check if the number of , matches with the number of args
                if(args.length-1 != (header[1].length() - header[1].replace(",", "").length())){
                    messageBody = null;
                    return false;
                }

                ArrayList <HeaderValue> argsList = new ArrayList<>();

                for(String arg: args){

                    arg = arg.trim();

                    //Check if it has errors
                    if(arg.equals("") || (arg.contains(";") && !arg.split(";",2)[1].contains("=")) ||
                            (!arg.contains(";") && arg.trim().contains(" "))||
                             (arg.contains(";") && arg.split(";",2)[0].trim().contains(" "))){
                        messageBody = null;
                        return false;
                    }

                    if (arg.contains(";") && arg.split(";",2)[1].contains("=") && arg.split(";")[1].split("=")[0].trim().equals("q") ){

                        if((arg.split(";")[1].split("=")[1].trim().equals(""))){
                            messageBody = null;
                            return false;
                        }

                        argsList.add(new HeaderValue(arg.split(";")[0].trim(),Double.parseDouble(arg.split(";")[1].split("=")[1].trim())));

                    } else if(arg.contains(";") && arg.split(";",2)[1].contains("=")) {

                        if((arg.split(";")[1].split("=")[1].trim().equals(""))){
                            messageBody = null;
                            return false;
                        }

                        argsList.add(new HeaderValue(arg.split(";")[0].trim(),arg.split(";")[1].split("=")[1].trim()));

                    } else {
                        argsList.add(new HeaderValue(arg.trim(),1));
                    }
                }

                Collections.sort(argsList);

                headers.put(header[0],argsList);
            }

        } catch (Exception e){
            messageBody = null;
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

    public byte[] getMessageBody(){ return messageBody;}

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
