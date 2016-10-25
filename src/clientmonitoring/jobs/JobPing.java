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
public class JobPing implements Job {

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------effectué ping: ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        int nbTentative = dataMap.getInt("nbTentative");
        String adresseAPinger = dataMap.getString("adresseAPinger");
        boolean alerteOK = dataMap.getBoolean("alerteOK");
        JobKey cle = context.getJobDetail().getKey();
        String msg;
        boolean pingOK;
        //synchronized (this) {//section critique
            pingOK = BeanClient.pinger(adresseAPinger, nbTentative);
        //}
        if (pingOK) {
            msg = "le ping vers \"" + adresseAPinger + "\" est OK";
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
            msg = "impossible de contacter :\"" + adresseAPinger + "\"";
            if (!alerteOK) {//si l'alerte n'a pas encore été envoyer, on le fait
                logger.log(Level.SEVERE, msg);
                alerteOK = BeanClient.envoiAlerteAuServeur(cle, 0);//on met à jour la variable "alerteOK" pour que à la prochaine exécution que l'alerte ne soit plus envoyer au serveur
                context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
            } else {
                logger.log(Level.WARNING, "ce problème a déjà été signaler au serveur: " + msg);
            }

            // beanClient.envoiAlerteAuServeur(cle, 0);
        }

    }
}
