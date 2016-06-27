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
        boolean redemarerAuto = dataMap.getBoolean("redemarerAuto");
        JobKey cle = context.getJobDetail().getKey();

        BeanClient beanClient = new BeanClient();
        String resultat = beanClient.serviceWindowsEnFonctionnement(nomService);

            int code ;//
        if (resultat.equals(ClientMonitoring.OK)) {//le service es en fonction
            logger.log(Level.INFO, "le service <<" + nomService + ">> es en cour de fonctionnement");
            code = -1;
        } else {//le processus n'es pas en cour de fonctionnement ou il ya un pb
            if (resultat.equals(ClientMonitoring.KO)) {
                logger.log(Level.SEVERE, "le service <<" + nomService + ">> n'es pas en cour de fonctionnement");
                
                if (redemarerAuto) {//le redemarage automatique es activé sur cette tache
                    if (beanClient.demarerServiceWindows(nomService)) {//on redemarer le service
                        logger.log(Level.INFO, "le service <<" + nomService + ">> a été redémarer");
                        code = -1;
                    } else {//le service n'a pas pus être redémarer
                        logger.log(Level.SEVERE, "le service <<" + nomService + ">> n'a pas pus être redémarer");
                        code = 0;
                    }
                }else{//le redemarage automatique es désactivé
                    logger.log(Level.INFO, "le redémarage automatique n'es pas activé pour le service <<" + nomService +">");
                    code = 0;
                }
                
            } else {//il ya eu un pb
                logger.log(Level.SEVERE, "le service <<" + nomService + ">> n'es pas valide");
                code = 1;
            }
            
        }
        if(code!=-1){//il ya eu un pb
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
