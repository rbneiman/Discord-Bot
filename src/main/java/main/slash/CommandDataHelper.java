package main.slash;

import main.commands.CommandType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.Arrays;

public enum CommandDataHelper {
    START(CommandType.START),
    STOP(CommandType.STOP),
    PLAY(CommandType.PLAY),
    SKIP(CommandType.SKIP),
    SERVER_IP(CommandType.SERVER_IP),
    ROLL(CommandType.ROLL),
    ASK(CommandType.ASK),
    FANCY_TEST(CommandType.FANCY_TEST),
    PARSE_TEST(CommandType.PARSE_TEST),
    ZORK(CommandType.ZORK),
    DEFAULT(CommandType.DEFAULT),
    COUNT(CommandType.COUNT),
    INTERRUPT(CommandType.INTERRUPT),
    LEAVE(CommandType.LEAVE),
    CLEAR(CommandType.CLEAR),
    BALANCE(CommandType.BALANCE),
    DEBUG_ADD(CommandType.DEBUG_ADD),
    DEBUG_SET(CommandType.DEBUG_SET),
    BET(CommandType.BET),
    BLACKJACK(CommandType.BLACKJACK),
    MONEY_PLS(CommandType.MONEY_PLS),
    RESERVE(CommandType.RESERVE),
    COURSES(CommandType.COURSES),
    CLASS(CommandType.CLASS),
    //class sub-commands
//    CLASS_ADD(CommandType.CLASS_ADD),
//    CLASS_DROP(CommandType.CLASS_DROP),
//    CLASS_LIST(CommandType.CLASS_LIST),
//    CLASS_SUDO_CREATE(CommandType.CLASS_SUDO_CREATE),
//    CLASS_SUDO_REMOVE(CommandType.CLASS_SUDO_REMOVE),
    KARMA(CommandType.KARMA),
    HIGHSCORES(CommandType.HIGHSCORES),
    LOWSCORES(CommandType.LOWSCORES),
    VERYHIGHSCORES(CommandType.VERYHIGHSCORES),
    MEANSCORES(CommandType.MEANSCORES),
    JOB(CommandType.JOB),
    HELP(CommandType.HELP);

    public CommandData value;
    CommandDataHelper(CommandType command){
        switch (command){
            case START:
                value = new CommandData("start", "Adds a given audio source to queue, and causes bot to join your voice channel.")
                        .addOption(OptionType.STRING, "source", "Can be a file, twitch stream, youtube link, or a youtube search if nothing matches." ,true);
                break;
            case STOP:
                value = new CommandData("stop", "Stops audio playback.");
                break;
            case PLAY:
                value = new CommandData("play", "Resumes audio playback.");
                break;
            case SKIP:
                value = new CommandData("skip", "Skips the next track in the queue.");
                break;
            case SERVER_IP:
                value = new CommandData("serverip", "Displays Alec's server ip for things like minecraft.");
                break;
            case ROLL:
                value = new CommandData("roll", "Simulates rolling one or more dice of arbitrary sides.")
                        .addOption(OptionType.INTEGER, "sides", "The number of sides on the dice, default is six." ,  false)
                        .addOption(OptionType.INTEGER, "number", "Number of dice to roll, default is one", false);
                break;
            case ASK:
                value = new CommandData("ask", "Ask the bot a question!")
                        .addOption(OptionType.STRING, "question", "The question to ask.", true);
                break;
            case FANCY_TEST:
                value = new CommandData("fancytest", "Old debug command, useless.");
                break;
            case PARSE_TEST:
                value = new CommandData("parsetest", "Old debug command, useless.");
                break;
            case ZORK:
                value = new CommandData("zork", "Play a text adventure game called Zork.")
                        .addOption(OptionType.STRING, "command", "The action you want to take", true);
                break;
            case DEFAULT:
                value = new CommandData("default", "Old meme. Not to be confused with the default case value.");
                break;
            case COUNT:
                value = new CommandData("count", "Count up to a number in an arbitrary base, spammy.")
                        .addOption(OptionType.INTEGER, "number", "The number to count up to.", true)
                        .addOption(OptionType.INTEGER, "base", "The base to count in, default is 10.", false);
                break;
            case INTERRUPT:
                value = new CommandData("interrupt", "An old command attempting to stop !count from spamming endlessly, useless.");
                break;
            case LEAVE:
                value = new CommandData("leave", "Makes the bot leave the voice channel it is in.");
                break;
            case CLEAR:
                value = new CommandData("clear", "Clears the audio queue.");
                break;
            case BALANCE:
                value = new CommandData("balance", "Displays your current dine-in dollar balance.");
                break;
            case DEBUG_ADD:
                value = new CommandData("debugadd", "Debug command for adding dine-in dollars.");
                break;
            case DEBUG_SET:
                value = new CommandData("debugset", "Debug command for setting dine-in dollars.");
                break;
            case BET:
                value = new CommandData("bet", "Roll a dice and hopefully earn some dine-in dollars.")
                        .addOption(OptionType.INTEGER, "amount", "The amount of dine-in dollars to bet.", true);
                break;
            case BLACKJACK:

                value = new CommandData("blackjack", "Starts a game of blackjack against the bot.")

                        .addSubcommands(Arrays.asList(
                                new SubcommandData("bet", "Start a new game with given bet.")
                                    .addOption(OptionType.INTEGER, "bet", "The amount to bet.", true),
                                new SubcommandData("hit", "Draw another card."),
                                new SubcommandData("stand", "Don't draw a card.")
                                ));
                break;
            case MONEY_PLS:
                value = new CommandData("moneypls", "Gives you a dine-in dollar upto a max of five.");
                break;
            case RESERVE:
                value = new CommandData("reserve", "Creates a private voice channel owned by you.")
                        .addOption(OptionType.STRING, "name", "The name of the voice channel.", true)
                        .addOption(OptionType.INTEGER, "size", "The number of slots in the voice channel")
                        .addOption(OptionType.USER, "tags", "Dont remember what this one does, optional.", false);
                break;
            case COURSES:
                value = new CommandData("courses", "View a list of all available courses to add. Not to be confused with !class.");
                break;
            case CLASS:
                value = new CommandData("class", "Various utilities to add/drop from class channels.")
//                        .addSubcommands(Arrays.asList(
//                                new SubcommandData("add", "Draw another card.")
//                                    .addOption(OptionType.STRING, "name", "The name of the class to add.", true),
//                                new SubcommandData("drop", "Don't draw a card.")
//                                        .addOption(OptionType.STRING, "name", "The name of the class to drop.", true)
//
//                        ))
                        .addSubcommandGroups(Arrays.asList(
                                new SubcommandGroupData("sudo", "Create/Remove a class. Admins only.")
                                        .addSubcommands(Arrays.asList(
                                                new SubcommandData("create", "Create a class.")
                                                        .addOption(OptionType.INTEGER, "channel_id", "The numeric id of the class channel."),
                                                new SubcommandData("remove", "Remove a class.")
                                                        .addOption(OptionType.INTEGER, "channel_id", "The numeric id of the class channel.")
                                        ))
                        ));
                break;
            case KARMA:
                value = new CommandData("karma", "Display stats about your karma");
                break;
            case HIGHSCORES:
                value = new CommandData("highscores", "Displays the current karma highscores.");
                break;
            case LOWSCORES:
                value = new CommandData("lowscores", "Ranks users by the amount of downvotes they have.");
                break;
            case VERYHIGHSCORES:
                value = new CommandData("veryhighscores", "Ranks users by the amount of upvotes they have.");
                break;
            case MEANSCORES:
                value = new CommandData("meanscores", "Ranks users by lowest karma first, uncapped.");
                break;
            case JOB:
                value = new CommandData("job", "The old job search utility, broken.");
                break;
            case HELP:
                value = new CommandData("help", "The original help menu for the bot, very old and outdated.");
                break;
        }
    }
}
