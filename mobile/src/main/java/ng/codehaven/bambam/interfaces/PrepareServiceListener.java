package ng.codehaven.bambam.interfaces;


import ng.codehaven.bambam.services.MusicService;

public interface PrepareServiceListener {

    /**
     * Called when the service is up and running.
     */
    void onServiceRunning(MusicService service);
    /**
     * Called when the service failed to start.
     * Also returns the failure reason via the exception
     * parameter.
     */
    void onServiceFailed(Exception exception);

}
