package net.jeebiss.spazz.wolfram;

import org.w3c.dom.*;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryResult {

    private final static Pattern hasVars = Pattern.compile("[a-zA-Z]");
    private final static Pattern hasUnicode = Pattern.compile("\\\\:([a-fA-F0-9]+)");
    private final static String[] equals = new String[] {
            "Substitution", "UnitSystem", "Encodings"
    };
    private final static String[] startsWith = new String[] {
            "Identification", "CityLocation", "Definition", "BasicInformation", "Taxonomy", "BasicProperties",
            "PhysicalCharacteristics", "TranslationsToEnglish", "HostInformationPodIP", "FlightStatus", "Area"
    };

    private String podId;
    private String futureTopic;
    private String suggestion;
    private String spellCheck;
    private String result;
    private String input;

    private boolean success;
    private boolean error;

    public QueryResult(Document doc, String inputFallback) {
        this.success = Boolean.parseBoolean(doc.getDocumentElement().getAttribute("success"));
        this.error = Boolean.parseBoolean(doc.getDocumentElement().getAttribute("error"));
        this.input = inputFallback;
        NodeList warnings = doc.getElementsByTagName("warnings");
        if (warnings.getLength() > 0) {
            NodeList spellchecks = ((Element) warnings.item(0)).getElementsByTagName("spellcheck");
            if (spellchecks.getLength() > 0) {
                spellCheck = ((Element) spellchecks.item(0)).getAttribute("text").replace("&quot;", "'");
            }
        }
        NodeList pods = doc.getElementsByTagName("pod");
        Element resultPod = null;
        for (int x = 0; x < pods.getLength(); x++) {
            Element pod = (Element) pods.item(x);
            String id = pod.getAttribute("id");
            if (id.equals("Input")) {
                input = ((Element) pod.getElementsByTagName("subpod").item(0))
                        .getElementsByTagName("plaintext").item(0).getTextContent();
                continue;
            }
            if (id.equals("Result")) {
                resultPod = pod;
                continue;
            }
            if (checkForResult(pod)) {
                podId = id;
                setResult(pod);
                return;
            }
        }
        if (resultPod != null) {
            podId = "Result";
            setResult(resultPod);
            return;
        }
        NodeList future = doc.getElementsByTagName("futuretopic");
        if (future.getLength() > 0) {
            Element futurePod = (Element) future.item(0);
            result = futurePod.getAttribute("topic") + " = " + futurePod.getAttribute("msg");
            return;
        }
        NodeList suggestions = doc.getElementsByTagName("didyoumeans");
        if (suggestions.getLength() > 0) {
            NodeList didyoumeans = ((Element) suggestions.item(0)).getElementsByTagName("didyoumean");
            if (didyoumeans.getLength() > 0) {
                suggestion = didyoumeans.item(0).getTextContent();
                return;
            }
        }
    }

    public QueryResult(String input, String result) {
        this.success = true;
        this.input = input;
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public String getInput() {
        return input;
    }

    public String getSpellCheck() { return spellCheck; }

    private boolean checkForResult(Element pod) {
        if (pod.hasAttribute("primary")) return true;
        String podId = pod.getAttribute("id");
        for (String s : equals)
            if (podId.equals(s)) return true;
        if (podId.contains(":")) {
            for (String s : startsWith)
                if (podId.startsWith(s)) return true;
        }
        return false;
    }

    private void setResult(Element pod) {
        String result = ((Element) pod.getElementsByTagName("subpod").item(0))
                .getElementsByTagName("plaintext").item(0).getTextContent();
        Matcher m = hasUnicode.matcher(result);
        boolean f = false;
        while (m.find()) {
            result = result.replaceFirst(m.group(0), String.valueOf((char) Integer.parseInt(m.group(1), 16)));
            f = true;
        }
        setResult(f ? result.replace("\\", "") : result);
    }

    private void setResult(String result) {
        if (podId.equals("Substitution")) {
            //if (result.contains("=")) {
            //    String leftSide = result.split(" = ")[0].trim();
            //    result = result.replace(leftSide, leftSide.replace(" ", "*"));
            //}
            HashMap<String, String> substitutions = new HashMap<String, String>();
            for (String sub : getInput().replaceAll("\\{|\\}", "").split(", ")) {
                if (!sub.contains("=")) continue;
                String[] s = sub.split("=");
                substitutions.put(s[0].replace(" ", ""), s[1].replace(" ", ""));
            }
            Matcher m = hasVars.matcher(result);
            while (m.find()) {
                if (!substitutions.containsKey(m.group(0))) continue;
                result = result.replace(m.group(0), substitutions.get(m.group(0)));
            }
            if (result.contains("=")) {
                String[] split = result.split("=");
                this.input = split[0].trim();
                result = result.substring(split[0].length());
            }
        }
        this.result = result.trim();
    }

    public boolean isFutureTopic() {
        return futureTopic != null;
    }

    public boolean isSuccess() {
        return success || isFutureTopic();
    }

    public boolean hasSuggestion() {
        return suggestion != null;
    }

    public boolean hasSpellCheck() { return spellCheck != null; }

    public String getSuggestion() {
        return suggestion;
    }

    public boolean isError() {
        return error;
    }

}
