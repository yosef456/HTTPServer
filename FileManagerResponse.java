package HTTPServer;

/**
 * Created by ytseitkin on 4/18/2017.
 */
public class FileManagerResponse {

    public enum fileStatus{
        SUCCESS,NOT_FOUND,INTERNAL_ERROR,NOT_ACCEPTABLE,CONFLICT,CREATED,METHOD_NOT_ALLOWED,SERVICE_UNAVAILABLE
    }

    private fileStatus stat;

    private String format;
    private String lang;
    private byte[] content;

    public FileManagerResponse(fileStatus stat, String format, String lang, byte[] content) {
        this.stat = stat;
        this.format = format;
        this.lang = lang;
        this.content = content;
    }

    public FileManagerResponse(fileStatus stat) {
        this.stat = stat;
        this.format = "text/html";
        this.lang = "en";
        this.content = new byte[100];
    }

    public fileStatus getStat() {
        return stat;
    }

    public String getFormat() {
        return format;
    }

    public String getLang() {
        return lang;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
