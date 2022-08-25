package main.slash;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class SlashCommandHelper {

    /** Test method for creating some guild specific slash commands.
     *
     */
    public static void createTestCommands(JDA jda){
        jda.updateCommands()
                .addCommands(
                    CommandDataHelper.PING.value,
                    CommandDataHelper.START.value,
                    CommandDataHelper.STOP.value,
                    CommandDataHelper.PLAY.value,
                    CommandDataHelper.SKIP.value,
                    CommandDataHelper.SERVER_IP.value,
                    CommandDataHelper.ROLL.value,
                    CommandDataHelper.ASK.value,
                    CommandDataHelper.ZORK.value,
                    CommandDataHelper.DEFAULT.value,
                    CommandDataHelper.COUNT.value,
                    CommandDataHelper.INTERRUPT.value,
                    CommandDataHelper.LEAVE.value,
                    CommandDataHelper.CLEAR.value,
                    CommandDataHelper.BALANCE.value,
                    CommandDataHelper.BET.value,
                    CommandDataHelper.BLACKJACK.value,
                    CommandDataHelper.MONEY_PLS.value,
                    CommandDataHelper.RESERVE.value,
                    CommandDataHelper.COURSES.value,
                    CommandDataHelper.CLASS.value,
                    CommandDataHelper.KARMA.value,
                    CommandDataHelper.HIGHSCORES.value,
                    CommandDataHelper.LOWSCORES.value,
                    CommandDataHelper.VERYHIGHSCORES.value,
                    CommandDataHelper.MEANSCORES.value,
                    CommandDataHelper.HELP.value
        ).queue();
    }

    /** Initializes the global slash commands. Only needs to be used once when slash commands are changed.
     *
     * @param jda The JDA instance.
     */
    public static void createGlobalCommands(JDA jda){

    }
}
