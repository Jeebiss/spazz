package net.jeebiss.spazz.github;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class GitHub {

    public static final String GITHUB_URL = "https://api.github.com";
    public final String authentication;

    private static GitHub current;
    private static Requester requester;

    private Map<String, String> emojis;

    private GitHub(String oauth) {
        authentication = oauth;
    }

    public static GitHub connect(String oauth) {
        current = new GitHub(oauth);
        requester = new Requester(current);
        current.emojis = current.retrieve().parseStringMap(GITHUB_URL + "/emojis");
        return current;
    }

    public static GitHub currentInstance() {
        return current;
    }

    public int responseCode() throws Exception {
        return retrieve().githubConnection().getResponseCode();
    }

    public boolean isConnected() throws Exception {
        return (retrieve().githubConnection().getResponseCode() == 200);
    }

    public Repository getRepository(String ownerProject) throws Exception {
        Repository repository = retrieve().parse(GITHUB_URL + "/repos/" + ownerProject, Repository.class);
        repository.upStats();
        return repository;
    }

    public Map<String, String> getEmojis() {
        return emojis;
    }

    public Requester retrieve() {
        return requester.method("GET");
    }

    public Requester post() {
        return requester.method("POST");
    }

    public Requester request() {
        return requester;
    }

    public RateLimit getRateLimit() {
        try {
            return retrieve().parse(GITHUB_URL + "/rate_limit", RateLimit.class);
        } catch(Exception e) {
            System.out.println("Could not retrieve rate limit: " + e.getMessage());
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
