package main.gpt;

import main.BotListener;
import net.dv8tion.jda.api.entities.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class TrainingDump {
    private static final Logger LOGGER = LogManager.getLogger(TrainingDump.class);

    private static void  saveString(String string){
        try {
            File file = new File("dump.txt");

            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(string);
            bufferedWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static CompletableFuture<List<Message>> iterateChannel(TextChannel channel, int limit){
        CompletableFuture<List<Message>> f = channel.getIterableHistory().takeAsync(limit);
        return f;
    }

    public static void dump(GuildChannel channel){
        LOGGER.debug("Getting messages for training dump...");
        List<Message> test = iterateChannel((TextChannel) channel, 20000).join();
        final String[] dumpStr = {""};

        test.forEach((message -> {
            dumpStr[0] += channel.getGuild().getMember(message.getAuthor()).getEffectiveName() + ": " + message.getContentRaw() + "<|endoftext|>";
        }));
        saveString(dumpStr[0]);
        LOGGER.debug("Done!");
        return;
    }

    public static void dump(Category category){
        final String[] dumpStr = {""};
        final Guild guild = category.getGuild();
        final ArrayList<CompletableFuture<List<Message>>> channelFutures = new ArrayList<>();
        for(GuildChannel channel : category.getChannels()){
            channelFutures.add(iterateChannel((TextChannel) channel, 10000));
        }
        final ArrayList<List<Message>> channelOuts = new ArrayList<>();
        LOGGER.debug("Getting messages for training dump...");
        try {
            for (CompletableFuture<List<Message>> f : channelFutures){

                    f.get().forEach((message -> {
                        Member member = message.getMember();
                        if(member != null){
                            String name = member.getEffectiveName();
                            dumpStr[0] += name + ": " + message.getContentRaw() + "<|endoftext|>";
                        }

                    }));

            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        saveString(dumpStr[0]);
        LOGGER.debug("Done!");
        return;
    }
}
