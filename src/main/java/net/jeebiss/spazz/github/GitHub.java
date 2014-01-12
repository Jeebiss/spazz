package net.jeebiss.spazz.github;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import net.jeebiss.spazz.util.Utilities;

public class GitHub {

    public static final String GITHUB_URL = "https://api.github.com";
    public final String authentication;
    
    private GitHub(String oauth) {
        authentication = oauth;
    }
    
    public static GitHub connect(String oauth) {
        return new GitHub(oauth);
    }
    
    public int responseCode() throws Exception {
        return retrieve().githubConnection().getResponseCode();
    }
    
    public boolean isConnected() throws Exception {
        return (retrieve().githubConnection().getResponseCode() == 200);
    }
    
    public Repository getRepository(String owner, String project, long updateDelay, boolean hasIssues, boolean hasComments) throws Exception {
        return new Repository(this, updateDelay, hasIssues, hasComments, retrieve().parse(GITHUB_URL + "/repos/" + owner + "/" + project));
    }
    
    public Requester retrieve() {
        return new Requester(this).method("GET");
    }
    
    public Requester post() {
        return new Requester(this);
    }
    
    @SuppressWarnings("unchecked")
    public int getMaxRateLimit() {
        try {
            return (int) Utilities.getJSONFromMap((Map<String, Object>) retrieve().parse(GITHUB_URL + "/rate_limit").get("rate")).get("limit");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    @SuppressWarnings("unchecked")
    public int getRemainingRateLimit() {
        try {
            return (int) Utilities.getJSONFromMap((Map<String, Object>) retrieve().parse(GITHUB_URL + "/rate_limit").get("rate")).get("remaining");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    @SuppressWarnings("unchecked")
    public Date getRateLimitReset() {
        try {
            return new Date((long)((int) Utilities.getJSONFromMap((Map<String, Object>) retrieve().parse(GITHUB_URL + "/rate_limit").get("rate")).get("reset"))*1000);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Date parseDate(String date) {
        for (String time : GITHUB_TIMES) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(time);
                format.setTimeZone(TimeZone.getTimeZone("GMT"));
                return format.parse(date);
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }
    
    public static String parseDateSimple(String date) {
        Calendar trueDate = Calendar.getInstance();
        trueDate.setTime(parseDate(date));
        long trueTime = trueDate.getTimeInMillis();
        double seconds = (Calendar.getInstance().getTimeInMillis()-trueTime)/1000;
        double minutes = seconds/60;
        seconds = seconds-(minutes*60);
        double hours = minutes/60;
        minutes = minutes-(hours*60);
        double days = hours/24;
        hours = hours-(days*24);
        double years = days/365.242199;
        days = days-(years*365.242199);
        if ((int) years > 0) {
            return ((int) years) + " year" + ((int) years > 1 ? "s" : "") + " ago";
        } else if ((int) days > 0) {
            return ((int) days) + " day" + ((int) days > 1 ? "s" : "") + " ago";
        } else if ((int) hours > 0) {
            return ((int) hours) + " hour" + ((int) hours > 1 ? "s" : "") + " ago";
        } else if ((int) minutes > 0) {
            return ((int) minutes) + " minute" + ((int) minutes > 1 ? "s" : "") + " ago";
        } else {
            return ((int) seconds) + " second" + ((int) seconds > 1 ? "s" : "") + " ago";
        }
    }
    
    private static final String[] GITHUB_TIMES = {"yyyy/MM/dd HH:mm:ss ZZZZ","yyyy-MM-dd'T'HH:mm:ss'Z'"};
    
}
