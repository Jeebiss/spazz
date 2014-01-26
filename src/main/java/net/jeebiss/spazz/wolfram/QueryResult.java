package net.jeebiss.spazz.wolfram;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryResult {

    private Pattern hasVars = Pattern.compile("[a-zA-Z]");
    private Document document;
    private HashMap<String, Element> podList;

    public QueryResult(Document doc) {
        this.document = doc;
        this.podList = new HashMap<String, Element>();
        NodeList pods = document.getElementsByTagName("pod");
        for (int x = 0; x < pods.getLength(); x++) {
            Element pod = (Element) pods.item(x);
            this.podList.put(pod.getAttribute("id"), pod);
        }
    }

    public String getResult() {
        if (podList.containsKey("Substitution")) {
            String ret = ((Element) podList.get("Substitution").getElementsByTagName("subpod").item(0))
                    .getElementsByTagName("plaintext").item(0).getTextContent();
            if (ret.contains("=")) {
                String leftSide = ret.split(" = ")[0];
                ret = ret.replace(leftSide, leftSide.replace(" ", "*"));
            }
            HashMap<String, String> substitutions = new HashMap<String, String>();
            for (String sub : ((Element) podList.get("Input").getElementsByTagName("subpod").item(0))
                    .getTextContent().replaceAll("\\{|\\}", "").split(", ")) {
                if (!sub.contains("=")) continue;
                String[] s = sub.split("=");
                substitutions.put(s[0].trim(), s[1].trim());
            }
            Matcher m = hasVars.matcher(ret);
            while (m.find()) {
                if (!substitutions.containsKey(m.group(0))) continue;
                ret = ret.replace(m.group(0), substitutions.get(m.group(0)));
            }
            return ret;
        }
        else if (podList.containsKey("Result")) {
            return ((Element) podList.get("Result").getElementsByTagName("subpod").item(0))
                    .getElementsByTagName("plaintext").item(0).getTextContent();
        }
        return null;
    }

    public boolean isSuccess() {
        return Boolean.parseBoolean(document.getDocumentElement().getAttribute("success"));
    }

    public boolean isError() {
        return Boolean.parseBoolean(document.getDocumentElement().getAttribute("error"));
    }

}
