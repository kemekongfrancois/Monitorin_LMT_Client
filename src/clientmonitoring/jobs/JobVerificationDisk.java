/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.jobs;

import classeServeur.Machine;
import classeServeur.Tache;
import clientmonitoring.BeanClient;
import clientmonitoring.ClientMonitoring;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 *
 * @author KEF10
 */
public class JobVerificationDisk implements Job {

    Scheduler scheduler = clientmonitoring.BeanClient.SCHEDULER;

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------verification disque: ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        int seuil = dataMap.getInt("seuil");
        String lettrePartition = dataMap.getString("lettrePartition");
        String ipAdresse = dataMap.getString("ipAdresse");
        JobKey cle = context.getJobDetail().getKey();
        //Tache tache = (Tache) dataMap.get("tache");
        BeanClient beanClient = new BeanClient();
        int pourcentage = beanClient.pourcentageOccupationDD(lettrePartition);

        if (pourcentage > seuil) {//le sueil es atteind

            fontionDeTraitementDeLAlerte(cle, ipAdresse, pourcentage,lettrePartition);
        } else {//le seuil d'allerte n'es pas atteind
            logger.log(Level.INFO, "espase disque OK: pourcentage d'utilisation du disque es de: " + pourcentage + "%");            
        }

        
    }

    private void fontionDeTraitementDeLAlerte(JobKey cle, String ipAdresse, int pourcentage,String lettrePartition) {
        if (pourcentage == 200) {//cas où la lettre de partition n'es pa valide
            logger.log(Level.SEVERE, "la lettre de partition ne correspont a aucune partition ou elle es invalide : <<" + lettrePartition + " >>");
        } else {
            logger.log(Level.SEVERE, " espace restant du disque <<" + lettrePartition + ">>" + "de la machine<<" + ipAdresse + ">> es faible ");
        }

        try {
            if (!ClientMonitoring.wsServeur.traitementAlerteTache(new Integer(cle.getName()),pourcentage)) {
                logger.log(Level.SEVERE, " le serveur n'a pas pus traiter le problème consulter les log serveur pour plus de détail");
            } else {//on stope la tache dans le cas où le serveur à bien traité le pb
                (new BeanClient()).arreterJob(cle);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "impossible de contacter le serveur \n" + e);
            /*
            try {
                scheduler.deleteJob(cle);//on supprime le job affin que je job principale puisse le redémarer car il fait tjr partir de la liste des jobs en cours d'éxécution
            } catch (SchedulerException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            */
        }

    }

}
