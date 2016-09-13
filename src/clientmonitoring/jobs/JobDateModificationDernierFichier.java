/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.jobs;

import clientmonitoring.BeanClient;
import java.util.Calendar;
import java.util.Date;
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
public class JobDateModificationDernierFichier implements Job {

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------verrifie date modification dernier fichier dans repertoire ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String nomRepertoire = dataMap.getString("nomRepertoire");
        int seuil = dataMap.getInt("seuil");
        boolean alerteOK = dataMap.getBoolean("alerteOK");
        JobKey cle = context.getJobDetail().getKey();

        Date dateModifDernierFichier;
        //synchronized (this) {//section critique
            dateModifDernierFichier = BeanClient.dateDernierFichier(nomRepertoire);
        //}
        String msg;
        if (dateModifDernierFichier != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(dateModifDernierFichier);
            c.add(Calendar.MINUTE, seuil);//on ajouter le seuil 
            Date dateModifDernierFichierEtseuil = c.getTime();

            if (dateModifDernierFichierEtseuil.after(new Date())) {//on comparer la date de dernier modification + le seuil à la date courante
                msg = "la date de derniere modification (" + dateModifDernierFichier + ") es OK pour le repertoire \"" + nomRepertoire + "\" ";
                if (alerteOK) {
                    logger.log(Level.INFO, "Problème résolue: " + msg);
                    if (BeanClient.problemeTacheResolu(cle)) {
                        alerteOK = false;
                        context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
                    }
                } else {
                    logger.log(Level.INFO, msg);
                }
                return;
            }
            //la date courante es supérieure à la date de dernière modification + le seuil
            msg = "la date de derniere modification (" + dateModifDernierFichier + ") es antérieur à la date autorisé pour le repertoire \"" + nomRepertoire + "\" ";

        } else {
            msg = null;
        }
        if (!alerteOK) {//si l'alerte n'a pas encore été envoyer, on le fait
            if (msg != null) {
                logger.log(Level.SEVERE, msg);
            }
            alerteOK = BeanClient.envoiAlerteAuServeur(cle, 0);//on met à jour la variable "alerteOK" pour que à la prochaine exécution que l'alerte ne soit plus envoyer au serveur
            context.getJobDetail().getJobDataMap().put("alerteOK", alerteOK);
        } else {
            logger.log(Level.WARNING, "ce problème à déja été signaler au serveur: " + msg);
        }

    }

}
