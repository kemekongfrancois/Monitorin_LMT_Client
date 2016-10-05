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
public class JobVerrifieLien implements Job {

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------Tester de Lien: ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        int nbTentative = dataMap.getInt("nbTentative");
        String lien = dataMap.getString("lien");
        boolean alerteOK = dataMap.getBoolean("alerteOK");
        JobKey cle = context.getJobDetail().getKey();
        String msg;
        String lienOK = BeanClient.testLien(lien, nbTentative);
        if (lienOK.equals(BeanClient.OK)) {
            msg = "le lien \"" + lien + "\" est OK";
            if (alerteOK) {
                logger.log(Level.INFO, "Problème résolue: " + msg);
                if (BeanClient.problemeTacheResolu(cle)) {
                    alerteOK = false;
                    context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
                }
            } else {
                logger.log(Level.INFO, msg);
            }
        } else {
            int code;
            if (lienOK.equals(BeanClient.PB)) {
                msg = "une exception c'est produit lors du test du lien :\"" + lien + "\"";
                code = -1;
            } else {
                msg = "impossible de contacter :\"" + lien + "\"";
                code = 0;
            }
            
            if (!alerteOK) {//si l'alerte n'a pas encore été envoyer, on le fait
                logger.log(Level.SEVERE, msg);
                alerteOK = BeanClient.envoiAlerteAuServeur(cle, code);//on met à jour la variable "alerteOK" pour que à la prochaine exécution que l'alerte ne soit plus envoyer au serveur
                context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
            } else {
                logger.log(Level.WARNING, "ce problème à déja été signaler au serveur: " + msg);
            }

            // beanClient.envoiAlerteAuServeur(cle, 0);
        }

    }
}
