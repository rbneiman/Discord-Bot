package main.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.MessageChannel;

public class BotAudioResultHandler implements AudioLoadResultHandler {

	private MessageChannel channel;
	private TrackScheduler trackScheduler;
	

	public BotAudioResultHandler(MessageChannel channel, TrackScheduler trackScheduler) {
		super();
		this.channel = channel;
		this.trackScheduler = trackScheduler;
	}
	
	@Override
	  public void trackLoaded(AudioTrack track) {
		track.setUserData(channel);
	    trackScheduler.queue(track);
	  }
	  	
	  @Override
	  public void playlistLoaded(AudioPlaylist playlist) {
	    for (AudioTrack track : playlist.getTracks()) {
	      trackScheduler.queue(track);
	    }
	  }

	  @Override
	  public void noMatches() {
		  channel.sendMessage("No matches found for search").queue();
	    // Notify the user that we've got nothing
	  }

	  @Override
	  public void loadFailed(FriendlyException throwable) {
	    // Notify the user that everything exploded
		  channel.sendMessage("Failed to load audio").queue();
		  System.out.println(throwable.toString());
	  }

}
