package net.jeebiss.spazz.util;

import net.jeebiss.spazz.github.Requester;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utilities {

    private static Random random = new Random();
    private static ArrayList<HashMap<Integer, String>> quotes;
    private static ArrayList<HashMap<String, String>> quotesInfo;

    @SuppressWarnings("unchecked")
    public static void loadQuotes() {
        try {
            quotes = new ArrayList<HashMap<Integer, String>>();
            quotesInfo = new ArrayList<HashMap<String, String>>();
            ArrayList<HashMap<Object, Object>> allQuotes = null;
            Yaml yaml = new Yaml();
            File f = new File(System.getProperty("user.dir") + "/storage/quotes.yml");
            InputStream is = f.toURI().toURL().openStream();
            allQuotes = (ArrayList<HashMap<Object, Object>>) yaml.load(is);
            for (HashMap<Object, Object> fullQuote : allQuotes) {
                HashMap<Integer, String> quote = new HashMap<Integer, String>();
                quotesInfo.add(new HashMap<String, String>());
                int line = 0;
                for (Entry<Object, Object> entry : fullQuote.entrySet()) {
                    if (entry.getValue() instanceof byte[])
                        entry.setValue(new String((byte[]) entry.getValue()));
                    if (!(entry.getKey() instanceof Integer))
                        quotesInfo.get(quotesInfo.size()-1).put((String) entry.getKey(), (String) entry.getValue());
                    else {
                        quote.put(line, (String) entry.getValue());
                        line++;
                    }
                }
                quotes.add(quote);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveQuotes() {
        try {
            ArrayList<HashMap<Object, Object>> allQuotes = new ArrayList<HashMap<Object, Object>>();
            for (int i = 0; i < quotes.size(); i++) {
                HashMap<Integer, String> quote = quotes.get(i);
                HashMap<String, String> quoteInfo = quotesInfo.get(i);
                HashMap<Object, Object> fullQuote = new HashMap<Object, Object>();
                fullQuote.putAll(quote);
                fullQuote.putAll(quoteInfo);
                allQuotes.add(fullQuote);
            }
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/storage/quotes.yml");
            writer.write(yaml.dump(allQuotes));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasQuote(int number) {
        return number >= 0 && (quotes.size()-1) >= number;
    }

    public static int addQuote(String quote, String adder) {
        HashMap<Integer, String> newQuote = new HashMap<Integer, String>();
        HashMap<String, String> newQuoteInfo = new HashMap<String, String>();
        newQuote.put(0, quote);
        newQuoteInfo.put("added_by", adder);
        quotes.add(newQuote);
        quotesInfo.add(newQuoteInfo);
        return quotes.size() - 1;
    }

    public static int addQuote(HashMap<Integer, String> quote, String adder) {
        quotesInfo.add(new HashMap<String, String>());
        quotesInfo.get(quotes.size()).put("added_by", adder);
        quotes.add(quote);
        return quotes.size() - 1;
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
        }
        else {
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
        for (File file : fList) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(fileExtension.toLowerCase())) {
                ret.add(file);
            }
            else if (file.isDirectory()) {
                ret.addAll(findAllFilesInFolder(file, fileExtension));
            }
        }
        return ret;
    }

    public static ArrayList<String> getFileComments(ArrayList<File> files) {
        ArrayList<String> ret = new ArrayList<String>();
        for (File file : files) {
            try {
                try (Scanner scanner = new Scanner(new FileInputStream(file))) {
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

    public static String getShortUrl(String url) {
        String shortened = null;
        try {
            shortened = Requester.getGson().fromJson(getStringFromUrl("https://api-ssl.bitly.com/v3/shorten?login=spazzmatic&apiKey="
                    + System.getProperty("spazz.bitly") + "&longUrl=" + url), BitlyResponse.class).data.url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shortened;
    }
    
   private class BitlyResponse {
        private Data data;
        private class Data {
            private String url;
        }
    }

    public static String capitalize(String string) {
        String firstLetter = string.substring(0, 1);
        return string.replaceFirst(firstLetter, firstLetter.toUpperCase());
    }

    public static String uncapitalize(String string) {
        String firstLetter = string.substring(0, 1);
        return string.replaceFirst(firstLetter, firstLetter.toLowerCase());
    }

    public static String join(final Iterator<String> iterator, final String separator) {
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return "";
        }
        final String first = iterator.next();
        if (!iterator.hasNext()) {
            return first;
        }
        final StringBuilder buf = new StringBuilder();
        if (first != null) {
            buf.append(first);
        }
        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            final String string = iterator.next();
            if (string != null) {
                buf.append(string);
            }
        }
        return buf.toString();
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
