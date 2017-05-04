package HTTPServer;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by ytseitkin on 5/2/2017.
 */
public class HTTPServerTest {

    @Test
    public void testIsInt() throws Exception {

        assertTrue(HTTPServer.isInt("5"));

        assertTrue(HTTPServer.isInt("5436"));

        assertFalse(HTTPServer.isInt("asdg"));

        assertFalse(HTTPServer.isInt("6yhfg4"));
    }

}