package main.audio;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.api.entities.MessageChannel;

public class TrackScheduler extends AudioEventAdapter {
	  private final AudioPlayer player;
	  private final Queue<AudioTrack> tracks;
	  public int playlistSize = 0;
	  
	  public TrackScheduler(AudioPlayer player) {
		  this.player=player;
		  tracks=new LinkedBlockingQueue<>();
	  }
	  
	  public void onPlayerPause() {
		  System.out.println("pau");
	    // Player was paused
	  }


	  public void onPlayerResume() {
		  System.out.println("res");
	    // Player was resumed
	  }


	  public void onTrackStart(AudioTrack track) {
		  System.out.println("play");
		  player.playTrack(track);
	  }
	  

	  @Override
	  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		MessageChannel channel=(MessageChannel) track.getUserData();
		
		
		
		switch(endReason) {
			case FINISHED:
				break;
			case LOAD_FAILED:
				channel.sendMessage("Failed to load track").queue();
				tracks.poll();
				return;
			case STOPPED:				
				return;
			case CLEANUP:
				return;
			case REPLACED:
				return;
			default:
				break;
		}
		if(tracks.isEmpty()) {
			channel.sendMessage("Reached end of queue").queue();	
			return;
		}
		else if (endReason.mayStartNext) {
			AudioTrack t = tracks.poll();
			channel.sendMessage("Now Playing "+ t.getInfo().title).queue();		
			player.playTrack(t);
		}
		
		// endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
		// endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
		// endReason == STOPPED: The player was stopped.
		// endReason == REPLACED: Another track started playing while this had not finished
		// endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
		//                       clone of this back to your queue
	  }

	  public void onTrackException(AudioTrack track, FriendlyException exception) {
		  System.out.println("stucke");
	    // An already playing track threw an exception (track end event will still be received separately)
	  }

	  public void onTrackStuck(AudioTrack track, long thresholdMs) {
		  System.out.println("stuck");
	    // Audio track has been unable to provide us any audio, might want to just start a new track
	  }

	public void queue(AudioTrack track) {
		MessageChannel channel=(MessageChannel) track.getUserData();	
		tracks.add(track);
		if(player.getPlayingTrack()==null) {			
			player.playTrack(tracks.poll());
		}
		else if(playlistSize == 0){
			channel.sendMessage("Added to number " + (tracks.size()) + " in queue").queue();			
		}	
		if(playlistSize > 0)
			playlistSize--;
	}
	
	public boolean skip() {
		player.stopTrack();
		if(!tracks.isEmpty()) {
			AudioTrack track = tracks.poll();
			player.playTrack(track);	
			MessageChannel channel=(MessageChannel) track.getUserData();
			channel.sendMessage("Now Playing: "+ track.getInfo().title).queue();
			return true;
		}
		return false;
	}
	
	public void clearTracks() {
		player.stopTrack();
		tracks.clear();
	  }
	
	public int getNumQueued() {
		return tracks.size();
	}
}