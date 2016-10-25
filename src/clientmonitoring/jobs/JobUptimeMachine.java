/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.jobs;

import clientmonitoring.BeanClient;
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
public class JobUptimeMachine implements Job {

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------verrifie la date de dernier demarage: ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        boolean alerteOK = dataMap.getBoolean("alerteOK");
        int seuil = dataMap.getInt("nbDeJourPourAlerte");
        JobKey cle = context.getJobDetail().getKey();
        String msg;
        int nbJour = BeanClient.uptimeMachine();
        if (nbJour < 0 || nbJour > seuil) {//il yà un pb si le nombre de jour appartiend à ]-inf;0[ U ]seuil;+inf[

            if (nbJour < 0) {
                msg = "Impossible de connaitre le nombre de jour depuis le quelle la machine est allumer une exception c'est produit lors de l’exécution de la commande";
            } else {
                msg = "La machine est allumer depuis " + nbJour + " jours hors le seuil est de " + seuil;
            }
            if (!alerteOK) {//si l'alerte n'a pas encore été envoyer, on le fait
                logger.log(Level.SEVERE, msg);
                alerteOK = BeanClient.envoiAlerteAuServeur(cle, nbJour);//on met à jour la variable "alerteOK" pour que à la prochaine exécution que l'alerte ne soit plus envoyer au serveur
                context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
            } else {
                logger.log(Level.WARNING, "ce problème a déjà été signaler au serveur: " + msg);
            }

        } else {
            msg = "la machine est alumer depuis " + nbJour + " jours ce qui est OK";
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
