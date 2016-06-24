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
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 *
 * @author KEF10
 */
public class JobVerificationService implements Job {

    //Scheduler scheduler = clientmonitoring.BeanClient.SCHEDULER;
    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------verification service: ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String nomService = dataMap.getString("nomService");
        String ipAdresse = dataMap.getString("ipAdresse");
        JobKey cle = context.getJobDetail().getKey();

        BeanClient beanClient = new BeanClient();
        String resultat = beanClient.serviceWindowsEnFonctionnement(nomService);

        if (resultat.equals(ClientMonitoring.OK)) {//le service es en fonction
            logger.log(Level.INFO, "le service <<" + nomService + ">> es en cour de fonctionnement");
        } else {//le processus n'es pas en cour de fonctionnement ou il ya un pb
            int code ;//
            if (resultat.equals(ClientMonitoring.KO)) {
                logger.log(Level.WARNING, "le service <<" + nomService + ">> n'es pas en cour de fonctionnement");
                code = 0;
            } else {//il ya eu un pb
                logger.log(Level.SEVERE, "le service <<" + nomService + ">> n'es pas valide");
                code = 1;
            }
            try {
                if (!ClientMonitoring.wsServeur.traitementAlerteTache(new Integer(cle.getName()), code)) {
                    logger.log(Level.SEVERE, " le serveur n'a pas pus traiter le problème consulter les log serveur pour plus de détail");
                } else {//on stope la tache dans le cas où le serveur à bien traité le pb
                    (new BeanClient()).arreterJob(cle);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "impossible de contacter le serveur \n" + e);
            }
        }

    }
}
