package HTTPServer;

/**
 * Created by ytseitkin on 4/28/2017.
 */
public class ServerError extends Exception {

    private FileManagerResponse.fileStatus status;

    public ServerError(FileManagerResponse.fileStatus status) {
        this.status = status;
    }

    public FileManagerResponse.fileStatus getStatus() {
        return status;
    }
}
