import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import twitter4j.TwitterException;

public class StartBot {

	public static final boolean DEBUG = false; // false to post to twitter
	
	public static void test(){
		System.out.println("Using downloaded agenda...");
		File file = new File("temp.pdf");
		Scraper.CONTEXT_LENGTH = 140 - 28 - AgendaBot.keyword.length() - 41; // see below
		ArrayList<ContextPageNumber> contexts = Scraper.getContexts(file, AgendaBot.keyword) ;
		if (contexts.isEmpty()){
			System.out.println("No matches in pdf");
		} else {
        	int numTweets = AgendaBot.tweetsPerExecution;
			for (ContextPageNumber context : contexts){
				// 140 characters
        		String tweet = AgendaBot.keyword 			// UNKNOWN
        				+ " in next council meeting! \".." 	// 29 chars
        				+ context.getContext()				// set to remainder of chars
        				+ "..\"(Pg " + context.getPageNo()   // 10 chars
        				+ ") Link: " + Scraper.urlASCII		// 8 + 23 chars for URL (required for shortener)
        				+ "#page=" + context.getPageNo() ; // links directly to page
				System.out.println("Sample Tweet:\n" + tweet + '\n');	
				numTweets--;
        		if (numTweets <= 0)
        			break; // stop after numTweets (5)
			}			
		}
	}
	
	public static void main(String[] args) throws SchedulerException {

		// BasicConfigurator.configure(); // initializes log4j

		// use a different keyword to search agenda for if user inputs
		if (args.length > 0)
			AgendaBot.keyword = args[0];
	
		if (DEBUG){
			test();
		} else {
			// run twitter bot to post a new tweet every week
			SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
			
			Scheduler sched = schedFact.getScheduler();
			
			sched.start();
			
			// define the job and tie it to AgendaBot
			JobDetail job = JobBuilder.newJob(AgendaBot.class)
					.withIdentity("myJob", "group1")
					.build();
		
			// Trigger to run every week
			Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity("myTrigger", "group1")
				.startNow()
				.withSchedule(CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
						.withIntervalInWeeks(1))
				.build();
			
			// Tell quartz to schedule the job using our trigger
			sched.scheduleJob(job, trigger);

		}			
	}
}