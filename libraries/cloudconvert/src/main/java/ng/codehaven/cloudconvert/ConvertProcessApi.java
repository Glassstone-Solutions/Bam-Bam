package ng.codehaven.cloudconvert;


import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;

public class ConvertProcessApi {

    private static CloudConvertGetProcess mConvertProcessService;

    public static CloudConvertGetProcess GetConvertProcessApi() {
        if (mConvertProcessService != null) {
            RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://api.cloudconvert.com").build();
            mConvertProcessService = restAdapter.create(CloudConvertGetProcess.class);
        }
        return mConvertProcessService;
    }

    public interface CloudConvertGetProcess {
        @Headers("Content-Type: application/json")
        @POST("/process")
        void getProcess(@Body ProcessBody body, Callback<Process> cb);
    }

}
