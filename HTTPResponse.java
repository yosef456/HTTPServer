package HTTPServer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ytseitkin on 3/16/2017.
 */
public class HTTPResponse {

    private int status;
    private String httpV;
    private HashMap<String,String> headers;
    private byte [] message;

    public HTTPResponse(){

        headers = new HashMap<>();

        httpV = "HTTP/1.1";

        headers.put("Date",getServerTime());

    }

    public HTTPResponse(int status){
        this.status = status;

        headers = new HashMap<>();

        httpV = "HTTP/1.1";

        headers.put("Date",getServerTime());

    }

    public void setStatus(int status){
        this.status = status;
    }

    public void addMessage(byte [] content){
        this.message = content;
        headers.put("Content-Length", Integer.toString(content.length));
    }

    public byte [] getMessage(){
        return message;
    }

    public void addHeader(String key,String value){
        headers.put(key,value);
    }

    private static String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    public byte[] toByte(){

        String response = "";

        response += "HTTP/1.1 ";

        switch (status){
            case 200:
                response += "200 OK\n";
                break;

            case 201:
                response += "201 Created\n";
                break;

            case 404:
                response += "404 Not Found\n";
                break;

            case 406:
                response += "406 Not Acceptable\n";
                break;


            case 400:
                response += "400 Bad Request\n";
                break;

            case 500:
                response += "500 Internal Error\n";
                break;

            case 409:
                response += "409 Conflict\n";
                break;

            case 405:
                response += "405 Method Not Allowed\n";
                break;

            case 503:
                response += "503 Service Unavailable\n";
                break;

            default:
                response += "200 OK\n";
        }

        for(String key: headers.keySet()){
            response += key + ": " + headers.get(key) + "\n";
        }

        response+="\n";

        byte[] headers = response.getBytes();

        byte[] httpResponse = new byte[headers.length+message.length];

        for (int i = 0; i < httpResponse.length; ++i)
        {
            httpResponse[i] = i < headers.length ? headers[i] : message[i - headers.length];
        }

        return httpResponse;
    }


}
