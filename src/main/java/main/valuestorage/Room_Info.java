package main.valuestorage;

import java.util.*;

import main.ConfigStorage;
import main.MiscUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;


public class Room_Info {
	
	class InactiveCheck extends TimerTask{
		boolean lastEmpty = false;
		public void run() { //check if channel has been empty for more than 30 mins
	        if(channel.getMembers().isEmpty()) {
	        	if(lastEmpty) {     
	        		if(fromChannel!=null) {
	        			fromChannel.sendMessage("Private channel \"" + channel.getName() + "\" deleted due to inactivity").queue();
	        		}       		
	        		channel.delete().queue();
	        		this.cancel();
	        		removeRoom(ownerId);
	        	}
	        	lastEmpty = true;
	        }
	        else {
	        	lastEmpty = false;
	        }
	    } 
	}
	
	VoiceChannel channel;
	MessageChannel fromChannel;
	UserVals parent;
	long ownerId;
	long roomId;
	int size;
	
	public Room_Info(UserVals parent, Guild g, long owner, MessageChannel fromChannel, int size, String name, List<String> others){
		this.parent = parent;
		this.ownerId = owner;
		this.fromChannel = fromChannel;
		
		this.size = size;
		
		
		if(owner == ConfigStorage.botHelperID) {
			owner = MiscUtils.findMember(g, others.get(0)).getIdLong();
			others = others.subList(1, others.size());
		}
		
		if(parent.roomOwners.containsKey(owner)) {
			parent.roomOwners.get(owner).channel.delete().queue();
			parent.roomOwners.remove(owner);
			fromChannel.sendMessage("Removed old channel").queue();
		}
		this.channel = g.createVoiceChannel(name).complete();
		this.roomId = channel.getIdLong();
		
		channel.getManager().setParent(g.getCategoryById(ConfigStorage.privateVoiceCategory)).queue();
		
		PermissionOverrideAction act = channel.upsertPermissionOverride(g.getMemberById(owner));
		act.grant(EnumSet.of(Permission.VIEW_CHANNEL,Permission.MANAGE_CHANNEL)).queue();
		for(String s : others) {
			if(s != null && MiscUtils.findMember(g, s)!=null) {
				channel.upsertPermissionOverride(MiscUtils.findMember(g, s)).grant(Permission.VIEW_CHANNEL).queue();
			}	
			else if(s!=null) {
				fromChannel.sendMessage("Could not find " + s);
			}
		}
		channel.upsertPermissionOverride(g.getRoleById(ConfigStorage.mainGuildID)).deny(EnumSet.of(Permission.VIEW_CHANNEL)).queue();
		channel.getManager().setUserLimit(size).queue();
		fromChannel.sendMessage("Created new voice channel\n```diff\n - Note: channel will auto delete after 30 minutes of inactivity```").queue();

		
		Timer timer = new Timer(); 
        TimerTask task = new InactiveCheck(); 
          
        timer.schedule(task, 1800000, 1800000); //30 mins
        
	}
	
	public Room_Info(UserVals parent, long owner, VoiceChannel channel){
		this.channel = channel;
		this.fromChannel = null;
		this.parent = parent;
		this.ownerId = owner;
		this.roomId = channel.getIdLong();
		this.size = channel.getUserLimit();
		
		Timer timer = new Timer(); 
        TimerTask task = new InactiveCheck(); 
          
        timer.schedule(task, 1800000, 1800000); //30 mins
	}
	
	public void removeRoom(long toRemove) {
		parent.roomOwners.remove(toRemove);
//		saveRoomsToFile();
	}
	
}
