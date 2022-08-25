package main.valuestorage;

import main.ConfigStorage;
import main.Main;
import main.MiscUtils;
import main.commands.CommandAction;
import main.commands.CommandType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


public class UserVals implements CommandAction {

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
	
	public void createRoom(Guild g, long owner, int size, MessageChannel fromChannel, String name, List<String> others) {
		roomOwners.put(owner, new Room_Info(this,g,owner,fromChannel,size,name,others));
	}
	
	
	public String addCourse(TextChannel channel, List<String> channelIdStrings){
		final StringBuilder createdString = new StringBuilder("Created:```\n");
		final StringBuilder updatedString = new StringBuilder("Updated:```\n");
		final StringBuilder errorString = new StringBuilder();
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
					errorString.append(channelIdString).append("is not a valid channel!\n");
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
				errorString.append(channelIdString).append(" is not a valid channel id!\n");
			}
		}
		createdString.append("```\n");
		updatedString.append("```\n");
		String out = "";
		if(created)
			out += createdString.toString();
		if(updated)
			out += updatedString.toString();
		out += errorString.toString();
		return out;
	}

	public String removeCourse(TextChannel channel, List<String> channelIdStrings){
		final StringBuilder removedString = new StringBuilder("Removed:```\n");
		final StringBuilder errorString = new StringBuilder();
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
					errorString.append(channelIdString).append("is not a valid channel!\n");
				} else {
					if (ValueStorage.removeCourse(courseChannel)) {
						removedString.append(" ").append(courseChannel.getName().toUpperCase());
						removed = true;
					}else{
						errorString.append("Course ").append(courseChannel.getName()).append("  does not exist!\n");
					}
				}
			} catch (NumberFormatException e) {
				errorString.append(channelIdString).append(" is not a valid channel id!\n");
			}
		}
		removedString.append("```\n");
		String out = "";
		if(removed)
			out += removedString.toString();
		return out + errorString;
	}
	
	public String enrollCourses(Member member, TextChannel channel, List<String> channelNames) {
		final StringBuilder addedStr = new StringBuilder("Added:```\n");
		final StringBuilder errorStr = new StringBuilder("");
		final Boolean[] added = {Boolean.FALSE};
		ArrayList<CourseInfo> guildCourses = ValueStorage.getGuildCourses(channel.getGuild());
		for(String channelName : channelNames){
			if(channelName == null)
				break;
			List<TextChannel> courseChannels = channel.getGuild().getTextChannelsByName(channelName, true);
			if(courseChannels.size() == 0){
				errorStr.append(channelName.toUpperCase()).append(" is not a valid course!\n");
			}else{
				courseChannels.forEach(courseChannel -> {
					final Boolean[] exists = {Boolean.FALSE};
					guildCourses.forEach(guildCourseInfo ->{
						if(guildCourseInfo.channelId == courseChannel.getIdLong()){
							ValueStorage.enrollCourse(member, courseChannel);
							addedStr.append(guildCourseInfo.name).append(" ");
							exists[0] = true;
							added[0] = true;
						}
					});
					if(!exists[0])
						errorStr.append(channelName.toUpperCase()).append(" is not a valid course!\n");
				});
			}
		}
		addedStr.append("```\n");
		if(added[0])
			return addedStr.append(errorStr).toString();
		else
			return errorStr.append("No courses were added\n").toString();
	}
	
	public String dropCourses(Member member, TextChannel channel, List<String> channelNames) {
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
		out.append("```\n");
		return out.toString();
	}
	
	public String listCourses(Member member, MessageChannel channel) {
		ArrayList<String> registeredCourses = ValueStorage.getRegisteredCourses(member);
		String temp = "Your courses:```\n";

		for(String s : registeredCourses) {
			temp += s + " ";
		}
		temp += "```\n";
		if(registeredCourses.size()>0)
			return temp;
		else
			return "You are not enrolled in any courses!\n";
	}
	
	public String allCourses(TextChannel c) {
		ArrayList<CourseInfo> courseList = ValueStorage.getGuildCourses(c.getGuild());
		String temp = "Available courses:```\n";
		for(CourseInfo info : courseList) {
			temp += info.name + " ";
		}
		temp += "```\n";
		return temp.toString();
	}

	public void updateMemberNames(Guild g){
		List<Member> members = g.getMembers();
		ArrayList<MemberName> memberNames = new ArrayList<>();
		members.forEach(member -> memberNames.add(new MemberName(member)));
		ValueStorage.updateMemberNames(memberNames);
	}

	private static final HashSet<Long> adminUsers = new HashSet<>();

	@Override
	public String doAction(CommandType type, TextChannel channel, Member member, ArrayList<String> words){
		Guild guild = member.getGuild();
		long id = member.getIdLong();
		String out = "";
		switch (type){
			case RESERVE:
				this.createRoom(guild, member.getIdLong(), Integer.parseInt(words.get(2)), channel, words.get(1), words.subList(3, words.size()));
				break;
			case COURSES:
				out += this.allCourses(channel);
				break;
			case CLASS:
				if(words.size()<2 || words.get(1).contentEquals("help")){
					return "```diff\nValid subcommands are:\n+ add (or enroll)\n+ drop\n+ list\n+ sudo create (administrators only)\n+ sudo remove (administrators only)```";
				}
				else if(words.get(1).contentEquals("add") || words.get(1).contentEquals("enroll")){
					out += enrollCourses(member, channel, words.subList(2, words.size()));
				}
				else if(words.get(1).contentEquals("drop")){
					out += dropCourses(member, channel, words.subList(2, words.size()));
				}
				else if(words.get(1).contentEquals("list")){
					out += listCourses(member, channel);
				}else if(words.get(1).startsWith("sudo")){
					if(!member.getPermissions().contains(Permission.ADMINISTRATOR)){
						return member.getEffectiveName() + " is not in the sudoers file. This incident will be reported.";
					}
					if(!adminUsers.contains(id)){
						out += MiscUtils.asciiArchive(10);
						adminUsers.add(id);
					}
					if(words.get(2).contentEquals("create")){
						out += addCourse(channel, words.subList(3, words.size()));
					}else if(words.get(2).contentEquals("remove")) {
						out += removeCourse(channel, words.subList(3, words.size()));
					}else if(words.get(1).contentEquals("sudo_create")){
						out += addCourse(channel, words.subList(2, words.size()));
					}else if(words.get(1).contentEquals("sudo_remove")){
						out += removeCourse(channel, words.subList(2, words.size()));
					}
				}else{
					out += "```diff\n- SUBCOMMAND NOT RECOGNIZED -\n\nValid subcommands are:\n+ add (or enroll)\n+ drop\n+ list\n+ sudo create (administrators only)\n+ sudo remove (administrators only)```";
				}
				break;
			case KARMA:
				if(id == ConfigStorage.developerID && words.size() > 1) {
					Long id2 = Long.parseLong(words.get(1));
					Member member2 = guild.getMemberById(id2);
					MemberInfo memberInfo = ValueStorage.getMemberInfo(member2);

					return member2.getEffectiveName()
							+ " has " + memberInfo.getKarma() + " karma!\n"
							+ "Upvotes: " + memberInfo.getUpvotes() + "\nDownvotes: " +  memberInfo.getDownvotes();
				}
				else {
					MemberInfo memberInfo = ValueStorage.getMemberInfo(member);
					return member.getEffectiveName()
							+ " has " + memberInfo.getKarma() + " karma!\n"
							+ "Upvotes: " + memberInfo.getUpvotes() + "\nDownvotes: " +  memberInfo.getDownvotes();
				}
			case HIGHSCORES:
				return leaderBoard(guild);
			case LOWSCORES:
				return leaderBoardDown(guild);
			case VERYHIGHSCORES:
				return leaderBoardUp(guild);
			case MEANSCORES:
				return leaderBoardMean(guild);
			default:
				LOGGER.error("given bad command type: " + type);
				break;
		}
		return out;
	}
}
