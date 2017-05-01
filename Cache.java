package HTTPServer;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ytseitkin on 3/16/2017.
 */
public class Cache {

    private ConcurrentHashMap <String, CacheValue> cache;
    private ConcurrentLinkedQueue<String> list;

    private int slotNum;

    private long freshnessTime;

    public Cache(int slotNum, long freshTime){
        cache = new ConcurrentHashMap<>();
        list = new ConcurrentLinkedQueue<>();
        freshnessTime = freshTime;
        this.slotNum = slotNum;

    }

    public CacheValue get(String key){
        return cache.get(key);
    }

    public boolean exists(String request){

        return cache.containsKey(request) && isFresh(request);
    }

    public boolean isFresh(String request){

        CacheValue value = cache.get(request);

        File file = value.getFile();

        return (System.currentTimeMillis() - file.lastModified()) < freshnessTime ;
    }

    public void delete(String url){
        cache.remove(url);
    }

    public void insert(String url, CacheValue cacheValue){


        if(list.size()+1>slotNum){
            synchronized(this) {
                String stomp = list.poll();

                cache.remove(stomp);
            }
        }

        cache.put(url, cacheValue);
        list.add(url);
    }

}
