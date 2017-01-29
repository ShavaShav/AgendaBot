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

public class Responder implements Job {

	public static ArrayList<Status> cachedTweets = new ArrayList<Status>();
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		//access the twitter API using your twitter4j.properties file
        Twitter twitter = TwitterFactory.getSingleton();

        // create a new search for tweets to windsor alert
        Query query = new Query("\"@WindsorAlert\"");

        // get the results from that search
        QueryResult result;
		try {
			result = twitter.search(query);
			
		    // get the newest tweet from those results
	        Status tweetResult = result.getTweets().get(0);
	        
	        
	        
	        String[] tokens = tweetResult.getText().trim().split(" ");
	        if (tokens.length > 1){
	        	String keyword = tokens[1]; // grab first word after @WindsorAlert
	        	
	        	// get list of contexts for keyword from Scraper to pick random tweet from
	        	Scraper.CONTEXT_LENGTH = 140 - tweetResult.getUser().getScreenName().length() - AgendaBot.keyword.length() - 45; // see below
	        	ArrayList<ContextPageNumber> contexts = Scraper.getContexts(new File("temp.pdf"), keyword); // "temp.pdf" must already exist! run main agendabot first
	        	
	        	if (contexts.isEmpty()){
	        		System.out.println("No mentions of " + keyword + " in the next agenda!");
	        	} else {
	        		int randomContext = (int) Math.random() * contexts.size();
	        		ContextPageNumber context = contexts.get(randomContext);
        			// 140 characters - excluding user name, context and link
        			String tweet = ".@" + tweetResult.getUser().getScreenName() // username to reply too
        					+ "\".." + context.getContext()	// set to remainder of chars + 3
        					+ "..\"(Pg " + context.getPageNo()  // 10 chars
        					+ ") Link: " + Scraper.urlASCII		// 8 + 23 chars for URL (required for shortener)
        					+ "#page=" + context.getPageNo(); 	// links directly to page
        			
        			// reply to that tweet
        			StatusUpdate statusUpdate = 
        					new StatusUpdate(tweet);
        			statusUpdate.inReplyToStatusId(tweetResult.getId());
        			
        			twitter.updateStatus(statusUpdate);
        			System.out.println("Replied to a tweet: \"" + tweet);		
	        	}
	        } else {
	        	System.out.println("Malformed query");
	        }
		} catch (TwitterException e) {
			// unable to respond to tweet
			if (e.getErrorCode() != 187){ // do nothing for 187 error, tweet already responded too
				e.printStackTrace();
			}
			System.out.println("Already responded");
		}	
	}
}
