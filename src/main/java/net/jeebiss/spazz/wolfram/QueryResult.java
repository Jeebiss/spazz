package net.jeebiss.spazz.wolfram;

import java.util.HashMap;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class QueryResult {

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
            HashMap<String, String> substitutions = new HashMap<String, String>();
            for (String sub : ((Element) podList.get("Input").getElementsByTagName("subpod").item(0))
                    .getTextContent().replaceAll("\\{|\\}", "").split(", ")) {
                if (!sub.contains("=")) continue;
                String[] s = sub.split("=");
                substitutions.put(s[0].trim(), s[1].trim());
            }
            while (ret.matches(".*[a-zA-Z].*")) {
                for (Entry<String, String> entry : substitutions.entrySet()) {
                    ret = ret.replace(entry.getKey(), entry.getValue());
                }
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
