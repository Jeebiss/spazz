package net.jeebiss.spazz.wolfram;

import net.jeebiss.spazz.util.Utilities;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URLEncoder;

public class QueryHandler {

    private static final String WOLFRAM_API = "http://api.wolframalpha.com/v2";

    public String WOLFRAM_KEY = null;
    private DocumentBuilder dBuilder = null;

    public QueryHandler(String key) {
        try {
            this.WOLFRAM_KEY = key;
            this.dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {}
    }

    public String parseMath(String input) {
        try {
            if (dBuilder == null) {
                System.out.println("dBuilder is null.");
                return null;
            }

            String url = WOLFRAM_API + "/query?input=" + URLEncoder.encode(input, "UTF-8") + "&appid=" + WOLFRAM_KEY + "&format=plaintext";
            Document doc = dBuilder.parse(Utilities.getStreamFromUrl(url));
            QueryResult output = new QueryResult(doc);

            if (output.isError()) {
                System.out.println("Query errored.");
                return null;
            }
            else if (!output.isSuccess()) {
                System.out.println("Query not successful.");
                return null;
            }

            return output.getResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
