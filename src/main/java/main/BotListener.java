package main;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.audio.BotAudioResultHandler;
import main.audio.BotAudio;
import main.audio.TrackScheduler;
import main.cardgames.GameHandler;
import main.valuestorage.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BotListener extends ListenerAdapter
{
	private EmbedBuilder builder = new EmbedBuilder();
	private long time = System.currentTimeMillis();
	private Random rand = new Random(time);
	private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

	private static final Logger LOGGER = LogManager.getLogger(BotListener.class);
	
	private ZorkManager zorkManager = new ZorkManager();
	private static final Map<String, Map.Entry<AudioPlayer, TrackScheduler>> players = new HashMap<>();
	private static HashSet<Long> adminUsers = new HashSet<>();
	private static UserVals userVals;

	private BotTimer limiter;
	private Timer timer;
	
	
	
	public BotListener() {
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
		builder.setAuthor("Alec Bot", null, "https://i.imgur.com/TT1jeRo.png");
		builder.setColor(2321802);
		Field field = new Field("Jobs", "[**1.**](http://google.com)",false);
		builder.addField(field);
    	timer = new Timer(); 
        limiter = new BotTimer(); 
        timer.schedule(limiter, 60000, 60000); //1 minute
        firstTime = true;
	}
	
	
	public void privateMsg(MessageReceivedEvent event, String[] words) {
		MessageChannel channel = event.getChannel();
		User user=event.getAuthor(); 
		try {
			if(words[0].contentEquals("!reserve")){
	        	userVals.createRoom(Main.api.getGuildById(ConfigStorage.mainGuildID), user.getIdLong(), Integer.parseInt(words[2]), channel, words[1], Arrays.copyOfRange(words, 3, words.length));
	        }
			else if(words[0].contentEquals("!help")) {
	        	channel.sendMessage("```diff\n"
	        			+ "𝐀𝐯𝐚𝐢𝐥𝐚𝐛𝐥𝐞 𝐂𝐨𝐦𝐦𝐚𝐧𝐝𝐬:\n"
	        			+ "- NOTE: inputs containing spaces need to be entirely surrounded by double quotes"
	        			+ "\n- Commands that I can do in pms are very limited right now, sorry \n"
	        			+ "\n!reserve <name> <room size> <tags> - create a private voice channel with a given number of slots and access for specified users\n```"
	        			
	        			).queue();
	        }
			
		}
		catch(Exception e) {
			e.printStackTrace();
			channel.sendMessage("```diff\n- SEGMENTATION FAULT (core dumped) -\n```").queue();
		}
	}
	

	boolean firstTime;
	@SuppressWarnings("unused")
	@Override
    public void onMessageReceived(MessageReceivedEvent event)
    {	
        if (event.getAuthor().isBot()) {
        	if(event.getAuthor().getIdLong() == ConfigStorage.botHelperID) {
        		if(limiter.counter>=10) {
        			if(firstTime) {
            			event.getChannel().sendMessage("```\nLimit reached! Please wait a bit for the counter to tick down\n```").queue();
        			}
            		firstTime = false;
            		return;
        		}
        		System.out.println(event.getMessage().toString());
        		
        		limiter.counter++;
        		firstTime = true;
        	}       
        	else {
        		return;
        	}
        }
        
        

        Message message = event.getMessage();
        String content = message.getContentRaw(); 
        String[] words=miscUtils.splitWords(content);
        
        if(!message.isFromGuild()) {
        	privateMsg(event, words);
        	return;
        }

        
        Guild guild = event.getGuild();
        ScheduledFuture<?> future=null;
        
        if(userVals == null) userVals = new UserVals();

		TextChannel channel = (TextChannel) event.getChannel();
        Member member = event.getMember();
        Long id = member.getIdLong();

		if(message.getContentRaw().contains("<@&" + ConfigStorage.botHelperID +">")) {
			channel.sendMessage("Here").queue();
			return;
		}

        if(id == ConfigStorage.botHelperID && words[0].contentEquals("!help")) {
			return;
		}
        
        
        if(words[0]==null) return;
        try {
 
	        if(words[0].contentEquals("!ping")) {
	        	LOGGER.fatal("Test");
	        	LOGGER.error("Test");
	        	LOGGER.warn("Test");
				LOGGER.info("Test");
				LOGGER.debug("Test");
				LOGGER.trace("Test");
	            channel.sendMessage("Pong!").queue();
//				channel.sendMessage("```0.33333 Karma has been added to your account!```").queue();
//				userVals.sqlMigrate(guild);
	        }
	        else if(words[0].contentEquals("!start")) {
	        	String[] search;
	        	if(words[1].contains("facebook.com")) {
	        		search = new String[] {miscUtils.getFaceVid(words[1])};
	        	}
	        	else if(!words[1].contains("https:") && !words[1].contains("C:")) {
        			search = new String[] {YtubeList.doSearch(words[1])};
        			channel.sendMessage(search[0]).queue();
	        	}
	        	else if(words[1].contains("&list") && (words[1].contains("youtube") || words[1].contains("youtu.be"))){
	        		search = YtubeList.getPlayList(words[1]);
	        	}
        		else {
        			search = new String[] {words[1]};
        		}
	        	
	        	if(search.length>1)
	        		channel.sendMessage("Added playlist of " + search.length + " videos").queue();
	        	
	        	
	        	if(!players.containsKey(guild.getId())||(!guild.getAudioManager().isConnected())) {
	        		
	        		VoiceChannel voicechannel = member.getVoiceState().getChannel();
	        		AudioSourceManagers.registerRemoteSources(playerManager);        		
		        	AudioPlayer player = playerManager.createPlayer();
		        	TrackScheduler trackScheduler = new TrackScheduler(player);
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
	        		TrackScheduler trackScheduler=players.get(guild.getId()).getValue();
	        		
	        		if(search.length>1)
		        		trackScheduler.playlistSize = search.length;
	        		LOGGER.debug(words[1]);
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
	        	
	        }
	        
	        else if(words[0].contentEquals("!stop")) {
	        	AudioPlayer player=players.get(guild.getId()).getKey();
	        	player.setPaused(true);
	        }
	        else if(words[0].contentEquals("!play")) {
	        	AudioPlayer player=players.get(guild.getId()).getKey();
	        	player.setPaused(false);
	        }
	        else if(words[0].contentEquals("!skip")) {
	        	TrackScheduler trackScheduler=players.get(guild.getId()).getValue();
	        	if(!trackScheduler.skip()) {
	        		channel.sendMessage("Nothing in queue!").queue();
	        	}
	        }
	        else if(words[0].contentEquals("!serverip")) {
	        	channel.sendMessage("Server ip is: alecserv.com").queue();	
	        }
	        else if(words[0].contentEquals("!roll")) {
				String tempS="Rolled ";
				int[] rolled;
	        	time=System.currentTimeMillis();
	        	rand=new Random(time);
	        	int toRoll=((int) Integer.parseInt(words[1]));
	        	try {
	        		rolled=new int[Integer.parseInt(words[2])];
	        	}
	        	catch(NumberFormatException e) {
	        		rolled=new int[1];
	        	}
	        	for(int i=0;i<rolled.length;i++) {
	        		rolled[i]=rand.nextInt(toRoll)+1;
	        		tempS+=rolled[i]+", ";
	        	}
	        	channel.sendMessage(tempS.substring(0, tempS.length()-2)).queue();
	        }
	        else if(words[0].contentEquals("!ask")) {
	        	channel.sendMessage( miscUtils.wolframRead(words[1]) ).queue();
	        }
	        else if(words[0].contentEquals("!fancytest")) {
	        	channel.sendMessage(builder.build()).queue();
	        }
	        else if(words[0].contentEquals("!parsetest")) {
	        	String testStr="";
	        	for(int i=0;words[i]!=null&&i+1<words.length;i++) {
	        		testStr+=words[i]+"\n";
	        	}
	        	channel.sendMessage(testStr).queue();
	        }	
	        else if(words[0].contentEquals("!zork")) {	        	
	        	channel.sendMessage(zorkManager.input(member,content.substring(words[0].length()))).queue();
	        }
	        else if(words[0].contentEquals("!default")) {	
	        	for (int i=0;i<10;i++) {
	        		channel.sendMessage("```" + miscUtils.asciiArchive(i) + "```").queue();
	        	}
	        }
	        else if(words[0].contentEquals("!count")) {	
	        	if(id != ConfigStorage.developerID) return;
	        	int radix = 10;
	        	try {
	        	if(words[2]!=null) {
		        	switch(words[2]) {
		        	case("bin"):
		        		radix = 2;
		        	break;
		        	case("hex"):
		        		radix = 16;
		        	break;
		        	default:
		        		radix = Integer.parseInt(words[2]);
		        	}
	        	}
	        	}
	        	catch(NumberFormatException e) {
	        		radix = 10;
	        	}
	        	if(Integer.parseInt(words[1])>=0) {
		        	for (int i=1;i<=Integer.parseInt(words[1]);i++) {
		        		future = channel.sendMessage(Integer.toString(i,radix)).submitAfter(i, TimeUnit.SECONDS);
		        	}
	        	}
	        	else {
	        		for (int i=-1;i>=Integer.parseInt(words[1]);i--) {
		        		future = channel.sendMessage(Integer.toString(i,radix)).submitAfter(i, TimeUnit.SECONDS);
		        	}
	        	}
	        	
	        }
	        else if(words[0].contentEquals("!interrupt")) {
	        	if(future!=null) {
	        		future.cancel(true);
	        	}
	        }
	        else if(words[0].contentEquals("!leave")) {
	        	if(guild.getAudioManager().isConnected()) {
	        		guild.getAudioManager().closeAudioConnection();
	        		TrackScheduler trackScheduler=players.get(guild.getId()).getValue();
	        		trackScheduler.clearTracks();
	        		channel.sendMessage("Leaving voice channel").queue();
	        	}
	        }
	        else if(words[0].contentEquals("!clear")) {
	        	if(guild.getAudioManager().isConnected()) {
	        		TrackScheduler trackScheduler=players.get(guild.getId()).getValue();
	        		trackScheduler.clearTracks();
	        		channel.sendMessage("Queue cleared").queue();
	        	}
	        }
	        else if(words[0].contentEquals("!balance")) {
				MemberInfo memberInfo;
				String name;
	        	if(id == ConfigStorage.developerID && words[1] != null) {
					memberInfo = ValueStorage.getMemberInfo(guild.getMemberById(Long.parseLong(words[1])));
					name = guild.getMemberById(Long.parseLong(words[1])).getEffectiveName();
				}
				else{
					memberInfo = ValueStorage.getMemberInfo(member);
					name = member.getEffectiveName();
				}


	        	channel.sendMessage(name + " has " + memberInfo.getBalance() + " dine-in dollars!").queue();
	        }	
	        else if(words[0].contentEquals("!debugAdd")){
	        	if(id == ConfigStorage.developerID) {
	        		MemberInfo memberInfo;
	        		if(words[2] == null)
						memberInfo = ValueStorage.getMemberInfo(member);
	        		else
						memberInfo = ValueStorage.getMemberInfo(guild.getMemberById(Long.parseLong(words[2])));

					memberInfo.setBalance(memberInfo.getBalance() + Integer.parseInt(words[1]));
					memberInfo.update();
	        	}
	        	else {
	        		channel.sendMessage("No").queue();
	        	}
	        }
	        else if(words[0].contentEquals("!debugSet")){
				if(id == ConfigStorage.developerID) {
					MemberInfo memberInfo;
					if(words[2] == null)
						memberInfo = ValueStorage.getMemberInfo(member);
					else
						memberInfo = ValueStorage.getMemberInfo(guild.getMemberById(Long.parseLong(words[2])));

					memberInfo.setBalance(Integer.parseInt(words[1]));
					memberInfo.update();
				}
	        	else {
	        		channel.sendMessage("No").queue();
	        	}
	        }
	        else if(words[0].contentEquals("!bet")){
	        	if(words[1]==null) {
	        		channel.sendMessage("Need an argument!").queue();
	        		return;
	        	}

	        	MemberInfo memberInfo = ValueStorage.getMemberInfo(member);

	        	int rolled = -1;
	        	time=System.currentTimeMillis();
	        	rand=new Random(time);
	        	try {
	        		int bet = Integer.parseInt(words[1]);
	        		
	        		if(bet<=memberInfo.getBalance()&&bet>0) {
	        			rolled= rand.nextInt(bet*2+1);
	        			if(rolled>bet) { channel.sendMessage(member.getEffectiveName() + " has earned " + (rolled-bet) + " dine-in dollars!").queue();}
	        			if(rolled<bet) { channel.sendMessage(member.getEffectiveName() + " has lost " + (bet-rolled) + " dine-in dollars!").queue();}
	        			if(rolled==bet) { channel.sendMessage(member.getEffectiveName() + " has broke even!").queue();}
						memberInfo.setBalance(memberInfo.getBalance() + (rolled-bet));
						memberInfo.update();
	        		}else if(bet>memberInfo.getBalance()&&bet>0){channel.sendMessage("Not enough dine-in dollars to bet!").queue();}
	        		else {channel.sendMessage("No negative bets!").queue();}
	        	}
	        	catch(NumberFormatException e) {e.printStackTrace();}
	        }	     
	        else if(words[0].contentEquals("!blackjack")){
	        	if(words[1]==null) {
	        		channel.sendMessage("Need an argument!").queue();
	        		return;
	        	}

				MemberInfo memberInfo = ValueStorage.getMemberInfo(member);
	        	
	        	int bet;
	        	String action;
	        	try {
	        		bet = Integer.parseInt(words[1]);
	        		action = "hit";
	        	}
	        	catch(NumberFormatException e) {
	        		bet = 0;
	        		action = words[1];
	        	}
	        	
	        	if(bet<0) {
	        		channel.sendMessage("No negative bets!").queue();
	        	}
	        	else if((bet>memberInfo.getBalance())){
	        		channel.sendMessage("Not enough dine-in dollars to bet!").queue();
	        	}
	        	else {
	        		channel.sendMessage(GameHandler.blackJackHandler(member.getEffectiveName(), bet, action, id)).queue();
	        	}        	
	        	int payOut = GameHandler.recievePayOut(id);
	        	if(payOut!=0) {memberInfo.setBalance(memberInfo.getBalance() + payOut);}
	        	
	        	memberInfo.update();
	        }	
	        else if(words[0].contentEquals("!moneyPls")){
				MemberInfo memberInfo = ValueStorage.getMemberInfo(member);
	        	
	        	if(memberInfo.getBalance()<5) {
					memberInfo.setBalance(memberInfo.getBalance() + 1);
	        		channel.sendMessage("Dine-in dollar added").queue();
					memberInfo.update();
	        	}
	        }
	        else if(words[0].contentEquals("!reserve")){
	        	userVals.createRoom(guild, member.getIdLong(), Integer.parseInt(words[2]), channel, words[1], Arrays.copyOfRange(words, 3, words.length));
	        }
	        else if(words[0].contentEquals("!courses")){
	        	userVals.allCourses(channel);
	        }
	        else if(words[0].contentEquals("!class")){
				if(words[1] == null || words[1].contentEquals("help")){
					channel.sendMessage("```diff\nValid subcommands are:\n+ add (or enroll)\n+ drop\n+ list\n+ sudo create (administrators only)\n+ sudo remove (administrators only)```").queue();
				}
	        	else if(words[1].contentEquals("add") || words[1].contentEquals("enroll")){
					userVals.enrollCourses(member, channel, Arrays.copyOfRange(words, 2, words.length));
	        	}
	        	else if(words[1].contentEquals("drop")){
	        		userVals.dropCourses(member, channel, Arrays.copyOfRange(words, 2, words.length));
	        	}
	        	else if(words[1].contentEquals("list")){
	        		userVals.listCourses(member, channel);
	        	}else if(words[1].contentEquals("sudo")){
					if(!member.getPermissions().contains(Permission.ADMINISTRATOR)){
						channel.sendMessage("```diff\n- ADMINISTRATORS ONLY -```").queue();
						return;
					}
					if(!adminUsers.contains(id)){
						channel.sendMessage(miscUtils.asciiArchive(10)).queue();
						adminUsers.add(id);
					}
					if(words[2].contentEquals("create")){
						userVals.addCourse(channel, Arrays.copyOfRange(words, 3, words.length));
					}else if(words[2].contentEquals("remove")) {
						userVals.removeCourse(channel, Arrays.copyOfRange(words, 3, words.length));
					}
				}else{
					channel.sendMessage("```diff\n- SUBCOMMAND NOT RECOGNIZED -\n\nValid subcommands are:\n+ add (or enroll)\n+ drop\n+ list\n+ sudo create (administrators only)\n+ sudo remove (administrators only)```").queue();
				}
	        }
	        else if(words[0].contentEquals("!karma")){

	        	if(id == ConfigStorage.developerID && words[1]!=null) {
	        		Long id2 = Long.parseLong(words[1]);
	        		Member member2 = guild.getMemberById(id2);
					MemberInfo memberInfo = ValueStorage.getMemberInfo(member2);

	        		channel.sendMessage(member2.getEffectiveName()
	        				+ " has " + memberInfo.getKarma() + " karma!\n"
    						+ "Upvotes: " + memberInfo.getUpvotes() + "\nDownvotes: " +  memberInfo.getDownvotes()).queue();
	        	}
	        	else {
					MemberInfo memberInfo = ValueStorage.getMemberInfo(member);
					channel.sendMessage(member.getEffectiveName()
							+ " has " + memberInfo.getKarma() + " karma!\n"
							+ "Upvotes: " + memberInfo.getUpvotes() + "\nDownvotes: " +  memberInfo.getDownvotes()).queue();
	        	}
	        	
	        }
	        else if(words[0].contentEquals("!highscores")){
	        	channel.sendMessage(userVals.leaderBoard(guild)).queue();
	        }
	        else if(words[0].contentEquals("!lowscores")){
	        	channel.sendMessage(userVals.leaderBoardDown(guild)).queue();
	        }
	        else if(words[0].contentEquals("!veryhighscores")){
	        	channel.sendMessage(userVals.leaderBoardUp(guild)).queue();
	        }
	        else if(words[0].contentEquals("!meanscores")){
	        	channel.sendMessage(userVals.leaderBoardMean(guild)).queue();
	        }
	        else if(words[0].contentEquals("!job")){
//	        	miscUtils.jobHelper(userVals.companyList, channel, words);
	        }
	       
	        else if(words[0].contentEquals("!help")) {
	        	channel.sendMessage("```diff\n"
	        			+ "𝐀𝐯𝐚𝐢𝐥𝐚𝐛𝐥𝐞 𝐂𝐨𝐦𝐦𝐚𝐧𝐝𝐬:\n"
	        			+ "- NOTE: inputs containing spaces need to be entirely surrounded by double quotes\n"
	        			+ "\n!start <filepath, or youtube link> - start playing stuff, or add audio to queue\n"
	        			+ "\n!play/!stop                        - resume/pause playback\n"
	        			+ "\n!skip                              - skips to next audio in queue\n"
	        			+ "\n!leave                             - leaves voice channel and clears queue\n"
	        			+ "\n!clear                         	- clears the current queue\n"
	        			+ "\n!roll <sides> <number(optional)>   - rolls a given dice a specified number of times\n"
	        			+ "\n!zork <input>                      - play some zork with the bot!\n"
	        			+ "\n!ask  <input>                      - ask the bot a question!\n"
	        			+ "\n!serverip                          - get my current minecraft server ip\n"
	        			+ "\n!balance                           - retreve your current dine-in dollar balance\n"
	        			+ "\n!bet <number>                      - gamble your bet for more dine-in dollars\n"
	        			+ "\n!blackjack <'hit'/'stand'> <bet>   - play blackjack with the bot\n"
	        			+ "\n!moneyPls                          - gives you one dine-in dollar up to five\n"
	        			+ "\n!reserve <name> <room size> <tags> - create a private voice channel with a given number of slots and access for specified users\n"
	        			+ "\n!class <add/drop> <course names>   - add or remove yourself from specified class channels\n"
	        			+ "\n!courses                           - list all classes that have existing channels\n"
	        			+ "\n+ A couple secret commands\n"
	        			+ "```").queue();
	        }
        }
		catch(IllegalArgumentException e) {
			channel.sendMessage("```diff\n- " + member.getEffectiveName() + " is not in the sudoers file. \n- This incident will be reported.\n```").queue();
			LOGGER.error("IllegalArgumentException with input:" + Arrays.toString(words));
			LOGGER.error(e, e);
		}
        catch(Exception e) {
        	channel.sendMessage("```diff\n- SEGMENTATION FAULT (core dumped) -\n```").queue();
			LOGGER.error("Exception with input:" + Arrays.toString(words));
			LOGGER.error(e, e);
        }
    }
	
	boolean skip = false;
    Long lastUser = null;
	@Override
	public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
		
		Guild guild = event.getGuild();
		if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
		Message msg = event.getChannel().retrieveMessageById(event.getMessageIdLong()).complete();
		String name = event.getReactionEmote().getName().toLowerCase();
		
		if(userVals == null) {userVals = new UserVals();}
		
		Long upvoterId = event.getUser().getIdLong();
		long authorId = msg.getAuthor().getIdLong();
		Member author = guild.getMemberById(authorId);
		Member upvoter = guild.getMemberById(upvoterId);

		if(author == null) return;
		if (event.getUser().isBot()) {return;}
		if(authorId == upvoterId) {
			if(!upvoterId.equals(lastUser)) {
				event.getChannel().sendMessage("Nice try " + upvoter.getEffectiveName());
				lastUser = upvoterId;
			}
			if(name.equals("upvote") || name.equals("downvote")) {
				event.getReaction().removeReaction(event.getUser()).queue();
				skip = true;
			}
			return;
		}

		MemberInfo authorMemberInfo = ValueStorage.getMemberInfo(author);

		if(event.getReactionEmote().getName().toLowerCase().contentEquals("upvote")) {
			if(authorMemberInfo.upvotesLeft>0) {
				authorMemberInfo.upvotesLeft--;
				authorMemberInfo.upvote();
				authorMemberInfo.update();
				LOGGER.info(upvoter.getEffectiveName() + " upvoted " +  author.getEffectiveName());
				miscUtils.karmaLog(upvoter.getEffectiveName() + " upvoted " +  author.getEffectiveName());
			}
			else {
				event.getReaction().removeReaction(event.getUser()).queue();
				skip = true;
				return;
			}
			
		}
		
		if(event.getReactionEmote().getName().toLowerCase().contentEquals("downvote")) {
			if(authorMemberInfo.downvotesLeft>0) {
				authorMemberInfo.downvotesLeft--;
				authorMemberInfo.downvote();
				authorMemberInfo.update();
				LOGGER.info(upvoter.getEffectiveName() + " downvoted " +  author.getEffectiveName());
				miscUtils.karmaLog(upvoter.getEffectiveName() + " downvoted " +  author.getEffectiveName());
			}
			else {
				event.getReaction().removeReaction(event.getUser()).queue();
				skip = true;
			}
					
		}

	}
	
	@Override
	public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
		
		return; //TODO check if valid removal
//		Guild guild = event.getGuild();
//		if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
//		Message msg = event.getChannel().retrieveMessageById(event.getMessageIdLong()).complete();
//		String name = event.getReactionEmote().getName().toLowerCase();
//		if(userVals == null) {userVals = new User_Vals();}
//		
////		System.out.println(event.getReactionEmote().getName());
//		Long id = event.getUser().getIdLong();
//		Long author = msg.getAuthor().getIdLong();
//		
//		if(skip) {
//			skip = false;
//			return;
//		}
//		if (event.getUser().isBot()) {return;}
//		if(author.equals(id)) {
//			if(name.equals("upvote") || name.equals("downvote")) {
//				event.getReaction().removeReaction(event.getUser()).queue();
//				skip = true;
//			}
//			
//			return;
//		}
//		
//		
//		
//		if(!userVals.karmaCounter.containsKey(author)) {
//    		System.out.println("noob");
//    		userVals.karmaCounter.put(author,  new KarmaCounts(0,0));
//    		userVals.saveToFile(SAVE_TYPE.KARMA);
//    	}
//		
//		if(!userVals.karmaCounter.containsKey(id)) {
//    		System.out.println("noob");
//    		userVals.karmaCounter.put(id,  new KarmaCounts(0,0));
//    		userVals.saveToFile(SAVE_TYPE.KARMA);
//    	}
//		
//
//		if(event.getReactionEmote().getName().toLowerCase().contentEquals("upvote")) {
////			System.out.println(guild.getMemberById(id).getEffectiveName());
//			if(userVals.karmaCounter.get(author).upvotes > 0) {
//				
//				userVals.karmaCounter.get(author).upvotes--;
//				
//			}
//		}
//		
//		else if(event.getReactionEmote().getName().toLowerCase().contentEquals("downvote")) {
//			if(userVals.karmaCounter.get(author).downvotes > 0) {
//				userVals.karmaCounter.get(author).downvotes--;
//			}			
//		}
//			
//		
//		userVals.saveToFile(SAVE_TYPE.KARMA);
//		return;
	}
	
	@Override
	public void onTextChannelCreate(TextChannelCreateEvent event) {

		if(userVals==null) userVals = new UserVals();
		
		TextChannel tChannel = event.getChannel();
		Category category = tChannel.getParent();
		if(category!=null && category.getName().toLowerCase().contentEquals("classes")) {
//			userVals.courseList.put(tChannel.getName().toUpperCase(), tChannel);
		}
	}

	@Override
	public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
		if(userVals==null) userVals = new UserVals();
		if(event.getGuild().getIdLong() != ConfigStorage.mainGuildID) return;
		
		
//		System.out.println(event.getMember().getEffectiveName());
		if(event.getMember().getIdLong() == ConfigStorage.specialUserID && !event.getNewNickname().contentEquals("ammar")) {
			event.getMember().modifyNickname("ammar").queueAfter(60, TimeUnit.SECONDS);
		}
		
	}
}


