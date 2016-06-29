/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.jobs;

import clientmonitoring.BeanClient;
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
public class JobExistanceFichier implements Job{
     Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------verrifie existance fichier: ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String nomFichier = dataMap.getString("nomFichier");
        JobKey cle = context.getJobDetail().getKey();

        BeanClient beanClient = new BeanClient();
        if(beanClient.verifiExistanceFichier(nomFichier)){
            logger.log(Level.INFO, "le fichier <<" + nomFichier + ">> es OK");
        }else{
            logger.log(Level.SEVERE, "le fichier <<" + nomFichier + ">> n'existe pas");
            beanClient.envoiAlerteAuServeur(cle, 0);
            /*try {
            if (!ClientMonitoring.wsServeur.traitementAlerteTache(new Integer(cle.getName()), 0)) {
            logger.log(Level.SEVERE, " le serveur n'a pas pus traiter le problème consulter les log serveur pour plus de détail");
            } else {//on stope la tache dans le cas où le serveur à bien traité le pb
            (new BeanClient()).arreterJob(cle);
            }
            } catch (Exception e) {
            logger.log(Level.SEVERE, "impossible de contacter le serveur \n" + e);
            }*/
        }

        

    }
    
}
