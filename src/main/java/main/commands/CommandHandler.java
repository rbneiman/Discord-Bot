package main.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandHandler {

    private static ArrayList<String> parseMessage(String message){
        int commandBegin = message.lastIndexOf('!');
        ArrayList<String> out = new ArrayList<>();
        if (commandBegin == -1){
            return out;
        }

        String commandText = message.substring(commandBegin);
        String[] commandTextWords = message.split(" ");
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


        Guild guild = event.getGuild();
        TextChannel channel = (TextChannel) event.getChannel();
        Member member = event.getMember();
        if(member == null)
            return;
        Long memberId = member.getIdLong();


    }

    private static void handleDM(MessageReceivedEvent event, ArrayList<String> commandList, CommandType commandType){

    }

    private static void handleCommand(){

    }
}
