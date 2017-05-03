package HTTPServer;

import junit.framework.TestCase;

/**
 * Created by ytseitkin on 5/2/2017.
 */
public class HTTPRequestTest extends TestCase {

    public void testParseRequest() throws Exception {

        String request = "GET /web/interface/index HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "Accept-Encoding: gzip, deflate\r\n" +
                "Connection: keep-alive\r\n" +
                "Upgrade-Insecure-Requests: 1\r\n" +
                "\r\n";

        HTTPRequest httpRequest = new HTTPRequest(1);

        assertTrue(httpRequest.parseRequest(request.getBytes()));

        request = "This is not the good format";

        assertFalse(httpRequest.parseRequest(request.getBytes()));

        request = "CONNECT /web/interface/index HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        //METHOD not allowed
        assertFalse(httpRequest.parseRequest(request.getBytes()));

        //Messed up header
        request = "GET /web/interface/index HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0\r\n" +
                "Accept: \r\n" +
                "\r\n";

        assertFalse(httpRequest.parseRequest(request.getBytes()));

        //Messed up header
        request = "GET /web/interface/index HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0\r\n" +
                "Accept: adsafasdf;\r\n" +
                "\r\n";

        assertFalse(httpRequest.parseRequest(request.getBytes()));

        //Messed up header
        request = "GET /web/interface/index HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0\r\n" +
                "Accept: aaaa,\r\n" +
                "\r\n";

        assertFalse(httpRequest.parseRequest(request.getBytes()));
    }

    public void testContainsHeader() throws Exception {

        HTTPRequest httpRequest = new HTTPRequest(1);

        String request = "GET /web/interface/index HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0\r\n" +
                "Accept: text/html\r\n" +
                "\r\n";

        assertTrue(httpRequest.parseRequest(request.getBytes()));

        assertTrue(httpRequest.containsHeader("Accept"));

    }

}