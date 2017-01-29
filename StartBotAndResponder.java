import java.io.File;

import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public class StartBotAndResponder {

	public static void main(String[] args) throws SchedulerException {	
		// BasicConfigurator.configure(); // initializes log4j
		
		// for testing -> seed with downloaded pdf
		Scraper.currentAgenda = new File("temp.pdf");

		// use a different keyword to search agenda for if user inputs
		if (args.length > 0)
			AgendaBot.keyword = args[0];
	
		// WEEKLY NOTIFIER
		// run twitter bot to post a new tweet every week
		// define the job and tie it to AgendaBot
		JobDetail job1 = JobBuilder.newJob(AgendaBot.class)
				.withIdentity("myJob1", "group1")
				.build();
	
		// RESPONDER
		// bot will respond to tweets
		// define the job and tie it to the Responder
		JobDetail job2 = JobBuilder.newJob(Responder.class)
				.withIdentity("myJob2", "group1")
				.build();

		// Trigger WEEKLY BOT to run every week
		Trigger trigger1 = TriggerBuilder.newTrigger()
			.withIdentity("myTrigger1", "group1")
			.startNow()
			.withSchedule(CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
					.withIntervalInWeeks(1))
			.build();

		// Trigger RESPONDER to run every 15 seconds
		Trigger trigger2 = TriggerBuilder.newTrigger()
			    .withIdentity("mytrigger2", "group1")
			    .startNow()
			    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
			            .withIntervalInSeconds(15)
			            .repeatForever())
			    .build();
		
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		Scheduler sched = schedFact.getScheduler();
		
		sched.start();
		// Tell quartz to schedule WEEKLY bot
		sched.scheduleJob(job1, trigger1);
		// Tell quartz to schedule RESPONDER bot
		sched.scheduleJob(job2, trigger2);
	}

}
