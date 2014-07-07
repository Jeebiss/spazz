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
    private String primaryPod;
    private String cachedPodId;

    public QueryResult(Document doc) {
        this.document = doc;
        this.podList = new HashMap<String, Element>();
        NodeList pods = document.getElementsByTagName("pod");
        for (int x = 0; x < pods.getLength(); x++) {
            Element pod = (Element) pods.item(x);
            if (pod.getAttribute("primary").equals("true"))
                primaryPod = pod.getAttribute("id");
            this.podList.put(pod.getAttribute("id"), pod);
        }
    }

    public String getResult() {
        if (containsPod("Substitution")) {
            String ret = getSubPodTextContent("Substitution");
            if (ret.contains("=")) {
                String leftSide = ret.split(" = ")[0];
                ret = ret.replace(leftSide, leftSide.replace(" ", "*"));
            }
            HashMap<String, String> substitutions = new HashMap<String, String>();
            for (String sub : getInput().replaceAll("\\{|\\}", "").split(", ")) {
                if (!sub.contains("=")) continue;
                String[] s = sub.split("=");
                substitutions.put(s[0].replace(" ", ""), s[1].replace(" ", ""));
            }
            Matcher m = hasVars.matcher(ret);
            while (m.find()) {
                if (!substitutions.containsKey(m.group(0))) continue;
                ret = ret.replace(m.group(0), substitutions.get(m.group(0)));
            }
            return ret;
        }
        else if (containsPod("Result")) {
            return getSubPodTextContent("Result");
        }
        else if (containsPodStartingWith("Identification:") || containsPodStartingWith("CityLocation:")) {
            return getSubPodTextContent(cachedPodId);
        }
        else if (primaryPod != null) {
            return getSubPodTextContent(primaryPod);
        }
        return null;
    }

    public String getInput() {
        return getSubPodTextContent("Input");
    }

    private boolean containsPod(String podId) {
        return podList.containsKey(podId);
    }

    private boolean containsPodStartingWith(String podIdStart) {
        for (String podId : podList.keySet()) {
            if (podId.startsWith(podIdStart)) {
                cachedPodId = podId;
                return true;
            }
        }
        return false;
    }

    private String getSubPodTextContent(String podId) {
        return ((Element) podList.get(podId).getElementsByTagName("subpod").item(0))
                .getElementsByTagName("plaintext").item(0).getTextContent();
    }

    public boolean isSuccess() {
        return Boolean.parseBoolean(document.getDocumentElement().getAttribute("success"));
    }

    public boolean isError() {
        return Boolean.parseBoolean(document.getDocumentElement().getAttribute("error"));
    }

}
