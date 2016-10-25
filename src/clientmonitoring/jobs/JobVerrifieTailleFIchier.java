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
public class JobVerrifieTailleFIchier implements Job {

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------verrifie taille fichier ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String nomFichier = dataMap.getString("nomFichier");
        int seuil = dataMap.getInt("seuil");
        boolean alerteOK = dataMap.getBoolean("alerteOK");
        JobKey cle = context.getJobDetail().getKey();
        String msg = null;
        long taille;
        taille = BeanClient.tailleFichier(nomFichier);
        int code = 20;
        if (taille != -1) {
            taille = taille / (1024);//on mais la taille en Ko
            if (seuil < 0) {//cas où on verrifie que le fichier à surveille est toujour plus grand que le seuil
                seuil *= -1;//on rend le seuil positif
                if (taille < seuil) {
                    msg = "Alerte: le fichier \"" + nomFichier + "\" est inférieure à la taille autorisé";
                    code = 0;
                }
            } else//cas où on verrifie que le fichier à surveille est toujour plus petit que le seuil
            {
                if (taille > seuil) {
                    msg = "Alerte: le fichier \"" + nomFichier + "\" est supérieure à la taille autorisé";
                    code = 0;
                }
            }
        } else {//il ya eu un pb lors de l'éxécution de la fonction
            msg = "le fichier \"" + nomFichier + "\" n'est pas valide ou il ya eu un pb inconue";
            code = -1;
        }

        if (code == 20) {//tous c'est bien passé
            msg = "la taille du fichier \"" + nomFichier + "\" est OK";
            if (alerteOK) {
                logger.log(Level.INFO, "Problème résolue: " + msg);
                if (BeanClient.problemeTacheResolu(cle)) {
                    alerteOK = false;
                    context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
                }
            } else {
                logger.log(Level.INFO, msg);
            }
        } else//il ya eu un pb
        if (!alerteOK) {//si l'alerte n'a pas encore été envoyer, on le fait
            logger.log(Level.SEVERE, msg);
            alerteOK = BeanClient.envoiAlerteAuServeur(cle, code);//on met à jour la variable "alerteOK" pour que à la prochaine exécution que l'alerte ne soit plus envoyer au serveur
            context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
        } else {
            logger.log(Level.WARNING, "ce problème a déjà été signaler au serveur: " + msg);
        }

    }

}
