package main.valueStorage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import main.Main;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;

public class KarmaGetter implements Runnable{

	
	@Override
	public void run() {
		getKarmaBack();
	}

	 public static ArrayList<Message> messageIterator(MessagePaginationAction action, OffsetDateTime t)
	 {
		 ArrayList<Message> out = new ArrayList<Message>();
	     CompletableFuture<?> f =  action.forEachAsync( (message) ->
	     {
	         if (t.isBefore(message.getTimeCreated())) {
	        	 out.add(message);
	         	 return true;
	         }
	         else
	             return false;
	     });
	     while(!f.isDone()) {
	    	 try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	     }
	     return out;
	 }
	 
	 
	public static ArrayList<Message> getMessagesPastDate(MessageChannel mChannel,Guild g, OffsetDateTime t) {
		ArrayList<Message> out = null;
		MessagePaginationAction itrHist = mChannel.getIterableHistory();
		out = messageIterator(itrHist,t);

		return out;
	}
	

	public void getKarmaBack() {
		Guild g = Main.api.getGuildById(644716660921466912L);
		StringBuffer out = new StringBuffer();
		OffsetDateTime t = ((MessageChannel) g.getGuildChannelById(646905163323408426L)).retrieveMessageById(741865088239009824L).complete().getTimeCreated();
		for(GuildChannel channel : g.getChannels()) {
			if(channel.getType() == ChannelType.TEXT) {
				MessageChannel mChannel = (MessageChannel) channel;

				ArrayList<Message> tempArr = getMessagesPastDate(mChannel,g,t);
				System.out.println(tempArr.size());
				for(Message m : tempArr) {
					for(MessageReaction r : m.getReactions()) {
						if(r.getReactionEmote().getName().toLowerCase().contentEquals("downvote")) {
							
							r.retrieveUsers().queue((users) -> {
								for(User u : users) {
//									if(!karmaCounter.containsKey(u.getIdLong())) {
//							    		System.out.println("noob");
//							    		karmaCounter.put(u.getIdLong(),  new KarmaCounts (0,0));
//							    		
//							    	}
									//System.out.println("Message");
//									karmaCounter.get(m.getAuthor().getIdLong()).upvotes++;
									out.append('\n' + g.getMemberById(m.getAuthor().getIdLong()).getEffectiveName() + " downvoted " +  g.getMemberById(u.getIdLong()).getEffectiveName());
								}
							});
							
						}
//						if(r.getReactionEmote().getName().toLowerCase().contentEquals("downvote")) {
//							for(User u : r.retrieveUsers().complete()) {
////								if(!karmaCounter.containsKey(u.getIdLong())) {
////						    		System.out.println("noob");
////						    		karmaCounter.put(u.getIdLong(),  new KarmaCounts (0,0));
////						    		
////						    	}
////								karmaCounter.get(u.getIdLong()).downvotes++;
//							}
//						}
					}
					
				}
				
				
			}
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Alec\\Documents\\Discord_Bot\\karmaLogManual.txt", true));
		    writer.append(out);
			writer.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
//		saveToFile(SAVE_TYPE.KARMA);
	}
}
