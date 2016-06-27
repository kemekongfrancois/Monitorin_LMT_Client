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
public class JobPing implements Job{
     Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------effectué ping: ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String ipAdresse = dataMap.getString("ipAdresse");
        int nbTentative = dataMap.getInt("nbTentative");
        String adresseAPinger = dataMap.getString("adresseAPinger");
        JobKey cle = context.getJobDetail().getKey();

        BeanClient beanClient = new BeanClient();
        if(beanClient.pinger(adresseAPinger, nbTentative)){
            logger.log(Level.INFO, "le ping vers <<" + adresseAPinger + ">> es OK");
        }else{
            logger.log(Level.SEVERE, "impossible de contacter :<<" + adresseAPinger + ">>");
            try {
                if (!ClientMonitoring.wsServeur.traitementAlerteTache(new Integer(cle.getName()), 0)) {
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
