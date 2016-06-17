/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.jobs;

import classeServeur.Machine;
import classeServeur.Tache;
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
public class JobVerificationDisk implements Job {

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Tache tache = (Tache) dataMap.get("tache");
        BeanClient beanClient = new BeanClient();
        boolean pb = false;
        if (tache.getTypeTache().equals(ClientMonitoring.TACHE_DD)) {//on ne fait les traitement que si la tache passé en paramettre es la bonne tache
            int pourcentage = beanClient.pourcentageOccupationDD(tache.getTachePK().getCleTache());

            if (pourcentage > tache.getSeuilAlerte()) {//le sueil es atteind
                if (pourcentage == 200) {//cas où la lettre de partition n'es pa valide
                    logger.log(Level.SEVERE, "la lettre de partition ne correspont a aucune partition ou elle es invalide : <<" + tache.getTachePK().getCleTache() + " >>");
                    pb = true;
                } else {
                    fontionDeTraitementDeLAlerte(tache, pourcentage);
                }
            } else {//le seuil d'allerte n'es pas atteind
                logger.log(Level.INFO, "espase disque OK: pourcentage d'utilisation du disque es de: " + pourcentage + "%");
            }
        } else {//la tache passé en paramettre n'es pas valide
            logger.log(Level.WARNING, ": cette tache ne correspond pas au type: " + ClientMonitoring.TACHE_DD);
            pb = true;
        }
        if(pb){//en cas de tache invalide, on stop la tache
            tache.setStatue(ClientMonitoring.STOP);//on stope la tache
            beanClient.demarerMetAJourOUStopperTache(tache);
        }
        System.out.println("------verification disque: ");
    }

    private void fontionDeTraitementDeLAlerte(Tache tache, int pourcentage) {
        if(!ClientMonitoring.wsServeur.traitementAlerte(tache)) return;
        tache.setStatue(ClientMonitoring.PAUSE);//on stope la tache
        (new BeanClient()).demarerMetAJourOUStopperTache(tache);
        logger.log(Level.SEVERE, " espace restant du disque <<" + tache.getTachePK().getCleTache()+">>" + "de la machine<<" + tache.getMachine().getAdresseIP()+">> es faible " );
    }

}
