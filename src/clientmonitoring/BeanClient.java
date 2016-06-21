/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring;

import classeServeur.Machine;
import classeServeur.Tache;
import static clientmonitoring.ClientMonitoring.START;
import static clientmonitoring.ClientMonitoring.TACHE_DD;
import clientmonitoring.jobs.JobPrincipale;
import clientmonitoring.jobs.JobVerificationDisk;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 *
 * @author KEF10
 */
public class BeanClient {
    public static Map<JobKey,Tache> TACHE_EN_COUR_D_EXECUTION = new HashMap<>();
    
    public static Scheduler SCHEDULER;
    
    static Logger logger= clientmonitoring.ClientMonitoring.LOGGER; 
    
    public boolean demarerLeScheduler(){
        try {
            SCHEDULER = StdSchedulerFactory.getDefaultScheduler();
            SCHEDULER.start();
            logger.log(Level.INFO, "SCHEDULER démarer ");
            return true;
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, "le SCHEDULER à bien démaré", ex);
            return false;
        }
    }
    
    public boolean arreterLeScheduler(){
        try {
            SCHEDULER.shutdown();
            TACHE_EN_COUR_D_EXECUTION.clear();
            logger.log(Level.INFO, "SCHEDULER Arreté ");
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
        String identifiant = tache.getIdTache()+"";
        String groupe = tache.getIdMachine().getIdMachine() + "";
        JobKey cle = JobKey.jobKey(identifiant, groupe);
        
        //ajouter les données
        //JobDataMap data = new JobDataMap();
        //data.put("tache", tache);

        JobDetail jobDetaille = newJob(JobVerificationDisk.class)
                .withIdentity(cle)
                .usingJobData("seuil", tache.getSeuilAlerte())
                .usingJobData("lettrePartition", tache.getNom())
                .usingJobData("ipAdresse", tache.getIdMachine().getAdresseIP())
                //.usingJobData(data)
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
            String identifiant = tache.getIdTache()+"";
        String groupe = tache.getIdMachine().getIdMachine() + "";
        JobKey cle = JobKey.jobKey(identifiant, groupe);

            if (!tache.getStatue().equals(START) || SCHEDULER.checkExists(cle)) {//si le job existe déja on le stoppe
                arreterJob(cle);//suppression du job
                if(!tache.getStatue().equals(START)){//cas où on veux stopper la tache ou la mettre en pause
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
                    logger.log(Level.WARNING, TypeDeTache + ": ce type n'es pas reconnue ");
                    return false;
            }

            SCHEDULER.scheduleJob(jobDetaille, trigger);//on démare la tache
            logger.log(Level.INFO, "tache démaré. cle=" + cle );
            TACHE_EN_COUR_D_EXECUTION.put(cle, tache);
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
    
    public boolean arreterJob(JobKey cle){
        try {
            if(SCHEDULER.checkExists(cle)){
            SCHEDULER.deleteJob(cle);//suppression du job
            logger.log(Level.INFO, "tache stoppé. cle=" + cle );
            }
            TACHE_EN_COUR_D_EXECUTION.remove(cle);//on le retire des taches en cour d'exécution
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
            String identifiant = machine.getIdMachine()+"";
            String groupe = machine.getAdresseIP();//le groupe sera l'adresse IP
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
            logger.log(Level.INFO, "Toutes les taches ont été démarer: nombre de tache="+TACHE_EN_COUR_D_EXECUTION.size());
            
            // on démarer le job principale
            SCHEDULER.scheduleJob(jobDetaille, trigger);
            logger.log(Level.INFO, "la tache principale à bien été lancé. cle=" + cle);
            return true;
            
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, "pb lors de l'éxécution du scheduler ", ex);
            return false;
        }
        
    }
    
    /**
     * 
     * @param lettreDD
     * @return "200" dans le cas où lettre de partition ne corespond à aucune dd
     */
    public int pourcentageOccupationDD(String lettreDD) {
        File cwd = new File(lettreDD);
        //File cwd = new File("l:");
        if(!cwd.exists()) return 200;
        float espaleTotal = cwd.getTotalSpace();
        float espaceLIbre = cwd.getFreeSpace();
        float espaceUtilise = cwd.getUsableSpace();
        
        float pourcentage = 100 - (espaceLIbre/espaleTotal)*100;
         /*
        System.out.println("pourcentage restant:  "+ pourcentage+"%");
        System.out.println("Espace total:  "+ espaleTotal/ (1024 * 1024) + " MBt"  );
        System.out.println("Espace Libre:  "+ espaceLIbre / (1024 * 1024) + " MBt");
        System.out.println("Espace Utilisé:  "+ espaceUtilise / (1024 * 1024) + " MBt");
        */
        
        return (int) pourcentage;
        
    }
    
    
}
