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
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 *
 * @author KEF10
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution//permet d'empéche les exécutions concurente, il n'exitera donc d'une instace du job
public class JobVerificationDisk implements Job {

    //public static boolean alerteOK = false;
    Scheduler scheduler = clientmonitoring.BeanClient.SCHEDULER;

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------verification disque: ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        int seuil = dataMap.getInt("seuil");
        String lettrePartition = dataMap.getString("lettrePartition");
        String ipAdresse = dataMap.getString("ipAdresse");
        boolean alerteOK = dataMap.getBoolean("alerteOK");
        JobKey cle = context.getJobDetail().getKey();
        //Tache tache = (Tache) dataMap.get("tache");
        int pourcentage;
        synchronized (this) {//section critique
             pourcentage = BeanClient.pourcentageOccupationDD(lettrePartition);
        }
        String msg;
        if (pourcentage > seuil) {//le sueil es atteind

            if (pourcentage == 200) {//cas où la lettre de partition n'es pa valide
                msg = "la lettre de partition ne correspont a aucune partition ou elle es invalide : \"" + lettrePartition + " \"";
            } else {
                msg = " espace restant du disque \"" + lettrePartition + "\"" + "de la machine\"" + ipAdresse + "\" es faible ";
            }

            if (!alerteOK) {//si l'alerte n'a pas encore été envoyer, on le fait
                logger.log(Level.SEVERE, msg);
                alerteOK = BeanClient.envoiAlerteAuServeur(cle, pourcentage);//on met à jour la variable "alerteOK" pour que à la prochaine exécution que l'alerte ne soit plus envoyer au serveur
                context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
            } else {
                logger.log(Level.WARNING, "ce problème à déja été signaler au serveur: " + msg);
            }

        } else {//le seuil d'allerte n'es pas atteind
            msg = "espase disque OK: pourcentage d'utilisation du disque es de: " + pourcentage + "%";
            if (alerteOK) {
                logger.log(Level.INFO, "Problème résolue: " + msg);
                if (BeanClient.problemeTacheResolu(cle)) {
                    alerteOK = false;
                    context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
                }
            } else {
                logger.log(Level.INFO, msg);
            }
        }

    }
}
