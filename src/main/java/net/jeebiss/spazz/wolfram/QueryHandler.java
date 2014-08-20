package net.jeebiss.spazz.wolfram;

import net.jeebiss.spazz.util.Utilities;
import org.w3c.dom.Document;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

public class QueryHandler {

    private static final String WOLFRAM_QUERY = "http://api.wolframalpha.com/v2/query?input=";
    private static final String WOLFRAM_FORMAT = "&format=plaintext";
    private static final String IP_PARAM = "&ip=";

    private String WOLFRAM_KEY = "&appid=";
    private DocumentBuilder dBuilder = null;

    private Map<String, String> customDefinitions = new HashMap<String, String>();
    private List<String> definitionLayers = new ArrayList<String>();

    public QueryHandler(String key) {
        try {
            loadDefinitions();
            this.WOLFRAM_KEY += key;
            this.dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {}
    }

    public QueryResult parse(String input, String hostMask) {
        if (dBuilder == null) {
            System.out.println("dBuilder is null.");
            return null;
        }

        String in = input.trim().toLowerCase().replaceAll("\\s+", " ");
        if (customDefinitions.containsKey(in)) {
            String def = customDefinitions.get(in);
            if (def.toLowerCase().startsWith("aliasfor:")) {
                definitionLayers.add(in);
                String alias = def.trim().substring(9).toLowerCase().replaceAll("\\s+", " ");
                if (!definitionLayers.contains(alias)) {
                    return parse(alias, hostMask);
                }
            }
            else {
                definitionLayers.clear();
                return new QueryResult(input, def);
            }
        }

        definitionLayers.clear();
        return new QueryResult(parseDoc(input, hostMask), input);
    }

    private Document parseDoc(String input, String hostMask) {
        try {
            return dBuilder.parse(Utilities.getStreamFromUrl(
                    WOLFRAM_QUERY + URLEncoder.encode(input, "UTF-8") + WOLFRAM_KEY + WOLFRAM_FORMAT
                            + IP_PARAM + hostMask
            ));
        } catch(Exception e) {
            return null;
        }
    }

    public String getKey() {
        return WOLFRAM_KEY.substring(7);
    }

    public boolean loadDefinitions() {
        try {
            LinkedHashMap map = null;
            Yaml yaml = new Yaml();
            File df = new File(System.getProperty("user.dir") + "/storage/definitions.yml");
            if (df.exists()) {
                InputStream is = df.toURI().toURL().openStream();
                map = (LinkedHashMap) yaml.load(is);
                if (map != null && !map.isEmpty()) {
                    for (Object e : map.entrySet()) {
                        Map.Entry entry = (Map.Entry) e;
                        String value = entry.getValue() instanceof String ? (String) entry.getValue()
                                : entry.getValue() instanceof byte[] ? new String((byte[]) entry.getValue()) : null;
                        customDefinitions.put((String) entry.getKey(), value);
                    }
                }
                is.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveDefinitions() {
        try {
            File df = new File(System.getProperty("user.dir") + "/storage/definitions.yml");
            if (df.isDirectory())
                df.delete();
            if (!df.exists())
                df.createNewFile();
            if (customDefinitions.isEmpty()) return true;
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            FileWriter writer = new FileWriter(df);
            writer.write(yaml.dump(customDefinitions));
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addDefinition(String phrase, String definition) {
        customDefinitions.put(phrase.trim().replaceAll("\\s+", " ").toLowerCase(), definition.trim());
    }

    public boolean removeDefinition(String phrase) {
        phrase = phrase.trim().replaceAll("\\s+", " ").toLowerCase();
        if (!customDefinitions.containsKey(phrase)) return false;
        customDefinitions.remove(phrase);
        return true;
    }

}
