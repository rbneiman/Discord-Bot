package main.valuestorage;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import main.MiscUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class CompanyInfo implements Serializable{

	public class Job implements Serializable{
		private static final long serialVersionUID = 90002L;
		
		public String title;
		private ArrayList<String> links;
		
		public Job(String title, String link) {
			links = new ArrayList<String>();
			links.add(link);	
		}
		
		public void addLink(String link) {
			if(links.contains(link)) return;
			links.add(link);
			links.sort(null);
		}
		
		public boolean removeLink(String link) {
			System.out.println(links.remove(link));
			
			if(links.isEmpty()) return true;			
			return false;
		}
		
		public String[] getLinks() {
			return links.toArray(new String[0]);
		}
		
		public boolean removeLink(int i) {
			links.remove(i);
			links.sort(null);
			if(links.isEmpty()) return true;
			
			return false;
		}
		
	}
	
	private static final long serialVersionUID = 90001L;
	private static EmbedBuilder builder;
	public String name;
	public String logoURL;
	public HashMap<String,Job> jobs;

	
	public CompanyInfo(String name) {
		if(builder == null) {
			builder = new EmbedBuilder();
			builder.setAuthor("Alec Bot Job Links", null, "https://i.imgur.com/TT1jeRo.png");
			builder.setColor(2321802);
			builder.addBlankField(false);
		}
		this.name = name;
		jobs = new HashMap<String,Job>();
	}
	
	public CompanyInfo(String name, String jobName, String link) {
		this(name);
		jobs.put(jobName, new Job(jobName,link));
	}

	
	Object readResolve() throws ObjectStreamException{
		return this;
	}
	
	public void addLink(String jobName, String link) {
		if(!jobs.containsKey(jobName)) {
			jobs.put(jobName, new Job(jobName, link));
			return;
		}
			
		jobs.get(jobName).addLink(link);
	}
	
	public void removeLink(String jobName, int ind) {
		if(!jobs.containsKey(jobName)) {
			return;
		}
			
		
		if(jobs.get(jobName).removeLink(ind)) jobs.remove(jobName);
	}
	
	public String[] getLinks(String jobName) {
		if(!jobs.containsKey(jobName)) return null;
		
		return jobs.get(jobName).getLinks();	
	}
	
	public String[] getJobs(){
		if(jobs.size()==0) {
			return null;
		}
		String out[] = jobs.keySet().toArray(new String[0]);
		Arrays.sort(out);
		return out;
	}
	
	public static void setLogo(HashMap<String,CompanyInfo> hmap, String companyName, String logoURL) {
		if(!hmap.containsKey(companyName)) {
			hmap.put(companyName, new CompanyInfo(companyName));
		}	
		hmap.get(companyName).logoURL = logoURL;
	}
	
	public static MessageEmbed getCompanies(HashMap<String,CompanyInfo> hmap) {
		if(builder == null) {
			builder = new EmbedBuilder();
			builder.setAuthor("Alec Bot Job Links", null, "https://i.imgur.com/TT1jeRo.png");
			builder.setColor(2321802);
			builder.addBlankField(false);
		}
		if(!builder.getFields().isEmpty()) builder.clearFields();
		builder.setThumbnail(null);
			

		String out = "    ";
		
		String[] companies = hmap.keySet().toArray(new String[0]);
		Arrays.sort(companies);
		for(String companyName : companies) {
			if(!hmap.get(companyName).jobs.isEmpty())
				out += companyName + "\n    ";
		}
		
		builder.addField(new Field("\n\nAvailable companies", out, false));	
		return builder.build();
	}
	
	public static MessageEmbed getJobs(HashMap<String,CompanyInfo> hmap, String companyName) {
		if(builder == null) {
			builder = new EmbedBuilder();
			builder.setAuthor("Alec Bot Job Links", null, "https://i.imgur.com/TT1jeRo.png");
			builder.setColor(2321802);
			builder.addBlankField(false);
		}
		if(!builder.getFields().isEmpty()) builder.clearFields();
		builder.setThumbnail(null);
		if(!hmap.containsKey(companyName)) {
			hmap.put(companyName, new CompanyInfo(companyName));
			builder.addField(new Field("\n\nPositions at " + companyName, "```diff\n - No positions found - \n ```", false));	
			return builder.build();
		}
		
		builder.setThumbnail(hmap.get(companyName).logoURL);

		String out = "    ";
		String[] jobNames = hmap.get(companyName).getJobs();
		
		if(jobNames == null) {
			builder.addField(new Field("\n\nPositions at " + companyName, "```diff\n - No positions found - \n ```", false));	
			return builder.build();
		}
		
		for(String jobName : jobNames) {
			out += jobName + "\n    ";
		}		
	
		builder.addField(new Field("\n\nPositions at " + companyName, out, false));	
		return builder.build();
	}
	
	
	public static MessageEmbed getLinks(HashMap<String,CompanyInfo> hmap, String companyName, String jobName) {
		if(builder == null) {
			builder = new EmbedBuilder();
			builder.setAuthor("Alec Bot Job Links", null, "https://i.imgur.com/TT1jeRo.png");
			builder.setColor(2321802);
			builder.addBlankField(false);
		}
		if(!builder.getFields().isEmpty()) builder.clearFields();
		builder.setThumbnail(null);
		
		if(!hmap.containsKey(companyName)) {
			hmap.put(companyName, new CompanyInfo(companyName));
			
			builder.addField(new Field("\n\nLinks for " + jobName + "s at " + companyName, "```diff\n - No links found - \n ```", false));	
			return builder.build();
		}
		
		builder.setThumbnail(hmap.get(companyName).logoURL);
		
		String out = "    ";
		String[] links = hmap.get(companyName).getLinks(jobName);
		
		if(links == null) {
			builder.addField(new Field("\n\nLinks for " + jobName + "s at " + companyName, "```diff\n - No links found - \n ```", false));	
			return builder.build();
		}
		
		for(int i=0; i<links.length-1; i++) {
			out += "[link" + MiscUtils.padSpaces(Integer.toString(i),7) + " ](" + links[i] + ") ";
			if(i%10==0 && i>0) out += "\n    ";
		}
		out += "[link" + MiscUtils.padSpaces(Integer.toString(links.length-1),7) + " ](" + links[links.length-1] + ") ";
		
		
		
		builder.addField(new Field("\n\nLinks for " + jobName + " at " + companyName, out, false));	
		return builder.build();
	}
	
	public static MessageEmbed addLink(HashMap<String,CompanyInfo> hmap, String companyName, String jobName, String link) {
		if(builder == null) {
			builder = new EmbedBuilder();
			builder.setAuthor("Alec Bot Job Links", null, "https://i.imgur.com/TT1jeRo.png");
			builder.setColor(2321802);
			builder.addBlankField(false);
		}
		
		if(!builder.getFields().isEmpty()) builder.clearFields();
		builder.setThumbnail(null);
		
		if(!link.contains("http")) link = "http://" + link;
		
		if(!hmap.containsKey(companyName)) {
			hmap.put(companyName, new CompanyInfo(companyName, jobName, link));
		}
		else {
			hmap.get(companyName).addLink(jobName, link);
		}
		
		builder.setThumbnail(hmap.get(companyName).logoURL);		
		builder.addField(new Field("", "Company: " + companyName + "\nJob title:" + jobName + "\n[Link](" + link + ")", true));
		return builder.build();
	}
	
	public static void removeLink(HashMap<String,CompanyInfo> hmap, String companyName, String jobName, String link) {
//		if(!link.contains("http")) link = "http://" + link;
		
		if(!hmap.containsKey(companyName)) {
			return;
		}
		
		hmap.get(companyName).removeLink(jobName, Integer.parseInt(link));
	}
	
//	public static MessageEmbed getJobsAndLinks(HashMap<String,CompanyInfo> hmap, String companyName) {		
//		builder.clearFields();
//		builder.setImage(null);
//		
//		return builder.build();
//	}
	
}






