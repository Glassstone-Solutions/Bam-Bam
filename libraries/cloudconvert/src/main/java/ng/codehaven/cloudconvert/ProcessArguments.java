package ng.codehaven.cloudconvert;

/**
 * Created by Thompson on 9/9/2015.
 */
public class ProcessArguments {
    String apikey;
    String inputformat;
    String outputformat;

    public ProcessArguments(String apikey, String inputformat, String outputformat) {
        this.apikey = apikey;
        this.inputformat = inputformat;
        this.outputformat = outputformat;
    }
}
