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
import main.commands.CommandHandler;
import main.slash.SlashCommandHelper;
import main.valuestorage.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import static net.dv8tion.jda.api.entities.ChannelType.TEXT;

public class BotListener extends ListenerAdapter
{
	private final EmbedBuilder builder = new EmbedBuilder();
	private long time = System.currentTimeMillis();
	private Random rand = new Random(time);
	private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

	private static final Logger LOGGER = LogManager.getLogger(BotListener.class);
	
	private ZorkManager zorkManager = new ZorkManager();
	private static final Map<String, Map.Entry<AudioPlayer, TrackScheduler>> players = new HashMap<>();
	private static HashSet<Long> adminUsers = new HashSet<>();
	private UserVals userVals;

	private BotTimer limiter;
	private Timer timer;
	
	
	
	public BotListener(UserVals userVals) {
		this.userVals = userVals;
//		AudioSourceManagers.registerRemoteSources(playerManager);
//		AudioSourceManagers.registerLocalSource(playerManager);
		builder.setAuthor("Alec Bot", null, "https://i.imgur.com/TT1jeRo.png");
		builder.setColor(2321802);
		Field field = new Field("Jobs", "[**1.**](http://google.com)",false);
		builder.addField(field);
    	timer = new Timer(); 
        limiter = new BotTimer(); 
        timer.schedule(limiter, 60000, 60000); //1 minute
        firstTime = true;
	}
	
	
	public void privateMsg(MessageReceivedEvent event, List<String> words) {
		MessageChannel channel = event.getChannel();
		User user=event.getAuthor(); 
		try {
			if(words.get(0).contentEquals("!reserve")){
	        	userVals.createRoom(Main.api.getGuildById(ConfigStorage.mainGuildID), user.getIdLong(), Integer.parseInt(words.get(2)), channel, words.get(1), words.subList(3, words.size()));
	        }
			else if(words.get(0).contentEquals("!help")) {
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

		TextChannel channel = (TextChannel) event.getChannel();
		Member member = event.getMember();
		if(member == null){
			return;
		}

        try {
			CommandHandler.handleMessageReceived(event);
        }
		catch(IllegalArgumentException e) {
			channel.sendMessage("```diff\n- " + member.getEffectiveName() + " is not in the sudoers file. \n- This incident will be reported.\n```").queue();
			LOGGER.error("IllegalArgumentException with input:" + event.getMessage());
			LOGGER.error(e, e);
		}
        catch(Exception e) {
        	channel.sendMessage("```diff\n- SEGMENTATION FAULT (core dumped) -\n```").queue();
			LOGGER.error("Exception with input:" + event.getMessage());
			LOGGER.error(e, e);
        }
    }
	
	boolean skip = false;
    Long lastUser = null;
	@Override
	public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
		if(!event.isFromGuild()) return;

		Guild guild = event.getGuild();
//		if(guild.getIdLong() != ConfigStorage.mainGuildID) return;
		Message msg = event.getChannel().retrieveMessageById(event.getMessageIdLong()).complete();
		String name = event.getEmoji().getName().toLowerCase();

		
		Long upvoterId = event.getUser().getIdLong();
		long authorId = msg.getAuthor().getIdLong();
		Member author = guild.getMemberById(authorId);
		Member upvoter = guild.getMemberById(upvoterId);

		if(author == null) return;
		if (event.getUser().isBot()) {return;}
		if(authorId == upvoterId) {
			if(!upvoterId.equals(lastUser)) {
//				event.getChannel().sendMessage("Nice try " + upvoter.getEffectiveName()).queue();
				lastUser = upvoterId;
			}
			if(name.equals("upvote") || name.equals("downvote")) {
				event.getReaction().removeReaction(event.getUser()).queue();
				skip = true;
			}
			return; 
		}

		MemberInfo authorMemberInfo = ValueStorage.getMemberInfo(author);

		MemberInfo upvoterMemberInfo = ValueStorage.getMemberInfo(upvoter);
		if(event.getEmoji().getName().toLowerCase().contentEquals("upvote")) {
			if(upvoterMemberInfo.canVote(authorMemberInfo, true)) {
				upvoterMemberInfo.upvotesLeft--;
				authorMemberInfo.upvote(upvoterMemberInfo);
				authorMemberInfo.update();
				upvoterMemberInfo.update();
				LOGGER.info(upvoter.getEffectiveName() + " upvoted " +  author.getEffectiveName());
				MiscUtils.karmaLog(upvoter.getEffectiveName() + " upvoted " +  author.getEffectiveName());
			}
			else {
				event.getReaction().removeReaction(event.getUser()).queue();
				skip = true;
				return;
			}
			
		}
		
		if(event.getEmoji().getName().toLowerCase().contentEquals("downvote")) {
			if(upvoterMemberInfo.canVote(authorMemberInfo, false)) {
				upvoterMemberInfo.downvotesLeft--;
				authorMemberInfo.downvote(upvoterMemberInfo);
				authorMemberInfo.update();
				upvoterMemberInfo.update();
				LOGGER.info(upvoter.getEffectiveName() + " downvoted " +  author.getEffectiveName());
				MiscUtils.karmaLog(upvoter.getEffectiveName() + " downvoted " +  author.getEffectiveName());
			}
			else {
				event.getReaction().removeReaction(event.getUser()).queue();
				skip = true;
			}
					
		}

	}
	
	@Override
	public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {
		
		return; //TODO check if valid removal
	}
	
	@Override
	public void onChannelCreate(@NotNull ChannelCreateEvent event) {
		if(!event.isFromType(TEXT)) return;

	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
		CommandHandler.handleSlashCommand(event);
	}
}


