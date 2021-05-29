package main.valuestorage;

import main.ConfigStorage;
import main.Main;
import net.dv8tion.jda.api.entities.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


public class UserVals {

	private static Logger LOGGER = LogManager.getLogger("UserVals");


	public final HashMap<Long,Room_Info> roomOwners;
	public final VoteStuff vote_stuff;

	
	public UserVals(){
		roomOwners  = new HashMap<Long,Room_Info>();
		vote_stuff = new VoteStuff(this);


		Timer timer = new Timer();
		timer.schedule(new DailyBackup(), 3600000 * 24, 3600000 * 24); //24 hours
	}
	
	
	public String leaderBoard(Guild g) {
		String out = "TOP KARMA SCORES:\n";
		ArrayList<MemberInfo> memberInfos = ValueStorage.getGuildMemberInfos(g);
		memberInfos.sort((o1, o2) -> o2.getKarma() - o1.getKarma());

		int extra = 0;
		for(int i = 0; i<5+extra && i<memberInfos.size(); i++) {
			MemberInfo memberInfo = memberInfos.get(i);
			Member member = g.getMemberById(memberInfo.memberId);
			if(member != null) {
				out += member.getEffectiveName() + ": " + memberInfo.getKarma() + "\n";
			}
		}

		return out;
	}
	
	public String leaderBoardDown(Guild g) {
		String out = "MOST DOWNVOTED:\n";
		ArrayList<MemberInfo> memberInfos = ValueStorage.getGuildMemberInfos(g);
		memberInfos.sort((o1, o2) -> o2.getDownvotes() - o1.getDownvotes());

		int extra = 0;
		for(int i = 0; i<5+extra && i<memberInfos.size(); i++) {
			MemberInfo memberInfo = memberInfos.get(i);
			Member member = g.getMemberById(memberInfo.memberId);
			if(member != null) {
				out += member.getEffectiveName() + ": " + memberInfo.getDownvotes() + "\n";
			}
		}

		return out;
	}
	
	public String leaderBoardUp(Guild g) {
		String out = "MOST UPVOTED:\n";
		ArrayList<MemberInfo> memberInfos = ValueStorage.getGuildMemberInfos(g);
		memberInfos.sort((o1, o2) -> o2.getUpvotes() - o1.getUpvotes());

		int extra = 0;
		for(int i = 0; i<5+extra && i<memberInfos.size(); i++) {
			MemberInfo memberInfo = memberInfos.get(i);
			Member member = g.getMemberById(memberInfo.memberId);
			if(member != null) {
				out += member.getEffectiveName() + ": " + memberInfo.getUpvotes() + "\n";
			}
		}


		return out;
	}
	
	public String leaderBoardMean(Guild g) {
		String out = "LOWEST UNCAPPED KARMA:\n";
		ArrayList<MemberInfo> memberInfos = ValueStorage.getGuildMemberInfos(g);
		memberInfos.sort(Comparator.comparingInt(MemberInfo::getKarmaUnbounded));

		int extra = 0;
		for(int i = 0; i<5+extra && i<memberInfos.size(); i++) {
			MemberInfo memberInfo = memberInfos.get(i);
			Member member = g.getMemberById(memberInfo.memberId);
			if(member != null) {
				out += member.getEffectiveName() + ": " + memberInfo.getKarmaUnbounded() + "\n";
			}
		}


		return out;
	}
	
	public void createRoom(Guild g, long owner, int size, MessageChannel fromChannel, String name, String[] others) {
		roomOwners.put(owner, new Room_Info(this,g,owner,fromChannel,size,name,others));
	}
	
	
	public void addCourse(TextChannel channel, String[] channelIdStrings){
		final StringBuilder createdString = new StringBuilder("Created:```\n");
		final StringBuilder updatedString = new StringBuilder("Updated:```\n");
		boolean created = false;
		boolean updated = false;

		for(String channelIdString: channelIdStrings) {
			if (channelIdString == null) {
				break;
			}
			long channelId;
			try {
				channelId = Long.parseLong(channelIdString);
				TextChannel courseChannel = channel.getGuild().getTextChannelById(channelId);
				if (courseChannel == null) {
					channel.sendMessage(channelIdString + "is not a valid channel!").queue();
				} else {
					if (ValueStorage.addCourse(courseChannel)) {
						createdString.append(" ").append(courseChannel.getName().toUpperCase());
						created = true;
					} else {
						updatedString.append(" ").append(courseChannel.getName().toUpperCase());
						updated = true;
					}
				}
			} catch (NumberFormatException e) {
				channel.sendMessage(channelIdString + " is not a valid channel id!").queue();
			}
		}
		createdString.append("```");
		updatedString.append("```");
		if(created)
			channel.sendMessage(createdString).queue();
		if(updated)
			channel.sendMessage(updatedString).queue();
	}

	public void removeCourse(TextChannel channel, String[] channelIdStrings){
		final StringBuilder removedString = new StringBuilder("Removed:```\n");
		boolean removed = false;

		for(String channelIdString: channelIdStrings) {
			if (channelIdString == null) {
				break;
			}
			long channelId;
			try {
				channelId = Long.parseLong(channelIdString);
				TextChannel courseChannel = channel.getGuild().getTextChannelById(channelId);
				if (courseChannel == null) {
					channel.sendMessage(channelIdString + " is not a valid channel!").queue();
				} else {
					if (ValueStorage.removeCourse(courseChannel)) {
						removedString.append(" ").append(courseChannel.getName().toUpperCase());
						removed = true;
					}else{
						channel.sendMessage("Course " + courseChannel.getName() +"  does not exist!").queue();
					}
				}
			} catch (NumberFormatException e) {
				channel.sendMessage(channelIdString + " is not a valid channel id!").queue();
			}
		}
		removedString.append("```");
		if(removed)
			channel.sendMessage(removedString).queue();
	}
	
	public void enrollCourses(Member member, TextChannel channel, String[] channelNames) {
		final StringBuilder out = new StringBuilder("Added:```\n");
		final Boolean[] added = {Boolean.FALSE};
		ArrayList<CourseInfo> guildCourses = ValueStorage.getGuildCourses(channel.getGuild());
		for(String channelName : channelNames){
			if(channelName == null)
				break;
			List<TextChannel> courseChannels = channel.getGuild().getTextChannelsByName(channelName, true);
			if(courseChannels.size() == 0){
				channel.sendMessage(channelName.toUpperCase() + " is not a valid course!").queue();
			}else{
				courseChannels.forEach(courseChannel -> {
					final Boolean[] exists = {Boolean.FALSE};
					guildCourses.forEach(guildCourseInfo ->{
						if(guildCourseInfo.channelId == courseChannel.getIdLong()){
							ValueStorage.enrollCourse(member, courseChannel);
							out.append(guildCourseInfo.name).append(" ");
							exists[0] = true;
							added[0] = true;
						}
					});
					if(!exists[0])
						channel.sendMessage(channelName.toUpperCase() + " is not a valid course!").queue();
				});
			}
		}
		out.append("```");
		if(added[0])
			channel.sendMessage(out.toString()).queue();
		else
			channel.sendMessage("No courses were added").queue();
	}
	
	public void dropCourses(Member member, TextChannel channel, String[] channelNames) {
		final StringBuilder out = new StringBuilder("Dropped:```\n");
		for(String channelName : channelNames){
			if(channelName == null)
				break;

			List<TextChannel> courseChannels = channel.getGuild().getTextChannelsByName(channelName, true);
			courseChannels.forEach(courseChannel -> {
				ValueStorage.dropCourse(member, courseChannel);
				out.append(courseChannel.getName().toUpperCase()).append(" ");
			});
		}
		out.append("```");
		channel.sendMessage(out.toString()).queue();
	}
	
	public void listCourses(Member member, MessageChannel channel) {
		ArrayList<String> registeredCourses = ValueStorage.getRegisteredCourses(member);
		String temp = "Your courses:```\n";

		for(String s : registeredCourses) {
			temp += s + " ";
		}
		temp += "```";
		if(registeredCourses.size()>0)
			channel.sendMessage(temp).queue();
		else
			channel.sendMessage("You are not enrolled in any courses!").queue();
	}
	
	public void allCourses(TextChannel c) {
		ArrayList<CourseInfo> courseList = ValueStorage.getGuildCourses(c.getGuild());
		String temp = "Available courses:```\n";
		for(CourseInfo info : courseList) {
			temp += info.name + " ";
		}
		temp += "```";
		c.sendMessage(temp).queue();
		
	}

	public void updateMemberNames(Guild g){
		List<Member> members = g.getMembers();
		ArrayList<MemberName> memberNames = new ArrayList<>();
		members.forEach(member -> memberNames.add(new MemberName(member)));
		ValueStorage.updateMemberNames(memberNames);
	}
}
