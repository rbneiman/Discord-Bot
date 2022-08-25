package main.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;

public interface CommandAction {

    String doAction(CommandType type, TextChannel channel, Member member, ArrayList<String> words);

}
