package HTTPServer;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by ytseitkin on 3/16/2017.
 */
public class FileManager {

    private Cache cache;
    private String fileBase;
    private ConcurrentHashMap<String,ReentrantReadWriteLock> locks;

    public FileManager(int slots, long freshTime, String fileBase){

        cache = new Cache(slots, freshTime);
        this.fileBase = fileBase;
        locks = new ConcurrentHashMap<>();

    }

    protected ReentrantReadWriteLock getLockForFile(String path){

        //Get the lock for the file
        if(!locks.containsKey(path)){
            locks.put(path,new ReentrantReadWriteLock());
        }

        return locks.get(path);
    }

    public FileManagerResponse get(HTTPRequest request){

        ArrayList<File> files = new ArrayList<>();

        String fileName = request.getUrl().substring(request.getUrl().lastIndexOf('/')+1);

        String dirUrl = fileBase + request.getUrl().substring(0,request.getUrl().lastIndexOf('/'));

        File dir = new File(dirUrl);

        String slash = "/";

        //Check if this a mac/linux or a windows machine
        if (!dir.getAbsolutePath().contains(slash))
            slash = "\\";

        //He is trying to escape the root folder for the web, in which case he should be able to access anything
        if(!dir.isDirectory() || !dir.getAbsolutePath().startsWith(fileBase + slash + "web")){
            return (new FileManagerResponse(FileManagerResponse.fileStatus.NOT_FOUND));
        }

        //Remove extension if is present
        if(fileName.contains("."))
            fileName = fileName.substring(0,  fileName.lastIndexOf('.'));

        findAllFiles(dirUrl,fileName,files);

        //No version of the file exists
        if(files.size()==0)
            return (new FileManagerResponse(FileManagerResponse.fileStatus.NOT_FOUND));

        File foundFile;
        try {
            foundFile = findPerfectFileVersion(request,fileName,dirUrl,slash,files);
        } catch (ServerError serverError) {
            return new FileManagerResponse(serverError.getStatus());
        }

        String foundLang, foundFormat;

        //Find the format and language from the path
        String path = foundFile.getAbsolutePath();
        path = path.replace(slash + fileName,"");
        int langSlash = path.lastIndexOf(slash);
        foundLang = path.substring(langSlash + 1);
        foundFormat = path.substring(path.lastIndexOf(slash, langSlash-1) + 1, langSlash).replace("#","/");

        //Lock the file
        ReentrantReadWriteLock lock = getLockForFile(foundFile.getAbsolutePath());
        boolean locked;

        try {
            locked = lock.readLock().tryLock(20,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return new FileManagerResponse(FileManagerResponse.fileStatus.INTERNAL_ERROR);
        }

        //If we can't get the lock means there are too many people trying to access the file
        if(!locked)
            return new FileManagerResponse(FileManagerResponse.fileStatus.SERVICE_UNAVAILABLE);

        FileManagerResponse fileManagerResponse;

        //Check if the file is in the cache and if the client allows it
        if(cache.exists(foundFile.getAbsolutePath()) && !(request.containsHeader("Cache-Control") &&
                (request.getHeader("Cache-Control").get(0).getValue().equals("no-cache") ||
                        request.getHeader("Cache-Control").get(0).getValue().equals("max-age=0") ))){

            lock.readLock().unlock();
            CacheValue cacheValue = cache.get(foundFile.getAbsolutePath());

            fileManagerResponse = new FileManagerResponse(FileManagerResponse.fileStatus.SUCCESS, foundFormat,foundLang,cacheValue.getContent());

            System.out.println("Yes cache");
        } else {

            //File was deleted in the time we were trying to lock it
            if(!foundFile.exists())
                return (new FileManagerResponse(FileManagerResponse.fileStatus.NOT_FOUND));

            System.out.println("No cache");

            byte [] content = readFromFile(foundFile);

            lock.readLock().unlock();

            //in case of IOException in readFromFile
            if (content == null) {
                return new FileManagerResponse(FileManagerResponse.fileStatus.INTERNAL_ERROR);
            }

            fileManagerResponse = new FileManagerResponse(FileManagerResponse.fileStatus.SUCCESS, foundFormat, foundLang, content);

            cache.insert(foundFile.getAbsolutePath(), new CacheValue(foundFile, content));
        }

        //Convert the bytes of the file to the needed encoding if it is a text file
        convertMessageEncoding( request,fileManagerResponse);

        return fileManagerResponse;
    }

    public FileManagerResponse post(HTTPRequest request){

        boolean created = false;

        //Make sure we have the needed headers
        if(!request.containsHeader("Content-Type") || !request.containsHeader("Content-Language"))
            return (new FileManagerResponse(FileManagerResponse.fileStatus.CONFLICT));

        ArrayList <HeaderValue> types = request.getHeader("Content-Type");

        //Remove any values that do not contain the extra info (i.e. the charset)
        types.removeIf(headerValue -> !headerValue.hasExtra());

        if(types.size()==0)
            return (new FileManagerResponse(FileManagerResponse.fileStatus.CONFLICT));

        String fileName = request.getUrl().substring(request.getUrl().lastIndexOf('/')+1);

        String path = fileBase + request.getUrl().substring(0,request.getUrl().lastIndexOf('/'));

        //Can't go outside of the designated dir or he is trying
        if(!path.startsWith(fileBase + "/" + "web") || !(new File(path).isDirectory()))
            return (new FileManagerResponse(FileManagerResponse.fileStatus.NOT_FOUND));

        path += "/" + request.getHeader("Content-Type").get(0).getValue().replace("/","#") + "/"
                + request.getHeader("Content-Language").get(0).getValue();

        File dir = new File(path);

        //If we don't have the format/language formats created
        if(!dir.isDirectory())
            dir.mkdirs();

        String slash = "/";

        //Check for windows/linux/mac machines
        if (!dir.getAbsolutePath().contains(slash))
            slash = "\\";

        //Find the file he is talking about
        File[] files = dir.listFiles((dirPath, name) -> name.startsWith(fileName));
        File file;

        //Check if the file exists yet, if yes send back a 200 else send back a 201
        if(files==null || files.length == 0 ){
            file = new File(path + slash + fileName);
            created = true;
        } else {
            file = files[0];
        }

        File temp = createTempFile(file,RequestVerb.POST,request.getID());

        if(temp==null)
            return (new FileManagerResponse(FileManagerResponse.fileStatus.INTERNAL_ERROR));

        byte[] contentMessage = request.getMessageBody();

        if(types.get(0).getValue().contains("text")) {
            try {
                String convert = new String(request.getMessageBody(), types.get(0).getExtra());
                contentMessage = convert.getBytes("utf8");
            } catch (UnsupportedEncodingException e) {
                return (new FileManagerResponse(FileManagerResponse.fileStatus.CONFLICT));
            }
        }

        //TODO lock file
        ReentrantReadWriteLock lock = getLockForFile(file.getAbsolutePath());
        boolean locked;

        try {
            locked = lock.writeLock().tryLock(20,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return new FileManagerResponse(FileManagerResponse.fileStatus.INTERNAL_ERROR);
        }

        if(!locked)
            return new FileManagerResponse(FileManagerResponse.fileStatus.SERVICE_UNAVAILABLE);

        try (OutputStream outputStream = new FileOutputStream(file,true)){
            outputStream.write(contentMessage);
            cache.delete(file.getAbsolutePath());
        } catch (IOException e){
            lock.writeLock().unlock();
            return (new FileManagerResponse(FileManagerResponse.fileStatus.INTERNAL_ERROR));
        }

        lock.writeLock().unlock();

        temp.delete();

        if(!created)
            return (new FileManagerResponse(FileManagerResponse.fileStatus.SUCCESS));
        else
            return (new FileManagerResponse(FileManagerResponse.fileStatus.CREATED));
    }

    public FileManagerResponse put(HTTPRequest request){

        boolean existed = false;

        String fileName = request.getUrl().substring(request.getUrl().lastIndexOf('/')+1);

        String url = fileBase + request.getUrl().substring(0,request.getUrl().lastIndexOf('/'));

        if(!(new File(url)).isDirectory()){
            return (new FileManagerResponse(FileManagerResponse.fileStatus.NOT_FOUND));
        }

        String format,encoding;

        //Check which folders to put this version
        if(request.containsHeader("Content-Type") && request.getHeader("Content-Type").stream().anyMatch((value) -> value.hasExtra()) ){

            HeaderValue headerValue =  request.getHeader("Content-Type").stream().filter((value) -> value.hasExtra())
                    .findFirst().get();

            encoding = headerValue.getExtra();

            format = headerValue.getValue().replace("/","#");
            url += "/" + format;
        } else {
            return (new FileManagerResponse(FileManagerResponse.fileStatus.CONFLICT));
        }

        if(request.containsHeader("Content-Language")){
            String lang = request.getHeader("Content-Language").get(0).getValue();
            url += "/" + lang;
        } else {
            return (new FileManagerResponse(FileManagerResponse.fileStatus.CONFLICT));
        }

        File foundDir = new File(url);

        boolean creatingDir = true;

        if(!foundDir.exists() || !foundDir.isDirectory())
            creatingDir = foundDir.mkdirs();

        if(!creatingDir){
            return (new FileManagerResponse(FileManagerResponse.fileStatus.INTERNAL_ERROR));
        }

        File file = new File(url + "/" + fileName);

        if(file.exists())
            existed = true;

        ReentrantReadWriteLock lock = getLockForFile(file.getAbsolutePath());
        boolean locked;

        try {
            locked = lock.writeLock().tryLock(20,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return new FileManagerResponse(FileManagerResponse.fileStatus.INTERNAL_ERROR);
        }

        if(!locked)
            return new FileManagerResponse(FileManagerResponse.fileStatus.SERVICE_UNAVAILABLE);

        FileManagerResponse fileManagerResponse;

        File temp = createTempFile(file,RequestVerb.PUT,request.getID());

        try(OutputStream output = new FileOutputStream(file)) {

            if(format.toLowerCase().contains("text")){
                String convert = new String(request.getMessageBody(),encoding);
                output.write(convert.getBytes("utf8"));
            }
            else
                output.write(request.getMessageBody());

            if(!existed)
                fileManagerResponse = new FileManagerResponse(FileManagerResponse.fileStatus.CREATED);
            else
                fileManagerResponse = new FileManagerResponse(FileManagerResponse.fileStatus.SUCCESS);

        } catch (IOException e) {
            fileManagerResponse = new FileManagerResponse(FileManagerResponse.fileStatus.CONFLICT);
        }

        lock.writeLock().unlock();

        temp.delete();

        return fileManagerResponse;

    }

    public FileManagerResponse delete(HTTPRequest request){

        //find all the versions of the file and delete them
        String fileName = request.getUrl().substring(request.getUrl().lastIndexOf("/")+1);

        ArrayList<File> allFiles = new ArrayList<>();

        if(fileName.contains("."))
            fileName = fileName.substring(0,  fileName.lastIndexOf('.'));

        findAllFiles(fileBase + request.getUrl().substring(0,request.getUrl().lastIndexOf("/")),fileName,allFiles);

        //There is no file return a 404
        if( allFiles.size()==0){
            return (new FileManagerResponse(FileManagerResponse.fileStatus.NOT_FOUND));
        }

        for (File file : allFiles){

            //For every file deleted make sure to lock it first
            ReentrantReadWriteLock lock = getLockForFile(file.getAbsolutePath());
            boolean locked;

            try {
                locked = lock.writeLock().tryLock(20,TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                return new FileManagerResponse(FileManagerResponse.fileStatus.INTERNAL_ERROR);
            }

            File temp = createTempFile(file,RequestVerb.DELETE,request.getID());

            if(!locked)
                return new FileManagerResponse(FileManagerResponse.fileStatus.SERVICE_UNAVAILABLE);

            cache.delete(file.getAbsolutePath());

            if(!file.delete()){
                //Something went wrong while deleting
                return new FileManagerResponse(FileManagerResponse.fileStatus.INTERNAL_ERROR);
            }
            locks.remove(file.getAbsolutePath());

            lock.writeLock().unlock();

            temp.delete();
        }

        return new FileManagerResponse(FileManagerResponse.fileStatus.SUCCESS);
    }

    protected byte [] readFromFile(File file) {

        try {
            return Files.readAllBytes(file.toPath());

        } catch (IOException e) {
            return null;
        }
    }


    protected void findAllFiles(String dir, String name,ArrayList<File> files){

        File directory  = new File(dir);

        File[] fList = directory.listFiles();

        //Remove the file extension

        if(fList == null){
            return;
        }

        for (File file : fList) {

            String fileName;

            if(file.getName().contains("."))
                fileName = file.getName().substring(0,  file.getName().lastIndexOf('.'));
            else
                fileName = file.getName();

            if (file.isFile() && fileName.equals(name)) {
                files.add(file);
            } else if (file.isDirectory()) {
                findAllFiles(file.getAbsolutePath(), name , files);
            }
        }

    }

    protected void convertMessageEncoding(HTTPRequest httpRequest,FileManagerResponse fileManagerResponse){

        if(!httpRequest.containsHeader("Accept-Charset") || httpRequest.getHeader("Accept-Charset").size()==0
                || !fileManagerResponse.getFormat().toLowerCase().contains("text")){
            return;
        }

        String encoding = httpRequest.getHeader("Accept-Charset").get(0).getValue();

        byte [] a = convertEncodingToGivenEncoding(fileManagerResponse.getContent(),encoding);


        if(a!=null)
            fileManagerResponse.setContent(a);

    }

    protected byte[] convertEncodingToGivenEncoding(byte[] message,String encoding){

        if(encoding.toLowerCase().equals("utf8") || encoding.toLowerCase().equals("utf-8"))
            return message;

        try{
            return (new String(message,"utf8")).getBytes(encoding);
        }catch (UnsupportedEncodingException e){
            return null;
        }


    }

    protected File findPerfectFileVersion(HTTPRequest request, String fileName, String dirUrl ,String slash, ArrayList<File> files) throws ServerError{

        ArrayList<HeaderValue> headerFormats = request.getHeader("Accept");

        if(headerFormats == null){
            headerFormats = new ArrayList<>();
            headerFormats.add(new HeaderValue("*/*",1));
        }

        ArrayList<HeaderValue> languageFormats = request.getHeader("Accept-Language");

        if(languageFormats == null){
            languageFormats = new ArrayList<>();
            headerFormats.add(new HeaderValue("*",1));
        }

        //Make sure we don't send a 406 just because we don't have the language. Add english as default else anything
        languageFormats.add(new HeaderValue("en",0.2));
        languageFormats.add(new HeaderValue("*",0.1));

        File foundFile= null;

        found: for(HeaderValue format: headerFormats) {
            for (HeaderValue lang : languageFormats) {

                if (format.getValue().equals("*/*") && lang.getValue().equals("*")) {

                    for(File file:files)
                        if(file.exists()){
                            foundFile = file;
                            break;
                        }

                    if(foundFile == null){
                        //All files were deleted by the time it found something
                        throw new ServerError(FileManagerResponse.fileStatus.INTERNAL_ERROR);
                    }
                    break found;
                }

                for (File file : files) {

                    String formatPath = format.getValue().replace("/", "#").replace("*","");

                    if (lang.getValue().equals("*")) {
                        if (file.exists() && file.getAbsolutePath().contains(formatPath)) {

                            foundFile = file;
                            break found;
                        }
                    } else {
                        if (file.exists() && file.getAbsolutePath().contains(formatPath) && file.getAbsolutePath().contains(slash + lang.getValue() + slash)) {
                            foundFile = file;
                            break found;
                        }
                    }
                }
            }
        }

        if(foundFile == null){
            throw new ServerError(FileManagerResponse.fileStatus.NOT_ACCEPTABLE);
        }

        return  foundFile;
    }

    protected File createTempFile(File file, RequestVerb verb, int ID){

        String slash = "/";

        //Check if this a mac/linux or a windows machine
        if (!file.getAbsolutePath().contains(slash))
            slash = "\\";

        File temp = new File(fileBase + slash +  "temp" + slash + Integer.toString(ID));

        try{
            temp.createNewFile();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(temp));

            switch ((verb)){
                case POST:
                    bufferedWriter.write("POST\n" + file.getAbsolutePath() + "\n" + (new RandomAccessFile(file,"rw")).length());
                    break;

                case PUT:
                    bufferedWriter.write("PUT\n" + file.getAbsolutePath());
                    break;

                case DELETE:
                    bufferedWriter.write("DELETE\n" + file.getAbsolutePath());
                    break;
            }

            bufferedWriter.flush();
            bufferedWriter.close();

        }catch (IOException e){
            return null;
        }

        return temp;
    }

}
