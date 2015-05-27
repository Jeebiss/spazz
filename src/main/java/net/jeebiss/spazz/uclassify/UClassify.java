package net.jeebiss.spazz.uclassify;

import com.google.gson.Gson;
import net.jeebiss.spazz.uclassify.results.GenderResult;
import net.jeebiss.spazz.util.Utilities;

import java.net.URLEncoder;

public class UClassify {

    private static final String UCLASSIFY_API = "http://uclassify.com/browse/uclassify/%s"
            + "/ClassifyText?readkey=%s&output=json&version=1.01&text=%s";
    private static final Gson gson = new Gson();
    public final String apiKey;

    public UClassify(String apiKey) {
        this.apiKey = apiKey;
    }

    public GenderResult classifyGender(String text) {
        try {
            String api = String.format(UCLASSIFY_API, "genderanalyzer_v5", apiKey, URLEncoder.encode(text, "UTF-8"));
            return gson.fromJson(Utilities.getStringFromUrl(api), GenderResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
