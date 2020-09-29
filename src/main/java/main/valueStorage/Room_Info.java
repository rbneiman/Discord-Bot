package main.valueStorage;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Timer;
import java.util.TimerTask;

import main.ConfigStorage;
import main.miscUtils;
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
	User_Vals parent;
	long ownerId;
	long roomId;
	int size;
	
	public Room_Info(User_Vals parent, Guild g, long owner, MessageChannel fromChannel, int size, String name, String[] others){
		this.parent = parent;
		this.ownerId = owner;
		this.fromChannel = fromChannel;
		
		this.size = size;
		
		
		if(owner == ConfigStorage.botHelperID) {
			owner = miscUtils.findMember(g, others[0]).getIdLong();
			others = Arrays.copyOfRange(others, 1, others.length);
		}
		
		if(parent.roomOwners.containsKey(owner)) {
			parent.roomOwners.get(owner).channel.delete().queue();
			parent.roomOwners.remove(owner);
			fromChannel.sendMessage("Removed old channel").queue();
		}
		this.channel = g.createVoiceChannel(name).complete();
		this.roomId = channel.getIdLong();
		
		channel.getManager().setParent(g.getCategoryById(ConfigStorage.privateVoiceCategory)).queue();
		
		PermissionOverrideAction act = channel.putPermissionOverride(g.getMemberById(owner));
		act = act.setAllow(Permission.VIEW_CHANNEL);
		act.setAllow(EnumSet.of(Permission.VIEW_CHANNEL,Permission.MANAGE_CHANNEL)).queue();
		for(String s : others) {
			if(s != null && miscUtils.findMember(g, s)!=null) {
				channel.putPermissionOverride(miscUtils.findMember(g, s)).setAllow(Permission.VIEW_CHANNEL).queue();
			}	
			else if(s!=null) {
				fromChannel.sendMessage("Could not find " + s);
			}
		}
		channel.putPermissionOverride(g.getRoleById(ConfigStorage.mainGuildID)).setDeny(EnumSet.of(Permission.VIEW_CHANNEL)).queue();
		channel.getManager().setUserLimit(size).queue();
		fromChannel.sendMessage("Created new voice channel\n```diff\n - Note: channel will auto delete after 30 minutes of inactivity```").queue();

		
		Timer timer = new Timer(); 
        TimerTask task = new InactiveCheck(); 
          
        timer.schedule(task, 1800000, 1800000); //30 mins
        
	}
	
	public Room_Info(User_Vals parent, long owner, VoiceChannel channel){
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
