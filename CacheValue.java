package HTTPServer;

import java.io.File;

/**
 * Created by ytseitkin on 4/14/2017.
 */
public class CacheValue {


        private File file;
        private byte[] content;

        protected CacheValue(File file, byte[] content){
            this.file = file;
            this.content = content;
        }

        public File getFile() {
            return file;
        }

        public byte[] getContent() {
            return content;
        }

}
