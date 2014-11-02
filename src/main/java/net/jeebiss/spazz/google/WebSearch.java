package net.jeebiss.spazz.google;

import com.google.gson.Gson;
import net.jeebiss.spazz.util.Utilities;

import java.net.URLDecoder;
import java.net.URLEncoder;

public class WebSearch {

    private static final Gson gson = new Gson();
    private final static String WEB_API = "http://ajax.googleapis.com/ajax/services/search/web?rsz=1&v=1.0&q=";

    public Response search(final String query) {
        try {
            return gson.fromJson(Utilities.getStringFromUrl(
                    WEB_API + URLEncoder.encode(query, "UTF-8")
            ), Response.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public class Response {
        private Data responseData;
        private String responseDetails;
        private int responseStatus;

        public Data getResponseData() { return responseData; }
        public String getResponseDetails() { return responseDetails; }
        public int getResponseStatus() { return responseStatus; }
    }

    public class Data {
        private Result[] results;
        private Cursor cursor;

        public Result getMainResult() { return results[0]; }
        public long getEstimatedResultCount() { return cursor.estimatedResultCount; }
        public double getSearchResultTime() { return cursor.searchResultTime; }

        private class Cursor {
            private long estimatedResultCount;
            private double searchResultTime;
        }
    }

    public class Result {
        private String url;
        private String content;

        public String getUrl() {
            try {
                return URLDecoder.decode(url, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        public String getContent() { return content; }
    }

}
