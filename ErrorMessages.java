package HTTPServer;

/**
 * Created by ytseitkin on 4/21/2017.
 */
public class ErrorMessages {

    public static String SUCCESS =
            "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Request status</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<h1>200</h1>\n" +
            "<h2>Request was completed successful</h2>\n" +
            "</body>\n" +
            "</html>";

    public static String METHOD_NOT_ALLOWED =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Request status</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>405</h1>\n" +
                    "<h2>The method sent is not supported.</h2>\n" +
                    "</body>\n" +
                    "</html>";

    public static String SERVICE_UNAVAILABLE =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Request status</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>503</h1>\n" +
                    "<h2>The server is currently unable to handle the request due to a temporary overloading or maintenance of the server.</h2>\n" +
                    "</body>\n" +
                    "</html>";

    public static String CREATED =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Request status</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>201</h1>\n" +
                    "<h2>File was created</h2>\n" +
                    "</body>\n" +
                    "</html>";


    public static String BAD_REQUEST =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Request status</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>400</h1>\n" +
                    "<h2>The request is malformed. Please make sure to check the syntax of the request. </h2>\n" +
                    "</body>\n" +
                    "</html>";

    public static String NOT_FOUND =
            "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Request status</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<h1>404</h1>\n" +
            "<h2>File not found. Please double check the URL. If you are attempting a PUT/POST, make sure that " +
                    "the topic exists. You may only create files not topics.</h2>\n" +
            "</body>\n" +
            "</html>";

    public static String INTERNAL_ERROR =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Request status</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>500</h1>\n" +
                    "<h2>Internal server error.\n " +
                    "Please resend request in a few minutes.</h2>\n" +
                    "</body>\n" +
                    "</html>";

    public static String NOT_ACCEPTABLE =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Request status</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>406</h1>\n" +
                    "<h2>Server doesn't have the version requested.\n " +
                    "Please change the headers to find a different version.</h2>\n" +
                    "</body>\n" +
                    "</html>";

    public static String CONFLICT =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Request status</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>409</h1>\n" +
                    "<h2>This means that the headers are either conflicting are missing. " +
                    "Please specify the version you wish to create/update in the headers" +
                    "Using the Content-Type and Content-Language headers. Please make sure Content-type includes a charset value\n </h2>" +
                    "</body>\n" +
                    "</html>";
}
