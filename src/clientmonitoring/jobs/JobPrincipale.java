/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.jobs;

import classeServeur.Machine;
import classeServeur.Tache;
import clientmonitoring.BeanClient;
import static clientmonitoring.BeanClient.TACHE_EN_COUR_D_EXECUTION;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
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
        
        //on verrifie que les taches sont en cour de fonctionnement
        List<Tache> listTachePB = new ArrayList<>();
        for(Entry<JobKey, Tache> e : TACHE_EN_COUR_D_EXECUTION.entrySet()) {
            try {
                //Entry<JobKey, Tache> e = it.next();
                if(!scheduler.checkExists(e.getKey())){//si une tache n'es pas en cour de fonctionnement on l'ajoute dans la liste des taches à pb
                    listTachePB.add(e.getValue());
                    logger.log(Level.SEVERE, "la tache "+e.getKey()+" n'es pas en cour de fonctionnement bien vouloir verrifier les log");
                }else{//la tache es bien en marche
                    logger.log(Level.INFO, e.getKey()+": est bien en cour de fonctionnement");
                }
                
            } catch (SchedulerException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
      }
        traitementDesTachesAProbleme(listTachePB);
        logger.log(Level.INFO, "le job principale c'est bien exécuté");
           // Logger.getLogger(JobPrincipale.class.getName()).log(Level.INFO, "le job principale c'est bien exécuté");
          
    }
    /**
     * cette fonction vas effectué le traiment choisi en cas d'arrét d'un job
     * pour le moment il vas redémarer le job
     * @param listTachePB 
     */
    public void traitementDesTachesAProbleme(List<Tache> listTachePB){
        BeanClient beanClient = new BeanClient();
        for(Tache tache:listTachePB){
            beanClient.demarerMetAJourOUStopperTache(tache);
            System.out.println(" traitement de la tache" +tache);
        }
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
