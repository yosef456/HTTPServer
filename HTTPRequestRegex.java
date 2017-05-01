package HTTPServer;

/**
 * Created by ytseitkin on 4/27/2017.
 */
public class HTTPRequestRegex {

    public static String tchar = "(!|#|\\$|%|&|'|\\*|\\+|-|\\.|\\^|`|\\||~|\\w)";
    public static String token =  "(" + tchar + "+)";
    public static String OWS = "( |\\t)*";
    public static String CRLF =  "(\\r\\n)";
    public static String unreserved = "(\\w|-|\\.|~)";
    public static String hexdig = "(\\d|[A-F])";
    public static String pctEncoding = "(%" + hexdig + "{2})";
    public static String subDelims = "(!|\\$|&|'|\\(|\\)|\\+|,|;|=)";
    public static String pchar = "(" + unreserved + "|" + pctEncoding + "|" + subDelims + "|:|@)";
    public static String segment = "(" + pchar + "*)";
    public static String absolutePath = "((\\/" + segment + ")+)";
    public static String query = "((" + pchar + "|\\/|\\?)*)";
    public static String originForm = "(" + absolutePath + "(\\?" + query + ")?)";
    public static String obsText = "(%x[8-9A-F][0-9A-F])";
    public static String vchar = "([[:ascii:]])";
    public static String fieldVchar = "(" + vchar + "|" + obsText + ")";
    public static String fieldContent = "(" + fieldVchar + "(( |\\t)+" + fieldVchar + ")?)";
    public static String obsFold = CRLF + "( |\\t)+";
    public static String fieldValue = "((" + fieldContent + "|" + obsFold + ")*)";
    public static String fieldName = token;
    public static String LWS = "(" + CRLF + "?" + "( |\\t)+)";
    public static String qvalue =  "([01](\\.\\d{0,3})?)";
    public static String weight = "(" + OWS + ";" + OWS + "q=" + qvalue + ")";
    public static String quotedPair = "(\\\\|\\t|" + vchar + "|" + obsText + ")";
    public static String qdtext = "(\\t| |%x21|%x(2[3-9A-F]|[3-4][0-9A-F]|5[0-9A-B])|%x(5[D-F]|6[0-9A-F]|7[0-9A-E])|" + obsText + ")";
    public static String quotedString = "(\\\"(" + qdtext + "|" + quotedPair + ")*\\\")";
    public static String ctext = "(\\t| |%x2[1-7]|%x(2[A-F]|[3-4][0-9A-F]|5[0-9A-B])|%x(5[D-F]|6[0-9A-F]|7[0-9A-E)|" + obsText + ")";
    public static String requestLine = "^(GET|HEAD|PUT|POST|DELETE|CONNECT|OPTIONS|TRACE) " + originForm + " HTTP\\/1\\.1";
    public static String supportedMethods = "(GET|HEAD|PUT|POST|DELETE)";
    public static String unsupportedMethods = "(CONNET|OPTIONS|TRACE)";
    public static String headerField = fieldName + ":" + OWS + fieldValue + OWS;
    public static String parameter = "(" + token + "=(" + token + "|" + quotedString + "))";
    public static String mediaRange = "((\\*\\/\\*|" + token + "\\/\\*|" + token + "\\/" + token + ")(" + OWS + ";" + OWS + parameter + ")*)";
    public static String acceptExt = "(" + OWS + ";" + OWS + token + "(=(" + token + "|" + quotedString + "))?)";
    public static String acceptParams = "(" + weight + "(" + acceptExt + ")*)";
    public static String accept = "((,|" + mediaRange + "(" + acceptParams + ")?)(" + OWS + "," + OWS + "(" + mediaRange + "(" + acceptParams + ")?))*)?";
    public static String acceptCharset = "(," + OWS + ")*((" + token + "|\\*)(" + weight + ")?)(" + OWS + ",(" + OWS + "((" + token + "|\\*)(" + weight + ")?))?)*";
    public static String codings = "(" + token + "|identity|\\*)";
    public static String acceptEncoding = "((,|(" + codings + "(" + weight + ")?))(" + OWS + ",(" + OWS + "(" + codings + "(" + weight + ")?))?)*)?";
    public static String languageRange = "([A-Za-z]{1,8}|\\*)(-([A-Za-z1-9]{1,8}|\\*))*";
    public static String acceptLanguage = "(," + OWS + ")*(" + languageRange + "(" + weight + ")?)(" + OWS + ",(" + OWS + "(" + languageRange + "(" + weight + ")?))?)*";
    public static String contentLength = "(\\d*)";
    public static String contentType = "(" + token + "\\/" + token + "(" + OWS + ";" + OWS + parameter + ")*)";
    public static String host = "^((([a-zA-Z]{1})|([a-zA-Z]{1}[a-zA-Z]{1})|([a-zA-Z]{1}[0-9]{1})|([0-9]{1}[a-zA-Z]{1})|([a-zA-Z0-9][a-zA-Z0-9-_]{1,61}[a-zA-Z0-9]))\\.([a-zA-Z]{2,6}|[a-zA-Z0-9-]{2,30}\\.[a-zA-Z]{2,3})|localhost)$";

}
