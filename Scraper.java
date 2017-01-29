import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup; 
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Scraper {
	
	// defaults will never be used
	protected static String urlASCII = "http://www.citywindsor.ca/cityhall/City-Council-Meetings/Meetings-This-Week/Pages/Current-Council-Agenda.aspx";
	protected static int CONTEXT_LENGTH = 60; // default to 60 chars
	protected static File currentAgenda = null;
	
	public static File getPDF(){
		try {
			// get first <a> element which contains pdf for next meeting
			Document councilAgendas = Jsoup.connect("http://www.citywindsor.ca/cityhall/City-Council-Meetings/Meetings-This-Week/Pages/Current-Council-Agenda.aspx").get();
			Elements nextMeeting = councilAgendas.select("#ctl00_PlaceHolderMain_Content__ControlWrapper_RichHtmlField p a");
			Element link = nextMeeting.first();
			// parse pdf link and date of meeting from element
			String urlString = link.attr("abs:href"); // absolute link to pdf	
		    String meetingDate = link.text(); // ex Monday February 6 2017
			// convert urlString to proper form with %20's for spaces
		    URL url = new URL(urlString);
		    URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
		    urlASCII = uri.toASCIIString();
		    URL pdfURL = new URL(urlASCII);
		    System.out.println("Found agenda, downloading: ");
			System.out.println(pdfURL);
			System.out.println("Agenda for " + meetingDate);
			// download pdf to temp file
			File file = new File("temp.pdf");
			FileUtils.copyURLToFile(pdfURL, file);
			currentAgenda = file;
			System.out.println("Downloaded successfully!");
			return file;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<ContextPageNumber> getContexts(File file, String keyword){
		try {			
			// Parse PDF to String using PDFBox
			PDDocument agenda = PDDocument.load(file);
			
			// get whole line containing keyword
			ArrayList<ContextPageNumber> contextsPgList = getContextsFromLine(agenda, keyword);

			return contextsPgList; // will return an empty list if no contexts!			
		} catch (InvalidPasswordException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static ArrayList<ContextPageNumber> getContextsFromLine(PDDocument agenda, String keyword) throws IOException{
		ArrayList<ContextPageNumber> contextsPgList = new ArrayList<ContextPageNumber>();
		// search each page for keyword, starting from tenth page (skipt table of contents)
		for(int pageNumber = 10; pageNumber < agenda.getNumberOfPages(); pageNumber++){
		    PDFTextStripper pdfStripper = new PDFTextStripper();
		    pdfStripper.setStartPage(pageNumber);
		    pdfStripper.setEndPage(pageNumber);
		    String contents = pdfStripper.getText(agenda);  

		    // lines is an array of all lines on page
		    String[] lines = contents.split("\n");
		    for (String line : lines){
		    	if (line.contains(keyword)){
		    		line = line.replaceAll("\\r|\\n", ""); // add whole line
		    		int keyLength = keyword.length();
		    		String context = "";
		    		if (line.length() <= CONTEXT_LENGTH){
		    			context = line; // add whole line
		    		} else {
		    			// include text to right and left, up to context length global
		    			int keyIndex = line.indexOf(keyword);
		    			int paddingChars = CONTEXT_LENGTH - keyLength;
		    			
		    			int start = keyIndex - (paddingChars / 2);
		    			// catch out of bounds exception
		    			if (start < 0){
		    				start = 0;
		    			}
		    			// trim halfcut words from start of line
		    			while (start < keyIndex && line.charAt(start) != ' ')
		    				start++;
		    			
		    			int end = start + CONTEXT_LENGTH - 1;
		    			// catch out of bounds
		    			if (end > line.length())
		    				end = line.length();
		    			
		    			context = line.substring(start, end).trim();
		    		}
		    		contextsPgList.add(new ContextPageNumber(context, pageNumber));
		    		break; // only grab one context per page for less repetition
		    	}
		    }	    
		}	
		return contextsPgList;
	}	
}
