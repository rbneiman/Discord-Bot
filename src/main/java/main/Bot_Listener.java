package main;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.io.InputStream;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.audio.Bot_Audio;
import main.audio.TrackScheduler;
import main.cardGames.GameHandler;
import main.valueStorage.KarmaCounts;
import main.valueStorage.KarmaGetter;
import main.valueStorage.SAVE_TYPE;
import main.valueStorage.User_Vals;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class Bot_Listener extends ListenerAdapter 
{
	private EmbedBuilder builder=new EmbedBuilder();
	private long time=System.currentTimeMillis();
	private Random rand=new Random(time);
	private AudioPlayerManager playerManager= new DefaultAudioPlayerManager();

	
	private ZorkManager zorkManager= new ZorkManager();
	private static final Map<String, Map.Entry<AudioPlayer, TrackScheduler>> players = new HashMap<>();
	private static User_Vals userVals;

	private BotTimer limiter;
	private Timer timer;
	
	
	
	public Bot_Listener() {
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
        
        
        MessageChannel channel = event.getChannel();
        Message message = event.getMessage();
        String content = message.getContentRaw(); 
        String[] words=miscUtils.splitWords(content);
        
        if(!message.isFromGuild()) {
        	privateMsg(event, words);
        	return;
        }
        if(message.getContentRaw().contains("<@&" + ConfigStorage.botHelperID +">")) {
        	channel.sendMessage("Here").queue();
        	return;
        }
        
        Guild guild = event.getGuild();
        ScheduledFuture<?> future=null;
        
        if(userVals == null) userVals = new User_Vals();

        
        Member user = event.getMember(); 
        Long id = user.getIdLong();
        
        if(id == ConfigStorage.botHelperID && words[0].contentEquals("!help")) {
			return;
		}
        
        
        if(words[0]==null) return;
        try {
 
	        if(words[0].contentEquals("!ping")) {
	            channel.sendMessage("Pong!").queue();
	        }	        
	        else if(words[0].contentEquals("!start")) {
	        	String search;
	        	if(words[1].contains("facebook.com")) {
	        		search = miscUtils.getFaceVid(words[1]);
	        	}
	        	else if(!words[1].contains("https:") && !words[1].contains("C:")) {
        			search = YtubeList.doSearch(words[1]);
        			channel.sendMessage(search).queue();
        			//System.out.println(search);
        		}
        		else {
        			search = words[1];
        		}
	        	if(!players.containsKey(guild.getId())||(!guild.getAudioManager().isConnected())) {
	        		VoiceChannel voicechannel = user.getVoiceState().getChannel();        		
	        		AudioSourceManagers.registerRemoteSources(playerManager);        		
		        	AudioPlayer player = playerManager.createPlayer();
		        	TrackScheduler trackScheduler=new TrackScheduler(player);
		        	player.addListener(trackScheduler);
		        	AudioManager manager = guild.getAudioManager();
		        	manager.setSendingHandler(new Bot_Audio(player));
		        	manager.openAudioConnection(voicechannel);
		        	players.put(guild.getId(), new AbstractMap.SimpleEntry<>(player,trackScheduler));
		        	playerManager.loadItem(search, new AudioLoadResultHandler() {
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
		
		        	});
		        	
	        	}
	        	else {
	        		TrackScheduler trackScheduler=players.get(guild.getId()).getValue();
	        		System.out.print(words[1]);
		        	playerManager.loadItemOrdered(guild,search, new AudioLoadResultHandler() {
		        		  @Override
		        		  public void trackLoaded(AudioTrack track) {
		        			  track.setUserData(channel);
			        		  trackScheduler.queue(track);
			        		  System.out.println("queue");
		        		  }
		
		        		  @Override
		        		  public void playlistLoaded(AudioPlaylist playlist) {
		        		    for (AudioTrack track : playlist.getTracks()) {
		        		      trackScheduler.queue(track);
		        		    }
		        		  }
		
		        		  @Override
		        		  public void noMatches() {
		        		    // Notify the user that we've got nothing
		        			  channel.sendMessage("No matches found for search").queue();
		        		  }
		
		        		  @Override
		        		  public void loadFailed(FriendlyException throwable) {
		        		    // Notify the user that everything exploded
		        			  channel.sendMessage("Failed to load audio").queue();
		        		  }
		
		        	});
		        	
		        	
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
	        	channel.sendMessage("Server ip is: " + miscUtils.getIp() + ":25565").queue();	
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
	        	channel.sendMessage(zorkManager.input(user,content.substring(words[0].length()))).queue();
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
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	if(!userVals.balanceSheet.containsKey(id)) {
	        		System.out.println("noob");
	        		userVals.balanceSheet.put(id, 0);
	        	}
	        	channel.sendMessage(user.getEffectiveName() + " has " + userVals.balanceSheet.get(id) + " dine-in dollars!").queue();
	        	userVals.saveToFile(SAVE_TYPE.BALANCE);
	        }	
	        else if(words[0].contentEquals("!debugAdd")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	
	        	if(id == ConfigStorage.developerID) {
	        		if(words[2]==null) {
	        			userVals.balanceSheet.put(id, userVals.balanceSheet.get(id)+Integer.parseInt(words[1]));
		        	}
	        		else {
	        			long otherId = Long.parseLong(words[2]);
	        			userVals.balanceSheet.put(otherId, userVals.balanceSheet.get(otherId)+Integer.parseInt(words[1]));
	        		}
	        	}
	        	else {
	        		channel.sendMessage("No").queue();
	        	}
	        	userVals.saveToFile(SAVE_TYPE.BALANCE);
	        }
	        else if(words[0].contentEquals("!debugSet")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	if(id == ConfigStorage.developerID) {
	        		if(words[2]==null) {
	        			userVals.balanceSheet.put(id, Integer.parseInt(words[1]));
		        	}
	        		else {
	        			long otherId = Long.parseLong(words[2]);
	        			userVals.balanceSheet.put(otherId, Integer.parseInt(words[1]));
	        		}
	        	}
	        	else {
	        		channel.sendMessage("No").queue();
	        	}
	        	userVals.saveToFile(SAVE_TYPE.BALANCE);
	        }
	        else if(words[0].contentEquals("!bet")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	if(words[1]==null) {
	        		channel.sendMessage("Need an argument!").queue();
	        		return;
	        	}
	        	if(!userVals.balanceSheet.containsKey(id)) {
	        		System.out.println("noob");
	        		userVals.balanceSheet.put(user.getIdLong(), 0);
	        	}
	        	int rolled = -1;
	        	time=System.currentTimeMillis();
	        	rand=new Random(time);
	        	try {
	        		int bet = (int) Integer.parseInt(words[1]);
	        		
	        		if(bet<=userVals.balanceSheet.get(id)&&bet>0) {
	        			rolled= rand.nextInt(bet*2+1);
	        			if(rolled>bet) { channel.sendMessage(user.getEffectiveName() + " has earned " + (rolled-bet) + " dine-in dollars!").queue();}
	        			if(rolled<bet) { channel.sendMessage(user.getEffectiveName() + " has lost " + (bet-rolled) + " dine-in dollars!").queue();}
	        			if(rolled==bet) { channel.sendMessage(user.getEffectiveName() + " has broke even!").queue();}
	        			userVals.balanceSheet.put(id, userVals.balanceSheet.get(id)+(rolled-bet));
	        		}else if(bet>userVals.balanceSheet.get(id)&&bet>0){channel.sendMessage("Not enough dine-in dollars to bet!").queue();}
	        		else {channel.sendMessage("No negative bets!").queue();}
	        	}
	        	catch(NumberFormatException e) {e.printStackTrace();}
	        	userVals.saveToFile(SAVE_TYPE.BALANCE);
	        }	     
	        else if(words[0].contentEquals("!blackjack")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	if(words[1]==null) {
	        		channel.sendMessage("Need an argument!").queue();
	        		return;
	        	}
	        	if(!userVals.balanceSheet.containsKey(id)) {
	        		System.out.println("noob");
	        		userVals.balanceSheet.put(user.getIdLong(), 0);
	        	}
	        	
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
	        	else if((bet>userVals.balanceSheet.get(id))){
	        		channel.sendMessage("Not enough dine-in dollars to bet!").queue();
	        	}
	        	else {
	        		channel.sendMessage(GameHandler.blackJackHandler(user.getEffectiveName(), bet, action, id)).queue();
	        	}        	
	        	int payOut = GameHandler.recievePayOut(id);
	        	if(payOut!=0) {userVals.balanceSheet.put(id, userVals.balanceSheet.get(id)+payOut);}
	        	
	        	userVals.saveToFile(SAVE_TYPE.BALANCE);
	        }	
	        else if(words[0].contentEquals("!moneyPls")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	if(!userVals.balanceSheet.containsKey(id)) {
	        		System.out.println("noob");
	        		userVals.balanceSheet.put(id, 0);
	        	}
	        	
	        	if(userVals.balanceSheet.get(id)<5) {
	        		userVals.balanceSheet.put(id, userVals.balanceSheet.get(id)+1);
	        		channel.sendMessage("Dine-in dollar added").queue();
	        		userVals.saveToFile(SAVE_TYPE.BALANCE);
	        	}
	        }
	        else if(words[0].contentEquals("!view")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	if(id == ConfigStorage.developerID) {
	        		long otherId = Long.parseLong(words[1]);
	        		channel.sendMessage((guild.getMemberById(otherId)).getEffectiveName() + " has " + userVals.balanceSheet.get(otherId) + " dine-in dollars!").queue();
	        	}
	        	else {channel.sendMessage("No").queue();}
	        }
	        else if(words[0].contentEquals("!reserve")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	userVals.createRoom(guild, user.getIdLong(), Integer.parseInt(words[2]), channel, words[1], Arrays.copyOfRange(words, 3, words.length));
	        }
	        else if(words[0].contentEquals("!courses")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	userVals.allCourses(channel);
	        }
	        else if(words[0].contentEquals("!class")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	if(words[1].contentEquals("add")){
	        		userVals.addCourses(channel, user.getIdLong(), words);
	        	}
	        	else if(words[1].contentEquals("drop")){
	        		userVals.dropCourses(channel, user.getIdLong(), words);
	        	}
	        	else if(words[1].contentEquals("list")){
	        		userVals.listCourses(channel, user.getIdLong());
	        	}
				else{
					channel.sendMessage("```diff\\n- COMMAND NOT RECOGNIZED -\\n```").queue();
				}
	        }
	        else if(words[0].contentEquals("!karma")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	if(id == ConfigStorage.developerID && words[1]!=null) {
	        		Long id2 = Long.parseLong(words[1]);
	        		Member user2 = guild.getMemberById(id2);
	        		if(!userVals.karmaCounter.containsKey(id2)) {
		        		userVals.karmaCounter.put(id2,  new KarmaCounts(0,0));
		        		userVals.saveToFile(SAVE_TYPE.KARMA);
		        	}
	        		channel.sendMessage(user2.getEffectiveName() 
	        				+ " has " + userVals.karmaCounter.get(id2).getKarma().toString() + " karma!\n"
    						+ "Upvotes: " + userVals.karmaCounter.get(id2).upvotes + "\nDownvotes: " +  userVals.karmaCounter.get(id2).downvotes).queue();
	        	}
	        	else {
		        			        	
		        	if(!userVals.karmaCounter.containsKey(id)) {
		        		userVals.karmaCounter.put(id,  new KarmaCounts(0,0));
		        		userVals.saveToFile(SAVE_TYPE.KARMA);
		        	}
		        	channel.sendMessage(user.getEffectiveName() + " has " + userVals.karmaCounter.get(id).getKarma().toString() + " karma!\n"
		        						+ "Upvotes: " + userVals.karmaCounter.get(id).upvotes + "\nDownvotes: " +  userVals.karmaCounter.get(id).downvotes).queue();
	        	}
	        	
	        }
	        else if(words[0].contentEquals("!highscores")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	channel.sendMessage(userVals.leaderBoard(guild)).queue();
	        	
	        }
	        else if(words[0].contentEquals("!lowscores")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	channel.sendMessage(userVals.leaderBoardDown(guild)).queue();
	        	
	        }
	        else if(words[0].contentEquals("!veryhighscores")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	channel.sendMessage(userVals.leaderBoardUp(guild)).queue();
	        	
	        }
	        else if(words[0].contentEquals("!meanscores")){
	        	if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
	        	channel.sendMessage(userVals.leaderBoardMean(guild)).queue();
	        	
	        }
	        else if(words[0].contentEquals("!job")){
	        	miscUtils.jobHelper(userVals.companyList, channel, words);
	        	userVals.saveToFile(SAVE_TYPE.COMPANIES);
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
			e.printStackTrace();
			channel.sendMessage("```diff\n- " + user.getEffectiveName() + " is not in the sudoers file. \n- This incident will be reported.\n```").queue();
		}
        catch(IOException e) {
			channel.sendMessage("```diff\n- TASK FAILED SUCCESSFULLY -\n```").queue();
		}
        catch(Exception e) {
        	channel.sendMessage("```diff\n- SEGMENTATION FAULT (core dumped) -\n```").queue();
        	System.out.println(e.getClass());
        	e.printStackTrace();
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
		
		if(userVals == null) {userVals = new User_Vals();}
		
//		System.out.println(event.getReactionEmote().getName());
		Long id = event.getUser().getIdLong();
		Long author = msg.getAuthor().getIdLong();
		
		if (event.getUser().isBot()) {return;}
		if(author.equals(id)) {
			if(!id.equals(lastUser)) {
				event.getChannel().sendMessage("Nice try " + guild.getMemberById(id).getEffectiveName());
				lastUser = id.longValue();
			}
			if(name.equals("upvote") || name.equals("downvote")) {
				event.getReaction().removeReaction(event.getUser()).queue();
				skip = true;
			}
			return;
		}
		
		
		if(!userVals.karmaCounter.containsKey(author)) {
    		System.out.println("noob");
    		userVals.karmaCounter.put(author,  new KarmaCounts(0,0));
    		userVals.saveToFile(SAVE_TYPE.KARMA);;
    	}
		
		if(!userVals.karmaCounter.containsKey(id)) {
    		System.out.println("noob");
    		userVals.karmaCounter.put(id,  new KarmaCounts(0,0));
    		userVals.saveToFile(SAVE_TYPE.KARMA);;
    	}
		
		KarmaCounts voter = userVals.karmaCounter.get(id);
		
		if(event.getReactionEmote().getName().toLowerCase().contentEquals("upvote")) {
			if(voter.upLeft>0) {
				voter.upLeft--;
				userVals.karmaCounter.get(author).upvotes++;
				System.out.println(guild.getMemberById(id).getEffectiveName() + " upvoted " +  guild.getMemberById(author).getEffectiveName());
				miscUtils.karmaLog(guild.getMemberById(id).getEffectiveName() + " upvoted " +  guild.getMemberById(author).getEffectiveName());
			}
			else {
				event.getReaction().removeReaction(event.getUser()).queue();
				skip = true;
				return;
			}
			
		}
		
		if(event.getReactionEmote().getName().toLowerCase().contentEquals("downvote")) {
			if(voter.downLeft>0) {
				voter.downLeft--;
				userVals.karmaCounter.get(author).downvotes++;	
				System.out.println(guild.getMemberById(id).getEffectiveName() + " downvoted " +  guild.getMemberById(author).getEffectiveName());
				miscUtils.karmaLog(guild.getMemberById(id).getEffectiveName() + " downvoted " +  guild.getMemberById(author).getEffectiveName());
			}
			else {
				event.getReaction().removeReaction(event.getUser()).queue();
				skip = true;
				return;
			}
					
		}
		
		userVals.saveToFile(SAVE_TYPE.KARMA);
		return;
	}
	
	@Override
	public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
		
		Guild guild = event.getGuild();
		if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
		Message msg = event.getChannel().retrieveMessageById(event.getMessageIdLong()).complete();
		String name = event.getReactionEmote().getName().toLowerCase();
		if(userVals == null) {userVals = new User_Vals();}
		
//		System.out.println(event.getReactionEmote().getName());
		Long id = event.getUser().getIdLong();
		Long author = msg.getAuthor().getIdLong();
		
		if(skip) {
			skip = false;
			return;
		}
		if (event.getUser().isBot()) {return;}
		if(author.equals(id)) {
			if(name.equals("upvote") || name.equals("downvote")) {
				event.getReaction().removeReaction(event.getUser()).queue();
				skip = true;
			}
			
			return;
		}
		
		
		
		if(!userVals.karmaCounter.containsKey(author)) {
    		System.out.println("noob");
    		userVals.karmaCounter.put(author,  new KarmaCounts(0,0));
    		userVals.saveToFile(SAVE_TYPE.KARMA);
    	}
		
		if(!userVals.karmaCounter.containsKey(id)) {
    		System.out.println("noob");
    		userVals.karmaCounter.put(id,  new KarmaCounts(0,0));
    		userVals.saveToFile(SAVE_TYPE.KARMA);
    	}
		

		if(event.getReactionEmote().getName().toLowerCase().contentEquals("upvote")) {
//			System.out.println(guild.getMemberById(id).getEffectiveName());
			if(userVals.karmaCounter.get(author).upvotes > 0) {
				
				userVals.karmaCounter.get(author).upvotes--;
				
			}
		}
		
		else if(event.getReactionEmote().getName().toLowerCase().contentEquals("downvote")) {
			if(userVals.karmaCounter.get(author).downvotes > 0) {
				userVals.karmaCounter.get(author).downvotes--;
			}			
		}
			
		
		userVals.saveToFile(SAVE_TYPE.KARMA);
		return;
	}
	
	@Override
	public void onTextChannelCreate(TextChannelCreateEvent event) {

		if(userVals==null) userVals = new User_Vals(); 
		
		TextChannel tChannel = event.getChannel();
		Category category = tChannel.getParent();
		if(category!=null && category.getName().toLowerCase().contentEquals("classes")) {
			userVals.courseList.put(tChannel.getName().toUpperCase(), tChannel);
		}
	}

	@Override
	public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
		if(userVals==null) userVals = new User_Vals(); 
		if(event.getGuild().getIdLong() != ConfigStorage.mainGuildID) return;
		
		
//		System.out.println(event.getMember().getEffectiveName());
		if(event.getMember().getIdLong() == ConfigStorage.specialUserID && !event.getNewNickname().contentEquals("ammar")) {
			event.getMember().modifyNickname("ammar").queueAfter(60, TimeUnit.SECONDS);
		}
		
	}
}


