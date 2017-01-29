import java.io.File;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public class StartResponder {

	public static void main(String[] args) throws SchedulerException {
		// for testing -> seed with downloaded pdf
		Scraper.currentAgenda = new File("temp.pdf");
		// run twitter bot to post a new reply every 5 minutes if existing
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		
		Scheduler sched = schedFact.getScheduler();
		
		sched.start();
		
		// define the job and tie it to the Responder
		JobDetail job = JobBuilder.newJob(Responder.class)
				.withIdentity("myJob", "group2")
				.build();
	
		// Trigger to run every 5 minutes
		Trigger trigger = TriggerBuilder.newTrigger()
			    .withIdentity("mytrigger", "group2")
			    .startNow()
			    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
			            .withIntervalInSeconds(30)
			            .repeatForever())
			    .build();
		
		// Tell quartz to schedule the job using our trigger
		sched.scheduleJob(job, trigger);
	}

}
