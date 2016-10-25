/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.jobs;

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

/**
 *
 * @author KEF10
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution//permet d'empéche les exécutions concurente, il n'exitera donc d'une instace du job
public class JobVerificationService implements Job {

    //Scheduler scheduler = clientmonitoring.BeanClient.SCHEDULER;
    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------verification service: ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String nomService = dataMap.getString("nomService");
        //String ipAdresse = dataMap.getString("ipAdresse");
        boolean redemarerAuto = dataMap.getBoolean("redemarerAuto");
        boolean alerteOK = dataMap.getBoolean("alerteOK");
        JobKey cle = context.getJobDetail().getKey();

        String resultat ;
        //synchronized (this) {//section critique
            resultat = BeanClient.verifiService(nomService);
        //}
        String msg;
        if (resultat.equals(BeanClient.OK)) {//le service est en fonction
            msg = "le service \"" + nomService + "\" est en cours de fonctionnement";
            if (alerteOK) {
                logger.log(Level.INFO, "Problème résolue: " + msg);
                if (BeanClient.problemeTacheResolu(cle)) {
                    alerteOK = false;
                    context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
                }
            } else {
                logger.log(Level.INFO, msg);
            }
            return;
        }
        //le processus n'est pas en cours de fonctionnement ou il ya un pb
        int code;
        if (resultat.equals(BeanClient.KO)) {
            msg = "le service \"" + nomService + "\" n'est pas en cours de fonctionnement";

            if (redemarerAuto) {//le redemarage automatique est activé sur cette tache
                if (BeanClient.demarerService(nomService)) {//on redemarer le service
                    logger.log(Level.INFO, "le service \"" + nomService + "\" a été redémarer");
                    code = -1;
                } else {//le service n'a pas pus être redémarer
                    logger.log(Level.SEVERE, "le service \"" + nomService + "\" n'a pas pus être redémarer");
                    code = 0;
                }
            } else {//le redemarage automatique est désactivé
                System.out.println("le redémarage automatique n'est pas activé pour le service \"" + nomService + ">");
                code = 0;
            }

        } else {//il ya eu un pb
            msg = "le service \"" + nomService + "\" n'est pas valide";
            code = 1;
        }
            if (!alerteOK) {//si l'alerte n'a pas encore été envoyer, on le fait
                logger.log(Level.SEVERE, msg);
                alerteOK = BeanClient.envoiAlerteAuServeur(cle, code);//on met à jour la variable "alerteOK" pour que à la prochaine exécution que l'alerte ne soit plus envoyer au serveur
                context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
            } else {
                logger.log(Level.WARNING, "ce problème a déjà été signaler au serveur: " + msg);
            }
        
        

    }
}
