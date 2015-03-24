package net.jeebiss.spazz.google;

import com.google.gson.Gson;
import net.jeebiss.spazz.util.Utilities;

import java.net.URLEncoder;

public class Translator {

    private static final String TRANSLATE_URL = "http://translate.google.com/translate_a/t?client=p&text=%1$s&sl=%2$s&tl=%3$s&ie=UTF-8&oe=UTF-8";
    private static Gson gson = new Gson();

    public static Translation translate(String string) {
        String response = "";
        try {
            response = Utilities.getStringFromUrl(String.format(TRANSLATE_URL,
                    URLEncoder.encode(string, "UTF-8"), "auto", "en"));
            Translation translation = gson.fromJson(response, Translation.class);
            return translation != null ? translation : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Translation {

        protected Sentence[] sentences;
        protected String src;
        protected int server_time;

        public Sentence[] getSentences() {
            return sentences;
        }

        public String getSourceLanguage() {
            return src;
        }

        public int getServerTime() {
            return server_time;
        }

        public static class Sentence {
            protected String trans;
            protected String orig;

            public String getTranslation() {
                return trans;
            }

            public String getOriginal() {
                return orig;
            }
        }
    }
}
