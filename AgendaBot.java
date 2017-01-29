import java.io.File;
import java.util.ArrayList;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class AgendaBot implements Job {

	protected static String keyword = "Ojibway"; // competition keyword
	protected static int tweetsPerExecution = 5;
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		File pdf = Scraper.getPDF(); // get pdf from site using Scraper

		//access the twitter API using twitter4j.properties file
        Twitter twitter = TwitterFactory.getSingleton();
        
        // get list of contexts for keyword from Scraper to include in tweets
		Scraper.CONTEXT_LENGTH = 140 - 28 - AgendaBot.keyword.length() - 41; // see below
		ArrayList<ContextPageNumber> contexts = Scraper.getContexts(pdf, AgendaBot.keyword) ;
        
        if (contexts.isEmpty()){
        	System.out.println("No mentions of " + keyword + " in agenda," 
        			+ " bot will check again next week");
        } else {
        	
        	int numTweets = tweetsPerExecution;
        	for (ContextPageNumber context : contexts){
        		// 140 characters - 50 chars excluding keyword and context
        		String tweet = AgendaBot.keyword 			// UNKNOWN
        				+ " in next council meeting! \".." 	// 29 chars
        				+ context.getContext()				// set to remainder of chars
        				+ "..\"(Pg " + context.getPageNo()  // 10 chars
        				+ ") Link: " + Scraper.urlASCII		// 8 + 23 chars for URL (required for shortener)
        				+ "#page=" + context.getPageNo(); 	// links directly to page
        		
        		// post tweet
        		try {
        			twitter.updateStatus(tweet);
        			System.out.println("Tweet posted:\n\"" + tweet + "\"");
        		} catch (TwitterException e) {
        			// unable to tweet
        			if (e.getErrorCode() == 187)
        				System.out.println("\"Already posted: " + tweet + "\"");
        			else
        				e.printStackTrace();
        		}
        		
        		// sleep for 30 minutes before posting next tweet about same agenda
        		try {
        			System.out.println("Waiting 15 minutes before next tweet...");
					Thread.sleep(30 * 60 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        		numTweets--;
        		if (numTweets <= 0)
        			break; // stop after numTweets
        	}
        	System.out.println("Done scheduled tweeting, will recheck agenda in a week");
        }      
	}
}
