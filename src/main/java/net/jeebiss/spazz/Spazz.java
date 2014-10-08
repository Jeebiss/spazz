package net.jeebiss.spazz;

import net.jeebiss.spazz.github.*;
import net.jeebiss.spazz.util.javaluator.DoubleEvaluator;
import net.jeebiss.spazz.urban.*;
import net.jeebiss.spazz.util.MinecraftServer;
import net.jeebiss.spazz.util.Utilities;
import net.jeebiss.spazz.wolfram.QueryHandler;
import net.jeebiss.spazz.wolfram.QueryResult;
import org.pircbotx.*;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
public class Spazz extends ListenerAdapter {

    public static Spazz spazz = new Spazz();
    public static PircBotX bot = new PircBotX();

    public static File usersFolder = null;

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz");

    public static Map<String, dUser> dUsers = new HashMap<String, dUser>();

    String[] temp;
    public static String chatColor = Colors.TEAL;
    public static String optionalColor = Colors.DARK_GREEN;
    public static String defaultColor = Colors.OLIVE;
    boolean charging = false;
    long chargeInitiateTime;
    long chargeFullTime = 30000;
    User charger;
    int botsnack = 0;
    ArrayList<User> feeders = new ArrayList<User>();
    String confirmingComment = null;
    int confirmingIssue = 0;
    Boolean confirmComment = false;
    String confirmIssueUser = null;
    public static boolean shuttingDown = false;

    public static boolean debugMode = false;
    public static boolean devMode = false;
    public static String chatChannel = "#denizen-dev";
    public static GitHub github = null;
    public static RepositoryManager repoManager = null;
    public static Pattern issuesPattern = Pattern.compile("(\\w+/\\w+)\\s*#(\\d+)");
    public static Pattern minecraftColor = Pattern.compile((char) 0xa7 + "([0-9a-fA-Fl-oL-OrR])");
    public static Pattern minecraftRandom = Pattern.compile("(" + (char) 0xa7 + "k([^" + (char) 0xa7 + "]+))");

    public static Map<String, List<Message>> cachedMessages = new HashMap<String, List<Message>>();
    public static Pattern sReplace = Pattern.compile("^s/([^/]+)/([^/]+)/?([^\\s/]+)?", Pattern.CASE_INSENSITIVE);

    public static long messageDelay = 0;
    private static boolean wumbo = false;

    public static QueryHandler queryHandler = null;

    public static void main(String[] args) {

        bot.setEncoding(Charset.forName("UTF-8"));

        System.out.println("Starting Spazzmatic...");

        try {
            usersFolder = new File(System.getProperty("user.dir") + "/users");

            Yaml yaml = new Yaml();
            File sf = new File(usersFolder + "/spazzmatic.yml");
            sf.mkdirs();
            InputStream is = sf.toURI().toURL().openStream();
            LinkedHashMap map = (LinkedHashMap) yaml.load(is);
            if (map.get("password") instanceof String) {
                System.setProperty("spazz.password", (String) map.get("password"));
            }
            if (map.get("bitly") instanceof String) {
                System.setProperty("spazz.bitly", (String) map.get("bitly"));
            }
            if (map.get("bitly-backup") instanceof String) {
                System.setProperty("spazz.bitly-backup", (String) map.get("bitly-backup"));
            }
            if (map.get("dev-mode") instanceof Boolean) {
                devMode = (boolean) map.get("dev-mode");
            }
            if (map.get("wolfram") instanceof byte[]) {
                queryHandler = new QueryHandler(new String((byte[]) map.get("wolfram")));
            }
            else if (map.get("wolfram") instanceof String) {
                queryHandler = new QueryHandler((String) map.get("wolfram"));
            }
            if (map.get("github") instanceof String) {
                System.setProperty("spazz.github", (String) map.get("github"));
            }
            if (map.get("message-delay") instanceof Integer) {
                messageDelay = (long) map.get("message-delay");
            }
            is.close();
        } catch (Exception e) {
            if (!usersFolder.isDirectory() && !usersFolder.mkdirs()) {
                System.out.println("Could not load users folder. Password for Spazzmatic not found. Cancelling startup...");
                return;
            }
        }

        github = GitHub.connect(System.getProperty("spazz.github"));
        repoManager = new RepositoryManager(github);

        Utilities.loadQuotes();
        reloadSites(debugMode);

        bot.getListenerManager().addListener(spazz);
        /*
		 * Connect to #denizen-dev on start up
		 */
        bot.setVersion("Spazzmatic v0.4 [Morphan1]");
        bot.setName("spazzmatic");
        bot.setLogin("spazz");
        bot.setVerbose(debugMode);
        bot.setAutoNickChange(true);
        bot.setMessageDelay(messageDelay);
        bot.setAutoReconnect(true);
        bot.setAutoReconnectChannels(true);

        identify();

        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                for (Channel chnl : bot.getChannels()) {
                    for (User usr : chnl.getUsers()) {
                        String nick = usr.getNick().toLowerCase();
                        if (dUsers.containsKey(nick) || bot.getName().equalsIgnoreCase(nick) || nick.length() == 0)
                            continue;
                        dUsers.put(nick, new dUser(usr.getNick()));
                    }
                }
                for (String usr : findUserFiles()) {
                    usr = usr.replace(".yml", "");
                    if (dUsers.containsKey(usr)) continue;
                    dUsers.put(usr, new dUser(usr));
                }
            }
        }, 3000);
    }

    public static void sendToAllChannels(String message) {
        for (Channel chnl : bot.getChannels())
            bot.sendMessage(chnl, chatColor + formatChat(message, false)[0]);
    }

    private static String send = "";
    public static void setSend(String send) {
        Spazz.send = send;
    }

    public static void send(String message) {
        send(message, false);
    }

    public static void send(String message, boolean raiseLimit) {
        if (send.equals("spazzmatic"))
            System.out.println("<spazzmatic> " + Colors.removeFormattingAndColors(message));
        else {
            String[] formatted = formatChat(message, raiseLimit);
            String first = address + chatColor + formatted[0];
            if (raiseLimit) {
                if (first.length() > 400) {
                    bot.sendMessage(send, first.substring(0, 400));
                    bot.sendMessage(send, chatColor + first.substring(401, first.length()));
                }
                else {
                    bot.sendMessage(send, first);
                }
                if (formatted.length > 1) {
                    for (int i = 1; i < formatted.length; i++) {
                        String next = formatted[i];
                        if (next.length() > 400) {
                            bot.sendMessage(send, chatColor + next.substring(0, 400));
                            bot.sendMessage(send, chatColor + next.substring(401, first.length()));
                        }
                        else {
                            bot.sendMessage(send, next);
                        }
                    }
                }
            }
            else {
                bot.sendMessage(send, first);
            }
        }
    }

    public static void sendNotice(String destination, String message) {
        if (destination.equals("spazzmatic"))
            System.out.println("-spazzmatic- " + Colors.removeFormattingAndColors(message));
        else
            bot.sendNotice(destination, chatColor + message);
    }

    public static void sendAction(String action) {
        if (send.equals("spazzmatic"))
            System.out.println("* spazzmatic " + Colors.removeFormattingAndColors(action));
        else
            bot.sendAction(send, chatColor + action);
    }

    @Override
    public void onJoin(JoinEvent event) {

        User usr = event.getUser();

        if (!dUsers.containsKey(usr.getNick().toLowerCase()))
            dUsers.put(usr.getNick().toLowerCase(), new dUser(usr.getNick()));

        if (usr.getNick().equals("spazzmatic")) {
            if (devMode) {
                chatChannel = "#denizen-devs";
                bot.sendMessage(chatChannel, chatColor + "Dev mode enabled.");
            }
        }

    }

    @Override
    public void onQuit(QuitEvent event) {
        // ...
    }

    @Override
    public void onPart(PartEvent event) {
        // ...
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        if (shuttingDown) {
            repoManager.shutdown();
            Utilities.saveQuotes();
            queryHandler.saveDefinitions();
            for (dUser usr : dUsers.values()) {
                try {
                    usr.saveAll();
                } catch (Exception e) {
                    if (debugMode) e.printStackTrace();
                    else
                        System.out.println("An error has occured while using saving user " + usr.getNick() + " on disconnect... Turn on debug for more information.");
                }
            }
            System.exit(0);
        }
        else {
            identify();
        }
    }

    @Override
    public void onPing(PingEvent event) {
        // CTCP PING stuff... Not incredibly useful
    }

    @Override
    public void onNickChange(NickChangeEvent event) {
        // ...
    }

    @Override
    public void onAction(ActionEvent event) {
        if (event.getChannel() != null) {
            cacheMessage(new Message(event.getUser().getNick(), event.getAction().replace("<", "<LT>"),true),
                    event.getChannel().getName());
            dUsers.get(event.getUser().getNick().toLowerCase()).setLastSeen("performing an action in "
                    + event.getChannel().getName() + chatColor + ": " + event.getAction());
        }
    }

    @Override
    public void onNotice(NoticeEvent event) {
        if (event.getUser().getNick().equals("NickServ") &&
                event.getMessage().equals("You are now identified for \u0002" + bot.getNick() + "\u0002.")) {
            if (!devMode) {
                bot.joinChannel("#denizen-dev");
            }
            else {
                String op = chatColor;
                chatColor = defaultColor;
                defaultColor = optionalColor;
                optionalColor = op;
            }
            bot.joinChannel("#denizen-devs");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPrivateMessage(PrivateMessageEvent event) {
        onMessage(new MessageEvent(bot, null, event.getUser(), event.getMessage()));
    }

    public static void cacheMessage(Message message, String channel) {
        if (send.equals("spazzmatic")) return;
        if (!cachedMessages.containsKey(channel)) cachedMessages.put(channel, new ArrayList<Message>());
        cachedMessages.get(channel).add(0, message);
    }

    private static String address = "";

    @Override
    public void onMessage(MessageEvent event) {

        Channel chnl = event.getChannel();

        String msg = event.getMessage().trim();
        User usr = event.getUser();

        if (!dUsers.containsKey(usr.getNick().toLowerCase()))
            dUsers.put(usr.getNick().toLowerCase(), new dUser(usr.getNick()));

        dUser dusr = dUsers.get(usr.getNick().toLowerCase());

        if (chnl == null) {
            setSend(usr.getNick());
            chnl = bot.getChannel(chatChannel);
        }
        else if (!chnl.getName().equals(send)) {
            setSend(chnl.getName());
            dusr.setLastSeen("Saying \"" + msg + chatColor + "\" in " + send);
        }
        else {
            dusr.setLastSeen("Saying \"" + msg + chatColor + "\" in " + send);
        }

        final String senderNick = usr.getNick();
        address = "";

        if (charging) {
            if (System.currentTimeMillis() > (chargeInitiateTime + chargeFullTime + chargeFullTime / 2)) {
                sendAction("loses control of his beams, accidentally making pew pew noises towards " + charger.getNick() + "!");
                charging = false;
                chargeInitiateTime = 0;
                charger = null;
                botsnack = 0;
                feeders.clear();
            }

        }

        if (msg.contains("@")) {
            String[] addressTest = msg.split(" ");
            if (addressTest[addressTest.length - 1].contains("@")) {
                address = addressTest[addressTest.length-1].replace("@", "") + ": ";
                msg = msg.replaceAll("\\s+" + addressTest[addressTest.length - 1] + "$", "");
            }
        }

        String msgLwr = msg.toLowerCase();

        Matcher m = issuesPattern.matcher(msgLwr);

        while (m.find()) {
            if (repoManager.hasRepository(m.group(1))) {
                Repository repo = repoManager.getRepository(m.group(1));
                Issue issue = repo.getIssue(Integer.valueOf(m.group(2)));
                if (issue != null) {
                    send("[<O>" + repo.getFullName() + "<C>] <D>" + issue.formatTitle() + "<C> (<D>" + issue.getNumber()
                            + "<C>) by <D>" + issue.getUser().getLogin() + "<C> -- " + issue.getShortUrl());
                }
                else {
                    send("That repository does not have an issue or pull request by that number.");
                }
            }
        }

        m = sReplace.matcher(msg);

        out: if (chnl != null) {
            if (m.find()) {
                boolean user = m.group(3) != null;
                if (cachedMessages.containsKey(chnl.getName())) {
                    for (Message message : cachedMessages.get(chnl.getName())) {
                        if (user && !message.getUser().equals(m.group(3))) continue;
                        if (message.matches("(?i)" + m.group(1))) {
                            send(message.replaceAll("(?i)" + m.group(1), Colors.BOLD + m.group(2) + Colors.NORMAL + "<C>"));
                            break out;
                        }
                    }
                }
            }
            cacheMessage(new Message(senderNick, msg.replace("<", "<LT>"), false), chnl.getName());
        }

        if (msgLwr.startsWith(".hello")) {
            send("Hello World");
            return;
        }
        else if (msgLwr.startsWith(".kitty")) {
            send("Meow.");
            return;
        }
        else if (msgLwr.startsWith(".color")) {

            if (!hasOp(usr, chnl) && !hasVoice(usr, chnl)) {
                send("I'm sorry, but you do not have clearance to alter my photon colorization beam.");
                return;
            }

            String[] args = msg.split(" ");
            if (args.length > 3) {
                send("I cannot read minds... yet. Hit me up with a bot-friendly color.");
                return;
            }
            String tempColor = parseColor(args[1]);
            if (args[1].equalsIgnoreCase("dev")) {
                chatColor = Colors.OLIVE;
                optionalColor = Colors.TEAL;
                defaultColor = Colors.DARK_GREEN;
            }
            else if (args[1].equalsIgnoreCase("normal")) {
                chatColor = Colors.TEAL;
                optionalColor = Colors.DARK_GREEN;
                defaultColor = Colors.OLIVE;
            }
            else if (tempColor == null) {
                send("I eat " + args[1] + " for breakfast. That's not a color.");
                return;
            }

            if (args.length == 3 && tempColor != null) {
                if (args[2].equalsIgnoreCase("default"))
                    defaultColor = tempColor;
                else if (args[2].equalsIgnoreCase("optional"))
                    optionalColor = tempColor;
                else if (args[2].equalsIgnoreCase("chat"))
                    chatColor = tempColor;
            }
            else if (tempColor != null)
                chatColor = tempColor;
            send("Photon colorization beam reconfigured " + "[] " + optionalColor + "() " + defaultColor + "{}");
            return;
        }
        else if (msgLwr.startsWith(".botsnack")) {
            String args[] = msg.split(" ");

            if (feeders.toString().contains(usr.toString())) {
                send("Thanks, but I can't have you controlling too much of my diet.");
                return;
            }

            if (args.length == 2) {
                if (chnl.getUsers().toString().contains(args[1]))
                    send("Gluttony mode activated. Beginning " + args[1] + " consumption sequence.");
                else {
                    ArrayList<User> users = new ArrayList<User>(chnl.getUsers());
                    User random = users.get(Utilities.getRandomNumber(users.size()));
                    send("Oh no! " + args[1] + " not found, nomming " + random.getNick() + " instead.");
                }
                feeders.add(usr);
                botsnack++;
                return;

            }
            else {
                send("OM NOM NOM! I love botsnacks!");
                feeders.add(usr);
                botsnack++;
                return;
            }

        }

        else if (msgLwr.startsWith(".repo")) {
            send("Check out scripts made by other users! - http://bit.ly/14o43eF");
        }
        else if (msgLwr.startsWith(".materials") || msgLwr.startsWith(".mats")) {
            send("Here is the list of all valid bukkit materials - http://bit.ly/X5smJK");
            send("All Denizen 'item:' arguments will accept a bukkit material name. Additionally you can add the data value to the name. (i.e. SANDSTONE:1)");
            return;
        }
        else if (msgLwr.startsWith(".enchantments") || msgLwr.startsWith(".enchants")) {
            send("Here is the list of all valid bukkit enchantments - http://bit.ly/YQ25ud");
            send("They do not follow the same naming conventions as they do in game, so be carefull.");
            return;
        }
        else if (msgLwr.startsWith(".anchors") || msgLwr.startsWith(".anchor")) {
            send("As of 0.8, locations can be referenced from scripts by using anchors linked to NPCs.");
            send("Check out the documentation on the anchor commands in the handbook.");
            return;
        }
        else if (msgLwr.startsWith(".assignments") || msgLwr.startsWith(".assignment") || msgLwr.startsWith(".assign")) {
            send("As of Denizen 0.8, the assignments.yml file is " + Colors.BOLD + "not " + Colors.NORMAL + chatColor + "necessary and the /trait command does " + Colors.BOLD + " not work.");
            send("Instead, create the assignment script alongside interact scripts and assign it with:");
            send(Colors.BOLD + "- /npc assign --set 'assignment_script_name'");
            send("Check out an example of this new script's implementation at " + Colors.BLUE + "http://bit.ly/YiQ0hs");
            return;
        }
        else if (msgLwr.startsWith(".help")) {
            send("Greetings. I am an interactive Denizen guide. I am a scripting guru. I am spazz.");
            send("For help with script commands, type " + Colors.BOLD + "!cmd <command_name>");
            send("For help with script requirements, type " + Colors.BOLD + "!req <requirement_name>");
            send("For info on replaceable tags, type " + Colors.BOLD + "!tag <tag_name>");
            send("For help with world events, type " + Colors.BOLD + "!event <event_name>");
            send("Refer to !help for more information.");
            return;
        }
        else if (msgLwr.startsWith(".paste") || msgLwr.startsWith(".pastie") || msgLwr.startsWith(".hastebin") || msgLwr.startsWith(".pastebin")) {
            send("Need help with a script issue or server error?");
            send("Help us help you by pasting your script " + Colors.BOLD + "and " + Colors.NORMAL + chatColor + "server log to " + Colors.BLUE + "http://mcmonkey.org/paste");
            send("From there, save the page and paste the link back in this channel.");
            return;
        }
        else if (msgLwr.startsWith(".update")) {
            send("Due to the nature of our project, Denizen is always built against the " + Colors.RED + "development" + chatColor + " builds of Craftbukkit and Citizens.");
            send("Most errors can be fixed by updating all 3.");
            send(Colors.BOLD + "Denizen" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/1aaGB3T");

            if (msgLwr.split(" ").length > 1 && msgLwr.split(" ")[1].equals("depenizen"))
                send(Colors.BOLD + "Depenizen" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/1aaGEfY");

            send(Colors.BOLD + "Citizens" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/1aaGEN2");
            send(Colors.BOLD + "Craftbukkit" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/A5I50a");
            return;
        }
        else if (msgLwr.startsWith(".newconfig") || msgLwr.startsWith(".nc")) {
            send("If you are having issues with triggers not firing, you may be using the old config file.");
            send("You can easily generate a new one by deleting your current config.yml file in the Denizen folder");
            return;
        }
        else if (msgLwr.startsWith(".wiki")) {
            send("The Denizen wiki is currently getting a makeover. This means that it doesn't currently have a lot of things.");
            send("Feel free to look at it anyway, though! http://bit.ly/14o3kdq");
            return;
        }
        else if (msgLwr.startsWith(".tags")) {
            send("Here's every replaceable tag in Denizen! - http://bit.ly/164DlSE");
        }

        else if (msgLwr.startsWith(".effects") || msgLwr.startsWith(".potions")) {
            send("A list of Bukkit potion effects is available here " + Colors.BOLD + "- http://bit.ly/13xyXur");
        }
        else if (msgLwr.startsWith(".debug")) {
            debugMode = !debugMode;
            send("Debug mode set to " + defaultColor + debugMode + chatColor + ".");
            bot.setVerbose(debugMode);
        }
        else if (msgLwr.startsWith(".tutorials")) {
            send("Here's a list of video tutorials on how to use Denizen (Thanks " + optionalColor + "Jeebiss and mcmonkey" + chatColor + "!)");
            send(defaultColor + "All videos are viewable here" + chatColor + " - http://mcmonkey.org/denizen/vids");
        }
        else if (msgLwr.startsWith(".shorten")) {
            String[] args = msg.split(" ");
            if (args.length > 1) {
                send(args[1] + " -> " + Utilities.getShortUrl(args[1]));
            }
        }

        else if (msgLwr.startsWith(".msg") || msgLwr.startsWith(".message")) {
            String[] args = msg.trim().split(" ");
            if (args.length < 3) {
                send("Check your argument count. Command format: .msg <user> <message>");
                return;
            }
            msg = msg.replaceFirst(args[0] + " " + args[1] + " ", "");
            if (!dUsers.containsKey(args[1].toLowerCase()))
                send("I've never seen that user '" + args[1] + "'.");
            else {
                dUsers.get(args[1].toLowerCase()).addMessage(new Message(senderNick, msg, false));
                send("Message sent to: " + defaultColor + args[1] + chatColor + ".");
            }
        }

        else if (msgLwr.startsWith(".yaml") || msgLwr.startsWith(".yml")) {

            String[] args = msg.split(" ");
            if (args.length < 2) {
                send("Check your argument count. Command format: .yml <link>");
                return;
            }
            String rawYaml = null;
            try {
                String url[] = args[1].split("/");
                if (args[1].contains("hastebin")) {
                    rawYaml = Utilities.getStringFromUrl("http://hastebin.com/raw/" + url[3]);
                }
                else if (args[1].contains("pastebin")) {
                    rawYaml = Utilities.getStringFromUrl("http://pastebin.com/raw.php?i=" + url[3]);
                }
                else if (args[1].contains("pastie")) {
                    rawYaml = Utilities.getStringFromUrl("http://pastie.org/pastes/" + url[3] + "/download");
                }
                else if (args[1].contains("ult-gaming")) {
                    rawYaml = Utilities.getStringFromUrl("http://paste.ult-gaming.com/" + url[3] + "?raw");
                }
                else if (args[1].contains("citizensnpcs")) {
                    rawYaml = Utilities.getStringFromUrl("http://scripts.citizensnpcs.co/raw/" + url[4]);
                }
                else if (args[1].contains("mcmonkey")) {
                    rawYaml = Utilities.getStringFromUrl("http://mcmonkey.org/paste/" + url[4] + ".txt");
                }
                else {
                    send(address + Colors.RED + "I can't get your script from that website :(");
                }
            } catch (Exception e) {
                if (debugMode) e.printStackTrace();
                else
                    System.out.println("An error has occured while getting script from website... Turn on debug for more information.");
                send("Invalid website format!");
                return;
            }

            Yaml yaml = new Yaml();
            try {
                yaml.load(rawYaml);
                send("Your YAML is valid.");
            } catch (YAMLException e) {
                String fullStack = getCustomStackTrace(e);
                String[] stackList = fullStack.split("\\n");
                int x = 0;
                while (!stackList[x].contains("org.yaml")) {
                    send(address + stackList[x]);
                    x++;
                }
            }
        }

        else if (msgLwr.startsWith(".quote") || msgLwr.startsWith(".q")) {
            String[] args = msg.split(" ");
            int number;
            if (args.length > 1 && Integer.valueOf(args[1]) != null) {
                number = Integer.valueOf(args[1]);
                if (number > (Utilities.getQuoteCount() - 1)) {
                    number = Utilities.getQuoteCount() - 1;
                }
                else if (number < 0)
                    number = 0;
            }
            else
                number = Utilities.getRandomNumber(Utilities.getQuoteCount());
            String spacing = "   ";
            for (int x = 0; x < String.valueOf(number).length(); x++)
                spacing += " ";

            for (Entry<Integer, String> quote : Utilities.getQuote(number).entrySet()) {
                send(chatColor
                        + (quote.getKey() == 0 ? "[" + optionalColor + number + chatColor + "] "
                        : spacing) + quote.getValue());
            }
        }

        else if (msgLwr.startsWith(".party") || msgLwr.startsWith(".celebrate")) {
            if (msgLwr.contains("reason: ")) {
                String[] split = msg.split("reason:", 2);
                String reason = split[1].replace(" me ", senderNick + " ");
                send("Woo! Let's party for " + reason.substring(1, reason.length()) + "!");
                return;
            }
            send("Woo! It's party time! Come on, celebrate with me!");
            return;
        }
        else if (msgLwr.startsWith(".blame")) {
            String[] args = msg.split(" ");
            if (args.length < 3) {
                send("Check your argument count. Command format: .blame <user> <reason>");
                return;
            }
            String blamed = args[1];
            args = msg.split(blamed);
            String reason = args[1];
            send(senderNick + " blames " + blamed + " for" + reason + "!");
        }
        else if (msgLwr.startsWith(".yaii")) {
            send("Your argument is invalid.");
            return;
        }
        else if (msgLwr.startsWith(".thmf")) {
            send("That hurt even my feelings. And I'm a robot.");
        }
        else if (msgLwr.startsWith(".tiafo")) {
            send("Try It And Find Out.");
        }

        else if (msgLwr.startsWith(".tias")) {
            send("Try It And See.");
        }
        else if (msgLwr.startsWith(".cb") || msgLwr.startsWith(".coolbeans")) {
            send("That's cool beans.");
        }
        else if (msgLwr.equals(".sound") || msgLwr.equals(".sounds")) {
            send("Here is the list of all valid bukkit sounds - " + Colors.BLUE + "http://bit.ly/14NYbvi");
        }
        else if (msgLwr.startsWith(".hb") || msgLwr.startsWith(".handbook")) {
            send("Current Documentation - " + Colors.BLUE + "http://bit.ly/XaWBLN");
            send("PDF download (always current) - " + Colors.BLUE + "http://bit.ly/159JBgM");
        }
        else if (msgLwr.startsWith(".getstarted") || msgLwr.startsWith(".gs")) {
            send("So, you're trying to use 0.9 for the first time?");
            send("It's recommended that you read the current documentation.");
            send(Colors.BOLD + "Denizen Handbook " + Colors.NORMAL + chatColor + "- http://bit.ly/XaWBLN");
            send(Colors.BOLD + "Denizen Wiki " + Colors.NORMAL + chatColor + "- http://bit.ly/14o3kdq");
            send(Colors.BOLD + "Beginner's Guide" + Colors.NORMAL + chatColor + "- http://bit.ly/1bHkByR");
            send("Please keep in mind that documentation is a work in progress. You will likely not find everything.");
        }
        else if (msgLwr.startsWith(".fire")) {
            if (hasOp(usr, chnl) || hasVoice(usr, chnl)) {
                String args[] = msg.split(" ");
                if (!charging) {
                    send("Erm... was I supposed to be charging? D:");
                    return;
                }
                if (usr != charger) {
                    send("Sorry, but my firing sequence has already been started by " + charger.getNick() + ".");
                    return;
                }
                if (args.length != 2) {
                    send("I can't just fire into thin air :(");
                    return;
                }
                if (chnl.getUsers().toString().contains(args[1])) {
                    double chance = (Math.random() * 99 + 1) * ((System.currentTimeMillis() - chargeInitiateTime) / chargeFullTime);
                    if (chance > 50) {
                        sendAction("makes pew pew noises towards " + args[1] + "... successfully!");
                        send("Take that " + args[1] + "!");
                    }
                    else {
                        sendAction("makes pew pew noises towards " + args[1] + "... and misses D:");
                        send("You've bested me this time " + args[1] + "...");
                    }
                    charging = false;
                    chargeInitiateTime = 0;
                    charger = null;
                    feeders.clear();
                    return;
                }
                else {
                    send(args[1] + ", really? I need a legitimate target. This is serious business.");
                    return;
                }
            }

            send("Whoa there, you can't touch that button.");
            return;

        }
        else if (msgLwr.startsWith(".lzrbms") || msgLwr.startsWith(".lzrbmz")) {
            if (hasOp(usr, chnl) || hasVoice(usr, chnl)) {
                if (botsnack < 3) {
                    send(": Botsnack levels too low. Can't charge lazers...");
                    return;
                }

                if (charging) {
                    send(": I'm already a bit occupied here!");
                    return;
                }

                chargeInitiateTime = System.currentTimeMillis();
                charging = true;
                charger = usr;
                send("Imma chargin' up meh lazerbeamz...");
                botsnack -= 3;
                return;
            }

            send("Umm, that's not for you.");
            return;

        }
        else if (msg.equalsIgnoreCase(".bye")) {
            if (!hasOp(usr, chnl) && !hasVoice(usr, chnl)) {
                send("Ahaha, you can never kill me, " + senderNick + "!");
            }
            else {
                repoManager.shutdown();
                Utilities.saveQuotes();
                queryHandler.saveDefinitions();
                for (dUser duser : dUsers.values()) {
                    try {
                        duser.saveAll();
                    } catch (Exception e) {
                        if (debugMode) e.printStackTrace();
                        else
                            System.out.println("An error has occured while saving for user " + duser.getNick() + "... Turn on debug for more information.");
                    }
                }
                String[] quotes = {"Ain't nobody got time for that...", "I'm backin' up, backin' up...", "Hide yo kids, hide yo wife..."};
                send(quotes[new Random().nextInt(quotes.length)]);
                bot.quitServer(senderNick + " said so.");
                shuttingDown = true;
                bot.disconnect();
                return;
            }
        }

        else if (msgLwr.startsWith(".seen")) {
            String[] args = msg.split(" ");
            String user = null;
            dUser dusr2 = null;
            if (args.length > 1)
                user = args[1];
            if (!dUsers.containsKey(user.toLowerCase())) {
                send("I've never seen that user: " + defaultColor + user);
            }
            else {
                dusr2 = dUsers.get(user.toLowerCase());
            }
            Calendar now = Calendar.getInstance();
            long currentTime = now.getTimeInMillis();
            Calendar seen = Calendar.getInstance();
            seen.setTime(dusr2.getLastSeenTime());
            long seenTime = seen.getTimeInMillis();
            long seconds = (currentTime - seenTime) / 1000;
            long minutes = seconds / 60;
            seconds = seconds - (minutes * 60);
            long hours = minutes / 60;
            minutes = minutes - (hours * 60);
            long days = hours / 24;
            hours = hours - (days * 24);
            long years = days / 365;
            days = days - (years * 365);
            long centuries = years / 100;
            years = years - (centuries * 100);
            long milleniums = centuries / 10;
            centuries = centuries - (milleniums * 10);
            send("Last I saw of " + defaultColor + dusr2.getNick() + chatColor + " was "
                    + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz)").format(dusr2.getLastSeenTime()).replace("DT)", "ST").replace(")", "")
                    + ", " + dusr2.getLastSeen()
                    + chatColor + ". That " + (seconds < -1 ? "is " : "was ")
                    + ((milleniums > 1 || milleniums < -1) ? (Math.abs(milleniums) + " millenia, ") : ((milleniums == 1 || milleniums == -1) ? "1 millenium, " : ""))
                    + ((centuries > 1 || centuries < -1) ? (Math.abs(centuries) + " centuries, ") : ((centuries == 1 || centuries == -1) ? "1 century, " : ""))
                    + ((years > 1 || years < -1) ? (Math.abs(years) + " years, ") : ((years == 1 || years == -1) ? "1 year, " : ""))
                    + ((days > 1 || days < -1) ? (Math.abs(days) + " days, ") : ((days == 1 || days == -1) ? "1 day, " : ""))
                    + ((hours > 1 || hours < -1) ? (Math.abs(hours) + " hours, ") : ((hours == 1 || hours == -1) ? "1 hour, " : ""))
                    + ((minutes > 1 || minutes < -1) ? (Math.abs(minutes) + " minutes, ") : ((minutes == 1 || minutes == -1) ? "1 minute, " : ""))
                    + ((seconds == 1 || seconds == -1) ? (seconds == -1 ? "1 second from now." : "1 second ago.") : Math.abs(seconds) + (seconds < -1 ? " seconds from now." : " seconds ago.")));
        }
        //else if ((msg.contains("hastebin.") || msg.contains("pastebin.") || msg.contains("pastie.")) && || chnl.hasVoice(usr) || chnl.isOp(usr))) {
        //    help.add(usr);
        //    sendNotice(senderNick, "If you want to whether a Denizen script will compile, type " + Colors.BOLD + ".yml link_to_the_script");
        //    return;
        //}

        else if (msgLwr.startsWith(".rate")) {
            RateLimit.Data rateLimit = github.getRateLimit().getRate();
            sendNotice(senderNick, "Max rate limit: " + rateLimit.getLimit());
            sendNotice(senderNick, "Remaining rate limit: " + rateLimit.getRemaining());

            long now = Calendar.getInstance().getTimeInMillis();
            long seconds = ((rateLimit.getReset() * 1000) - now) / 1000;
            long minutes = seconds / 60;
            seconds = seconds - (minutes * 60);

            sendNotice(senderNick, "Next reset: "
                    + (minutes > 0 ? minutes > 1 ? minutes + " minutes, " : "1 minute, " : "")
                    + (seconds > 0 ? seconds > 1 ? seconds + " seconds" : "1 second" : minutes > 0 ? "0 seconds" : "Now."));
        }

        else if (msgLwr.startsWith(".add")) {

            String[] args = msgLwr.trim().split(" ");
            if (args.length < 2)
                send("That command is written as: .add [<object>]");

            if (args[1].startsWith("r")) {
                if (!hasOp(usr, chnl) && !hasVoice(usr, chnl))
                    send("Sorry, " + senderNick + ", that's only for the Dev Team.");
                else if (args.length > 2 && args[2].contains("/")) {
                    String ownerProject = args[2];
                    boolean i = true;
                    boolean c = true;
                    boolean p = true;
                    double updateDelay = 60;
                    for (String arg : args) {
                        if (arg.startsWith("delay:") && Double.valueOf(arg.split(":")[1]) != null)
                            updateDelay = Double.valueOf(arg.split(":")[1]);
                        else if (arg.equals("no_issues")) i = false;
                        else if (arg.equals("no_comments")) c = false;
                        else if (arg.equals("no_pulls")) p = false;
                    }
                    if (repoManager.addRepository(ownerProject, updateDelay, i, c, p)) {
                        send("I am now tracking " + ownerProject + " with a delay of " + updateDelay
                                + (!i ? !c || !p ? ", no issues" : " and no issues" : "")
                                + (!c ? !p ? ", no comments" : (!i ? "," : "") + " and no comments" : "")
                                + (!p ? (!i || !c ? "," : "") + " and no pulls" : "") + ".");
                        try {
                            repoManager.saveAll();
                        } catch (Exception ignored) {}
                    }
                    else
                        send("Error while adding repository " + args[2] + ": are you sure a repo by that name exists?");
                }
                else
                    send("That command is written as: .add repo [<owner>/<project>] (no_issues) (no_comments) (no_pulls) (delay:<#.#>)");
            }
            else if (args[1].startsWith("q")) {
                if (args.length > 2) {
                    String quoteMsg = msg.substring(args[0].length() + args[1].length() + 2);
                    if (quoteMsg.length() < 5)
                        send("Quote must have at least 5 characters.");
                    else {
                        send("Added quote as #" + Utilities.addQuote(quoteMsg, senderNick) + ".");
                        Utilities.saveQuotes();
                    }
                }
                else
                    send("That command is written as: .add quote [<message>]");
            }
            else if (args[1].startsWith("toq")) {
                if (args.length > 3) {
                    try {
                        int number = Integer.valueOf(args[2]);
                        String quoteMsg = msg.substring(args[0].length() + args[1].length() + args[2].length() + 3);
                        if (quoteMsg.length() < 5)
                            send("Quote must have at least 5 characters.");
                        else {
                            Utilities.addToQuote(number, quoteMsg);
                            send("Added line to quote #" + number);
                            Utilities.saveQuotes();
                        }
                    } catch (Exception e) {
                        send("That command is written as: .add toquote [<#>] [<message>]");
                    }
                }
            }
            else if (args[1].startsWith("d")) {
                if (args.length > 2 && msg.contains(":")) {
                    String key = msg.substring(args[0].length() + args[1].length() + 2, msg.indexOf(':'));
                    String value = msg.substring(args[0].length() + args[1].length() + key.length() + 3);
                    queryHandler.addDefinition(key, value);
                    send("Definition '" + key + "' added as: " + value);
                    queryHandler.saveDefinitions();
                }
                else
                    send("That command is written as: .add definition [<phrase>:<definition>]");
            }
            else
                send("That command is written as: .add [<object>]");
        }

        else if (msgLwr.startsWith(".remove")) {

            String[] args = msgLwr.trim().split(" ");

            if (!hasOp(usr, chnl) && !hasVoice(usr, chnl))
                send("Sorry, " + senderNick + ", that's only for the Dev Team.");
            else if (args[1].startsWith("r")) {
                if (args.length > 2) {
                    if (!repoManager.hasRepository(args[2]))
                        send("I am not tracking any projects by that name. (Did you specify the owner?)");
                    else if (repoManager.removeRepository(args[2]))
                        send("I am now no longer tracking " + args[2] + ".");
                    else
                        send("Error while removing repository " + args[2] + "...");
                    try {
                        repoManager.saveAll();
                    } catch (Exception ignored) {}
                }
                else
                    send("That command is written as: .remove repo [<owner>/<project>]");
            }
            else if (args[1].startsWith("q")) {
                if (args.length > 2) {
                    try {
                        int number = Integer.valueOf(args[2]);
                        if (Utilities.hasQuote(number)) {
                            Utilities.removeQuote(number);
                            send("Quote #" + number + " removed.");
                            Utilities.saveQuotes();
                        }
                        else
                            send("That quote doesn't exist.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        send("That command is written as: .remove quote [<#>]");
                    }
                }
                else
                    send("That command is written as: .remove quote [<#>]");
            }
            else if (args[1].startsWith("d")) {
                if (args.length > 2) {
                    String key = msg.substring(args[0].length() + args[1].length() + 2);
                    if (queryHandler.removeDefinition(key)) {
                        send("Definition '" + key + "' has been removed.");
                        queryHandler.saveDefinitions();
                    }
                    else
                        send("Definition '" + key + "' does not exist.");
                }
                else
                    send("That command is written as: .remove definition [<phrase>]");
            }
            else
                send("That command is written as: .remove [<object>]");
        }

        else if (msgLwr.startsWith(".info")) {
            String[] args = msgLwr.trim().split(" ");

            if (args[1].startsWith("r")) {
                if (args.length > 2) {
                    if (!repoManager.hasRepository(args[2]))
                        send("I am not tracking any projects by that name. (Did you specify the owner?)");
                    else {
                        Repository repo = repoManager.getRepository(args[2]);
                        send(repo.getFullName() + ": Delay("
                                + repo.getUpdateDelay() + ") Issues(" + repo.hasIssues() + ") Comments("
                                + repo.hasComments() + ") Pulls(" + repo.hasPulls() + ")");
                    }
                }
                else
                    send("That command is written as: .info repo [<owner>/<project>]");
            }
            else if (args[1].startsWith("u")) {
                if (args.length > 2) {
                    if (dUsers.containsKey(args[2])) {
                        dUser dusr2 = dUsers.get(args[2]);
                        send(args[2] + ": LastSeen(" + dusr2.getLastSeen() + ")");
                    }
                }
            }
            else if (args[1].startsWith("q")) {
                if (args.length > 2) {
                    try {
                        int number = Integer.valueOf(args[2]);
                        String infoText = "";
                        for (Entry<String, String> info : Utilities.getQuoteInfo(number).entrySet()) {
                            infoText += " " + Utilities.capitalize(info.getKey()) + "(" + info.getValue() + ")";
                        }
                        for (Entry<Integer, String> quote : Utilities.getQuote(number).entrySet()) {
                            infoText += " Quote#" + quote.getKey() + "(" + quote.getValue() + ")";
                        }
                        send(number + ":" + infoText);
                    } catch (Exception e) {
                        send("That command is written as: .info quote [<#>]");
                    }
                }
            }
            else
                send("That command is written as: .info [<object>]");
        }

        else if (msgLwr.startsWith(".list") || msgLwr.startsWith(".count")) {
            String[] args = msgLwr.trim().split(" ");
            if (args.length < 2)
                send("That command is written as: .list [<object>]");

            if (args[1].startsWith("r")) {
                Set<String> repos = repoManager.getRepositories();
                send("I'm currently watching " + repos.size() + " repositories...");
                send(repos.toString());
            }
            else if (args[1].startsWith("q")) {
                send("I currently have " + Utilities.getQuoteCount() + " quotes listed.");
            }
            else
                send("That command is written as: .list [<object>]");
        }

        else if (msgLwr.startsWith(".save")) {
            String[] args = msgLwr.trim().split(" ");
            if (args.length < 2)
                send("That command is written as: .save [<object>]");

            if (args[1].startsWith("r")) {
                try {
                    repoManager.saveAll();
                    send("Successfully saved all repository information.");
                } catch (Exception e) {
                    send("Error while saving repository information... Report this to " + Colors.CYAN + "Morphan1");
                }
            }
            else if (args[1].startsWith("q")) {
                Utilities.saveQuotes();
                send("Successfully saved all quotes.");
            }
            else if (args[1].startsWith("u")) {
                for (dUser dusr2 : dUsers.values()) {
                    if (debugMode) System.out.println("Saving dUser: " + dusr2.getNick() + "...");
                    try {
                        dusr2.saveAll();
                    } catch (Exception e) {
                        send(Colors.RED + "ERROR. Failed to save user information: " + defaultColor + dusr2.getNick()
                                + "... Report this to " + Colors.CYAN + "Morphan1");
                        if (debugMode) e.printStackTrace();
                        else
                            System.out.println("An error has occured while using .save-all for user " + dusr2.getNick()
                                    + "... Turn on debug for more information.");
                        return;
                    }
                }
                send("Successfully saved all user information.");
            }
            else if (args[1].startsWith("d")) {
                if (queryHandler.saveDefinitions())
                    send("Successfully saved all definitions.");
                else
                    send("Error while saving definitions... Report this to " + Colors.CYAN + "Morphan1");
            }
        }

        else if (msgLwr.startsWith(".load")) {
            String[] args = msgLwr.trim().split(" ");
            if (args.length < 2)
                send("That command is written as: .load [<object>]");

            if (args[1].startsWith("r")) {
                try {
                    repoManager.loadAll();
                    send("Successfully loaded all repository information.");
                } catch (Exception e) {
                    send("Error while loading repository information... Report this to " + Colors.CYAN + "Morphan1");
                }
            }
            else if (args[1].startsWith("q")) {
                Utilities.loadQuotes();
                send("Successfully loaded all quotes.");
            }
            else if (args[1].startsWith("u")) {
                for (dUser dusr2 : dUsers.values()) {
                    if (debugMode) System.out.println("Loading dUser information: " + dusr2.getNick() + "...");
                    try {
                        dusr2.loadAll();
                    } catch (Exception e) {
                        send(Colors.RED + "ERROR. Failed to load user information: " + defaultColor + dusr2.getNick()
                                + "... Report this to " + Colors.CYAN + "Morphan1");
                        if (debugMode) e.printStackTrace();
                        else
                            System.out.println("An error has occured while using .load for user " + dusr2.getNick() + "... Turn on debug for more information.");
                        return;
                    }
                }
                send("Successfully loaded all user information.");
            }
            else if (args[1].startsWith("d")) {
                if (queryHandler.loadDefinitions())
                    send("Successfully loaded all definitions.");
                else
                    send(Colors.RED + "Error while loading definitions... Report this to " + Colors.CYAN + "Morphan1");
            }
        }

        else if (msgLwr.startsWith(".edit")) {
            if (!hasOp(usr, chnl) && !hasVoice(usr, chnl))
                send("Sorry, " + senderNick + ", that's only for the Dev Team.");
            else {
                String[] args = msgLwr.trim().split(" ");
                if (args.length < 2)
                    send("That command is written as: .edit [<object>](:<key>) [<value>]");

                if (args[1].startsWith("m") || args[1].startsWith("d")) {
                    try {
                        if (args.length < 3 || Double.valueOf(args[2]) == null) throw new Exception();
                        messageDelay = Double.valueOf(args[2]).longValue() * 1000;
                        bot.setMessageDelay(messageDelay);
                        send("Message delay set to " + args[2] + "s.");
                    } catch (Exception e) {
                        send("That command is written as: .edit message-delay [<#.#>]");
                    }
                }
                else if (args[1].startsWith("r")) {
                    if (args.length > 3) {
                        if (!repoManager.hasRepository(args[2]))
                            send("I am not tracking any projects by that name. (Did you specify the owner?)");
                        else {
                            try {
                                Repository repo = repoManager.getRepository(args[2]);
                                double delay = repo.getUpdateDelay();
                                boolean issues = repo.hasIssues();
                                boolean comments = repo.hasComments();
                                boolean pulls = repo.hasPulls();
                                for (int i = 3; i < args.length; i++) {
                                    String[] arg = args[i].split(":");
                                    if (arg[0].equalsIgnoreCase("delay"))
                                        delay = Double.parseDouble(arg[1]);
                                    else if (arg[0].equalsIgnoreCase("issues"))
                                        issues = Boolean.parseBoolean(arg[1]);
                                    else if (arg[0].equalsIgnoreCase("comments"))
                                        comments = Boolean.parseBoolean(arg[1]);
                                    else if (arg[0].equalsIgnoreCase("pulls"))
                                        pulls = Boolean.parseBoolean(arg[1]);
                                }
                                repo.setStuff((long) delay * 1000, issues, comments, pulls);
                                send("Done.");
                            } catch (Exception e) {
                                send("That command is written as: .edit repo [<owner>/<project>] (delay:<#.#>) (issues:true/false) (comments:true/false) (pulls:true/false)");
                            }
                        }
                    }
                    else
                        send("That command is written as: .edit repo [<owner>/<project>] (delay:<#.#>) (issues:true/false) (comments:true/false) (pulls:true/false)");
                }
            }
        }

        else if (msgLwr.startsWith(".math ")) {
            try {
                Double eval = new DoubleEvaluator().evaluate(msgLwr.substring(6));
                send(defaultColor + "<math:" + msgLwr.substring(6).replace(" ", "") + ">" + chatColor + " = " + eval);
            } catch (Exception e) {
                send("Invalid math statement. Denizen will not parse that correctly.");
            }
        }

        else if (msgLwr.matches("\\.(realmath|define|wolfram|parse) .+")) {
            String input = msg.substring(msg.split("\\s+")[0].length() + 1);
            QueryResult output = queryHandler.parse(input, Utilities.getIPAddress(usr.getHostmask()));
            String result = output.getResult();

            if (output.isError() || !output.isSuccess() || result == null) {
                if (output.hasSuggestion())
                    send("Sorry, I don't know the definition of that. Did you mean '" + output.getSuggestion() + "'?");
                else
                    send("There was an error while parsing that statement.");
            }
            else {
                if (output.hasSpellCheck()) send(output.getSpellCheck());
                send(output.getInput() + " = " + result);
            }
        }

        else if (msgLwr.startsWith(".urban")) {
            String input = msg.substring(6).replaceAll("\\s+", " ").trim();
            Response response = UrbanDictionary.getDefinition(input);
            if (response.getResultType() == Response.Result.NONE) {
                send("No definition found.");
            }
            else {
                List<Definition> definitions = response.getDefinitions();
                Definition definition = definitions.get(Utilities.getRandomNumber(definitions.size()));
                send(definition.getWord() + ": " + definition.getDefinition());
                send("Example: " + definition.getExample());
            }
        }

        else if (msgLwr.startsWith(".mcping")) {
            String[] args = msgLwr.split(" ");
            if (args.length < 2) {
                send("Invalid server specified.");
                return;
            }
            if (!args[1].contains(".") && !args[1].toLowerCase().contains("localhost")) {
                if (dUsers.containsKey(args[1].toLowerCase())) {
                    String s = dUsers.get(args[1].toLowerCase()).getServerAddress();
                    if (s.equals("")) {
                        send("Sorry, I don't know that user's server IP.");
                        return;
                    }
                    else args[1] = s;
                }
                else {
                    send("Invalid server specified.");
                    return;
                }
            }
            int port = 25565;
            if (args[1].contains(":")) {
                try {
                    port = Integer.valueOf(args[1].split(":")[1].replace("/", ""));
                } catch (Exception ignored) {}
            }
            MinecraftServer server = new MinecraftServer(args[1].contains(":") ? args[1].split(":")[0] : args[1], port);
            if (server.getAddress().isUnresolved())
                send("Invalid server specified.");
            else try {
                MinecraftServer.StatusResponse response = server.ping();
                MinecraftServer.Players players = response.getPlayers();
                InetSocketAddress address = server.getAddress();
                String serverInfo = address.getHostName() + ":" + address.getPort() + " - " + response.getDescription()
                        + (char) 0xa7 + "r" + chatColor + " - " + response.getVersion().getName() + " - "
                        + players.getOnline() + "/" + players.getMax();
                m = minecraftRandom.matcher(serverInfo);
                while (m.find()) {
                    serverInfo = serverInfo.replaceFirst(m.group(1), Utilities.getRandomString(m.group(2)));
                }
                m = minecraftColor.matcher(serverInfo);
                String lastColor = "";
                while (m.find()) {
                    String color = Colors.NORMAL + (m.group(1).toLowerCase().matches("o") ? lastColor
                            : m.group(1).toLowerCase().matches("n|l") ? lastColor + parseColor("&" + m.group(1))
                            : parseColor("&" + m.group(1)));
                    serverInfo = serverInfo.replaceFirst((char) 0xa7 + m.group(1), lastColor = color);
                }
                send(serverInfo.replace(String.valueOf((char) 0xc2), "").replaceAll("\\s+|\r|\n", " "), true);
            } catch (Exception e) {
                send("Error contacting that server. (" + e.getMessage() + ")");
            }
        }

        else if (msgLwr.equals(".dev")) {
            if (hasOp(usr, bot.getChannel("#denizen-devs")) || hasVoice(usr, bot.getChannel("#denizen-devs"))) {
                if (!devMode) {
                    devMode = true;
                    send("Entering dev mode...");
                    bot.partChannel(bot.getChannel("#denizen-dev"), "Dev mode enabled.");
                    String op = chatColor;
                    chatColor = defaultColor;
                    defaultColor = optionalColor;
                    optionalColor = op;
                    chatChannel = "#denizen-devs";
                    bot.sendMessage(chatChannel, chatColor + "Dev mode enabled.");
                }
                else {
                    devMode = false;
                    bot.sendMessage(chatChannel, chatColor + "Exiting dev mode...");
                    bot.joinChannel("#denizen-dev");
                    String de = defaultColor;
                    chatColor = optionalColor;
                    optionalColor = defaultColor;
                    defaultColor = de;
                    bot.sendMessage(chatChannel, chatColor + "Dev mode disabled.");

                }
            }
            else {
                send("Sorry, " + usr.getNick() + ", you're not allowed to do that.");
            }
        }

        else if (msgLwr.equals(".wumbo")) {
            wumbo = !wumbo == false ? !!!!!wumbo : !wumbo == true ? true : !!false;
            send("Wumbo mode " + (!wumbo ? "de" : "") + "activated.");
        }

        else if (msgLwr.startsWith(".myip")) {
            String[] args = msg.split(" ");
            if (args.length < 2 || args[1].isEmpty()) {
                send("That command is used like: .myip <your_server>");
                return;
            }
            dusr.setServerAddress(args[1]);
            send("Your server has been set, and can now be accessed by other users via '.mcping " + dusr.getNick() + "'");
        }

        address = "";
        dusr.checkMessages();

    }

    private static void reloadSites(boolean debug) {
        boolean original = debugMode;
        if (!debugMode)
            debugMode = debug;
        debugMode = original;
    }

    private static ArrayList<String> findUserFiles() {
        return Utilities.findFileNamesByExtension(usersFolder.getPath(), ".yml");
    }

    private boolean hasVoice(User chatter, Channel channel) {
        return channel.hasVoice(chatter);
    }

    private boolean hasOp(User chatter, Channel channel) {
        return channel.isOp(chatter);
    }

    private static String[] formatChat(String message, boolean newLines) {
        message = message.replace("<C>", chatColor).replace("<D>", defaultColor)
                .replace("<O>", optionalColor).replace("<LT>", "<").replace("\r", "")
                .replace("Citizens", (char) 0x10A + "itizens")
                .replace("fullwall", "f" + (char) 0x1B0 + "llwall");
        if (newLines)
            return message.split("\n");
        else
            return new String[]{message.replace("\n", " - ")};
    }

    private static void identify() {
        try {
            bot.connect("irc.esper.net");
        } catch (Exception e) {
            System.out.println("Failed to connect to EsperNet. Check your internet connection and try again.");
            return;
        }

        bot.identify(System.getProperty("spazz.password"));

        System.out.println("Successfully loaded Spazzmatic. You may now begin using console commands.");
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("/me <action>             Performs an action in the current chat channel.");
        System.out.println("/msg <user> <msg>        Sends a private message to a user.");
        System.out.println("/plain <msg>             Sends a non-colored message to the current chat channel.");
        System.out.println("/join <channel>          Joins a channel.");
        System.out.println("/leave <channel> <msg>   Leaves a channel with an optional message.");
        System.out.println("/raw <protocol>          Sends a raw IRC protocol to the server.");
        System.out.println("/disconnect              Saves user info and disconnects from the server.");
        System.out.println("/chat <channel>          Set the current chat channel.");
        System.out.println("/test <command>          Test Spazzmatic IRC commands (Ex: /test .botsnack)");
        System.out.println("/debug                   Turns console debug on or off.");
        System.out.println("<msg>                    Sends a message to the current chat channel.");
        System.out.println();

        Scanner scanner = new Scanner(System.in);
        String inputCommand = "";
        String commandArgs = "";
        String channel = "";

        input: while (!shuttingDown && scanner.hasNext()) {

            String rawInput = scanner.nextLine();
            String[] inputArgs = rawInput.split(" ");

            if (rawInput.startsWith("/")) {
                inputCommand = inputArgs[0].substring(1).toLowerCase();
                if (inputArgs.length > 1) {
                    channel = inputArgs[1];
                    if (inputArgs.length > 2) {
                        commandArgs = rawInput.substring(inputCommand.length() + inputArgs[1].length() + 3);
                    }
                }
            }

            switch (inputCommand) {

                case "chat":
                    if (bot.channelExists(channel))
                        chatChannel = channel;
                    else
                        System.out.println("Not connected to channel \"" + chatChannel + "\".");
                    break;

                case "debug":
                    debugMode = !debugMode;
                    System.out.println("Debug mode set to " + debugMode + ".");
                    bot.setVerbose(debugMode);
                    break;

                case "disconnect":
                    if (repoManager != null)
                        repoManager.shutdown();
                    Utilities.saveQuotes();
                    queryHandler.saveDefinitions();
                    bot.quitServer("Command sent by console: /disconnect");
                    System.out.println();
                    System.out.println();
                    System.out.println("Disconnected.");
                    shuttingDown = true;
                    break;

                case "reconnect":
                    try {
                        bot.connect("irc.esper.net");
                    } catch (Exception e) {
                        System.out.println("Failed to connect to EsperNet. Check your internet connection and try again.");
                        return;
                    }
                    shuttingDown = false;
                    identify();
                    break input;

                case "join":
                    if (bot.channelExists(channel))
                        System.out.println("Already in channel \"" + channel + "\".");
                    else
                        bot.joinChannel(channel);
                    break;

                case "leave":
                    if (bot.channelExists(channel))
                        bot.partChannel(bot.getChannel(channel), commandArgs);
                    else
                        System.out.println("Not connected to channel \"" + channel + "\".");
                    break;

                case "me":
                    if (bot.channelExists(chatChannel))
                        bot.sendAction(chatChannel, chatColor + channel + " " + commandArgs);
                    else
                        System.out.println("Not connected to channel \"" + chatChannel + "\".");
                    break;

                case "msg":
                    if (dUsers.containsKey(channel.toLowerCase()))
                        bot.sendMessage(channel, chatColor + commandArgs);
                    else if (bot.channelExists(channel))
                        System.out.println("You can't use /msg for channels! Instead, use \"<channel> <message>\"!");
                    else
                        System.out.println("That user doesn't exist \"" + channel + "\".");
                    break;

                case "plain":
                    if (bot.channelExists(chatChannel))
                        bot.sendMessage(chatChannel, channel + " " + commandArgs);
                    else
                        System.out.println("Not connected to channel \"" + chatChannel + "\".");
                    break;

                case "raw":
                    bot.sendRawLineNow(channel.toUpperCase() + " " + commandArgs);
                    break;

                case "test":
                    spazz.onMessage(new MessageEvent(bot, null, bot.getUserBot(), channel + " " + commandArgs));
                    break;

                default:
                    if (rawInput.startsWith("/")) {
                        System.out.println("Invalid command: " + inputCommand);
                        break;
                    }
                    if (bot.channelExists(chatChannel)) {
                        String prev = send;
                        send = chatChannel;
                        send(chatColor + rawInput);
                        send = prev;
                    }
                    else
                        System.out.println("Not connected to channel \"" + chatChannel + "\".");
                    break;

            }
            inputCommand = "";
            commandArgs = "";
            channel = "";

        }

        scanner.close();
    }

    private static String parseUsage(String unparsed) {

        // Keeping debugging code for use with future commands

        String formatted = unparsed;
        String beforeColor = chatColor;

        formatted = formatted.replace("\\", "");
        formatted = formatted.replace("[", chatColor + "[");
        formatted = formatted.replace("(", optionalColor + "(");
        formatted = formatted.replace("{", defaultColor + "{");
        formatted = formatted.replace("]", chatColor + "]");
        formatted = formatted.replace(")", optionalColor + ")");
        formatted = formatted.replace("}", defaultColor + "}");
        int requiredIndex = formatted.indexOf("]");
        //if (debugMode) System.out.println("Beginning " +formatted);

        while (requiredIndex != -1) {

            int lastRequired = formatted.indexOf("]", requiredIndex + 1);
            int lastOptional = formatted.indexOf(")", requiredIndex);
            int lastDefault = formatted.indexOf("}", requiredIndex);
            int lastSpace = formatted.indexOf(" ", requiredIndex);

            if (lastSpace == -1 || lastSpace > lastDefault || lastSpace > lastOptional || lastSpace > lastRequired) {
                if (lastRequired != -1 && (lastSpace > lastRequired || lastSpace == -1)) {
                    if ((lastDefault == -1 || lastOptional == -1) || lastRequired < lastDefault && lastRequired < lastOptional)
                        ;
                    beforeColor = chatColor;
                }
                else if (lastOptional != -1 && (lastSpace > lastOptional || lastSpace == -1)) {
                    if (lastDefault == -1 || lastOptional < lastDefault) ;
                    beforeColor = optionalColor;
                }

                else {
                    if (lastDefault != -1 && (lastSpace > lastDefault || lastSpace == -1))
                        beforeColor = defaultColor;
                }
            }
            //if (debugMode) System.out.println("Required Loop before change " +formatted);
            //if (debugMode) System.out.println("First half " + formatted.substring(0, requiredIndex + 1));
            //if (debugMode) System.out.println("Second half " + formatted.substring(requiredIndex + 1));

            formatted = formatted.substring(0, requiredIndex + 1) + beforeColor + formatted.substring(requiredIndex + 1);
            requiredIndex = formatted.indexOf("]", requiredIndex + 1);
            //if (debugMode) System.out.println("Required Loop after change " +formatted);

        }
        //if (debugMode) System.out.println("After Req Loop " + formatted);

        int optionalIndex = formatted.indexOf(")");

        while (optionalIndex != -1) {

            int lastOptional = formatted.indexOf(")", optionalIndex + 1);
            int lastRequired = formatted.indexOf("]", optionalIndex);
            int lastDefault = formatted.indexOf("}", optionalIndex);
            int lastSpace = formatted.indexOf(" ", optionalIndex);

            if (lastSpace == -1 || lastSpace > lastDefault || lastSpace > lastRequired || lastSpace > lastOptional) {
                if (lastOptional != -1 && (lastSpace > lastOptional || lastSpace == -1)) {
                    if ((lastDefault == -1 && lastRequired == -1) || (lastOptional < lastDefault && lastOptional < lastRequired))
                        beforeColor = optionalColor;
                }
                else if (lastRequired != -1 && (lastSpace > lastRequired || lastSpace == -1)) {
                    if (lastDefault == -1 || lastRequired < lastDefault) ;
                    beforeColor = chatColor;
                }

                else {
                    if (lastDefault != -1 && (lastSpace > lastDefault || lastSpace == -1))
                        beforeColor = defaultColor;
                }
            }
            //if (debugMode) System.out.println("Optional Loop before change " + formatted);

            formatted = formatted.substring(0, optionalIndex + 1) + beforeColor + formatted.substring(optionalIndex + 1);
            optionalIndex = formatted.indexOf(")", optionalIndex + 1);
            //if (debugMode) System.out.println("Optional Loop after change " + formatted);

        }
        //if (debugMode) System.out.println("After OL " + formatted);

        int defaultIndex = formatted.indexOf("}");

        while (defaultIndex != -1) {

            int lastRequired = formatted.indexOf("]", defaultIndex);
            int lastOptional = formatted.indexOf(")", defaultIndex);
            int lastSpace = formatted.indexOf(" ", defaultIndex);

            if (lastSpace == -1 || lastSpace > lastOptional || lastSpace > lastRequired) {
                if (lastRequired != -1 && (lastSpace > lastRequired || lastSpace == -1)) {
                    if (lastOptional == -1 || lastRequired < lastOptional) ;
                    beforeColor = chatColor;
                }

                else {
                    if (lastOptional != -1 && (lastSpace > lastOptional || lastSpace == -1))
                        beforeColor = optionalColor;
                }
            }
            //if (debugMode) System.out.println("DL before change " + formatted);

            formatted = formatted.substring(0, defaultIndex + 1) + beforeColor + formatted.substring(defaultIndex + 1);
            defaultIndex = formatted.indexOf("}", defaultIndex + 1);
            //if (debugMode) System.out.println("DL after change " + formatted);

        }


        //if (debugMode) System.out.println("Final " + formatted);
        return formatted;
    }

    private String parseColor(String colorName) {

        if (colorName.contains("&")) {
            String symbol = colorName.substring(1, colorName.length()).toLowerCase();
            switch (symbol) {
                case "0":
                    return Colors.BLACK;
                case "1":
                    return Colors.DARK_BLUE;
                case "2":
                    return Colors.DARK_GREEN;
                case "3":
                    return Colors.TEAL;
                case "4":
                    return Colors.RED;
                case "5":
                    return Colors.PURPLE;
                case "6":
                    return Colors.OLIVE;
                case "7":
                    return Colors.LIGHT_GRAY;
                case "8":
                    return Colors.DARK_GRAY;
                case "9":
                    return Colors.BLUE;
                case "a":
                    return Colors.GREEN;
                case "b":
                    return Colors.CYAN;
                case "c":
                    return Colors.RED;
                case "d":
                    return Colors.MAGENTA;
                case "e":
                    return Colors.YELLOW;
                case "f":
                    return Colors.WHITE;
                case "l":
                    return Colors.BOLD;
                case "n":
                    return Colors.UNDERLINE;
                case "r":
                    return Colors.NORMAL;
                default:
                    return null;
            }
        }

        if (colorName.equalsIgnoreCase("black"))
            return Colors.BLACK;
        if (colorName.equalsIgnoreCase("blue") || colorName.equalsIgnoreCase("lightblue"))
            return Colors.BLUE;
        if (colorName.equalsIgnoreCase("brown") || colorName.equalsIgnoreCase("poop"))
            return Colors.BROWN;
        if (colorName.equalsIgnoreCase("cyan"))
            return Colors.CYAN;
        if (colorName.equalsIgnoreCase("darkblue"))
            return Colors.DARK_BLUE;
        if (colorName.equalsIgnoreCase("darkgray"))
            return Colors.DARK_GRAY;
        if (colorName.equalsIgnoreCase("green") || colorName.equalsIgnoreCase("darkgreen"))
            return Colors.DARK_GREEN;
        if (colorName.equalsIgnoreCase("lime") || colorName.equalsIgnoreCase("brightgreen") || colorName.equalsIgnoreCase("lightgreen") || colorName.equalsIgnoreCase("&a"))
            return Colors.GREEN;
        if (colorName.equalsIgnoreCase("lightgray"))
            return Colors.LIGHT_GRAY;
        if (colorName.equalsIgnoreCase("magenta"))
            return Colors.MAGENTA;
        if (colorName.equalsIgnoreCase("olive") || colorName.equalsIgnoreCase("orange"))
            return Colors.OLIVE;
        if (colorName.equalsIgnoreCase("purple"))
            return Colors.PURPLE;
        if (colorName.equalsIgnoreCase("red") || colorName.equalsIgnoreCase("warning"))
            return Colors.RED;
        if (colorName.equalsIgnoreCase("reverse") || colorName.equalsIgnoreCase("contrast"))
            return Colors.REVERSE;
        if (colorName.equalsIgnoreCase("teal"))
            return Colors.TEAL;
        if (colorName.equalsIgnoreCase("white"))
            return Colors.WHITE;
        if (colorName.equalsIgnoreCase("yellow"))
            return Colors.YELLOW;

        return null;
    }

    public static String getCustomStackTrace(Throwable aThrowable) {
        //add the class name and any message passed to constructor
        final StringBuilder result = new StringBuilder();
        result.append(aThrowable.toString());
        final String NEW_LINE = System.getProperty("line.separator");
        result.append(NEW_LINE);

        //add each element of the stack trace
        for (StackTraceElement element : aThrowable.getStackTrace()) {
            result.append(element);
            result.append(NEW_LINE);
        }
        return result.toString();
    }

    public static class dUser {

        private String lastSeen;
        private Date lastSeenTime;
        private String nick;
        private MessageList messages;
        private String serverAddress;

        private File userFile = null;

        public dUser(String nick) {
            if (nick == null || nick.equals("")) return;
            this.nick = nick;
            this.messages = new MessageList();
            setLastSeen("Existing");
            serverAddress = "";
            userFile = new File(System.getProperty("user.dir") + "/users/" + nick.toLowerCase().replace('|', '_') + ".yml");
            if (!userFile.exists() || userFile.isDirectory()) {
                try {
                    if (userFile.isDirectory())
                        userFile.delete();
                    userFile.createNewFile();
                    saveAll();
                } catch (Exception e) {
                    if (debugMode) e.printStackTrace();
                    else
                        System.out.println("An error has occured while saving new user " + getNick() + "... Turn on debug for more information.");
                }
            }
            else {
                try {
                    loadAll();
                } catch (Exception e) {
                    if (debugMode) e.printStackTrace();
                    else
                        System.out.println("An error has occured while loading existing user " + getNick() + "... Turn on debug for more information.");
                }
            }

        }

        void setServerAddress(String serverAddress) {
            this.serverAddress = serverAddress;
        }

        void setLastSeen(String lastSeen) {
            this.lastSeenTime = Calendar.getInstance().getTime();
            this.lastSeen = Utilities.uncapitalize(lastSeen);
        }

        void setLastSeenRaw(String lastSeen) {
            this.lastSeen = lastSeen;
        }

        void addMessage(Message msg) {
            this.messages.add(msg);
        }

        void saveAll() throws Exception {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("name", getNick());
            data.put("lastseen", getLastSeen().getBytes());
            Calendar seenTime = Calendar.getInstance();
            seenTime.setTime(getLastSeenTime());
            data.put("lasttime", new SimpleDateFormat((seenTime.getTimeInMillis() < 0 ? "-yyyy" : "yyyy") + "-MM-dd HH:mm:ss.SSS zzz").format(getLastSeenTime()));
            data.put("messages", getMessages().getMessages());
            data.put("server_address", getServerAddress());
            if (getNick().equals("spazzmatic")) {
                data.put("password", System.getProperty("spazz.password"));
                data.put("bitly", System.getProperty("spazz.bitly"));
                data.put("bitly-backup", System.getProperty("spazz.bitly-backup"));
                data.put("dev-mode", devMode);
                data.put("wolfram", queryHandler.getKey());
                data.put("github", System.getProperty("spazz.github"));
                data.put("message-delay", messageDelay);
            }

            FileWriter writer = new FileWriter(userFile);
            writer.write(yaml.dump(data));
            writer.close();
        }

        void loadAll() throws Exception {
            Yaml yaml = new Yaml();
            InputStream is = userFile.toURI().toURL().openStream();
            LinkedHashMap map = (LinkedHashMap) yaml.load(is);

            // loadLastSeen()
            if (map.get("lastseen") instanceof byte[]) {
                this.lastSeen = new String((byte[]) map.get("lastseen"));
                this.lastSeenTime = dateFormat.parse((String) map.get("lasttime"));
            }
            // Keep string checker so we can edit a YAML file from a program and have Spazz translate it later
            else if (map.get("lastseen") instanceof String) {
                this.lastSeen = ((String) map.get("lastseen")).replace("`````", "#").replace("````", "|");
                this.lastSeenTime = dateFormat.parse((String) map.get("lasttime"));
            }
            else
                setLastSeen("Existing");

            // loadMessages()
            if (map.get("messages") instanceof HashMap<?, ?>) {
                for (Object msgObj : ((HashMap<Integer, Object>) map.get("messages")).values()) {
                    String msg;
                    if (msgObj instanceof byte[])
                        msg = new String((byte[]) msgObj);
                    else
                        msg = (String) msgObj;
                    String[] split = msg.split("_", 2);
                    addMessage(new Message(split[0], split[1], false));
                }
            }

            // loadServerAddress()
            if (map.get("server_address") instanceof String) {
                this.serverAddress = ((String) map.get("server_address"));
            }

            // Close the InputStream
            is.close();
        }

        public String getLastSeen() {
            return this.lastSeen;
        }

        public Date getLastSeenTime() {
            return this.lastSeenTime;
        }

        public String getNick() {
            return this.nick;
        }

        public String getServerAddress() { return this.serverAddress; }

        public void checkMessages() {
            if (this.messages.isEmpty()) return;
            send(Colors.NORMAL + getNick() + ": " + chatColor + "You have messages waiting for you...");
            String lastNick = null;
            for (String msg : this.messages.getMessages().values()) {
                String[] split = msg.split("_", 2);
                if (!split[0].equals(lastNick)) {
                    lastNick = split[0];
                    sendNotice(getNick(), defaultColor + lastNick + chatColor + ":");
                }
                sendNotice(getNick(), "  " + split[1]);
            }
            this.messages.clear();
            try {
                saveAll();
            } catch (Exception e) {
                if (debugMode) e.printStackTrace();
                else
                    System.out.println("An error has occured while using checkMessages() for user " + getNick() + "... Turn on debug for more information.");
            }
        }

        public MessageList getMessages() {
            if (this.messages == null) {
                try {
                    loadAll();
                } catch (Exception e) {
                    if (debugMode) e.printStackTrace();
                    else
                        System.out.print("An error has occured while using getMessages() for user " + getNick() + "... Turn on debug for more information.");
                }
            }
            return this.messages;
        }

    }

    public static class MessageList {

        HashMap<Integer, String> messages;

        public MessageList(ArrayList<Message> messages) {
            this.messages = new HashMap<Integer, String>();
            for (Message message : messages) {
                this.messages.put(messages.size(), message.getUser() + "_" + message.getMessage());
            }
        }

        public MessageList() {
            this.messages = new HashMap<Integer, String>();
        }

        public boolean isEmpty() {
            return this.messages.isEmpty();
        }

        public void clear() {
            this.messages.clear();
        }

        public HashMap<Integer, String> getMessages() {
            return messages;
        }

        public MessageList add(Message msg) {
            messages.put(messages.size(), msg.getUser() + "_" + msg.getMessage());
            return this;
        }

    }

    public static class Message {

        private String user;
        private String message;
        private boolean action;

        public Message(String user, String message, boolean action) {
            this.user = user;
            this.message = message;
            this.action = action;
        }

        public String getUser() {
            return this.user;
        }

        public String getMessage() { return this.message; }

        public boolean matches(String s) {
            return message.matches(String.format(".*(%s).*", s));
        }

        public String replaceAll(String target, String replacement) {
            String ret = "";
            if (action) ret += "* " + user;
            else ret += "<" + user + ">";
            message = message.replaceAll(target, replacement);
            return ret + " " + message;
        }

    }

}
