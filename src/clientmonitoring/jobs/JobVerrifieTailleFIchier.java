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
public class JobVerrifieTailleFIchier implements Job {

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------verrifie taille fichier ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String nomFichier = dataMap.getString("nomFichier");
        int seuil = dataMap.getInt("seuil");
        JobKey cle = context.getJobDetail().getKey();

        BeanClient beanClient = new BeanClient();
        long taille = beanClient.tailleFichier(nomFichier);
        
        
        if (taille!=-1) {
            taille = taille / (1024 * 1024);//on mais la taille en Mo
            if (seuil < 0) {//cas où on verrifie que le fichier à surveille es toujour plus grand que le seuil
                seuil *= -1;//on rend le seuil positif
                if(taille<seuil){
                    logger.log(Level.SEVERE, "Alerte: le fichier <<" + nomFichier + ">> es inférieure à la taille autorisé");
                    beanClient.envoiAlerteAuServeur(cle, 0);
                }else{
                    logger.log(Level.INFO, "la taille du fichier <<" + nomFichier + ">> es OK");
                }
            } else {//cas où on verrifie que le fichier à surveille es toujour plus petit que le seuil
                if(taille>seuil){
                    logger.log(Level.SEVERE, "Alerte: le fichier <<" + nomFichier + ">> es supérieure à la taille autorisé");
                    beanClient.envoiAlerteAuServeur(cle, 0);
                }else{
                    logger.log(Level.INFO, "la taille du fichier <<" + nomFichier + ">> es OK");
                }
            }
        }else{//il ya eu un pb lors de l'éxécution de la fonction
            logger.log(Level.SEVERE, "le fichier <<" + nomFichier + ">> n'es pas valide ou il ya eu un pb inconue");
            beanClient.envoiAlerteAuServeur(cle, -1);
        }

    }

}
