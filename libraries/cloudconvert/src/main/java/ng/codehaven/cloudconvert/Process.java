package ng.codehaven.cloudconvert;


public class Process {

    private String url;
    private String id;
    private String host;
    private String expires;
    private int maxtime;
    private int minutes;

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host The host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return The expires
     */
    public String getExpires() {
        return expires;
    }

    /**
     * @param expires The expires
     */
    public void setExpires(String expires) {
        this.expires = expires;
    }

    /**
     * @return The maxtime
     */
    public int getMaxtime() {
        return maxtime;
    }

    /**
     * @param maxtime The maxtime
     */
    public void setMaxtime(int maxtime) {
        this.maxtime = maxtime;
    }

    /**
     * @return The minutes
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * @param minutes The minutes
     */
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }


}
