package ng.codehaven.cloudconvert;

/**
 * Created by Thompson on 9/16/2015.
 */
public class ProcessBody {
    private String apikey, inputformat, outputformat;

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getInputformat() {
        return inputformat;
    }

    public void setInputformat(String inputformat) {
        this.inputformat = inputformat;
    }

    public String getOutputformat() {
        return outputformat;
    }

    public void setOutputformat(String outputformat) {
        this.outputformat = outputformat;
    }
}
