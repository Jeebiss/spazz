package net.jeebiss.spazz.github;

import net.jeebiss.spazz.util.Utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

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

    public Repository getRepository(String owner, String project, long updateDelay, boolean hasIssues, boolean hasComments, boolean hasPulls) throws Exception {
        return new Repository(this, updateDelay, hasIssues, hasComments, hasPulls, retrieve().parse(GITHUB_URL + "/repos/" + owner + "/" + project));
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
            return new Date((long) ((int) Utilities.getJSONFromMap((Map<String, Object>) retrieve().parse(GITHUB_URL + "/rate_limit").get("rate")).get("reset")) * 1000);
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
        long seconds = (Calendar.getInstance().getTimeInMillis() - trueTime) / 1000;
        long minutes = seconds / 60;
        seconds = seconds - (minutes * 60);
        long hours = minutes / 60;
        minutes = minutes - (hours * 60);
        long days = hours / 24;
        hours = hours - (days * 24);
        long years = days / 365;
        days = days - (years * 365);
        return (years > 1 ? years + " years, " : years == 1 ? "1 year, " : "")
                + (days > 1 ? days + " days, " : days == 1 ? "1 day, " : "")
                + (hours > 1 ? hours + " hours, " : hours == 1 ? "1 hour, " : "")
                + (minutes > 1 ? minutes + " minutes, " : minutes == 1 ? "1 minute, " : "")
                + (seconds == 1 ? "1 second ago" : seconds + " seconds ago");
    }

    private static final String[] GITHUB_TIMES = {"yyyy/MM/dd HH:mm:ss ZZZZ", "yyyy-MM-dd'T'HH:mm:ss'Z'"};

}
