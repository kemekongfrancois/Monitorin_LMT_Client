/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.jobs;

import classeServeur.Machine;
import static clientmonitoring.ClientMonitoring.machine;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.Job;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;

/**
 *
 * @author KEF10
 */
public class JobPrincipale implements Job {
    //private Scheduler SCHEDULER;
    static boolean sousJobOK = false;
    
    Scheduler scheduler = clientmonitoring.ClientMonitoring.SCHEDULER;

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        JobKey key = context.getJobDetail().getKey();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String jobSays = dataMap.getString("jobSays");
        String userId = dataMap.getString("userId");
        Machine machine = (Machine) dataMap.get("machine");

        System.out.println("execution du job: key=" + key + " jobSays="+ jobSays+" userId="+userId + "machine=" + machine.getAdresseIP() );
        
        if(!sousJobOK) demarerSousTache();
              
        
    }
    
    public void demarerSousTache(){
        try {
            
            scheduler.start();
            
            Trigger trigger = newTrigger()
                    .withIdentity("trigger11", "group11")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(3)
                            //.withRepeatCount(2))
                            .repeatForever())
                    .build();
            JobDetail jobDetaille = newJob(JobVerificationDisk.class)
                    .withIdentity("job11", "group11")
                    .build();
            
            scheduler.scheduleJob(jobDetaille, trigger);
            sousJobOK = true;
        } catch (SchedulerException ex) {
            Logger.getLogger(JobPrincipale.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
