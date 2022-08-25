package main.slash;

import main.commands.CommandHandler;
import main.commands.CommandType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
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
    HELP(CommandType.HELP),
    PING(CommandType.PING);

    public CommandData value;
    CommandDataHelper(CommandType command){
        switch (command){
            case PING:
                value = Commands.slash("ping", "Makes the bot reply with \"pong\"!");
                break;
            case START:
                value = Commands.slash("start", "Adds a given audio source to queue, and causes bot to join your voice channel.")
                        .addOption(OptionType.STRING, "source", "Can be a file, twitch stream, youtube link, or a youtube search if nothing matches." ,true);
                break;
            case STOP:
                value = Commands.slash("stop", "Stops audio playback.");
                break;
            case PLAY:
                value = Commands.slash("play", "Resumes audio playback.");
                break;
            case SKIP:
                value = Commands.slash("skip", "Skips the next track in the queue.");
                break;
            case SERVER_IP:
                value = Commands.slash("serverip", "Displays Alec's server ip for things like minecraft.");
                break;
            case ROLL:
                value = Commands.slash("roll", "Simulates rolling one or more dice of arbitrary sides.")
                        .addOption(OptionType.INTEGER, "sides", "The number of sides on the dice, default is six." ,  false)
                        .addOption(OptionType.INTEGER, "number", "Number of dice to roll, default is one", false);
                break;
            case ASK:
                value = Commands.slash("ask", "Ask the bot a question!")
                        .addOption(OptionType.STRING, "question", "The question to ask.", true);
                break;
            case FANCY_TEST:
                value = Commands.slash("fancytest", "Old debug command, useless but very fancy.");
                break;
            case PARSE_TEST:
                value = Commands.slash("parsetest", "Old debug command, useless.");
                break;
            case ZORK:
                value = Commands.slash("zork", "Play a text adventure game called Zork.")
                        .addOption(OptionType.STRING, "command", "The action you want to take", true);
                break;
            case DEFAULT:
                value = Commands.slash("default", "Old meme. Not to be confused with the default case value.");
                break;
            case COUNT:
                value = Commands.slash("count", "Count up to a number in an arbitrary base, spammy.")
                        .addOption(OptionType.INTEGER, "number", "The number to count up to.", true)
                        .addOption(OptionType.INTEGER, "base", "The base to count in, default is 10.", false);
                break;
            case INTERRUPT:
                value = Commands.slash("interrupt", "An old command attempting to stop !count from spamming endlessly, useless.");
                break;
            case LEAVE:
                value = Commands.slash("leave", "Makes the bot leave the voice channel it is in.");
                break;
            case CLEAR:
                value = Commands.slash("clear", "Clears the audio queue.");
                break;
            case BALANCE:
                value = Commands.slash("balance", "Displays your current dine-in dollar balance.");
                break;
            case DEBUG_ADD:
                value = Commands.slash("debugadd", "Debug command for adding dine-in dollars.");
                break;
            case DEBUG_SET:
                value = Commands.slash("debugset", "Debug command for setting dine-in dollars.");
                break;
            case BET:
                value = Commands.slash("bet", "Roll a dice and hopefully earn some dine-in dollars.")
                        .addOption(OptionType.INTEGER, "amount", "The amount of dine-in dollars to bet.", true);
                break;
            case BLACKJACK:

                value = Commands.slash("blackjack", "Starts a game of blackjack against the bot.")

                        .addSubcommands(Arrays.asList(
                                new SubcommandData("bet", "Start a new game with given bet.")
                                    .addOption(OptionType.INTEGER, "bet", "The amount to bet.", true),
                                new SubcommandData("hit", "Draw another card."),
                                new SubcommandData("stand", "Don't draw a card.")
                                ));
                break;
            case MONEY_PLS:
                value = Commands.slash("moneypls", "Gives you a dine-in dollar upto a max of five.");
                break;
            case RESERVE:
                value = Commands.slash("reserve", "Creates a private voice channel owned by you.")
                        .addOption(OptionType.STRING, "name", "The name of the voice channel.", true)
                        .addOption(OptionType.INTEGER, "size", "The number of slots in the voice channel")
                        .addOption(OptionType.USER, "tags", "Dont remember what this one does, optional.", false);
                break;
            case COURSES:
                value = Commands.slash("courses", "View a list of all available courses to add. Not to be confused with !class.");
                break;
            case CLASS:
                value = Commands.slash("class", "Various utilities to add/drop from class channels.")
                        .addSubcommands(Arrays.asList(
                                new SubcommandData("sudo_create", "Create a new class. Admins only.")
                                        .addOption(OptionType.STRING, "channel_id", "The numeric id of the class channel.", true),
                                new SubcommandData("sudo_remove", "Remove a class. Admins only.")
                                        .addOption(OptionType.STRING, "channel_id", "The numeric id of the class channel.", true),
                                new SubcommandData("add", "Add yourself to a class channel.")
                                        .addOption(OptionType.STRING, "name", "The name of the class to add.", true),
                                new SubcommandData("drop", "Remove yourself from a class channel.")
                                        .addOption(OptionType.STRING, "name", "The name of the class to drop.", true)

                        ));
                break;
            case KARMA:
                value = Commands.slash("karma", "Display stats about your karma")
                        .addOption(OptionType.STRING, "id", "For debugging.", false);
                break;
            case HIGHSCORES:
                value = Commands.slash("highscores", "Displays the current karma highscores.");
                break;
            case LOWSCORES:
                value = Commands.slash("lowscores", "Ranks users by the amount of downvotes they have.");
                break;
            case VERYHIGHSCORES:
                value = Commands.slash("veryhighscores", "Ranks users by the amount of upvotes they have.");
                break;
            case MEANSCORES:
                value = Commands.slash("meanscores", "Ranks users by lowest karma first, uncapped.");
                break;
            case JOB:
                value = Commands.slash("job", "The old job search utility, broken.");
                break;
            case HELP:
                value = Commands.slash("help", "The original help menu for the bot, very old and outdated.");
                break;
        }
    }
}
