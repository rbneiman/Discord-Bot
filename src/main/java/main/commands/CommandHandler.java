package main.commands;

import main.BotListener;
import main.ConfigStorage;
import main.MiscUtils;
import main.ZorkManager;
import main.audio.AudioCommandHandler;
import main.audio.TrackScheduler;
import main.cardgames.GameHandler;
import main.slash.SlashCommandHelper;
import main.valuestorage.MemberInfo;
import main.valuestorage.UserVals;
import main.valuestorage.ValueStorage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CommandHandler {
    static HashMap<CommandType, CommandAction> handlers = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(CommandHandler.class);
    static void registerAction(CommandType type, CommandAction handler){
        handlers.put(type, handler);
    }

    public static void registerHandlers(JDA jda, UserVals userVals){
        SlashCommandHelper.createTestCommands(jda);

        registerAction(CommandType.PING, (type, channel, member, words)->{
            LOGGER.fatal("Test");
            LOGGER.error("Test");
            LOGGER.warn("Test");
            LOGGER.info("Test");
            LOGGER.debug("Test");
            LOGGER.trace("Test");

            return "Pong!";
        });
        AudioCommandHandler audioHandler = new AudioCommandHandler();
        registerAction(CommandType.START, audioHandler);
        registerAction(CommandType.STOP, audioHandler);
        registerAction(CommandType.PLAY, audioHandler);
        registerAction(CommandType.SKIP, audioHandler);
        registerAction(CommandType.SERVER_IP, (type, channel, member, words)-> "Server ip is: alecserv.com");
        registerAction(CommandType.ROLL, (type, channel, member, words)->{
            long time = System.currentTimeMillis();
            Random rand = new Random();
            String tempS="Rolled ";
            int[] rolled;
            time=System.currentTimeMillis();
            rand=new Random(time);
            int toRoll=((int) Integer.parseInt(words.get(1)));
            try {
                rolled=new int[Integer.parseInt(words.get(2))];
            }
            catch(NumberFormatException e) {
                rolled=new int[1];
            }
            for(int i=0;i<rolled.length;i++) {
                rolled[i]=rand.nextInt(toRoll)+1;
                tempS+=rolled[i]+", ";
            }
            return tempS.substring(0, tempS.length()-2);
        });
        registerAction(CommandType.ASK, (type, channel, member, words)->{
            return MiscUtils.wolframRead(words.get(1));
        });
        registerAction(CommandType.FANCY_TEST, new CommandAction() {
            private final EmbedBuilder builder = new EmbedBuilder()
                    .setAuthor("Alec Bot", null, "https://i.imgur.com/TT1jeRo.png")
                    .setColor(2321802)
                    .addField(new MessageEmbed.Field("Jobs", "[**1.**](http://google.com)",false));
            @Override
            public String doAction(CommandType type, TextChannel channel, Member member, ArrayList<String> words) {
                channel.sendMessageEmbeds(builder.build()).queue();
                return "Wow so fancy.";
            }
        });
        registerAction(CommandType.PARSE_TEST, (type, channel, member, words)->{
            StringBuilder testStr = new StringBuilder();
            for(int i=0;words.get(i)!=null&&i+1<words.size();i++) {
                testStr.append(words.get(i)).append("\n");
            }
            return testStr.toString();
        });
        registerAction(CommandType.ZORK, new CommandAction() {
            private final ZorkManager zorkManager = new ZorkManager();
            @Override
            public String doAction(CommandType type, TextChannel channel, Member member, ArrayList<String> words) {
                if(words.size() < 2) return zorkManager.input(member, null);
                else return zorkManager.input(member, words.get(1));
            }
        });
        registerAction(CommandType.DEFAULT, (type, channel, member, words)->{
            StringBuilder out = new StringBuilder();
            for (int i=0; i<10; i++) {
                out.append("```").append(MiscUtils.asciiArchive(i)).append("```");
            }
            return out.toString();
        });
        CommandAction counterAction = new CommandAction() {
            ScheduledFuture<?> future;
            @Override
            public String doAction(CommandType type, TextChannel channel, Member member, ArrayList<String> words) {
                if(type == CommandType.COUNT){
                    if(member.getIdLong() != ConfigStorage.developerID) return "Not allowed! Sorry.";
                    int radix = 10;
                    try {
                        if(words.size() > 2) {
                            switch(words.get(2)) {
                                case("bin"):
                                    radix = 2;
                                    break;
                                case("hex"):
                                    radix = 16;
                                    break;
                                default:
                                    radix = Integer.parseInt(words.get(2));
                            }
                        }
                    }
                    catch(NumberFormatException e) {
                        radix = 10;
                    }
                    if(Integer.parseInt(words.get(1))>=0) {
                        for (int i=1;i<=Integer.parseInt(words.get(1));i++) {
                            future = channel.sendMessage(Integer.toString(i,radix)).submitAfter(i, TimeUnit.SECONDS);
                        }
                    }
                    else {
                        for (int i=-1;i>=Integer.parseInt(words.get(1));i--) {
                            future = channel.sendMessage(Integer.toString(i,radix)).submitAfter(i, TimeUnit.SECONDS);
                        }
                    }
                    return "Counting started.";
                }else if(type == CommandType.INTERRUPT){
                    if(future!=null) {
                        future.cancel(true);
                    }
                    return "Cancelled counting.";
                }
                return "Bad command?";
            }
        };
        registerAction(CommandType.COUNT, counterAction);
        registerAction(CommandType.INTERRUPT, counterAction);
        registerAction(CommandType.LEAVE, audioHandler);
        registerAction(CommandType.CLEAR, audioHandler);
        registerAction(CommandType.BALANCE, (type, channel, member, words)->{
            Guild guild = channel.getGuild();
            long id = member.getIdLong();
            MemberInfo memberInfo;
            String name;
            if(id == ConfigStorage.developerID && words.size() > 1) {
                memberInfo = ValueStorage.getMemberInfo(guild.getMemberById(Long.parseLong(words.get(1))));
                name = guild.getMemberById(Long.parseLong(words.get(1))).getEffectiveName();
            }
            else{
                memberInfo = ValueStorage.getMemberInfo(member);
                name = member.getEffectiveName();
            }


            return name + " has " + memberInfo.getBalance() + " dine-in dollars!";
        });
        registerAction(CommandType.DEBUG_ADD, (type, channel, member, words)->{
            Guild guild = channel.getGuild();
            long id = member.getIdLong();
            if(id == ConfigStorage.developerID) {
                MemberInfo memberInfo;
                if(words.size() < 3)
                    memberInfo = ValueStorage.getMemberInfo(member);
                else
                    memberInfo = ValueStorage.getMemberInfo(guild.getMemberById(Long.parseLong(words.get(2))));
                memberInfo.setBalance(memberInfo.getBalance() + Integer.parseInt(words.get(1)));
                memberInfo.update();
                return "Added " + words.get(1) + " to balance. New balance: " + memberInfo.getBalance();
            }
            else {
                return member.getEffectiveName() + " is not in the sudoers file. This incident will be reported.";
            }
        });
        registerAction(CommandType.DEBUG_SET, (type, channel, member, words)->{
            Guild guild = channel.getGuild();
            long id = member.getIdLong();
            if(id == ConfigStorage.developerID) {
                MemberInfo memberInfo;
                if(words.size() > 2)
                    memberInfo = ValueStorage.getMemberInfo(member);
                else
                    memberInfo = ValueStorage.getMemberInfo(guild.getMemberById(Long.parseLong(words.get(2))));

                memberInfo.setBalance(Integer.parseInt(words.get(1)));
                memberInfo.update();
                return "Balance set";
            }
            else {
                return member.getEffectiveName() + " is not in the sudoers file. This incident will be reported.";
            }
        });
        registerAction(CommandType.BET, (type, channel, member, words)->{
            String out = null;
            if(words.size() < 2) {
                return "Need an argument!";
            }

            MemberInfo memberInfo = ValueStorage.getMemberInfo(member);

            int rolled = -1;
            long time=System.currentTimeMillis();
            Random rand = new Random(time);
            try {
                int bet = Integer.parseInt(words.get(1));

                if(bet<=memberInfo.getBalance()&&bet>0) {
                    rolled= rand.nextInt(bet*2+1);
                    if(rolled>bet) { out = member.getEffectiveName() + " has earned " + (rolled-bet) + " dine-in dollars!";}
                    if(rolled<bet) { out = member.getEffectiveName() + " has lost " + (bet-rolled) + " dine-in dollars!";}
                    if(rolled==bet) { out = member.getEffectiveName() + " has broke even!";}
                    memberInfo.setBalance(memberInfo.getBalance() + (rolled-bet));
                    memberInfo.update();
                }else if(bet>memberInfo.getBalance()&&bet>0){out = "Not enough dine-in dollars to bet!";}
                else {out = "No negative bets!";}
            }
            catch(NumberFormatException e) {e.printStackTrace();}
            return out;
        });
        registerAction(CommandType.BLACKJACK, (type, channel, member, words)->{
            String out;
            long id = member.getIdLong();
            if(words.size() < 2) {
                return "Need an argument!";
            }

            MemberInfo memberInfo = ValueStorage.getMemberInfo(member);

            int bet;
            String action;
            try {
                bet = Integer.parseInt(words.get(1));
                action = "hit";
            }
            catch(NumberFormatException e) {
                bet = 0;
                action = words.get(1);
            }

            if(bet<0) {
                return "No negative bets!";
            }
            else if((bet>memberInfo.getBalance())){
                return "Not enough dine-in dollars to bet!";
            }
            else {
                out = GameHandler.blackJackHandler(member.getEffectiveName(), bet, action, id);
            }
            int payOut = GameHandler.recievePayOut(id);
            if(payOut!=0) {memberInfo.setBalance(memberInfo.getBalance() + payOut);}

            memberInfo.update();
            return out;
        });
        registerAction(CommandType.MONEY_PLS, (type, channel, member, words)->{
            MemberInfo memberInfo = ValueStorage.getMemberInfo(member);

            if(memberInfo.getBalance()<5) {
                memberInfo.setBalance(memberInfo.getBalance() + 1);
                memberInfo.update();
                return "Dine-in dollar added";
            }
            return null;
        });
        registerAction(CommandType.RESERVE, userVals);
        registerAction(CommandType.COURSES, userVals);
        registerAction(CommandType.CLASS, userVals);
        registerAction(CommandType.KARMA, userVals);
        registerAction(CommandType.HIGHSCORES, userVals);
        registerAction(CommandType.LOWSCORES, userVals);
        registerAction(CommandType.VERYHIGHSCORES, userVals);
        registerAction(CommandType.MEANSCORES, userVals);
        registerAction(CommandType.HELP, (type, channel, member, words)->
                "```diff\n"
                + "𝐀𝐯𝐚𝐢𝐥𝐚𝐛𝐥𝐞 𝐂𝐨𝐦𝐦𝐚𝐧𝐝𝐬:\n"
                + "- NOTE: inputs containing spaces need to be entirely surrounded by double quotes\n"
                + "\n!start <filepath, or youtube link> - start playing stuff, or add audio to queue\n"
                + "\n!play/!stop                        - resume/pause playback\n"
                + "\n!skip                              - skips to next audio in queue\n"
                + "\n!leave                             - leaves voice channel and clears queue\n"
                + "\n!clear                         	- clears the current queue\n"
                + "\n!roll <sides> <number(optional)>   - rolls a given dice a specified number of times\n"
                + "\n!zork <input>                      - play some zork with the bot!\n"
                + "\n!ask  <input>                      - ask the bot a question!\n"
                + "\n!serverip                          - get my current minecraft server ip\n"
                + "\n!balance                           - retreve your current dine-in dollar balance\n"
                + "\n!bet <number>                      - gamble your bet for more dine-in dollars\n"
                + "\n!blackjack <'hit'/'stand'> <bet>   - play blackjack with the bot\n"
                + "\n!moneyPls                          - gives you one dine-in dollar up to five\n"
                + "\n!reserve <name> <room size> <tags> - create a private voice channel with a given number of slots and access for specified users\n"
                + "\n!class <add/drop> <course names>   - add or remove yourself from specified class channels\n"
                + "\n!courses                           - list all classes that have existing channels\n"
                + "\n+ A couple secret commands\n"
                + "```");
    }

    private static ArrayList<String> parseMessage(String message){
        int commandBegin = message.lastIndexOf('!');
        ArrayList<String> out = new ArrayList<>();
        if (commandBegin == -1){
            return out;
        }

        String commandText = message.substring(commandBegin);
        String[] commandTextWords = commandText.split(" ");
        out.addAll(Arrays.asList(commandTextWords));
        return out;
    }

    public static void handleMessageReceived(MessageReceivedEvent event){
        ArrayList<String> commandList = parseMessage(event.getMessage().getContentStripped());
        if(commandList.size() == 0)
            return;

        CommandType commandType = CommandType.fromString(commandList.get(0));
        if(commandType == CommandType.BAD)
            return;
        if(!event.getMessage().isFromGuild()){
            handleDM(event, commandList, commandType);
            return;
        }

        TextChannel channel = (TextChannel) event.getChannel();
        Member member = event.getMember();
        if(member == null)
            return;
        if(!handlers.containsKey(commandType)){
            channel.sendMessage("Unrecognized command!").queue();
            return;
        }
        String response = handlers.get(commandType).doAction(commandType, channel, member, commandList);
        if(response != null && !response.isEmpty()){
            channel.sendMessage(response).queue();
        }
    }

    private static void handleDM(MessageReceivedEvent event, ArrayList<String> commandList, CommandType commandType){
        
    }

    public static void handleSlashCommand(@NotNull SlashCommandInteractionEvent event){
        String[] sp = event.getCommandPath().split("/");
        ArrayList<String> commandList = new ArrayList<>(Arrays.asList(sp));
        for(OptionMapping option : event.getOptions()){
            commandList.add(option.getAsString());
        }

        CommandType commandType = CommandType.fromString("!" + commandList.get(0));
        if(commandType == CommandType.BAD){
            event.reply("Bad command?").queue();
            return;
        }


        TextChannel channel = (TextChannel) event.getChannel();
        Member member = event.getMember();
        if(member == null)
            return;

        if(!handlers.containsKey(commandType)){
            channel.sendMessage("Unrecognized command!").queue();
            return;
        }
        String response = handlers.get(commandType).doAction(commandType, channel, member, commandList);
        if(response != null && !response.isEmpty()){
            event.reply(response).queue();
        }else{
            event.reply("Done").queue();
        }
    }
}
