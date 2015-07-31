package ng.codehaven.bambam.models;

import com.parse.ParseObject;
import com.parse.ParseUser;

public class Activities extends ParseObject {
    public Activities(){

    }

    public String getActivityType(){
        return getString("activity");
    }

    public void setActivityType(String activity){
        put("activity", activity);
    }

    public ParseUser getFrom(){
        return getParseUser("from");
    }

    public void setFrom(ParseUser user){
        put("from", user);
    }

    public ParseUser getTo(){
        return getParseUser("to");
    }

    public void setTo(ParseUser user){
        put("to", user);
    }

    public String getTuneId(){
        return getString("tune");
    }

    public void setTuneId(String t){
        put("tune", t);
    }
}
