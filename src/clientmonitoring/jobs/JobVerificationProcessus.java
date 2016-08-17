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
import org.quartz.Scheduler;

/**
 *
 * @author KEF10
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution//permet d'empéche les exécutions concurente, il n'exitera donc d'une instace du job
public class JobVerificationProcessus implements Job {

    //Scheduler scheduler = clientmonitoring.BeanClient.SCHEDULER;
    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------verification processus: ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String nomProcessus = dataMap.getString("nomProcessus");
        //String ipAdresse = dataMap.getString("ipAdresse");
        boolean alerteOK = dataMap.getBoolean("alerteOK");
        JobKey cle = context.getJobDetail().getKey();

        BeanClient beanClient = new BeanClient();
        String resultat = beanClient.verifiProcessusWindows(nomProcessus);
        String msg;
        if (resultat.equals(BeanClient.OK)) {//le processus es en fonction
            msg = "le processus <<" + nomProcessus + ">> es en cour de fonctionnement";
            if (alerteOK) {
                logger.log(Level.INFO, "Problème résolue: " + msg);
                if (BeanClient.problemeTacheResolu(cle)) {
                    alerteOK = false;
                    context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
                }
            } else {
                logger.log(Level.INFO, msg);
            }
        } else {//le processus n'es pas en cour de fonctionnement ou il ya un pb
            int code;//
            if (resultat.equals(BeanClient.KO)) {
                msg = "le processus <<" + nomProcessus + ">> n'es pas en cour de fonctionnement";
                code = 0;
            } else {//il ya eu un pb
                msg = "le processus <<" + nomProcessus + ">> n'es pas valide";
                code = 1;
            }
            if (!alerteOK) {//si l'alerte n'a pas encore été envoyer, on le fait
                logger.log(Level.SEVERE, msg);
                alerteOK = BeanClient.envoiAlerteAuServeur(cle, code);//on met à jour la variable "alerteOK" pour que à la prochaine exécution que l'alerte ne soit plus envoyer au serveur
                context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
            } else {
                logger.log(Level.WARNING, "ce problème à déja été signaler au serveur: " + msg);
            }

        }

    }
}
