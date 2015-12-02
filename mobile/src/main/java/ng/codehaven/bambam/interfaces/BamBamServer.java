package ng.codehaven.bambam.interfaces;


import com.parse.ParseUser;

import java.text.ParseException;
import java.util.List;

public interface BamBamServer {
    List<ParseUser> getFriends(int order, int skip) throws ParseException;
}
