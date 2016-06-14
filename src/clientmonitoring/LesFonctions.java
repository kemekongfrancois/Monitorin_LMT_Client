/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring;

import classeServeur.Tache;
import static clientmonitoring.ClientMonitoring.STOP;
import static clientmonitoring.ClientMonitoring.TACHE_DD;
import clientmonitoring.jobs.JobVerificationDisk;
import clientmonitoring.until.Until;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;

/**
 *
 * @author KEF10
 */
public class LesFonctions {

    Scheduler scheduler = clientmonitoring.ClientMonitoring.SCHEDULER;

    /**
     * cette fonction es un complement à la fonction de démarage de tache
     * elle permet de démarer les taches de type verifie DD
     */
    private JobDetail initialiseVerificationDD(Tache tache) {
        String cle = tache.getTachePK().getCleTache();
        String groupe = tache.getTachePK().getIdMachine() + "";

        JobDetail jobDetaille = newJob(JobVerificationDisk.class)
                .withIdentity(cle, groupe)
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
            String cle = tache.getTachePK().getCleTache();
            String groupe = tache.getTachePK().getIdMachine() + "";

            if (tache.getStatue().equals(STOP) || scheduler.checkExists(JobKey.jobKey(cle, groupe))) {//si le job existe déja ou si on le stoppè
                scheduler.deleteJob(JobKey.jobKey(cle, groupe));//si ce job existe déja on le suprime
                if(tache.getStatue().equals(STOP)){//cas où on veux stopper la tache
                    System.out.println("tache stoppé. cle=" + cle + ", groupe=" + groupe);
                    return true;//le job a été stoppé
                }
            }
            //-------- on définie la périodicité--------
            Trigger trigger = newTrigger()
                    .withIdentity(cle, groupe)
                    .startNow()
                    .startNow()
                    .withSchedule(cronSchedule(tache.getPeriodeVerrification()))
                    .build();
            
            //---- on initialise le job e fonction de son type-------
            JobDetail jobDetaille = null;
            String TypeDeTache = tache.getTypeTache();
            switch (TypeDeTache) {
                case TACHE_DD://cas de la tache de vérification de dd
                    jobDetaille = initialiseVerificationDD(tache);
                    break;
                default:
                    String msg = TypeDeTache + ": ce type n'es pas reconnue \n";
                    System.out.println(msg);
                    Until.savelog(msg, Until.fichieLog);
                    return false;
            }

            scheduler.scheduleJob(jobDetaille, trigger);//on démare la tache
            System.out.println("la tache à bien été demarer. cle=" + cle + ", groupe=" + groupe);
            return true;
        } catch (SchedulerException ex) {
            Logger.getLogger(LesFonctions.class.getName()).log(Level.SEVERE, null, ex);
            Until.savelog("pb sur le scheduler \n" + ex, Until.fichieLog);
            return false;
        }
        
    }
    
    public void demarerListeTAche(List<Tache> listTache){
            for(Tache tache:listTache){
                demarerMetAJourOUStopperTache(tache);
            }
        }
}
