package main.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import main.BotListener;
import main.MiscUtils;
import main.YtubeList;
import main.commands.CommandAction;
import main.commands.CommandType;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class AudioCommandHandler implements CommandAction {
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final Map<String, Map.Entry<AudioPlayer, TrackScheduler>> players = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(AudioCommandHandler.class);

    public AudioCommandHandler(){
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    @Override
    public String doAction(CommandType type, TextChannel channel, Member member, ArrayList<String> words){
        Guild guild = channel.getGuild();
        AudioPlayer player;
        TrackScheduler trackScheduler;
        String out = "";
        switch(type){
            case START:
                String[] search;
                if(words.get(1).contains("facebook.com")) {
                    search = new String[] {MiscUtils.getFaceVid(words.get(1))};
                }
                else if(!words.get(1).contains("https:") && !words.get(1).contains("C:")) {
                    search = new String[] {YtubeList.doSearch(words.get(1))};
                    channel.sendMessage(search[0]).queue();
                }
                else if(words.get(1).contains("&list") && (words.get(1).contains("youtube") || words.get(1).contains("youtu.be"))){
                    search = YtubeList.getPlayList(words.get(1));
                }
                else {
                    search = new String[] {words.get(1)};
                }

                if(search.length>1)
                    out += "Added playlist of " + search.length + " videos";

                if(!players.containsKey(guild.getId())||(!guild.getAudioManager().isConnected())) {

                    AudioChannel voicechannel = Objects.requireNonNull(member.getVoiceState()).getChannel();
                    AudioSourceManagers.registerRemoteSources(playerManager);
                    player = playerManager.createPlayer();
                    trackScheduler = new TrackScheduler(player);
                    player.addListener(trackScheduler);
                    AudioManager manager = guild.getAudioManager();
                    manager.setSendingHandler(new BotAudio(player));
                    manager.openAudioConnection(voicechannel);
                    players.put(guild.getId(), new AbstractMap.SimpleEntry<>(player,trackScheduler));

                    if(search.length>1)
                        trackScheduler.playlistSize = search.length;

                    for(String track : search) {
                        playerManager.loadItem(track, new BotAudioResultHandler(channel, trackScheduler));
                    }

                }
                else {
                    trackScheduler=players.get(guild.getId()).getValue();

                    if(search.length>1)
                        trackScheduler.playlistSize = search.length;
                    LOGGER.debug(words.get(1));
                    for(String track : search) {
                        playerManager.loadItemOrdered(guild, track, new BotAudioResultHandler(channel, trackScheduler){
                            @Override
                            public void trackLoaded(AudioTrack track) {
                                track.setUserData(channel);
                                trackScheduler.queue(track);
                                LOGGER.debug("queue");
                            }
                        });
                    }
                }
                break;
            case STOP:
                player = players.get(guild.getId()).getKey();
                player.setPaused(true);
                break;
            case PLAY:
                if(players.containsKey(guild.getId())){
                    player = players.get(guild.getId()).getKey();
                    player.setPaused(false);
                }else{
                    out += "No audio to play. Did you mean \"!start\" ?";
                }
                break;
            case SKIP:
                trackScheduler=players.get(guild.getId()).getValue();
                if(!trackScheduler.skip()) {
                    out += "Nothing in queue!";
                }
                break;
            case LEAVE:
                if(guild.getAudioManager().isConnected()) {
                    guild.getAudioManager().closeAudioConnection();
                    trackScheduler=players.get(guild.getId()).getValue();
                    trackScheduler.clearTracks();
                    out += "Leaving voice channel";
                }
                break;
            case CLEAR:
                if(guild.getAudioManager().isConnected()) {
                    trackScheduler=players.get(guild.getId()).getValue();
                    trackScheduler.clearTracks();
                    out += "Queue cleared";
                }
                break;
            default:
                LOGGER.error("given bad command type: " + type);
        }
        return out;
    }
}
