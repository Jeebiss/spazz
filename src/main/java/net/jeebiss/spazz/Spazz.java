package net.jeebiss.spazz;

import net.jeebiss.spazz.github.*;
import net.jeebiss.spazz.google.Translator;
import net.jeebiss.spazz.google.WebSearch;
import net.jeebiss.spazz.irc.IRCMessage;
import net.jeebiss.spazz.irc.IRCUser;
import net.jeebiss.spazz.irc.IRCUserManager;
import net.jeebiss.spazz.uclassify.UClassify;
import net.jeebiss.spazz.uclassify.results.GenderResult;
import net.jeebiss.spazz.urban.Definition;
import net.jeebiss.spazz.urban.Response;
import net.jeebiss.spazz.urban.UrbanDictionary;
import net.jeebiss.spazz.util.MinecraftServer;
import net.jeebiss.spazz.util.Utilities;
import net.jeebiss.spazz.wolfram.QueryHandler;
import net.jeebiss.spazz.wolfram.QueryResult;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

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

    public static IRCUserManager userManager = null;

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz");

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
    public static Pattern issuesPattern = Pattern.compile("([^\\s]+)\\s*#(\\d+)");
    public static Pattern minecraftColor = Pattern.compile((char) 0xa7 + "([0-9a-fA-Fl-oL-OrR])");
    public static String random = "\u0023\u0076\u006f\u0078\u0061\u006c\u0069\u0061";
    public static Pattern minecraftRandom = Pattern.compile("(" + (char) 0xa7 + "k([^" + (char) 0xa7 + "]+))");

    public static Map<String, List<IRCMessage>> cachedMessages = new HashMap<String, List<IRCMessage>>();
    public static Pattern sReplace = Pattern.compile("^s/([^/]+)/([^/]+)/?([^\\s/]+)?", Pattern.CASE_INSENSITIVE);

    public static long messageDelay = 0;
    private static boolean wumbo = false;

    public static QueryHandler queryHandler = null;

    public static WebSearch google = null;
    public static UClassify uClassify = null;

    public static void main(String[] args) {

        bot.setEncoding(Charset.forName("UTF-8"));

        System.out.println("Starting Spazzmatic...");

        try {
            userManager = new IRCUserManager();

            LinkedHashMap map = userManager.getSpazzData();
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
            if (map.get("uclassify") instanceof String) {
                uClassify = new UClassify((String) map.get("uclassify"));
            }
            if (map.get("message-delay") instanceof Integer) {
                messageDelay = (long) ((int) map.get("message-delay"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        github = GitHub.connect(System.getProperty("spazz.github"));
        repoManager = new RepositoryManager(github);

        google = new WebSearch();

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
                        String nick = usr.getNick();
                        if (userManager.hasUser(nick) || bot.getName().equals(nick) || nick.length() == 0)
                            continue;
                        userManager.initUser(nick);
                    }
                }
            }
        }, 3000);
    }

    private static final List<String> announceChannels = Arrays.asList("#denizen-dev", "#denizen-devs");

    public static void sendToAllChannels(String message) {
        for (Channel chnl : bot.getChannels()) {
            if (announceChannels.contains(chnl.getName()))
                bot.sendMessage(chnl, chatColor + formatChat(message, false)[0]);
        }
    }

    public static void sendRandom(String message) {
        bot.sendMessage(random, message);
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
            bot.sendNotice(destination, chatColor + formatChat(message, false)[0]);
    }

    public static void sendAction(String action) {
        if (send.equals("spazzmatic"))
            System.out.println("* spazzmatic " + Colors.removeFormattingAndColors(action));
        else
            bot.sendAction(send, chatColor + action);
    }

    @Override
    public void onJoin(JoinEvent event) {

        String nick = event.getUser().getNick();

        if (!userManager.hasUser(nick))
            userManager.initUser(nick);

        if (nick.equals(bot.getNick())) {
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
        if (!shuttingDown) {
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
        Channel channel = event.getChannel();
        if (event.getChannel() != null) {
            String channelName = channel.getName();
            String nick = event.getUser().getNick();
            String action = event.getAction();
            cacheMessage(new IRCMessage(nick, action.replace("<", "<LT>"), true), channelName);
            userManager.setLastSeen(nick, "performing an action in " + channelName + chatColor + ": " + action);
        }
    }

    @Override
    public void onNotice(NoticeEvent event) {
        if (event.getUser().getNick().equals("NickServ") &&
                event.getMessage().equals("You are now identified for \u0002" + bot.getNick() + "\u0002.")) {
            if (!devMode) {
                bot.joinChannel("#denizen-dev");
                bot.joinChannel("#denizen-server-project");
                bot.joinChannel("#denizenserver");
                bot.joinChannel(random);
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

    public static void cacheMessage(IRCMessage message, String channel) {
        if (send.equals("spazzmatic")) return;
        if (!cachedMessages.containsKey(channel)) cachedMessages.put(channel, new ArrayList<IRCMessage>());
        cachedMessages.get(channel).add(0, message);
    }

    private static String address = "";

    @Override
    public void onMessage(MessageEvent event) {

        Channel chnl = event.getChannel();

        String msg = event.getMessage().trim();
        User usr = event.getUser();
        final String senderNick = usr.getNick();

        if (!userManager.hasUser(senderNick))
            userManager.initUser(senderNick);

        IRCUser ircUser = userManager.getUser(senderNick);

        if (chnl == null) {
            setSend(usr.getNick());
            chnl = bot.getChannel(chatChannel);
        }
        else if (!chnl.getName().equals(send)) {
            setSend(chnl.getName());
            userManager.setLastSeen(senderNick, "Saying \"" + msg + chatColor + "\" in " + send);
        }
        else {
            userManager.setLastSeen(senderNick, "Saying \"" + msg + chatColor + "\" in " + send);
        }

        address = senderNick;

        List<IRCMessage> messages = ircUser.getMessages();
        if (!messages.isEmpty()) {
            address = senderNick + ": ";
            Spazz.send("You have messages!");
            String lastNick = null;
            for (IRCMessage message : messages) {
                String user = message.getUser();
                if (!user.equals(lastNick)) {
                    lastNick = user;
                    Spazz.sendNotice(senderNick, lastNick + Spazz.chatColor + ":");
                }
                Spazz.sendNotice(senderNick, "  " + message.getMessage());
            }
            ircUser.clearMessages();
        }

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

        String[] args = msg.split(" ");
        String possibleAddress = args[args.length-1];

        if (possibleAddress.contains("@")) {
            address = possibleAddress.replace("@", "") + ": ";
            msg = msg.replace(possibleAddress, "").trim();
            args = msg.split(" ");
        }

        String msgLwr = msg.toLowerCase();
        String[] argsLwr = msgLwr.split(" ");

        Matcher m = issuesPattern.matcher(msgLwr);

        while (m.find() && m.group(1).contains("/")) {
            Repository repo = repoManager.searchRepository(m.group(1));
            if (repo != null) {
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
                    for (IRCMessage message : cachedMessages.get(chnl.getName())) {
                        if (user && !message.getUser().equals(m.group(3))) continue;
                        if (message.matches("(?i)" + m.group(1))) {
                            send(message.replaceAll("(?i)" + m.group(1), Colors.BOLD + m.group(2) + Colors.NORMAL + "<C>"));
                            break out;
                        }
                    }
                }
            }
            cacheMessage(new IRCMessage(senderNick, msg.replace("<", "<LT>"), false), chnl.getName());
        }

        if (!msgLwr.startsWith("."))
            return;
        String cmd = msgLwr.substring(1).split(" ", 2)[0];

        if (cmd.equals("hello")) {
            send("Hello World");
            return;
        }
        else if (cmd.equals("kitty") || cmd.equals("cat") || cmd.equals("kitteh") || cmd.equals("meow")) {
            send("Meow.");
            return;
        }
        else if (cmd.equals("color")) {

            if (!hasOp(usr, chnl) && !hasVoice(usr, chnl)) {
                send("I'm sorry, but you do not have clearance to alter my photon colorization beam.");
                return;
            }

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
        else if (cmd.equals("botsnack") || cmd.equals("bs") || cmd.equals("bsnack")) {
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

        else if (cmd.equals("repo")) {
            send("Check out scripts made by other users! - http://bit.ly/14o43eF");
        }
        else if (cmd.equals("materials") || cmd.equals("mats")) {
            send("Here is the list of all valid bukkit materials - http://bit.ly/X5smJK");
            send("All Denizen 'item:' arguments will accept a bukkit material name. Additionally you can add the data value to the name. (i.e. SANDSTONE:1)");
            return;
        }
        else if (cmd.equals("enchantments") || cmd.equals("enchants")) {
            send("Here is the list of all valid bukkit enchantments - http://bit.ly/YQ25ud");
            send("They do not follow the same naming conventions as they do in game, so be carefull.");
            return;
        }
        else if (cmd.equals("anchors") || cmd.equals("anchor")) {
            send("As of 0.8, locations can be referenced from scripts by using anchors linked to NPCs.");
            send("Check out the documentation on the anchor commands in the handbook.");
            return;
        }
        else if (cmd.equals("assignments") || cmd.equals("assignment") || cmd.equals("assign")) {
            send("As of Denizen 0.8, the assignments.yml file is " + Colors.BOLD + "not " + Colors.NORMAL + chatColor + "necessary and the /trait command does " + Colors.BOLD + " not work.");
            send("Instead, create the assignment script alongside interact scripts and assign it with:");
            send(Colors.BOLD + "- /npc assign --set 'assignment_script_name'");
            send("Check out an example of this new script's implementation at " + Colors.BLUE + "http://bit.ly/YiQ0hs");
            return;
        }
        else if (cmd.equals("help")) {
            send("Greetings. I am an interactive Denizen guide. I am a scripting guru. I am spazz.");
            send("For help with script commands, type " + Colors.BOLD + "!cmd <command_name>");
            send("For help with script requirements, type " + Colors.BOLD + "!req <requirement_name>");
            send("For info on replaceable tags, type " + Colors.BOLD + "!tag <tag_name>");
            send("For help with world events, type " + Colors.BOLD + "!event <event_name>");
            send("Refer to !help for more information.");
            return;
        }
        else if (cmd.equals("paste") || cmd.equals("pastie") || cmd.equals("hastebin") || cmd.equals("pastebin")) {
            send("Need help with a script issue or server error?");
            send("Help us help you by pasting your script " + Colors.BOLD + "and " + Colors.NORMAL + chatColor + "server log to " + Colors.BLUE + "http://mcmonkey.org/paste");
            send("From there, save the page and paste the link back in this channel.");
            return;
        }
        else if (cmd.equals("update")) {
            send("Due to the nature of our project, Denizen is always built against the " + Colors.RED + "development" + chatColor + " builds of Craftbukkit and Citizens.");
            send("Most errors can be fixed by updating all 3.");
            send(Colors.BOLD + "Denizen" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/1aaGB3T");

            if (msgLwr.split(" ").length > 1 && msgLwr.split(" ")[1].equals("depenizen"))
                send(Colors.BOLD + "Depenizen" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/1aaGEfY");

            send(Colors.BOLD + "Citizens" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/1aaGEN2");
            send(Colors.BOLD + "Craftbukkit" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/A5I50a");
            return;
        }
        else if (cmd.equals("newconfig") || cmd.equals("nc")) {
            send("If you are having issues with triggers not firing, you may be using the old config file.");
            send("You can easily generate a new one by deleting your current config.yml file in the Denizen folder");
            return;
        }
        else if (cmd.equals("wiki")) {
            send("The Denizen wiki is currently getting a makeover. This means that it doesn't currently have a lot of things.");
            send("Feel free to look at it anyway, though! http://bit.ly/14o3kdq");
            return;
        }
        else if (cmd.equals("tags")) {
            send("Here's every replaceable tag in Denizen! - http://bit.ly/164DlSE");
        }

        else if (cmd.equals("effects") || cmd.equals("potions")) {
            send("A list of Bukkit potion effects is available here " + Colors.BOLD + "- http://bit.ly/13xyXur");
        }
        else if (cmd.equals("debug") || cmd.equals("db")) {
            debugMode = !debugMode;
            send("Debug mode set to " + defaultColor + debugMode + chatColor + ".");
            bot.setVerbose(debugMode);
        }
        else if (cmd.equals("tutorials")) {
            send("Here's a list of video tutorials on how to use Denizen (Thanks " + optionalColor + "Jeebiss and mcmonkey" + chatColor + "!)");
            send(defaultColor + "All videos are viewable here" + chatColor + " - http://bit.ly/1BTzSqD");
        }
        else if (cmd.equals("shorten")) {
            if (args.length > 1) {
                send(args[1] + " -> " + Utilities.getShortUrl(args[1]));
            }
        }

        else if (cmd.equals("msg") || cmd.equals("message")) {
            if (args.length < 3) {
                send("Check your argument count. Command format: .msg <user> <message>");
                return;
            }
            msg = msg.replaceFirst(args[0] + " " + args[1] + " ", "");
            if (!userManager.hasUser(args[1]))
                send("I've never seen that user '" + args[1] + "'.");
            else {
                userManager.sendMessage(args[1], new IRCMessage(senderNick, msg, false));
                send("Message sent to: " + defaultColor + args[1] + chatColor + ".");
            }
        }

        else if (cmd.equals("yaml") || cmd.equals("yml")) {

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

        else if (cmd.equals("quote") || cmd.equals("q")) {
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

        else if (cmd.equals("p") || cmd.equals("party") || cmd.equals("celebrate")) {
            if (msgLwr.contains("reason: ")) {
                String[] split = msg.split("reason:", 2);
                String reason = split[1].replace(" me ", senderNick + " ");
                send("Woo! Let's party for " + reason.substring(1, reason.length()) + "!");
                return;
            }
            send("Woo! It's party time! Come on, celebrate with me!");
            return;
        }
        else if (cmd.equals("blame")) {
            if (args.length < 3) {
                send("Check your argument count. Command format: .blame <user> <reason>");
                return;
            }
            String blamed = args[1];
            args = msg.split(blamed);
            String reason = args[1];
            send(senderNick + " blames " + blamed + " for" + reason + "!");
        }
        else if (cmd.equals("yaii")) {
            send("Your argument is invalid.");
            return;
        }
        else if (cmd.equals("thmf")) {
            send("That hurt even my feelings. And I'm a robot.");
        }
        else if (cmd.equals("tiafo")) {
            send("Try It And Find Out.");
        }
        else if (cmd.equals("tias")) {
            send("Try It And See.");
        }
        else if (cmd.equals("lol")) {
            send("Laugh Out Loud. :D");
        }
        else if (cmd.equals("cb") || cmd.equals("coolbeans")) {
            send("That's cool beans.");
        }
        else if (msgLwr.equals("sound") || msgLwr.equals("sounds")) {
            send("Here is the list of all valid bukkit sounds - " + Colors.BLUE + "http://bit.ly/14NYbvi");
        }
        else if (cmd.equals("hb") || cmd.equals("handbook")) {
            send("Current Documentation - " + Colors.BLUE + "http://bit.ly/XaWBLN");
            send("PDF download (always current) - " + Colors.BLUE + "http://bit.ly/159JBgM");
        }
        else if (cmd.equals("getstarted") || cmd.equals("gs")) {
            send("So, you're trying to use 0.9 for the first time?");
            send("It's recommended that you read the current documentation.");
            send(Colors.BOLD + "Denizen Handbook " + Colors.NORMAL + chatColor + "- http://bit.ly/XaWBLN");
            send(Colors.BOLD + "Denizen Wiki " + Colors.NORMAL + chatColor + "- http://bit.ly/14o3kdq");
            send(Colors.BOLD + "Beginner's Guide" + Colors.NORMAL + chatColor + "- http://bit.ly/1bHkByR");
            send("Please keep in mind that documentation is a work in progress. You will likely not find everything.");
        }
        else if (cmd.equals("fire")) {
            if (hasOp(usr, chnl) || hasVoice(usr, chnl)) {
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
        else if (cmd.equals("lzrbms") || cmd.equals("lzrbmz")) {
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
                repoManager.shutdown(false);
                Utilities.saveQuotes();
                queryHandler.saveDefinitions();
                userManager.saveUserFiles();
                String[] quotes = {"Ain't nobody got time for that...", "I'm backin' up, backin' up...", "Hide yo kids, hide yo wife..."};
                send(quotes[new Random().nextInt(quotes.length)]);
                bot.quitServer(senderNick + " said so.");
                shuttingDown = true;
                bot.disconnect();
                return;
            }
        }

        else if (cmd.equals("seen")) {
            String user = null;
            IRCUser ircUser2;
            if (args.length > 1)
                user = args[1];
            if (!userManager.hasUser(user)) {
                send("I've never seen that user: " + defaultColor + user);
                return;
            }
            else {
                ircUser2 = userManager.getUser(user);
            }
            Calendar now = Calendar.getInstance();
            long currentTime = now.getTimeInMillis();
            Calendar seen = Calendar.getInstance();
            seen.setTime(ircUser2.getParsedLastSeenTime());
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
            send("Last I saw of " + defaultColor + ircUser2.getNick() + chatColor + " was "
                    + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz)").format(ircUser2.getParsedLastSeenTime()).replace("DT)", "ST").replace(")", "")
                    + ", " + new String(ircUser2.getLastSeen())
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

        else if (cmd.equals("rate")) {
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

        else if (cmd.equals("add") || cmd.equals("a")) {

            if (args.length < 2)
                send("That command is written as: .add [<object>]");

            if (args[1].startsWith("r")) {
                if (!hasOp(usr, chnl) && !hasVoice(usr, chnl))
                    send("Sorry, " + senderNick + ", that's only for the Dev Team.");
                else if (args.length > 2 && args[2].contains("/")) {
                    String ownerProject = args[2];
                    if (repoManager.hasRepository(ownerProject)) {
                        send("I am already tracking " + ownerProject + "!");
                    }
                    else {
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
                            } catch (Exception ignored) {
                            }
                        }
                        else
                            send("Error while adding repository " + args[2] + ": are you sure a repo by that name exists?");
                    }
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

        else if (cmd.equals("remove") || cmd.equals("r")) {

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

        else if (cmd.equals("info") || cmd.equals("i")) {
            if (args[1].startsWith("r")) {
                if (args.length > 2) {
                    Repository repo = repoManager.searchRepository(msg.trim().substring(7 + args[1].length()));
                    if (repo == null)
                        send("I don't recognize that repository. Perhaps be less specific?");
                    else {
                        send(repo.getUrl() + " (" + repo.getFullName() + "): Delay("
                                + repo.getUpdateDelay() + ") Issues(" + repo.hasIssues() + ") Comments("
                                + repo.hasComments() + ") Pulls(" + repo.hasPulls() + ") AverageRequests("
                                + repo.averageStats() + ")");
                    }
                }
                else
                    send("That command is written as: .info repo [<owner>/<project>]");
            }
            else if (args[1].startsWith("u")) {
                if (args.length > 2) {
                    if (userManager.hasUser(args[2])) {
                        IRCUser ircUser2 = userManager.getUser(args[2]);
                        send(args[2] + ": LastSeen(" + new String(ircUser2.getLastSeen()) + ")");
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
            else if (args[1].startsWith("m")) {
                send("Current message delay: " + bot.getMessageDelay() + "ms");
            }
            else
                send("That command is written as: .info [<object>]");
        }

        else if (cmd.equals("list") || cmd.equals("count")) {
            if (args.length < 2)
                send("That command is written as: .list [<object>]");

            if (args[1].startsWith("r")) {
                Set<String> repos = repoManager.getRepositories();
                send("I'm currently watching " + repos.size() + " repositories...");
                send(repos.toString(), true);
            }
            else if (args[1].startsWith("q")) {
                send("I currently have " + Utilities.getQuoteCount() + " quotes listed.");
            }
            else
                send("That command is written as: .list [<object>]");
        }

        else if (cmd.equals("recent")) {
            if (args.length < 3) {
                send("That command is written as: .recent [<type>] [<repository>] ((<#>-)<#>)");
            }
            if (args[1].startsWith("c")) {
                Repository repo = repoManager.searchRepository(args[2]);
                if (repo == null)
                    send("I don't recognize that repository. Perhaps be less specific?");
                else {
                    int start = 1;
                    int end = 10;
                    if (args.length > 3) {
                        if (args[3].matches("(\\d+\\-)?\\d+")) {
                            String[] split = args[3].split("-", 2);
                            if (split.length == 2) {
                                start = Integer.valueOf(split[0]);
                                end = Integer.valueOf(split[1]);
                            }
                            else {
                                end = Integer.valueOf(split[0]);
                            }
                        }
                    }
                    if (start < 1)
                        start = 1;
                    if (end < 1)
                        end = start;
                    else if (end > start + 9)
                        end = start + 9;
                    send("[<O>" + repo.getFullName() + "<C>] Listing recent commits " + start + "-" + end + "...");
                    for (Commit commit : repo.getRecentCommits(start-1, end-1)) {
                        for (String string : commit.format()) {
                            send(string);
                        }
                    }
                }
            }
        }

        else if (cmd.equals("save")) {
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
                userManager.saveUserFiles();
                send("Successfully saved all user information.");
            }
            else if (args[1].startsWith("d")) {
                if (queryHandler.saveDefinitions())
                    send("Successfully saved all definitions.");
                else
                    send("Error while saving definitions... Report this to " + Colors.CYAN + "Morphan1");
            }
        }

        else if (cmd.equals("load")) {
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
                userManager.loadUserFiles();
                send("Successfully loaded all user information.");
            }
            else if (args[1].startsWith("d")) {
                if (queryHandler.loadDefinitions())
                    send("Successfully loaded all definitions.");
                else
                    send(Colors.RED + "Error while loading definitions... Report this to " + Colors.CYAN + "Morphan1");
            }
        }

        else if (cmd.equals("edit")) {
            if (!hasOp(usr, chnl) && !hasVoice(usr, chnl))
                send("Sorry, " + senderNick + ", that's only for the Dev Team.");
            else {
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

//        else if (cmd.equals("math") || cmd.equals("m")) {
//            try {
//                Double eval = new DoubleEvaluator().evaluate(msgLwr.substring(cmd.length()+1));
//                send(defaultColor + "<math:" + msgLwr.substring(cmd.length()+1).replace(" ", "") + ">" + chatColor + " = " + eval);
//            } catch (Exception e) {
//                send("Invalid math statement. Denizen will not parse that correctly.");
//            }
//        }

        else if (cmd.equals("math") || cmd.equals("m") || cmd.equals("define") || cmd.equals("d") || cmd.equals("wolfram")
                || cmd.equals("parse")) {
            String input = msg.substring(cmd.length() + 1).trim();
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

        else if (cmd.equals("urban") || cmd.equals("u")) {
            String input = msg.substring(cmd.length()+1).replaceAll("\\s+", " ").trim();
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

        else if (cmd.equals("google") || cmd.equals("g")) {
            String input = msg.substring(cmd.length()+1).replaceAll("\\s+", " ").trim();
            WebSearch.Response response = google.search(input);
            if (response == null) {
                send(Colors.RED + "Error! Response not found!");
            }
            else if (response.getResponseStatus() != 200) {
                send("Query failed: " + response.getResponseDetails());
            }
            else {
                WebSearch.Data data = response.getResponseData();
                WebSearch.Result result = data.getMainResult();
                String content = result.getContent().replace("\n", "")
                        .replace("<b>", Colors.BOLD).replace("</b>", Colors.NORMAL + chatColor).replace("&#39;", "'")
                        .replace("&quot;", "\"").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
                send("[Result found in " + data.getSearchResultTime() + "] " + content + " -- " + result.getUrl());
            }
        }

        else if (cmd.equals("translate") || cmd.equals("tr")) {
            String input = msg.substring(cmd.length()+1).replaceAll("\\s+", " ").trim();
            Translator.Translation translation = Translator.translate(input);
            if (translation == null) {
                send(Colors.RED + "Error! No translation found!");
            }
            else {
                StringBuilder b = new StringBuilder("[Translated from ").append(translation.getSourceLanguage())
                        .append(" to en] ");
                for (Translator.Translation.Sentence sentence : translation.getSentences()) {
                    b.append(sentence.getTranslation());
                }
                send(b.toString());
            }
        }

        else if (cmd.equals("behappy") || cmd.equals("bh")) {
            send("Turn that frown upside-down! :D");
        }

        else if (cmd.equals("gender")) {
            if (args.length < 2) {
                send("Must specify a user nick.");
                return;
            }
            else if (!userManager.hasUser(args[1])) {
                send("I don't know who that is.");
                return;
            }
            StringBuilder s = new StringBuilder();
            // Use as much data as possible (all channels)
            for (List<IRCMessage> messageList : cachedMessages.values()) {
                for (IRCMessage message : messageList) {
                    if (message.getUser().equalsIgnoreCase(args[1]))
                        s.append(message.getMessage()).append("\n");
                }
            }
            GenderResult genderResult = uClassify.classifyGender(s.toString());
            double female = genderResult.getFemale();
            double male = genderResult.getMale();
            if (male > female) {
                send("That user is most likely a male.");
            }
            else if (female > male) {
                send("That user is most likely a female.");
            }
            else {
                send("I could not determine that user's gender from recent messages.");
            }
        }

        else if (cmd.equals("mcping") || cmd.equals("ping")) {
            if (args.length < 2) {
                send("Invalid server specified.");
                return;
            }
            if (!args[1].contains(".") && !args[1].toLowerCase().contains("localhost")) {
                if (userManager.hasUser(args[1])) {
                    String s = userManager.getUser(args[1]).getServerAddress();
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
            int port = 0;
            if (args[1].contains(":")) {
                try {
                    port = Integer.valueOf(args[1].split(":")[1].replace("/", ""));
                } catch (Exception ignored) {}
            }
            MinecraftServer server;
            try {
                server = new MinecraftServer(args[1].contains(":") ? args[1].split(":")[0] : args[1], port);
            } catch (Exception e) {
                server = null;
            }
            if (server == null || server.getAddress().isUnresolved()) {
                port = 25565;
                try {
                    server = new MinecraftServer(args[1].contains(":") ? args[1].split(":")[0] : args[1], port);
                } catch (Exception e) {
                    send("Error contacting that server. (" + e.getMessage() + ")");
                    return;
                }
            }
            try {
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

        else if (msgLwr.equals("dev")) {
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

        else if (msgLwr.equals("wumbo")) {
            wumbo = !wumbo == false ? !!!!!wumbo : !wumbo == true ? true : !!false;
            send("Wumbo mode " + (!wumbo ? "de" : "") + "activated.");
        }

        else if (cmd.equals("roll")) {
            String dice = argsLwr[1];
            if (dice.matches("\\d*d\\d+")) {
                String[] split = dice.split("d");
                int die;
                if (split[0] != null && !split[0].isEmpty()) {
                    die = Integer.valueOf(split[0]);
                }
                else {
                    die = 1;
                }
                int sides = Integer.valueOf(split[1]);
                if (die > 100 || sides > 100) {
                    send("Maximum roll is 100d100.");
                }
                else {
                    int total = 0;
                    StringBuilder message = new StringBuilder("You rolled: ");
                    for (int i = 0; i < die; i++) {
                        int roll = (int) (Utilities.getRandomDouble() * sides) + 1;
                        message.append(roll).append(", ");
                        total += roll;
                    }
                    send(message.substring(0, message.length() - 2));
                    send("Total roll: " + total);
                }
            }
            else {
                send("That command is written as: .roll <number of die>d<sides on die> (Example: .roll 1d6)");
            }
        }

        else if (cmd.equals("emoji")) {
            String input = msgLwr.substring(cmd.length()+1).trim().replaceAll("\\s+", "_");
            Map<String, String> emojis = github.getEmojis();
            String emoji = null;
            String link = null;
            if (input.isEmpty()) {
                emoji = (String) emojis.keySet().toArray()[Utilities.getRandomNumber(emojis.size())];
                link = emojis.get(emoji);
            }
            else if(emojis.containsKey(input)) {
                emoji = input;
                link = emojis.get(input);
            }
            else {
                int highest = 0;
                char[] chars = input.toCharArray();
                for (Map.Entry<String, String> entry : github.getEmojis().entrySet()) {
                    int number = 0;
                    char[] key = entry.getKey().toCharArray();
                    for (char chr : chars) {
                        inner: for(char c : key) {
                            if (c == chr) {
                                number++;
                                break inner;
                            }
                        }
                    }
                    if (number > highest) {
                        emoji = entry.getKey();
                        link = entry.getValue();
                        highest = number;
                    }
                }
            }
            if (emoji == null || link == null) {
                send("Sorry, I couldn't find that emoji.");
            }
            else {
                send(emoji + ": " + Utilities.getShortUrl(link));
            }
        }

        else if (cmd.equals("myip")) {
            if (args.length < 2 || args[1].isEmpty()) {
                send("That command is used like: .myip <your_server>");
                return;
            }
            userManager.setUserServer(senderNick, args[1]);
            send("Your server has been set, and can now be accessed by other users via '.mcping " + senderNick + "'");
        }

        address = "";

    }

    private static void reloadSites(boolean debug) {
        boolean original = debugMode;
        if (!debugMode)
            debugMode = debug;
        debugMode = original;
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
        System.out.println("/plain <msg>             Sends a no-color message to the current chat channel.");
        System.out.println("/join <channel>          Joins a channel.");
        System.out.println("/leave <channel> <msg>   Leaves a channel with an optional message.");
        System.out.println("/raw <protocol>          Sends a raw IRC protocol to the server.");
        System.out.println("/disconnect              Saves user info and disconnects from the server.");
        System.out.println("/quit                    Saves and shuts down the bot.");
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
                        repoManager.shutdown(false);
                    Utilities.saveQuotes();
                    queryHandler.saveDefinitions();
                    userManager.saveUserFiles();
                    bot.quitServer("Command sent by console: /disconnect");
                    System.out.println();
                    System.out.println();
                    System.out.println("Disconnected.");
                    shuttingDown = true;
                    break;

                case "quit":
                    System.out.println();
                    System.out.println("Shutting down...");
                    bot.quitServer("Command sent by console: /quit");
                    shuttingDown = true;
                    Utilities.saveQuotes();
                    queryHandler.saveDefinitions();
                    userManager.saveUserFiles();
                    if (repoManager != null)
                        repoManager.shutdown(false);
                    System.exit(0);
                    break input;

                case "reconnect":
                    try {
                        try {
                            bot.shutdown();
                        }
                        catch (Exception e) {}
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
                    if (bot.userExists(channel))
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

}
