package net.jeebiss.spazz.github;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;

import net.jeebiss.spazz.Utilities;

public class GitHub {

    private static final String GITHUB_URL = "https://api.github.com";
    
    public final String authentication;
    
    private GitHub(String user, String password) {
        authentication = new String(Base64.encodeBase64((user + ":" + password).getBytes()));
    }
    
    public static GitHub connect(String user, String password) {
        return new GitHub(user, password);
    }
    
    public Repository getRepository(String owner, String project) throws Exception {
        String url = GITHUB_URL + "/repos/" + owner + "/" + project;
        return new Repository(this, url, retrieve().parse(url));
    }
    
    public Requester retrieve() {
        return new Requester(this).method("GET");
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
    
    private static final String[] GITHUB_TIMES = {"yyyy/MM/dd HH:mm:ss ZZZZ","yyyy-MM-dd'T'HH:mm:ss'Z'"};
    
}
