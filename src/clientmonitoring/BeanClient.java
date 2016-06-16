/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring;

import classeServeur.Machine;
import classeServeur.Tache;
import static clientmonitoring.ClientMonitoring.STOP;
import static clientmonitoring.ClientMonitoring.TACHE_DD;
import clientmonitoring.jobs.JobPrincipale;
import clientmonitoring.jobs.JobVerificationDisk;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.JobDataMap;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;

/**
 *
 * @author KEF10
 */
public class BeanClient {
    public static Scheduler SCHEDULER;
    
    static Logger logger= clientmonitoring.ClientMonitoring.LOGGER; 
    
    public boolean demarerLeScheduler(){
        try {
            SCHEDULER = StdSchedulerFactory.getDefaultScheduler();
            SCHEDULER.start();
            return true;
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, "le SCHEDULER à bien démaré", ex);
            return false;
        }
    }
    
    public boolean arreterLeScheduler(){
        try {
            SCHEDULER.shutdown();
            return true;
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, "impossible de stopper le SCHEDULER", ex);
            return false;
        }
    }
    

    /**
     * cette fonction es un complement à la fonction de démarage de tache
     * elle permet de démarer les taches de type verifie DD
     */
    private JobDetail initialiseVerificationDD(Tache tache) {
        String identifiant = tache.getTachePK().getCleTache();
        String groupe = tache.getTachePK().getIdMachine() + "";
        JobKey cle = JobKey.jobKey(identifiant, groupe);

        JobDetail jobDetaille = newJob(JobVerificationDisk.class)
                .withIdentity(cle)
                .build();

        return jobDetaille;
    }

    /**
     * cette fonction permet de démarrer ou de mette à jour n'importe quelle tache
     * @param tache
     * @return true si tous c'est bien passé
     */
    public boolean demarerMetAJourOUStopperTache(Tache tache) {
        try {
            String identifiant = tache.getTachePK().getCleTache();
            String groupe = tache.getTachePK().getIdMachine() + "";
            JobKey cle = JobKey.jobKey(identifiant, groupe);

            if (tache.getStatue().equals(STOP) || SCHEDULER.checkExists(cle)) {//si le job existe déja on le stoppe
                arreterJob(cle);//suppression du job
                if(tache.getStatue().equals(STOP)){//cas où on veux stopper la tache
                    return true;//le job a été stoppé
                }
            }
            //-------- on définie la périodicité--------
            Trigger trigger = newTrigger()
                    .withIdentity(identifiant,groupe)
                    .startNow()
                    .startNow()
                    .withSchedule(cronSchedule(tache.getPeriodeVerrification()))
                    .build();
            
            //---- on initialise le job en fonction de son type-------
            JobDetail jobDetaille = null;
            String TypeDeTache = tache.getTypeTache();
            switch (TypeDeTache) {
                case TACHE_DD://cas de la tache de vérification de dd
                    jobDetaille = initialiseVerificationDD(tache);
                    break;
                default:
                    String msg = TypeDeTache + ": ce type n'es pas reconnue \n";
                    logger.log(Level.WARNING, msg);
                    return false;
            }

            SCHEDULER.scheduleJob(jobDetaille, trigger);//on démare la tache
            logger.log(Level.INFO, "tache démaré. cle=" + cle );
            return true;
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        
    }
    
    private void demarerListeTAche(List<Tache> listTache){
            for(Tache tache:listTache){
                demarerMetAJourOUStopperTache(tache);
            }
        }
    
    private boolean arreterJob(JobKey cle){
        try {
            SCHEDULER.deleteJob(cle);//suppression du job
            logger.log(Level.INFO, "tache stoppé. cle=" + cle );
            return true;
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
    }
    /**
     * cette fonction démare(ou redémare) la tache principale ainsi que la liste des taches associé
     * @param machine
     * @return 
     */
    public boolean redemarerTachePrincipaleEtSousTache(Machine machine,List<Tache> listTache){
        try {
            String groupe = machine.getIdMachine()+"";//le groupe sera l'identifiant de la machine
            String identifiant = machine.getAdresseIP();//l'identifiant sera l'adresse IP 
            JobKey cle = JobKey.jobKey(identifiant, groupe);
             
            if (SCHEDULER.checkExists(cle)) {//si le job existe déja on le stoppe
                arreterJob(cle);
            }
            
            //ajouter les données
            JobDataMap data = new JobDataMap();
            
        
            // on définie le job
            JobDetail jobDetaille = newJob(JobPrincipale.class)
                    .withIdentity(cle)
                    .usingJobData(data)
                    .build();

            //on défini la périodicité
            Trigger trigger = newTrigger()
                    .withIdentity(identifiant,groupe)
                    .startNow()
                    .withSchedule(cronSchedule(machine.getPeriodeDeCheck()))
                    .build();
            
            //on démare les taches
            demarerListeTAche(listTache);
            logger.log(Level.INFO, "Toutes les taches ont été démarer");
            
            // on démarer le job principale
            SCHEDULER.scheduleJob(jobDetaille, trigger);
            logger.log(Level.INFO, "la tache principale à bien été lancé. cle=" + cle);
            return true;
            
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, "pb lors de l'éxécution du scheduler ", ex);
            return false;
        }
        
    }
    
}
