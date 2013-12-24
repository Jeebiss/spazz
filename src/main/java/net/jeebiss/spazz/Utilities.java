package net.jeebiss.spazz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class Utilities {
    
    private static Random random = new Random();
    private static ArrayList<HashMap<Integer, String>> quotes;
    private static ArrayList<HashMap<String, String>> quotesInfo;
    
    @SuppressWarnings("unchecked")
    public static void loadQuotes() {
        try {
            quotes = new ArrayList<HashMap<Integer, String>>();
            quotesInfo = new ArrayList<HashMap<String, String>>();
            ArrayList<HashMap<Object, Object>> map = null;
            Yaml yaml = new Yaml();
            File f = new File(System.getProperty("user.dir") + "/storage/quotes.yml");
            InputStream is = f.toURI().toURL().openStream();
            map = (ArrayList<HashMap<Object, Object>>) yaml.load(is);
            for (HashMap<Object, Object> quote : map) {
                HashMap<Integer, String> endQuote = new HashMap<Integer, String>();
                quotesInfo.add(new HashMap<String, String>());
                for (Entry<Object, Object> entry : quote.entrySet()) {
                    if (entry.getValue() instanceof byte[])
                        entry.setValue(new String((byte[]) entry.getValue()));
                    if (!(entry.getKey() instanceof Integer))
                        quotesInfo.get(quotes.size()).put((String) entry.getKey(), (String) entry.getValue());
                    else
                        endQuote.put((int) entry.getKey(), (String) entry.getValue());
                }
                quotes.add(endQuote);
            }
        } catch(Exception e) {}
    }
    
    public static void saveQuotes() {
        try {
            ArrayList<HashMap<Object, Object>> allQuotes = new ArrayList<HashMap<Object, Object>>();
            for (int x = 0; x < quotes.size(); x++) {
                allQuotes.add(new HashMap<Object, Object>());
                for (String quote : quotes.get(x).values()) {
                    allQuotes.get(x).put(x, quote);
                }
                for (Entry<String, String> quoteInfo : quotesInfo.get(x).entrySet()) {
                    allQuotes.get(x).put(quoteInfo.getKey(), quoteInfo.getValue());
                }
            }
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/storage/quotes.yml");
            writer.write(yaml.dump(allQuotes));
            writer.close();
        } catch (Exception e) {}
    }
    
    public static boolean hasQuote(int number) {
        return (quotes.size() <= number);
    }
    
    public static int addQuote(String quote, String adder) {
        HashMap<Integer, String> newQuote = new HashMap<Integer, String>();
        HashMap<String, String> newQuoteInfo = new HashMap<String, String>();
        newQuote.put(0, quote);
        newQuoteInfo.put("added_by", adder);
        quotes.add(newQuote);
        quotesInfo.add(newQuoteInfo);
        return quotes.size()-1;
    }
    
    public static int addQuote(HashMap<Integer, String> quote, String adder) {
        quotesInfo.add(new HashMap<String, String>());
        quotesInfo.get(quotes.size()).put("added_by", adder);
        quotes.add(quote);
        return quotes.size()-1;
    }
    
    public static void addToQuote(int number, String quote) {
        quotes.get(number).put(quotes.get(number).size(), quote);
    }
    
    public static void removeQuote(int number) {
        quotes.remove(number);
    }
    
    public static HashMap<Integer, String> getQuote(int number) {
        return quotes.get(number);
    }
    
    public static HashMap<String, String> getQuoteInfo(int number) {
        return quotesInfo.get(number);
    }
    
    public static int getRandomNumber() {
        return random.nextInt();
    }
    
    public static int getRandomNumber(int limit) {
        return random.nextInt(limit);
    }
    
    public static int getQuoteCount() {
        return quotes.size();
    }

    public static String getStringFromUrl(String url) throws IOException {
        return getStringFromStream(getStreamFromUrl(url));
    }
    
    public static String getStringFromUrls(List<String> urls) throws IOException {
        String returns = "";
        for (String url : urls) {
            returns += getStringFromUrl(url);
        }
        return returns;
    }
    
    public static InputStream getStreamFromUrl(String url) throws IOException {
        InputStream is;
        HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
        if (httpConn.getResponseCode() >= 400) {
            is = httpConn.getErrorStream();
        } else {
            is = httpConn.getInputStream();
        }
        return is;
    }
    
    public static String getStringFromStream(InputStream is) {
        Scanner sc = new Scanner(is);
        String ret = sc.useDelimiter("\\A").next();
        sc.close();
        return ret;
    }
    
    public static void unzipFileFromURL(String url, String outputFolder) {
        byte[] buffer = new byte[1024];
        try {
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }
            ZipInputStream zis = new ZipInputStream(new URL(url).openStream());
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                if (!newFile.getName().contains(".")) {
                    newFile.mkdirs();
                    ze = zis.getNextEntry();
                    continue;
                }
                FileOutputStream fos = new FileOutputStream(newFile);         
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static ArrayList<File> findFilesByExtension(String folder, final String fileExtension) {
        return findAllFilesInFolder(new File(folder), fileExtension);
    }
    
    public static ArrayList<String> findFileNamesByExtension(String folder, String fileExtension) {
        ArrayList<String> ret = new ArrayList<String>();
        for (File file : findFilesByExtension(folder, fileExtension)) {
            ret.add(file.getName());
        }
        return ret;
    }
    
    public static ArrayList<File> findAllFilesInFolder(File folder, String fileExtension) {
        ArrayList<File> ret = new ArrayList<File>();
        File[] fList = folder.listFiles();
        for (File file : fList){
            if (file.isFile() && file.getName().toLowerCase().endsWith(fileExtension.toLowerCase())){
                ret.add(file);
            } else if (file.isDirectory()){
                ret.addAll(findAllFilesInFolder(file, fileExtension));
            }
        }
        return ret;
    }
    
    public static ArrayList<String> getFileComments(ArrayList<File> files) {
        ArrayList<String> ret = new ArrayList<String>();
        for (File file : files) {
            try {
                try (Scanner scanner =  new Scanner(new FileInputStream(file))) {
                    while (scanner.hasNextLine()) {
                        String next = scanner.nextLine().trim();
                        System.out.println("     " + next);
                        if (next.startsWith("//")) {
                            ret.add(next);
                        }
                    }
                  }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
    
    @SuppressWarnings("unchecked")
    public static String getShortUrl(String url) {
        String shortened = null;
        try {
            shortened = (String) ((Map<String, Object>) getJSONFromMap((JSONObject) JSONValue.parse(
                    getStringFromUrl("https://api-ssl.bitly.com/v3/shorten?login=spazzmatic&apiKey=" 
                    + System.getProperty("spazz.bitly") + "&longUrl=" + url))).get("data")).get("url");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shortened;
    }
    
    public static String capitalize(String string) {
        String firstLetter = string.substring(0, 1);
        return string.replaceFirst(firstLetter, firstLetter.toUpperCase());
    }
    
    public static String uncapitalize(String string) {
        String firstLetter = string.substring(0, 1);
        return string.replaceFirst(firstLetter, firstLetter.toLowerCase());
    }
    
    public static JSONObject getJSONFromMap(Map<String, Object> map) {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }
        return json;
    }
    
    public static String encodeBase64(String string) {
        String encoded = "";
        byte[] stringArray;
        try {
            stringArray = string.getBytes("UTF-8");
        } catch (Exception e) {
            stringArray = string.getBytes();
        }
        int paddingCount = (3 - (stringArray.length % 3)) % 3;
        stringArray = b64ZeroPad(stringArray.length + paddingCount, stringArray);
        for (int i = 0; i < stringArray.length; i += 3) {
            int j = ((stringArray[i] & 0xff) << 16) +
                ((stringArray[i + 1] & 0xff) << 8) + 
                (stringArray[i + 2] & 0xff);
            encoded = encoded + CS.charAt((j >> 18) & 0x3f) +
                CS.charAt((j >> 12) & 0x3f) +
                CS.charAt((j >> 6) & 0x3f) +
                CS.charAt(j & 0x3f);
        }
        return encoded + "==".substring(0, paddingCount);
    }
    
    private static byte[] b64ZeroPad(int length, byte[] bytes) {
        byte[] padded = new byte[length];
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        return padded;
    }
    
    private static final String CS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    
}
