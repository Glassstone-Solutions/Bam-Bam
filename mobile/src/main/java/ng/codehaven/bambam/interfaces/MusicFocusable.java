package ng.codehaven.bambam.interfaces;

/**
 * Created by Thompson on 9/28/2015.
 */
public interface MusicFocusable {
    void focusGained();
    void focusLost(boolean isTransient, boolean canDuck);
}
