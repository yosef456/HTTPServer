package HTTPServer;

/**
 * Created by ytseitkin on 3/27/2017.
 */
public class HeaderValue implements Comparable {

    private String value;
    private String extra;
    private double priority;
    private boolean pri;

    public HeaderValue(String value, double priority){
        this.value = value;
        this.priority = priority;
        pri = true;
    }

    public HeaderValue(String value, String extra){
        this.value = value;
        this.extra = extra;
        pri = false;
    }

    public boolean hasExtra(){
        return !pri;
    }

    public String getExtra() {
        return extra;
    }

    public double getPriority() {
        return priority;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(Object o) {
        return   Double.compare(((HeaderValue) o).getPriority(), this.getPriority());
    }

    public String toString(){
        return value;
    }
}
