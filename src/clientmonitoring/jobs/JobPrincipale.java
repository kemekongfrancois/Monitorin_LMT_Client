/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.jobs;

import classeServeur.Machine;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.Job;
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
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;

/**
 *
 * @author KEF10
 */
public class JobPrincipale implements Job {
    //protected static Logger logger = Logger.getLogger(Class.class.getName());

    Logger logger= clientmonitoring.ClientMonitoring.LOGGER;
    /*public JobPrincipale() {
    clientmonitoring.until.Until.initialisationGestionFichierLog(logger);
    }*/
    
    
    
    Scheduler scheduler = clientmonitoring.BeanClient.SCHEDULER;

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        JobKey key = context.getJobDetail().getKey();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        logger.log(Level.INFO, "le job principale c'est bien exécuté");
           // Logger.getLogger(JobPrincipale.class.getName()).log(Level.INFO, "le job principale c'est bien exécuté");
          
    }
    /*
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
    */
}
