package net.jeebiss.spazz;

import net.jeebiss.spazz.github.*;
import net.jeebiss.spazz.javaluator.DoubleEvaluator;
import net.jeebiss.spazz.util.MinecraftServer;
import net.jeebiss.spazz.util.Utilities;
import net.jeebiss.spazz.wolfram.QueryHandler;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URLEncoder;
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
    public static boolean metaBackup = false;
    public static boolean monkeybotScan = false;
    public static String chatChannel = "#denizen-dev";
    public static GitHub github = null;
    public static RepositoryManager repoManager = null;
    public static Pattern issuesPattern = Pattern
            .compile("http(?:s)?://(?:www\\.)?github\\.com/(\\w+)/(\\w+)/(?:issues|pulls)/(\\d+)", Pattern.CASE_INSENSITIVE);
    public static Pattern altIssuesPattern = Pattern.compile("(\\w+)\\s*#(\\d+)");
    public static Pattern minecraftColor = Pattern.compile((char) 0xa7 + "(.)");

    public static QueryHandler queryHandler = null;

    public static void main(String[] args) {

        System.out.println("Starting Spazzmatic...");

        try {
            usersFolder = new File(System.getProperty("user.dir") + "/users");

            LinkedHashMap map = null;
            Yaml yaml = new Yaml();
            InputStream is = new File(usersFolder + "/spazzmatic.yml").toURI().toURL().openStream();
            map = (LinkedHashMap) yaml.load(is);
            if (map.get("password") instanceof String) {
                System.setProperty("spazz.password", (String) map.get("password"));
            }
            if (map.get("bitly") instanceof String) {
                System.setProperty("spazz.bitly", (String) map.get("bitly"));
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
        } catch (Exception e) {
            if (!usersFolder.isDirectory() && !usersFolder.mkdir()) {
                System.out.println("Could not load users folder. Password for Spazzmatic not found. Cancelling startup...");
                return;
            }
        }

        github = GitHub.connect(System.getProperty("spazz.github"));

        Utilities.loadQuotes();
        reloadSites(debugMode);

        bot.getListenerManager().addListener(spazz);
        /*
		 * Connect to #denizen-dev on start up
		 */
        bot.setVersion("Spazzmatic v0.3 [Morphan1]");
        bot.setAutoReconnect(true);
        bot.setName("spazzmatic");
        bot.setLogin("spazz");
        bot.setVerbose(debugMode);
        bot.setAutoNickChange(true);
        bot.setAutoReconnect(true);
        bot.identify(System.getProperty("spazz.password"));

        try {
            bot.connect("irc.esper.net");
        } catch (Exception e) {
            System.out.println("Failed to connect to EsperNet. Check your internet connection and try again.");
            return;
        }
        bot.setMessageDelay(500);
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

        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                boolean monkeybot = false;
                for (Channel chnl : bot.getChannels()) {
                    for (User usr : chnl.getUsers()) {
                        String nick = usr.getNick().toLowerCase();
                        if (dUsers.containsKey(nick) || bot.getName().equalsIgnoreCase(nick) || nick.length() == 0)
                            continue;
                        dUsers.put(nick, new dUser(usr.getNick()));
                        if (nick.equals("monkeybot"))
                            monkeybot = true;
                    }
                }
                for (String usr : findUserFiles()) {
                    usr = usr.replace(".yml", "");
                    if (dUsers.containsKey(usr)) continue;
                    dUsers.put(usr, new dUser(usr));
                }
                if (!monkeybot) {
                    sendToAllChannels(chatColor + "User 'monkeybot' not detected. Enabling meta backup systems.");
                    metaBackup = true;
                }
            }
        }, 3000);

        System.out.println("Successfully loaded Spazzmatic. You may now begin using console commands.");
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("/me <action>             Performs an action in the current chat channel.");
        System.out.println("/msg <user> <msg>        Sends a private message to a user.");
        System.out.println("/plain <msg>             Sends a non-colored message to the current chat channel.");
        System.out.println("/join <channel>          Joins a channel.");
        System.out.println("/leave <channel> <msg>   Leaves a channel with an optional message.");
        System.out.println("/disconnect              Saves user info and disconnects from the server.");
        System.out.println("/channel <channel>       Set the chat channel.");
        System.out.println("/debug                   Turns console debug on or off.");
        System.out.println("/raw                     Sends a raw IRC protocol to the server.");
        System.out.println("<msg>                    Sends a message to the current chat channel.");
        System.out.println();

        Scanner scanner = new Scanner(System.in);
        String rawInput = "";
        String inputCommand = "";
        String commandArgs = "";
        String channel = "";

        while (!shuttingDown) {

            rawInput = scanner.nextLine();
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

                case "channel":
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
                    bot.quitServer("Command sent by console: /disconnect");
                    System.out.println();
                    System.out.println();
                    System.out.println("Disconnected.");
                    shuttingDown = true;
                    break;

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
                        bot.sendAction(chatChannel, chatColor + commandArgs);
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
                        bot.sendMessage(chatChannel, commandArgs);
                    else
                        System.out.println("Not connected to channel \"" + chatChannel + "\".");
                    break;

                case "raw":
                    bot.sendRawLineNow(channel.toUpperCase() + " " + commandArgs);
                    break;

                default:
                    if (rawInput.startsWith("/")) {
                        System.out.println("Invalid command: " + inputCommand);
                        break;
                    }
                    if (bot.channelExists(chatChannel))
                        bot.sendMessage(chatChannel, chatColor + rawInput);
                    else
                        System.out.println("Not connected to channel \"" + chatChannel + "\".");
                    break;

            }
            rawInput = "";
            inputCommand = "";
            commandArgs = "";
            channel = "";

        }

        scanner.close();
    }

    public static void sendToAllChannels(String message) {
        for (Channel chnl : bot.getChannels())
            bot.sendMessage(chnl, message);
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
        repoManager.shutdown();
        Utilities.saveQuotes();
        for (dUser usr : dUsers.values()) {
            try {
                usr.saveAll();
            } catch (Exception e) {
                if (debugMode) e.printStackTrace();
                else
                    System.out.println("An error has occured while using saving user " + usr.getNick() + " on disconnect... Turn on debug for more information.");
            }
        }
    }

    @Override
    public void onReconnect(ReconnectEvent event) {

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
        dUsers.get(event.getUser().getNick()).setLastSeen("performing an action in " + event.getChannel().getName()
                + chatColor + ": " + event.getAction());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPrivateMessage(PrivateMessageEvent event) {
        onMessage(new MessageEvent(bot, null, event.getUser(), event.getMessage()));
    }

    public static void onIssue(IssueEvent event) {

        Issue issue = event.getIssue();
        Repository repo = issue.getRepo();

        sendToAllChannels(chatColor + "[" + optionalColor + repo.getName() + chatColor + "] Issue " + event.getState().name().toLowerCase()
                + ": [" + defaultColor + issue.getNumber() + chatColor + "] \"" + defaultColor + issue.getTitle()
                + chatColor + "\" by " + optionalColor + issue.getUser().getLogin() + chatColor + " -- " + issue.getShortUrl());

    }

    public static void onPullRequest(PullRequestEvent event) {
        PullRequest request = event.getPullRequest();
        Repository repo = request.getRepo();

        sendToAllChannels(chatColor + "[" + optionalColor + repo.getName() + chatColor + "] Pull request " + event.getState().toLowerCase()
                + ": [" + defaultColor + request.getNumber() + chatColor + "] \"" + defaultColor + request.getTitle()
                + chatColor + "\" by " + optionalColor + request.getUser().getLogin() + chatColor + " -- " + request.getShortUrl());

    }

    public static void onCommit(CommitEvent event) {

        ArrayList<Commit> commits = event.getCommits();
        Repository repo = event.getRepo();

        String users = "";
        for (String user : event.getUsers()) {
            users += user + ", ";
        }

        sendToAllChannels(chatColor + "[" + optionalColor + repo.getName() + chatColor + "] "
                + defaultColor + users.substring(0, users.length() - 2) + chatColor + " pushed " + commits.size()
                + " commit" + (commits.size() == 1 ? "" : "s") + " to master branch");

        for (Commit commit : commits) {
            String message = commit.getMessage();
            String shortenedUrl = commit.getShortUrl();
            if (message.contains("\n")) {
                message = message.substring(0, message.indexOf('\n'));
            }

            sendToAllChannels(defaultColor + "  " + commit.getAuthor() + chatColor + ": " + message + " -- " + shortenedUrl);
        }

    }

    public static void onComment(CommentEvent event) {

        Comment comment = event.getComment();
        Repository repo = event.getRepo();

        if (comment instanceof IssueComment) {
            IssueComment icomment = (IssueComment) comment;
            Issue issue = icomment.getIssue();
            String url = icomment.getShortUrl();
            sendToAllChannels(chatColor + "[" + optionalColor + repo.getName() + chatColor + "] " + defaultColor
                    + comment.getUser().getLogin() + chatColor + " commented on "
                    + issue.getState() + " issue: ["
                    + defaultColor + issue.getNumber() + chatColor + "] " + defaultColor
                    + issue.getTitle() + chatColor + " by " + defaultColor
                    + issue.getUser().getLogin() + chatColor
                    + (url != null ? " -- " + url : ""));
        }
        else if (comment instanceof CommitComment) {
            CommitComment ccomment = (CommitComment) comment;
            Commit commit = ccomment.getCommit();
            String url = ccomment.getShortUrl();
            sendToAllChannels(chatColor + "[" + optionalColor + repo.getName() + chatColor + "] " + defaultColor
                    + comment.getUser().getLogin() + chatColor + " commented on commit: " + defaultColor
                    + commit.getMessage() + chatColor + " by " + defaultColor
                    + commit.getAuthor() + chatColor
                    + (url != null ? " -- " + url : ""));
        }

    }

    @Override
    public void onMessage(MessageEvent event) {

        final Channel chnl = event.getChannel();

        String msg = event.getMessage();
        User usr = event.getUser();
        String send = null;

        dUser dusr = null;
        if (!dUsers.containsKey(usr.getNick().toLowerCase()))
            dUsers.put(usr.getNick().toLowerCase(), new dUser(usr.getNick()));

        dusr = dUsers.get(usr.getNick().toLowerCase());

        if (chnl == null)
            send = usr.getNick();
        else {
            send = chnl.getName();
            dusr.setLastSeen("Saying \"" + msg + chatColor + "\" in " + send);
        }

        String msgLwr = msg.toLowerCase();
        final Matcher issuesMatcher = issuesPattern.matcher(msgLwr);
        final Matcher altIssuesMatcher = altIssuesPattern.matcher(msgLwr);
        final String senderNick = event.getUser().getNick();
        String address = "";

        if (charging) {
            if (System.currentTimeMillis() > (chargeInitiateTime + chargeFullTime + chargeFullTime / 2)) {
                bot.sendAction(chnl, "loses control of his beams, accidentally making pew pew noises towards " + charger.getNick() + "!");
                charging = false;
                chargeInitiateTime = 0;
                charger = null;
                botsnack = 0;
                feeders.clear();
            }

        }

        if (msg.endsWith("@@")) {
            String args[] = msg.split(" ");
            if (debugMode) System.out.println(args.length);
            for (int x = 0; x < args.length; x++) {
                if (!args[x].contains("@@"))
                    continue;
                else {
                    address = args[x];
                    address = address.substring(0, address.length() - 2);
                    address = address + ": ";
                }
            }
        }

        while (issuesMatcher.find()) {
            if (repoManager.hasRepository(issuesMatcher.group(1), issuesMatcher.group(2))) {
                Repository repo = repoManager.getRepository(issuesMatcher.group(2));
                Issue issue = repo.getIssue(Integer.valueOf(issuesMatcher.group(3)));
                if (issue != null) {
                    bot.sendMessage(send, chatColor + "[" + optionalColor + repo.getName() + chatColor + "] " + defaultColor
                            + issue.getTitle() + chatColor + " by " + optionalColor + issue.getUser().getLogin() + chatColor
                            + " -- " + issue.getState().toLowerCase() + " " + (issue.isPullRequest() ? "pull request" : "issue")
                            + ". (Created " + issue.getCreatedAtSimple() + ", last updated " + issue.getLastUpdatedSimple() + ".)");
                }
            }
        }

        while (altIssuesMatcher.find()) {
            String[] repoName = altIssuesMatcher.group(1).split("/");
            if (repoManager.hasRepository(repoName[repoName.length - 1])) {
                Repository repo = repoManager.getRepository(repoName[repoName.length - 1]);
                Issue issue = repo.getIssue(Integer.valueOf(altIssuesMatcher.group(2)));
                if (issue != null) {
                    bot.sendMessage(send, chatColor + "[" + optionalColor + repo.getName() + chatColor + "] " + defaultColor
                            + issue.getTitle() + chatColor + " by " + optionalColor + issue.getUser().getLogin() + chatColor
                            + " -- " + issue.getState().toLowerCase() + " " + (issue.isPullRequest() ? "pull request" : "issue")
                            + ". (Created " + issue.getCreatedAtSimple() + ", last updated " + issue.getLastUpdatedSimple() + ".) - "
                            + issue.getShortUrl());
                }
            }
        }

        if (msg.equalsIgnoreCase(".hello")) {
            bot.sendMessage(send, address + "Hello World");
            return;
        }
        else if (msgLwr.startsWith(".kitty")) {
            bot.sendMessage(send, address + chatColor + "Meow.");
            return;
        }
        else if (msgLwr.startsWith(".color")) {

            if (!hasOp(usr, chnl) && !hasVoice(usr, chnl)) {
                bot.sendMessage(send, address + chatColor + "I'm sorry, but you do not have clearance to alter my photon colorization beam.");
                return;
            }

            String[] args = msg.split(" ");
            if (args.length > 3) {
                bot.sendMessage(send, address + chatColor + "I cannot read minds... yet. Hit me up with a bot-friendly color.");
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
                bot.sendMessage(send, address + chatColor + "I eat " + args[1] + " for breakfast. That's not a color.");
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
            bot.sendMessage(send, address + chatColor + "Photon colorization beam reconfigured " + "[] " + optionalColor + "() " + defaultColor + "{}");
            return;
        }
        else if (msgLwr.startsWith(".botsnack")) {
            String args[] = msg.split(" ");

            if (feeders.toString().contains(usr.toString())) {
                bot.sendMessage(send, address + chatColor + "Thanks, but I can't have you controlling too much of my diet.");
                return;
            }

            if (args.length == 2) {
                if (chnl.getUsers().toString().contains(args[1]))
                    bot.sendMessage(send, address + chatColor + "Gluttony mode activated. Beginning " + args[1] + " consumption sequence.");
                else {
                    ArrayList<User> users = new ArrayList<User>(chnl.getUsers());
                    Random rand = new Random();
                    User random = users.get(rand.nextInt(users.size()));
                    bot.sendMessage(send, address + chatColor + "Oh no! " + args[1] + " not found, nomming " + random.getNick() + " instead.");
                }
                feeders.add(usr);
                botsnack++;
                return;

            }
            else {
                bot.sendMessage(send, address + chatColor + "OM NOM NOM! I love botsnacks!");
                feeders.add(usr);
                botsnack++;
                return;
            }

        }

        else if (msgLwr.startsWith(".repo")) {
            bot.sendMessage(send, address + chatColor + "Check out scripts made by other users! - http://bit.ly/14o43eF");
        }
        else if (msgLwr.startsWith(".materials") || msgLwr.startsWith(".mats")) {
            bot.sendMessage(send, address + chatColor + "Here is the list of all valid bukkit materials - http://bit.ly/X5smJK");
            bot.sendMessage(send, chatColor + "All Denizen 'item:' arguments will accept a bukkit material name. Additionally you can add the data value to the name. (i.e. SANDSTONE:1)");
            return;
        }
        else if (msgLwr.startsWith(".enchantments") || msgLwr.startsWith(".enchants")) {
            bot.sendMessage(send, address + chatColor + "Here is the list of all valid bukkit enchantments - http://bit.ly/YQ25ud");
            bot.sendMessage(send, chatColor + "They do not follow the same naming conventions as they do in game, so be carefull.");
            return;
        }
        else if (msgLwr.startsWith(".anchors") || msgLwr.startsWith(".anchor")) {
            bot.sendMessage(send, address + chatColor + "As of 0.8, locations can be referenced from scripts by using anchors linked to NPCs.");
            bot.sendMessage(send, chatColor + "Check out the documentation on the anchor commands in the handbook.");
            return;
        }
        else if (msgLwr.startsWith(".assignments") || msgLwr.startsWith(".assignment") || msgLwr.startsWith(".assign")) {
            bot.sendMessage(send, address + chatColor + "As of Denizen 0.8, the assignments.yml file is " + Colors.BOLD + "not " + Colors.NORMAL + chatColor + "necessary and the /trait command does " + Colors.BOLD + " not work.");
            bot.sendMessage(send, chatColor + "Instead, create the assignment script alongside interact scripts and assign it with:");
            bot.sendMessage(send, chatColor + Colors.BOLD + "- /npc assign --set 'assignment_script_name'");
            bot.sendMessage(send, chatColor + "Check out an example of this new script's implementation at " + Colors.BLUE + "http://bit.ly/YiQ0hs");
            return;
        }
        else if (msgLwr.startsWith(".help")) {
            bot.sendMessage(send, address + chatColor + "Greetings. I am an interactive Denizen guide. I am a scripting guru. I am spazz.");
            bot.sendMessage(send, chatColor + "For help with script commands, type " + Colors.BOLD + "!cmd <command_name>");
            bot.sendMessage(send, chatColor + "For help with script requirements, type " + Colors.BOLD + "!req <requirement_name>");
            bot.sendMessage(send, chatColor + "For info on replaceable tags, type " + Colors.BOLD + "!tag <tag_name>");
            bot.sendMessage(send, chatColor + "For help with world events, type " + Colors.BOLD + "!event <event_name>");
            bot.sendMessage(send, chatColor + "Refer to !help for more information.");
            return;
        }
        else if (msgLwr.startsWith(".paste") || msgLwr.startsWith(".pastie") || msgLwr.startsWith(".hastebin") || msgLwr.startsWith(".pastebin")) {
            bot.sendMessage(send, address + chatColor + "Need help with a script issue or server error?");
            bot.sendMessage(send, chatColor + "Help us help you by pasting your script " + Colors.BOLD + "and " + Colors.NORMAL + chatColor + "server log to " + Colors.BLUE + "http://hastebin.com");
            bot.sendMessage(send, chatColor + "From there, save the page and paste the link back in this channel.");
            return;
        }
        else if (msgLwr.startsWith(".update")) {
            bot.sendMessage(send, address + chatColor + "Due to the nature of our project, Denizen is always built against the " + Colors.RED + "development" + chatColor + " builds of Craftbukkit and Citizens.");
            bot.sendMessage(send, chatColor + "Most errors can be fixed by updating all 3. (NOTE: We build on Bukkit and therefore do not support Spigot issues!)");
            bot.sendMessage(send, Colors.BOLD + "Denizen" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/1aaGB3T");

            if (msgLwr.split(" ").length > 1 && msgLwr.split(" ")[1].equals("depenizen"))
                bot.sendMessage(send, Colors.BOLD + "Depenizen" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/1aaGEfY");

            bot.sendMessage(send, Colors.BOLD + "Citizens" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/1aaGEN2");
            bot.sendMessage(send, Colors.BOLD + "Craftbukkit" + Colors.NORMAL + Colors.BLUE + "- http://bit.ly/A5I50a");
            return;
        }
        else if (msgLwr.startsWith(".newconfig") || msgLwr.startsWith(".nc")) {
            bot.sendMessage(send, address + chatColor + "If you are having issues with triggers not firing, you may be using the old config file.");
            bot.sendMessage(send, chatColor + "You can easily generate a new one by deleting your current config.yml file in the Denizen folder");
            return;
        }
        else if (msgLwr.startsWith(".wiki")) {
            bot.sendMessage(send, address + chatColor + "The Denizen wiki is currently getting a makeover. This means that it doesn't currently have a lot of things.");
            bot.sendMessage(send, chatColor + "Feel free to look at it anyway, though! http://bit.ly/14o3kdq");
            return;
        }
        else if (msgLwr.startsWith(".tags")) {
            bot.sendMessage(send, chatColor + "Here's every replaceable tag in Denizen! - http://bit.ly/164DlSE");
        }

        else if (msgLwr.startsWith(".tag")) {
            if (!metaBackup) {
                bot.sendMessage(send, chatColor + "Meta backup mode not enabled. Please use !tag <tag_name>.");
            }
            else try {
                String arg = msgLwr.split(" ")[1];
                ArrayList<HashMap<String, String[]>> tags = findMonkeyTag(arg);
                if (tags.isEmpty()) {
                    bot.sendMessage(send, chatColor + "I found " + defaultColor + 0 + chatColor + " matching tags.");
                }
                else if (tags.size() > 1) {
                    bot.sendMessage(send, chatColor + "I found " + defaultColor + tags.size() + chatColor + " matching tags.");
                    String send_msg = optionalColor;
                    int x = 1;
                    for (HashMap<String, String[]> tag : tags) {
                        send_msg += tag.get("name")[0] + ", ";
                        x++;
                        if (x % 10 == 0) {
                            bot.sendMessage(send, send_msg.substring(0, send_msg.length() - 1));
                            send_msg = optionalColor;
                        }
                    }
                    bot.sendMessage(send, send_msg.substring(0, send_msg.length() - 2) + ".");
                }
                else {
                    HashMap<String, String[]> tag = tags.get(0);
                    bot.sendMessage(send, chatColor + "Tag found: " + defaultColor + tag.get("name")[0]
                            + chatColor + ", which returns a " + defaultColor + tag.get("returns")[0]);
                    for (String line : tag.get("description"))
                        bot.sendMessage(send, chatColor + "  " + line);
                }
            } catch (Exception e) {
                bot.sendMessage(send, chatColor + "That command is written as: .tag <tag_name>");
            }
        }

        else if (msgLwr.startsWith(".effects") || msgLwr.startsWith(".potions")) {
            bot.sendMessage(send, chatColor + "A list of Bukkit potion effects is available here " + Colors.BOLD + "- http://bit.ly/13xyXur");
        }
        else if (msgLwr.startsWith(".debug")) {
            debugMode = !debugMode;
            bot.sendMessage(send, chatColor + "Debug mode set to " + defaultColor + debugMode + chatColor + ".");
            bot.setVerbose(debugMode);
        }
        else if (msgLwr.startsWith(".tutorials")) {
            bot.sendMessage(send, chatColor + "Here's a list of video tutorials on how to use Denizen (Thanks " + optionalColor + "Jeebiss" + chatColor + "!)");
            bot.sendMessage(send, chatColor + "1) " + defaultColor + "Hello World" + chatColor + " - http://bit.ly/1dgwyOn");
            bot.sendMessage(send, chatColor + "2) " + defaultColor + "Questing 101" + chatColor + " - http://bit.ly/13RT8JY");
        }
        else if (msgLwr.startsWith(".shorten")) {
            String[] args = msg.split(" ");
            if (args.length > 1) {
                bot.sendMessage(send, chatColor + args[1] + " -> " + Utilities.getShortUrl(args[1]));
            }
        }

        else if (msgLwr.startsWith(".msg") || msgLwr.startsWith(".message")) {
            String[] args = msg.split(" ");
            if (args.length < 2 || args[2].length() < 1) {
                bot.sendMessage(send, address + chatColor + "Check your argument count. Command format: .msg <user> <message>");
                return;
            }
            msg = msg.replaceFirst(args[0] + " " + args[1] + " ", "");
            if (!dUsers.containsKey(args[1].toLowerCase()))
                dUsers.put(args[1].toLowerCase(), new dUser(args[1]));
            dUsers.get(args[1].toLowerCase()).addMessage(new Message(senderNick, msg));
            bot.sendMessage(send, chatColor + "Message sent to: " + defaultColor + args[1] + chatColor + ".");
            return;
        }

        else if (msgLwr.startsWith(".yaml") || msgLwr.startsWith(".yml")) {

            String[] args = msg.split(" ");
            if (args.length < 2) {
                bot.sendMessage(send, address + chatColor + "Check your argument count. Command format: .yml <link>");
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
                else {
                    bot.sendMessage(send, address + Colors.RED + "I can't get your script from that website :(");
                }
            } catch (Exception e) {
                if (debugMode) e.printStackTrace();
                else
                    System.out.println("An error has occured while getting script from website... Turn on debug for more information.");
                bot.sendMessage(send, chatColor + "Invalid website format!");
                return;
            }

            Yaml yaml = new Yaml();
            try {
                yaml.load(rawYaml);
                bot.sendMessage(send, address + chatColor + "Your YAML is valid.");
            } catch (YAMLException e) {
                String fullStack = getCustomStackTrace(e);
                String[] stackList = fullStack.split("\\n");
                int x = 0;
                while (!stackList[x].contains("org.yaml")) {
                    bot.sendMessage(send, address + stackList[x]);
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
                bot.sendMessage(send, chatColor
                        + (quote.getKey() == 0 ? "[" + optionalColor + number + chatColor + "] "
                        : spacing) + quote.getValue());
            }
        }

        else if (msgLwr.startsWith(".party") || msgLwr.startsWith(".celebrate")) {
            if (msgLwr.contains("reason: ")) {
                String[] split = msg.split("reason:");
                String reason = split[1].replace(" me ", senderNick + " ");
                bot.sendMessage(send, address + chatColor + "Woo! Let's party for " + reason.substring(1, reason.length()) + "!");
                return;
            }
            bot.sendMessage(send, address + chatColor + "Woo! It's party time! Come on, celebrate with me!");
            return;
        }
        else if (msgLwr.startsWith(".blame")) {
            String[] args = msg.split(" ");
            if (args.length < 3) {
                bot.sendMessage(send, address + chatColor + "Check your argument count. Command format: .blame <user> <reason>");
                return;
            }
            String blamed = args[1];
            args = msg.split(blamed);
            String reason = args[1];
            bot.sendMessage(send, address + chatColor + senderNick + " blames " + blamed + " for" + reason + "!");
        }
        else if (msgLwr.startsWith(".yaii")) {
            bot.sendMessage(send, address + chatColor + "Your argument is invalid.");
            return;
        }
        else if (msgLwr.startsWith(".thmf") || msgLwr.startsWith(".tfw")) {
            bot.sendMessage(send, address + chatColor + "That hurt even my feelings. And I'm a robot.");
        }
        else if (msgLwr.startsWith(".cb") || msgLwr.startsWith(".coolbeans")) {
            bot.sendMessage(send, address + chatColor + "That's cool beans.");
            return;
        }
        else if (msgLwr.equals(".sound") || msgLwr.equals(".sounds")) {
            bot.sendMessage(send, address + chatColor + "Here is the list of all valid bukkit sounds - " + Colors.BLUE + "http://bit.ly/14NYbvi");
        }
        else if (msgLwr.startsWith(".hb") || msgLwr.startsWith(".handbook")) {
            bot.sendMessage(send, address + chatColor + "Current Documentation - " + Colors.BLUE + "http://bit.ly/XaWBLN");
            bot.sendMessage(send, chatColor + "PDF download (always current) - " + Colors.BLUE + "http://bit.ly/159JBgM");
            return;
        }
        else if (msgLwr.startsWith(".getstarted") || msgLwr.startsWith(".gs")) {
            bot.sendMessage(send, address + chatColor + "So, you're trying to use 0.9 for the first time?");
            bot.sendMessage(send, chatColor + "It's recommended that you read the current documentation.");
            bot.sendMessage(send, Colors.BOLD + "Denizen Handbook " + Colors.NORMAL + chatColor + "- http://bit.ly/XaWBLN");
            bot.sendMessage(send, Colors.BOLD + "Denizen Wiki " + Colors.NORMAL + chatColor + "- http://bit.ly/14o3kdq");
            bot.sendMessage(send, Colors.BOLD + "Beginner's Guide" + Colors.NORMAL + chatColor + "- http://bit.ly/1bHkByR");
            bot.sendMessage(send, chatColor + "Please keep in mind that documentation is a work in progress. You will likely not find everything.");
            return;
        }
        else if (msgLwr.startsWith(".fire")) {
            if (hasOp(usr, chnl) || hasVoice(usr, chnl)) {
                String args[] = msg.split(" ");
                if (!charging) {
                    bot.sendMessage(send, address + chatColor + "Erm... was I supposed to be charging? D:");
                    return;
                }
                if (usr != charger) {
                    bot.sendMessage(send, address + chatColor + "Sorry, but my firing sequence has already been started by " + charger.getNick() + ".");
                    return;
                }
                if (args.length != 2) {
                    bot.sendMessage(send, address + chatColor + "I can't just fire into thin air :(");
                    return;
                }
                if (chnl.getUsers().toString().contains(args[1])) {
                    double chance = (Math.random() * 99 + 1) * ((System.currentTimeMillis() - chargeInitiateTime) / chargeFullTime);
                    if (chance > 50) {
                        bot.sendAction(send, chatColor + "makes pew pew noises towards " + args[1] + "... successfully!");
                        bot.sendMessage(send, chatColor + "Take that " + args[1] + "!");
                    }
                    else {
                        bot.sendAction(send, chatColor + "makes pew pew noises towards " + args[1] + "... and misses D:");
                        bot.sendMessage(send, chatColor + "You've bested me this time " + args[1] + "...");
                    }
                    charging = false;
                    chargeInitiateTime = 0;
                    charger = null;
                    feeders.clear();
                    return;
                }
                else {
                    bot.sendMessage(send, chatColor + args[1] + ", really? I need a legitimate target. This is serious business.");
                    return;
                }
            }

            bot.sendMessage(send, chatColor + "Whoa there, you can't touch that button.");
            return;

        }
        else if (msgLwr.startsWith(".lzrbms") || msgLwr.startsWith(".lzrbmz")) {
            if (hasOp(usr, chnl) || hasVoice(usr, chnl)) {
                if (botsnack < 3) {
                    bot.sendMessage(send, address + chatColor + ": Botsnack levels too low. Can't charge lazers...");
                    return;
                }

                if (charging) {
                    bot.sendMessage(send, address + chatColor + ": I'm already a bit occupied here!");
                    return;
                }

                chargeInitiateTime = System.currentTimeMillis();
                charging = true;
                charger = usr;
                bot.sendMessage(send, address + chatColor + "Imma chargin' up meh lazerbeamz...");
                botsnack -= 3;
                return;
            }

            bot.sendMessage(send, address + chatColor + "Umm, that's not for you.");
            return;

        }
        else if (msg.equalsIgnoreCase(".bye")) {
            if (!hasOp(usr, chnl) && !hasVoice(usr, chnl)) {
                bot.sendMessage(send, chatColor + "Ahaha, you can never kill me, " + senderNick + "!");
            }
            else {
                repoManager.shutdown();
                Utilities.saveQuotes();
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
                bot.sendMessage(send, chatColor + quotes[new Random().nextInt(quotes.length)]);
                bot.quitServer(senderNick + " said so.");
                shuttingDown = true;
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
                bot.sendMessage(send, chatColor + "I've never seen that user: " + defaultColor + user);
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
            bot.sendMessage(send, chatColor + "Last I saw of " + defaultColor + dusr2.getNick() + chatColor + " was "
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
        //    bot.sendNotice(usr, "If you want to whether a Denizen script will compile, type " + Colors.BOLD + ".yml link_to_the_script");
        //    return;
        //}

        else if (msgLwr.startsWith(".rate")) {
            bot.sendNotice(usr, chatColor + "Max rate limit: " + github.getMaxRateLimit());
            bot.sendNotice(usr, chatColor + "Remaining rate limit: " + github.getRemainingRateLimit());

            long currentTime = Calendar.getInstance().getTimeInMillis();
            Calendar seen = Calendar.getInstance();
            seen.setTime(github.getRateLimitReset());
            long seenTime = seen.getTimeInMillis();
            long seconds = (seenTime - currentTime) / 1000;
            long minutes = seconds / 60;
            seconds = seconds - (minutes * 60);

            bot.sendNotice(usr, chatColor + "Next reset: "
                    + (minutes > 0 ? minutes > 1 ? minutes + " minutes, " : "1 minute, " : "")
                    + (seconds > 0 ? seconds > 1 ? seconds + " seconds" : "1 second" : minutes > 0 ? "0 seconds" : "Now."));
        }

        else if (msgLwr.startsWith(".add")) {

            String[] args = msgLwr.trim().split(" ");
            if (args.length < 2)
                bot.sendMessage(send, chatColor + "That command is written as: .add [<object>]");

            if (args[1].startsWith("r")) {
                if (!hasOp(usr, bot.getChannel("#denizen-devs")) && !hasVoice(usr, bot.getChannel("#denizen-devs")))
                    bot.sendMessage(send, chatColor + "Sorry, " + senderNick + ", that's only for the Dev Team.");
                else if (args.length > 2 && args[2].contains("/")) {
                    String[] proj = args[2].split("/", 2);
                    if (repoManager.hasRepository(proj[1]))
                        bot.sendMessage(send, chatColor + "I'm already tracking a \"" + proj[1] + "\" project!");
                    else {
                        double updateDelay = 60;
                        for (String arg : args) {
                            if (arg.startsWith("delay:") && Double.valueOf(arg.split(":")[1]) != null)
                                updateDelay = Double.valueOf(arg.split(":")[1]);
                        }
                        if (repoManager.addRepository(proj[0], proj[1], updateDelay, !msgLwr.contains("no_issues"), !msgLwr.contains("no_comments"),
                                !msgLwr.contains("no_pulls")))
                            bot.sendMessage(send, chatColor + "I am now tracking " + proj[1]
                                    + " with a delay of " + updateDelay + ".");
                        else
                            bot.sendMessage(send, chatColor + "Error while adding repository " + args[2] + "...");
                    }
                }
                else
                    bot.sendMessage(send, chatColor + "That command is written as: "
                            + parseUsage(".add repo [<author>/<project>] (no_issues) (no_comments) (no_pulls) (delay:<#.#>)"));
            }
            else if (args[1].startsWith("q")) {
                if (args.length > 2) {
                    String quoteMsg = msg.substring(args[0].length() + args[1].length() + 2);
                    if (quoteMsg.length() < 5)
                        bot.sendMessage(send, chatColor + "Quote must have at least 5 characters.");
                    else {
                        bot.sendMessage(send, chatColor + "Added quote as #" + Utilities.addQuote(quoteMsg, senderNick) + ".");
                    }
                }
                else
                    bot.sendMessage(send, chatColor + "That command is written as: .add quote [<message>]");
            }
            else if (args[1].startsWith("toq")) {
                if (args.length > 3) {
                    try {
                        int number = Integer.valueOf(args[2]);
                        String quoteMsg = msg.substring(args[0].length() + args[1].length() + args[2].length() + 3);
                        if (quoteMsg.length() < 5)
                            bot.sendMessage(send, chatColor + "Quote must have at least 5 characters.");
                        else {
                            Utilities.addToQuote(number, quoteMsg);
                            bot.sendMessage(send, chatColor + "Added line to quote #" + number);
                        }
                    } catch (Exception e) {
                        bot.sendMessage(send, chatColor + "That command is written as: .add toquote [<#>] [<message>]");
                    }
                }
            }
            else
                bot.sendMessage(send, chatColor + "That command is written as: .add [<object>]");
        }

        else if (msgLwr.startsWith(".remove")) {

            String[] args = msgLwr.trim().split(" ");

            if (!hasOp(usr, bot.getChannel("#denizen-devs")) && !hasVoice(usr, bot.getChannel("#denizen-dev")))
                bot.sendMessage(send, chatColor + "Sorry, " + senderNick + ", that's only for the Dev Team.");
            else if (args[1].startsWith("r")) {
                if (args.length > 2) {
                    if (!repoManager.hasRepository(args[2]))
                        bot.sendMessage(send, chatColor + "I am not tracking any projects by that name.");
                    else if (repoManager.removeRepository(args[2]))
                        bot.sendMessage(send, chatColor + "I am now no longer tracking " + args[2] + ".");
                    else
                        bot.sendMessage(send, chatColor + "Error while removing repository " + args[2] + "...");
                }
                else
                    bot.sendMessage(send, chatColor + "That command is written as: .remove repo [<project>]");
            }
            else if (args[1].startsWith("q")) {
                if (args.length > 2) {
                    try {
                        int number = Integer.valueOf(args[2]);
                        if (Utilities.hasQuote(number)) {
                            Utilities.removeQuote(number);
                            bot.sendMessage(send, chatColor + "Quote #" + number + " removed.");
                        }
                        else
                            bot.sendMessage(send, chatColor + "That quote doesn't exist.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        bot.sendMessage(send, chatColor + "That command is written as: .remove quote [<#>]");
                    }
                }
                else
                    bot.sendMessage(send, chatColor + "That command is written as: .remove quote [<#>]");
            }
            else
                bot.sendMessage(send, chatColor + "That command is written as: .remove [<object>]");
        }

        else if (msgLwr.startsWith(".info")) {
            String[] args = msgLwr.trim().split(" ");

            if (args[1].startsWith("r")) {
                if (args.length > 2) {
                    if (!repoManager.hasRepository(args[2]))
                        bot.sendMessage(send, chatColor + "I am not tracking any projects by that name.");
                    else {
                        Repository repo = repoManager.getRepository(args[2]);
                        bot.sendMessage(send, chatColor + repo.getFullName() + ": Delay("
                                + repo.getUpdateDelay() + ") Issues(" + repo.hasIssues() + ") Comments("
                                + repo.hasComments() + ") Pulls(" + repo.hasPulls() + ")");
                    }
                }
                else
                    bot.sendMessage(send, chatColor + "That command is written as: .info repo [<project>]");
            }
            else if (args[1].startsWith("u")) {
                if (args.length > 2) {
                    if (dUsers.containsKey(args[2])) {
                        dUser dusr2 = dUsers.get(args[2]);
                        bot.sendMessage(send, chatColor + args[2] + ": LastSeen(" + dusr2.getLastSeen() + ")");
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
                        bot.sendMessage(send, chatColor + number + ":" + infoText);
                    } catch (Exception e) {
                        bot.sendMessage(send, chatColor + "That command is written as: .info quote [<#>]");
                    }
                }
            }
            else
                bot.sendMessage(send, chatColor + "That command is written as: .info [<object>]");
        }

        else if (msgLwr.startsWith(".list") || msgLwr.startsWith(".count")) {
            String[] args = msgLwr.trim().split(" ");
            if (args.length < 2)
                bot.sendMessage(send, chatColor + "That command is written as: .list [<object>]");

            if (args[1].startsWith("r")) {
                Set<String> repos = repoManager.getRepositories();
                bot.sendMessage(send, chatColor + "I'm currently watching " + repos.size() + " repositories...");
                bot.sendMessage(send, chatColor + repos.toString());
            }
            else if (args[1].startsWith("q")) {
                bot.sendMessage(send, chatColor + "I currently have " + Utilities.getQuoteCount() + " quotes listed.");
            }
            else
                bot.sendMessage(send, chatColor + "That command is written as: .list [<object>]");
        }

        else if (msgLwr.startsWith(".save")) {
            String[] args = msgLwr.trim().split(" ");
            if (args.length < 2)
                bot.sendMessage(send, chatColor + "That command is written as: .save [<object>]");

            if (args[1].startsWith("r")) {
                try {
                    repoManager.saveAll();
                    bot.sendMessage(send, chatColor + "Successfully saved all repository information.");
                } catch (Exception e) {
                    bot.sendMessage(send, chatColor + "Error while saving repository information...");
                }
            }
            else if (args[1].startsWith("q")) {
                Utilities.saveQuotes();
                bot.sendMessage(send, chatColor + "Successfully saved all quotes.");
            }
            else if (args[1].startsWith("u")) {
                for (dUser dusr2 : dUsers.values()) {
                    if (debugMode) System.out.println("Saving dUser: " + dusr2.getNick() + "...");
                    try {
                        dusr2.saveAll();
                    } catch (Exception e) {
                        bot.sendMessage(send, Colors.RED + "ERROR. Failed to save user information: " + defaultColor + dusr2.getNick());
                        if (debugMode) e.printStackTrace();
                        else
                            System.out.println("An error has occured while using .save-all for user " + dusr2.getNick() + "... Turn on debug for more information.");
                        return;
                    }
                }
                bot.sendMessage(send, chatColor + "Successfully saved all user information.");
            }
        }

        else if (msgLwr.startsWith(".load")) {
            String[] args = msgLwr.trim().split(" ");
            if (args.length < 2)
                bot.sendMessage(send, chatColor + "That command is written as: .load [<object>]");

            if (args[1].startsWith("r")) {
                try {
                    repoManager.loadAll();
                    bot.sendMessage(send, chatColor + "Successfully loaded all repository information.");
                } catch (Exception e) {
                    bot.sendMessage(send, chatColor + "Error while loading repository information...");
                }
            }
            else if (args[1].startsWith("q")) {
                Utilities.loadQuotes();
                bot.sendMessage(send, chatColor + "Successfully loaded all quotes.");
            }
            else if (args[1].startsWith("u")) {
                for (dUser dusr2 : dUsers.values()) {
                    if (debugMode) System.out.println("Loading dUser information: " + dusr2.getNick() + "...");
                    try {
                        dusr2.loadAll();
                    } catch (Exception e) {
                        bot.sendMessage(send, Colors.RED + "ERROR. Failed to load user information: " + defaultColor + dusr2.getNick());
                        if (debugMode) e.printStackTrace();
                        else
                            System.out.println("An error has occured while using .load for user " + dusr2.getNick() + "... Turn on debug for more information.");
                        return;
                    }
                }
                bot.sendMessage(send, chatColor + "Successfully loaded all user information.");
            }
        }

        else if (msgLwr.startsWith(".math ")) {
            try {
                Double eval = new DoubleEvaluator().evaluate(msgLwr.substring(6));
                bot.sendMessage(chnl, defaultColor + "<math:" + msgLwr.substring(6).replace(" ", "") + ">"
                        + chatColor + " = " + eval);
            } catch (Exception e) {
                bot.sendMessage(chnl, chatColor + "Invalid math statement. Denizen will not parse that correctly.");
            }
        }

        else if (msgLwr.matches("\\.realmath .+")) {
            String input = msgLwr.substring(10).trim();
            String output = queryHandler.parseMath(input);

            if (output == null) {
                bot.sendMessage(send, chatColor + "Invalid math statement.");
            }
            else {
                bot.sendMessage(send, chatColor + input + " = " + output);
            }
        }

        else if (msgLwr.startsWith(".mcping")) {
            String[] args = msgLwr.split(" ");
            if (args.length < 2) {
                bot.sendMessage(send, address + chatColor + "Invalid server specified.");
                return;
            }
            int port = 25565;
            if (args[1].contains(":")) {
                try {
                    port = Integer.valueOf(args[1].split(":")[1].replace("/", ""));
                } catch (Exception e) {}
            }
            MinecraftServer server = new MinecraftServer(args[1].contains(":") ? args[1].split(":")[0] : args[1], port);
            if (server.getAddress().isUnresolved())
                bot.sendMessage(send, address + chatColor + "Invalid server specified.");
            else try {
                MinecraftServer.StatusResponse response = server.ping();
                MinecraftServer.Players players = response.getPlayers();
                String serverInfo = response.getDescription() + chatColor + " - " + response.getVersion().getName() +
                        chatColor + " - " + players.getOnline() + "/" + players.getMax();
                Matcher m = minecraftColor.matcher(serverInfo);
                while (m.find()) {
                    String color = parseColor("&" + m.group(1));
                    serverInfo = serverInfo.replace((char) 0xa7 + m.group(1), color != null ? color : "");
                }
                bot.sendMessage(send, address + chatColor + serverInfo.replace(String.valueOf((char) 0xc2), "")
                        .replaceAll("\\s+", " "));
            } catch (Exception e) {
                bot.sendMessage(send, address + chatColor + "Error contacting that server. (" + e.getMessage() + ")");
            }
        }

        else if (msgLwr.equals(".dev")) {
            if (usr.getNick().startsWith("Morph") && hasVoice(usr, bot.getChannel("#denizen-devs"))) {
                if (!devMode) {
                    devMode = true;
                    bot.sendMessage(send, chatColor + "Entering dev mode...");
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
                bot.sendMessage(send, chatColor + "Sorry, " + usr.getNick() + ", you're not allowed to do that.");
            }
        }

        dusr.checkMessages(chnl);

    }

    private static void reloadSites(boolean debug) {
        boolean original = debugMode;
        if (!debugMode)
            debugMode = debug;
        if (repoManager == null)
            repoManager = new RepositoryManager(github);
        debugMode = original;
    }

    private static ArrayList<String> findUserFiles() {
        return Utilities.findFileNamesByExtension(usersFolder.getPath(), ".yml");
    }

    private boolean hasVoice(User chatter, Channel channel) {
        if (channel.hasVoice(chatter))
            return true;
        return false;
    }

    private boolean hasOp(User chatter, Channel channel) {
        if (channel.isOp(chatter))
            return true;
        return false;
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
            if (symbol.equals("0"))
                return Colors.BLACK;
            else if (symbol.equals("1"))
                return Colors.DARK_BLUE;
            else if (symbol.equals("2"))
                return Colors.DARK_GREEN;
            else if (symbol.equals("3"))
                return Colors.TEAL;
            else if (symbol.equals("4"))
                return Colors.RED;
            else if (symbol.equals("5"))
                return Colors.PURPLE;
            else if (symbol.equals("6"))
                return Colors.YELLOW;
            else if (symbol.equals("7"))
                return Colors.LIGHT_GRAY;
            else if (symbol.equals("8"))
                return Colors.DARK_GRAY;
            else if (symbol.equals("9"))
                return Colors.BLUE;
            else if (symbol.equals("a"))
                return Colors.GREEN;
            else if (symbol.equals("b"))
                return Colors.CYAN;
            else if (symbol.equals("c"))
                return Colors.RED;
            else if (symbol.equals("d"))
                return Colors.MAGENTA;
            else if (symbol.equals("e"))
                return Colors.YELLOW;
            else if (symbol.equals("f"))
                return Colors.WHITE;
            else return null;
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
        private boolean status;
        private MessageList messages;

        public dUser(String nick) {
            if (nick == null || nick.equals("")) return;
            this.nick = nick;
            this.messages = new MessageList();
            setLastSeen("Existing");
            if (!new File(System.getProperty("user.dir") + "/users/" + nick.toLowerCase().replace('|', '_') + ".yml").exists()) {
                try {
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

        void setLastSeen(String lastSeen) {
            this.lastSeenTime = Calendar.getInstance().getTime();
            this.lastSeen = Utilities.uncapitalize(lastSeen);
        }

        void setLastSeenRaw(String lastSeen) {
            this.lastSeen = lastSeen;
        }

        void setStatus(boolean status) {
            this.status = status;
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
            if (getNick().equals("spazzmatic")) {
                data.put("password", System.getProperty("spazz.password"));
                data.put("bitly", System.getProperty("spazz.bitly"));
                data.put("dev-mode", devMode);
                data.put("wolfram", queryHandler.WOLFRAM_KEY);
                data.put("github", System.getProperty("spazz.github"));
            }

            FileWriter writer = new FileWriter(usersFolder + "/" + getNick().toLowerCase().replace('|', '_') + ".yml");
            writer.write(yaml.dump(data));
            writer.close();
        }

        @SuppressWarnings("unchecked")
        void loadMessages() throws Exception {
            LinkedHashMap map = null;
            Yaml yaml = new Yaml();
            File f = new File(usersFolder + "/" + getNick().toLowerCase().replace('|', '_') + ".yml");
            InputStream is = f.toURI().toURL().openStream();
            map = (LinkedHashMap) yaml.load(is);
            if (map.get("messages") instanceof HashMap<?, ?>) {
                for (Object msgObj : ((HashMap<Integer, Object>) map.get("messages")).values()) {
                    String msg;
                    if (msgObj instanceof byte[])
                        msg = new String((byte[]) msgObj);
                    else
                        msg = (String) msgObj;
                    String[] split = msg.split("_", 2);
                    addMessage(new Message(split[0], split[1]));
                }
            }
        }

        void loadLastSeen() throws Exception {
            LinkedHashMap map = null;
            Yaml yaml = new Yaml();
            File f = new File(usersFolder + "/" + getNick().toLowerCase().replace('|', '_') + ".yml");
            f.mkdirs();
            InputStream is = f.toURI().toURL().openStream();
            map = (LinkedHashMap) yaml.load(is);
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

            if (debugMode) System.out.println("Loaded user with lasttime: " + dateFormat.format(getLastSeenTime()));
        }

        void loadAll() throws Exception {
            if (debugMode) System.out.println("Loading messages for \"" + getNick() + "\"...");
            loadMessages();
            if (debugMode) System.out.println("Messages loaded: " + this.messages.getMessages());
            if (debugMode) System.out.println("Loading lastseen for \"" + getNick() + "\"...");
            loadLastSeen();
            if (debugMode) System.out.println("Lastseen loaded: " + this.lastSeen);
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

        public boolean getStatus() {
            return this.status;
        }

        public void checkMessages(Channel chnl) {
            if (this.messages.isEmpty() || chnl == null) return;
            bot.sendMessage(chnl, getNick() + ": " + chatColor + "You have messages waiting for you...");
            String lastNick = null;
            for (String msg : this.messages.getMessages().values()) {
                String[] split = msg.split("_", 2);
                if (!split[0].equals(lastNick)) {
                    lastNick = split[0];
                    bot.sendNotice(getNick(), defaultColor + lastNick + chatColor + ":");
                }
                bot.sendNotice(getNick(), chatColor + "  " + split[1]);
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

        public Message(String user, String message) {
            this.user = user;
            this.message = message;
        }

        public String getUser() {
            return this.user;
        }

        public String getMessage() {
            return this.message;
        }

    }

    private static Pattern tagInfo = Pattern.compile("<tr class=\"first\"><td>Name</td><td>(.*)</td></tr>\n\n"
            + "<tr class=\"second\"><td>Returns</td><td>(.*)</td></tr>\n\n"
            + "<tr class=\"second\"><td>Description</td><td>(.*)\n<br></td></tr>");

    private static ArrayList<HashMap<String, String[]>> findMonkeyTag(String input) throws Exception {
        ArrayList<HashMap<String, String[]>> ret = new ArrayList<HashMap<String, String[]>>();
        String monkeytag = Utilities.getStringFromUrl("http://mcmonkey4eva.dyndns.org/tags/"
                + URLEncoder.encode(input, "UTF-8")).replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
        Matcher m = tagInfo.matcher(monkeytag);
        while (m.find()) {
            HashMap<String, String[]> tag = new HashMap<String, String[]>();
            tag.put("name", m.group(1).split("<br>"));
            tag.put("returns", m.group(2).split("<br>"));
            tag.put("description", m.group(3).split("<br>"));
            ret.add(tag);
        }
        return ret;
    }
	/*
	private static ArrayList<HashMap<String, String[]>> findMonkeyCmd(String input) throws Exception {
	    ArrayList<HashMap<String, String[]>> ret = new ArrayList<HashMap<String, String[]>>();
	    String monkeycmd = Utilities.getStringFromUrl("http://mcmonkey4eva.dynsns.org/cmds/"
	            + URLEncoder.encode(input, "UTF-8")).replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
	}
	*/
}