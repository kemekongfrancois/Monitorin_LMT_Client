/*
 * ce job a pour rôle principale de s'assurer que toute les taches sont fonctionnelles
 * elle permet aussi de dire au serveur que la machine es en fonctionnement
 */
package clientmonitoring.jobs;

import classeServeur.Tache;
import clientmonitoring.BeanClient;
import static clientmonitoring.BeanClient.TACHE_EN_COUR_D_EXECUTION;
import clientmonitoring.ClientMonitoring;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 *
 * @author KEF10
 */
@DisallowConcurrentExecution//permet d'empéche les exécutions concurente, il n'exitera donc d'une instace du job
public class JobPrincipale implements Job {
    //protected static Logger logger = Logger.getLogger(Class.class.getName());

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;
    /*public JobPrincipale() {
    clientmonitoring.until.Until.initialisationGestionFichierLog(logger);
    }*/

    Scheduler scheduler = clientmonitoring.BeanClient.SCHEDULER;

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        System.out.println("------Job Principale************************* ");
        JobKey key = context.getJobDetail().getKey();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        //on verrifie que les taches sont en cour de fonctionnement
        List<Integer> listTachePB = new ArrayList<>();
        Tache tache;
        for (Entry<JobKey, Tache> e : TACHE_EN_COUR_D_EXECUTION.entrySet()) {
            tache = e.getValue();
            JobKey jobKeyTache = e.getKey();
            System.out.println("key="+jobKeyTache+" statue="+tache.getStatue());
            if (!BeanClient.jobExiste(jobKeyTache.getName(), jobKeyTache.getGroup())) {
                listTachePB.add(tache.getIdTache());
                (new BeanClient()).demarerMetAJourOUStopperTache(tache);//on redémarer la taches
                logger.log(Level.WARNING, "la tache " + jobKeyTache + " n'es pas en cour de fonctionnement bien vouloir verrifier les log, elle vien d'être redémarer");
            } else//la tache es bien en marche
            if (tache.getStatue().equals(BeanClient.ALERTE)) {//On demande la réxécution du Job dans cas où il y avait un pb ainsi si le pb es resolue on vas le signallé
                logger.log(Level.INFO, "la tache " + jobKeyTache + " es de nouveau éxcuter pour verrifie si la sitution es de nouveau normal");
                BeanClient.executeJob(jobKeyTache);
            } else {
                System.out.println(jobKeyTache + ": est bien en cour de fonctionnement");

            } // logger.log(Level.INFO, e.getKey() + ": est bien en cour de fonctionnement");
        }
        traitementDesTachesAProbleme(listTachePB, key);

    }

    /**
     * cette fonction vas effectué le traiment choisi en cas d'arrét d'un job
     * pour le moment il vas redémarer le job
     *
     * @param listTachePB
     */
    public void traitementDesTachesAProbleme(List<Integer> listTachePB, JobKey key) {
        if (listTachePB.isEmpty()) {//il n'ya pas eu de problème
            logger.log(Level.INFO, "le job principale c'est bien exécuté");

        } else {//il ya eu des pb
            logger.log(Level.SEVERE, "des probléme on été trouvé. les taches ayant des probléme on été redémarer");

        }
        try {
            if (!BeanClient.wsServeur.traitementAlerteMachine(new Integer(key.getName()), listTachePB)) {//on envoie un msg au serveur pour signalé les taches qui on eu un pb si pb il ya sinon on dis au serveur qu'on es là
                logger.log(Level.WARNING, " le serveur n'a pas pus traiter la requete");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "impossible de contacter le serveur \n" + e);
        }

    }
    /*
    public void demarerSousTache(){
        try {
            
            scheduler.start();
            
            Trigger trigger = newTrigger()
                    .withIdentity("trigger11", "group11")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(3)
                            //.withRepeatCount(2))
                            .repeatForever())
                    .build();
            JobDetail jobDetaille = newJob(JobVerificationDisk.class)
                    .withIdentity("job11", "group11")
                    .build();
            
            scheduler.scheduleJob(jobDetaille, trigger);
            sousJobOK = true;
        } catch (SchedulerException ex) {
            Logger.getLogger(JobPrincipale.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     */
}
