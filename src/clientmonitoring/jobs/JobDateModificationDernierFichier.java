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
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 *
 * @author KEF10
 */
public class JobDateModificationDernierFichier implements Job {

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------verrifie date modification dernier fichier dans repertoire ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String nomRepertoire = dataMap.getString("nomRepertoire");
        int seuil = dataMap.getInt("seuil");
        JobKey cle = context.getJobDetail().getKey();

        BeanClient beanClient = new BeanClient();
        Date dateModifDernierFichier = beanClient.dateDernierFichier(nomRepertoire);
        int code;
        if (dateModifDernierFichier == null) {
            code = -1;
        } else {
            Calendar c = Calendar.getInstance();
            c.setTime(dateModifDernierFichier);
            c.add(Calendar.MINUTE, seuil);//on ajouter le seuil 
            Date dateModifDernierFichierEtseuil = c.getTime();
            if (dateModifDernierFichierEtseuil.after(new Date())) {//on comparer la date de dernier modification + le seuil à la date courante
                logger.log(Level.INFO, "la date de derniere modification (" + dateModifDernierFichier + ") es OK pour le repertoire <<" + nomRepertoire + ">> ");
                return;
            } else {//la date courante es supérieure à la date de dernière modification + le seuil
                logger.log(Level.SEVERE, "la date de derniere modification (" + dateModifDernierFichier + ") es antérieur à la date autorisé pour le repertoire <<" + nomRepertoire + ">> ");
                code = 0;
            }
        }
        beanClient.envoiAlerteAuServeur(cle, code);


    }

}
