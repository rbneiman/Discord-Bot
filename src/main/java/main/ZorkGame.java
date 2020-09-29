package main;



import java.io.IOException;
import java.net.MalformedURLException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlBold;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class ZorkGame {
	WebClient webClient;
	HtmlPage page;
	HtmlInput input;
	HtmlInput button;
	HtmlForm form;
	DomNode table;
	DomText text;
	DomNodeList<DomNode> dlist;
	String lastFound="";
	
	public ZorkGame() {
		webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		//System.out.println(webClient.getOptions().isCssEnabled());
		//System.out.println(webClient.getOptions().isJavaScriptEnabled());
		webClient.getOptions().setJavaScriptEnabled(false);
		
		try {
			page = webClient.getPage("http://www.web-adventures.org/cgi-bin/webfrotz?s=ZorkDungeon");
			table= (DomNode) page.getByXPath("/html/body/table[2]/tbody/tr/td[1]").get(0);
			dlist= table.getChildNodes();
			input=(HtmlInput) page.getByXPath("/html/body/form/input[3]").get(0);
			button=(HtmlInput) page.getByXPath("/html/body/form/input[4]").get(0);
			form=(HtmlForm) page.getByXPath("/html/body/form").get(0);
//			lastFound=	"Welcome to ZORK.\r\n" + 
//						"Release 12 / Serial number 990623 / Inform v6.14 Library 6/7\r\n" + 
//						"\n" + 
//						"This is an open field west of a white house, with a boarded front door.\r\n" + 
//						"There is a small mailbox here.\r\n" + 
//						"A rubber mat saying 'Welcome to Zork!' lies by the door.";
			lastFound=getMessage();
			
			
			
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Exception e) {
			System.out.println("test4");
			e.printStackTrace();
		}
		
	}
	
	public String getMessage() {
		table= (DomNode) page.getByXPath("/html/body/table[2]/tbody/tr/td[1]").get(0);
		
		//System.out.println("hello?");
		dlist= table.getChildNodes();
		String content="";
		int i=dlist.getLength()-1;
		for(;i>0;i-=1) {
			if(dlist.get(i).getNodeName().contains("p")) {
				i+=1;
				break;
			}
		}
		for(;i<dlist.getLength();i+=1) {
			//System.out.println(i);
			if(dlist.get(i).getNodeName().contains("#text")) {
				content+=((DomText)dlist.get(i)).getTextContent();
			}
			if(dlist.get(i).getNodeName().equals("b")) {
				content+="__"+((HtmlBold)dlist.get(i)).getTextContent()+"__";
			}
		}
		//System.out.println(content);
		return content;
	}
	
	public String command(String toput){
		input.setValueAttribute(toput);
		//System.out.println(input.getValueAttribute());
		try {
			
			page=button.click();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return getMessage();//span.asText();
	}
}
