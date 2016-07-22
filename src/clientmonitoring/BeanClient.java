/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring;

import classeServeur.Machine;
import classeServeur.Tache;
import static clientmonitoring.ClientMonitoring.OSWINDOWS;
import static clientmonitoring.ClientMonitoring.OS_MACHINE;
import static clientmonitoring.ClientMonitoring.START;
import static clientmonitoring.ClientMonitoring.TACHE_DATE_MODIFICATION_DERNIER_FICHIER;
import static clientmonitoring.ClientMonitoring.TACHE_DD;
import static clientmonitoring.ClientMonitoring.TACHE_FICHIER_EXISTE;
import static clientmonitoring.ClientMonitoring.TACHE_PING;
import static clientmonitoring.ClientMonitoring.TACHE_PROCESSUS;
import static clientmonitoring.ClientMonitoring.TACHE_SERVICE;
import static clientmonitoring.ClientMonitoring.TACHE_TAILLE_FICHIER;
import static clientmonitoring.ClientMonitoring.TACHE_TELNET;
import clientmonitoring.jobs.JobDateModificationDernierFichier;
import clientmonitoring.jobs.JobExistanceFichier;
import clientmonitoring.jobs.JobPing;
import clientmonitoring.jobs.JobPrincipale;
import clientmonitoring.jobs.JobTelnet;
import clientmonitoring.jobs.JobVerificationDisk;
import clientmonitoring.jobs.JobVerificationProcessus;
import clientmonitoring.jobs.JobVerificationService;
import clientmonitoring.jobs.JobVerrifieTailleFIchier;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.telnet.TelnetClient;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;

/**
 *
 * @author KEF10
 */
public class BeanClient {

    public static Map<JobKey, Tache> TACHE_EN_COUR_D_EXECUTION = new HashMap<>();

    public static Scheduler SCHEDULER;

    static Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    public boolean demarerLeScheduler() {
        try {
            SCHEDULER = StdSchedulerFactory.getDefaultScheduler();
            SCHEDULER.start();
            logger.log(Level.INFO, "SCHEDULER démarer ");
            return true;
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, "le SCHEDULER à bien démaré", ex);
            return false;
        }
    }

    public boolean arreterLeScheduler() {
        try {
            SCHEDULER.shutdown();
            TACHE_EN_COUR_D_EXECUTION.clear();
            logger.log(Level.INFO, "SCHEDULER Arreté ");
            return true;
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, "impossible de stopper le SCHEDULER", ex);
            return false;
        }
    }

    /**
     * permet de recupérer la clés d'une tache
     *
     * @param tache
     * @return
     */
    private JobKey getJobKeyTache(Tache tache) {
        String identifiant = tache.getIdTache() + "";
        String groupe = tache.getIdMachine().getIdMachine() + "";
        return JobKey.jobKey(identifiant, groupe);
    }

    /**
     * cette fonction es un complement à la fonction de démarage de tache elle
     * permet de démarer les taches de type verifie DD
     */
    private JobDetail initialiseVerificationDD(Tache tache) {
        JobKey cle = getJobKeyTache(tache);

        //ajouter les données
        //JobDataMap data = new JobDataMap();
        //data.put("tache", tache);
        JobDetail jobDetaille = newJob(JobVerificationDisk.class)
                .withIdentity(cle)
                .usingJobData("seuil", tache.getSeuilAlerte())
                .usingJobData("lettrePartition", tache.getNom())
                .usingJobData("ipAdresse", tache.getIdMachine().getAdresseIP())
                //.usingJobData(data)
                .build();

        return jobDetaille;
    }

    private JobDetail initialiseVerificationProcessus(Tache tache) {
        JobKey cle = getJobKeyTache(tache);
        JobDetail jobDetaille = newJob(JobVerificationProcessus.class)
                .withIdentity(cle)
                .usingJobData("nomProcessus", tache.getNom())
                .usingJobData("ipAdresse", tache.getIdMachine().getAdresseIP())
                .build();

        return jobDetaille;
    }

    private JobDetail initialiseVerificationService(Tache tache) {
        JobKey cle = getJobKeyTache(tache);
        JobDetail jobDetaille = newJob(JobVerificationService.class)
                .withIdentity(cle)
                .usingJobData("nomService", tache.getNom())
                .usingJobData("redemarerAuto", tache.isRedemarerAutoService())
                .usingJobData("ipAdresse", tache.getIdMachine().getAdresseIP())
                .build();

        return jobDetaille;
    }

    private JobDetail initialisePing(Tache tache) {
        JobKey cle = getJobKeyTache(tache);
        JobDetail jobDetaille = newJob(JobPing.class)
                .withIdentity(cle)
                .usingJobData("nbTentative", tache.getSeuilAlerte())
                .usingJobData("adresseAPinger", tache.getNom())
                .build();

        return jobDetaille;
    }

    private JobDetail initialiseExistanceFichier(Tache tache) {
        JobKey cle = getJobKeyTache(tache);
        JobDetail jobDetaille = newJob(JobExistanceFichier.class)
                .withIdentity(cle)
                .usingJobData("nomFichier", tache.getNom())
                .build();

        return jobDetaille;
    }

    private JobDetail initialiseVerrifieTailleFIchier(Tache tache) {
        JobKey cle = getJobKeyTache(tache);
        JobDetail jobDetaille = newJob(JobVerrifieTailleFIchier.class)
                .withIdentity(cle)
                .usingJobData("nomFichier", tache.getNom())
                .usingJobData("seuil", tache.getSeuilAlerte())
                .build();

        return jobDetaille;
    }
    
    private JobDetail initialiseDateModificationDernierFichier(Tache tache) {
        JobKey cle = getJobKeyTache(tache);
        JobDetail jobDetaille = newJob(JobDateModificationDernierFichier.class)
                .withIdentity(cle)
                .usingJobData("nomRepertoire", tache.getNom())
                .usingJobData("seuil", tache.getSeuilAlerte())
                .build();

        return jobDetaille;
    }

    private JobDetail initialiseTelnet(Tache tache) {
        JobKey cle = getJobKeyTache(tache);
        JobDetail jobDetaille = newJob(JobTelnet.class)
                .withIdentity(cle)
                .usingJobData("ipAdresse", tache.getIdMachine().getAdresseIP())
                .usingJobData("adresseAEtPort", tache.getNom())
                .build();

        return jobDetaille;
    }

    /**
     * cette fonction permet de démarrer ou de mette à jour n'importe quelle
     * tache
     *
     * @param tache
     * @return true si tous c'est bien passé
     */
    public boolean demarerMetAJourOUStopperTache(Tache tache) {
        try {
            String identifiant = tache.getIdTache() + "";
            String groupe = tache.getIdMachine().getIdMachine() + "";
            JobKey cle = JobKey.jobKey(identifiant, groupe);

            if (!tache.getStatue().equals(START) || SCHEDULER.checkExists(cle)) {//si le job existe déja on le stoppe
                arreterJob(cle);//suppression du job
                if (!tache.getStatue().equals(START)) {//cas où on veux stopper la tache ou la mettre en pause
                    return true;//le job a été stoppé
                }
            }
            //-------- on définie la périodicité--------
            Trigger trigger = newTrigger()
                    .withIdentity(identifiant, groupe)
                    .startNow()
                    .startNow()
                    .withSchedule(cronSchedule(tache.getPeriodeVerrification()))
                    .build();

            //---- on initialise le job en fonction de son type-------
            JobDetail jobDetaille = null;
            String TypeDeTache = tache.getTypeTache();
            switch (TypeDeTache) {
                case TACHE_DD://cas de la tache de vérification de dd
                    jobDetaille = initialiseVerificationDD(tache);
                    break;
                case TACHE_PROCESSUS://cas de la tache de vérification de processus
                    jobDetaille = initialiseVerificationProcessus(tache);
                    break;
                case TACHE_SERVICE://cas de la tache de vérification de Service
                    jobDetaille = initialiseVerificationService(tache);
                    break;
                case TACHE_PING://cas de la tache de ping
                    jobDetaille = initialisePing(tache);
                    break;
                case TACHE_FICHIER_EXISTE://cas de la tache qui verrifie l'existance de fichier
                    jobDetaille = initialiseExistanceFichier(tache);
                    break;
                case TACHE_TAILLE_FICHIER://cas de la tache qui verrifie la taille des fichiers
                    jobDetaille = initialiseVerrifieTailleFIchier(tache);
                    break;
                case TACHE_TELNET://cas de la tache Telnet
                    jobDetaille = initialiseTelnet(tache);
                    break;
                case TACHE_DATE_MODIFICATION_DERNIER_FICHIER://cas de la tache qui vérrifier que la date de derniere modification du dernier fichier es correct
                    jobDetaille = initialiseDateModificationDernierFichier(tache);
                    break;
                default:
                    logger.log(Level.WARNING, TypeDeTache + ": ce type n'es pas reconnue ");
                    return false;
            }

            SCHEDULER.scheduleJob(jobDetaille, trigger);//on démare la tache
            logger.log(Level.INFO, "tache démaré. cle=" + cle);
            TACHE_EN_COUR_D_EXECUTION.put(cle, tache);
            return true;
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }

    }

    private void demarerListeTAche(List<Tache> listTache) {
        for (Tache tache : listTache) {
            demarerMetAJourOUStopperTache(tache);
        }
    }

    public boolean arreterJob(JobKey cle) {
        try {
            if (SCHEDULER.checkExists(cle)) {
                SCHEDULER.deleteJob(cle);//suppression du job
                logger.log(Level.INFO, "tache stoppé. cle=" + cle);
            }
            TACHE_EN_COUR_D_EXECUTION.remove(cle);//on le retire des taches en cour d'exécution
            return true;
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * cette fonction démare(ou redémare) la tache principale ainsi que la liste
     * des taches associé
     *
     * @param machine
     * @return
     */
    public boolean redemarerTachePrincipaleEtSousTache(Machine machine, List<Tache> listTache) {
        try {
            String identifiant = machine.getIdMachine() + "";
            String groupe = machine.getAdresseIP();//le groupe sera l'adresse IP
            JobKey cle = JobKey.jobKey(identifiant, groupe);

            //on redemarer le Scheduler
            arreterLeScheduler();
            demarerLeScheduler();

            // on définie le job
            JobDetail jobDetaille = newJob(JobPrincipale.class)
                    .withIdentity(cle)
                    .build();

            //on défini la périodicité
            Trigger trigger = newTrigger()
                    .withIdentity(identifiant, groupe)
                    .startNow()
                    .withSchedule(cronSchedule(machine.getPeriodeDeCheck()))
                    .build();

            //on démare les taches
            demarerListeTAche(listTache);
            logger.log(Level.INFO, "Toutes les taches ont été démarer: nombre de tache=" + TACHE_EN_COUR_D_EXECUTION.size());

            // on démarer le job principale
            SCHEDULER.scheduleJob(jobDetaille, trigger);
            logger.log(Level.INFO, "la tache principale à bien été lancé. cle=" + cle);
            return true;

        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, "pb lors de l'éxécution du scheduler ", ex);
            return false;
        }

    }

    /**
     *
     * @param lettreDD
     * @return "200" dans le cas où lettre de partition ne corespond à aucune dd
     */
    public int pourcentageOccupationDD(String lettreDD) {
        File cwd = new File(lettreDD);
        //File cwd = new File("l:");
        if (!cwd.exists()) {
            return 200;
        }
        float espaleTotal = cwd.getTotalSpace();
        float espaceLIbre = cwd.getFreeSpace();
        float espaceUtilise = cwd.getUsableSpace();

        float pourcentage = 100 - (espaceLIbre / espaleTotal) * 100;
        /*
        System.out.println("pourcentage restant:  "+ pourcentage+"%");
        System.out.println("Espace total:  "+ espaleTotal/ (1024 * 1024) + " MBt"  );
        System.out.println("Espace Libre:  "+ espaceLIbre / (1024 * 1024) + " MBt");
        System.out.println("Espace Utilisé:  "+ espaceUtilise / (1024 * 1024) + " MBt");
         */

        return (int) pourcentage;

    }

    /**
     * cete fonction verrifie si le processus donc le nom es pris en paramettre
     * es en cour de fonctionnement cette fonction ne marche pour le moment que
     * sur les machine windows
     *
     * @param nomProcessus exemple: "vlc.exe"
     * @return OK s'il es en cour de fonctionnement, KO s'il n'es pas en cour de
     * fonctionnement , PB s'il ya une exception
     */
    public String verifiProcessusWindows(String nomProcessus) {
        String commande = "tasklist /fi  \"ImageName eq  " + nomProcessus + "\"";
        List<String> resultatCommande = executeCommand(commande);
        if (resultatCommande == null) {
            return ClientMonitoring.PB;
        }
        if (resultatCommande.size() > 1) {//le processus es en cour d'éxécution
            return ClientMonitoring.OK;
        } else {
            return ClientMonitoring.KO;
        }
    }

    /**
     *
     * @param nomService
     * @return OK s'il es en cour de fonctionnement, KO s'il n'es pas en cour de
     * fonctionnement , PB s'il ya une exception ou si service n'existe pas
     */
    public String verifiService(String nomService) {
        if (OS_MACHINE.equals(OSWINDOWS)) {
            return verifiServiceWindows(nomService);
        } else {
            return verifiServiceLinux(nomService);
        }
    }

    /**
     * verifie si un service es en fonctionnement dans une machine Windows
     *
     * @param nomService
     * @return "OK" , "KO" et "PB"
     */
    private String verifiServiceWindows(String nomService) {
        /* ce qui es en commentaire es meilleur mais n'es pas fonctionnel sous windows server 2000
        String commande = "sc query "+nomService;
        List<String> resultatCommande = executeCommandWindows(commande);
        if (resultatCommande == null) {
        return ClientMonitoring.PB;
        }
        for (String ligne : resultatCommande) {
        if(ligne.contains("STATE")){
        if(ligne.contains("RUNNING")){
        return ClientMonitoring.OK;
        }else{
        return ClientMonitoring.KO;
        }
        }
        }
        //on retourn PB car la ligne qui contiend "STATE" n'existe pas
        //ce qui veux dire que le service n'es pas reconnue
        return ClientMonitoring.PB;
         */
        //cette version es moin optimisé mais marche sur toute les vertion de windows
        String commande = "net start ";
        List<String> resultatCommande = executeCommand(commande);
        if (resultatCommande == null) {
            return ClientMonitoring.PB;
        }
        for (String ligne : resultatCommande) {
            if (ligne.contains(nomService)) {
                return ClientMonitoring.OK;
            }
        }
        return ClientMonitoring.KO;
    }

    /**
     * verifie si un service es en fonctionnement dans une machine linux
     *
     * @param nomService
     * @return "OK" , "KO" et "PB"
     */
    private String verifiServiceLinux(String nomService) {

        List<String> resultatCommande = executeCommand("service " + nomService + " status");
        if (resultatCommande == null) {
            return ClientMonitoring.PB;
        }
        for (String ligne : resultatCommande) {
            //System.out.println(ligne);
            if (ligne.contains("running")) {
                return ClientMonitoring.OK;
            }
        }
        return ClientMonitoring.KO;

    }

    /**
     * cette fonction permet de démarer le service donc le nom es pris en
     * paramettre
     *cette fonction demande une autorisation Super admin pour fonctionné
     * @param nomService
     * @return
     */
    public boolean demarerService(String nomService) {
        if (OS_MACHINE.equals(OSWINDOWS)) {
            return demarerServiceWindows(nomService);
        } else {
            return demarerServiceLinux(nomService);
        }
    }

    /**
     * cette fonction permet de démarer un service windows
     *
     * @param nomService
     * @return
     */
    private boolean demarerServiceWindows(String nomService) {
        String commande = "net start \"" + nomService + "\"";
        executeCommand(commande);//on relance le service
        String etatService = verifiService(nomService);
        if (etatService.equals(ClientMonitoring.OK)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * cette fonction permet de démarer un service linux
     *
     * @param nomService
     * @return
     */
    private boolean demarerServiceLinux(String nomService) {
        List<String> resul = executeCommand("service " + nomService + " start");
        //System.out.println(resul.size());
        for (String ligne : resul) {
            System.out.println(ligne);
        }
        if (verifiServiceLinux(nomService).equals("OK")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * cette fonction verrifie si le fichier pris en paramettre existe
     *
     * @param nomFichier
     * @return
     */
    public boolean verifiExistanceFichier(String nomFichier) {
        try {
            File f = new File(nomFichier);
            if (f.exists()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
            return false;
        }
    }

    /**
     * retourne la taille d'un fichier
     *
     * @param nomFichier
     * @return "-1" s'il ya eu un pb: le fichier n'existe pas
     */
    public long tailleFichier(String nomFichier) {
        try {
            File f = new File(nomFichier);
            if (f.exists()) {
                return f.length();
            } else {
                return -1;
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
            return -1;
        }
    }

    public List<String> executeCommand(String commande) {
        List<String> processes = new ArrayList<String>();
        try {
            String line;
            Process p = Runtime.getRuntime().exec(commande);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                processes.add(line);
                //  System.out.println(line);
            }
            input.close();
            /*
            String resultatCommande = "";
            for (String ligne : processes) {
                resultatCommande += ligne + "\n";
            }
            logger.log(Level.INFO, "le resultat de l'éxécution de la commande <<" + commande + ">> est:\n" + resultatCommande);
             */
            logger.log(Level.INFO, "la commande <<" + commande + ">> c'es bien exécuté");
            return processes;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "impossible d'exécuter la command <<" + commande + ">>\n", e);
            return null;
        }
    }

    /**
     * cette fonction permet d'effectué un ping à l'adresse passé en paramettre
     *
     * @param adres
     * @param nbTentative represente le nb de fois qu'on vas faire le ping
     * @return
     */
    public boolean pinger(String adres, int nbTentative) {
        try {
            int i = 0;
            boolean pingOK = false;
            while (i < nbTentative && !pingOK) {
                System.out.println(i + ": ping à l'adresse " + adres);
                char param;
                if (OS_MACHINE.equals(OSWINDOWS)) {//on es sur une machine windows
                    param = 'n';
                } else {//on es sur une machine linux
                    param = 'c';
                }
                Process p = java.lang.Runtime.getRuntime().exec("ping -" + param + " 1 " + adres);
                int valeurDeRetour = p.waitFor();
                pingOK = (valeurDeRetour == 0);
                i++;
            }

            //System.out.println("le nombre es: " + valeurDeRetour);
            return pingOK;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "problème avec le ping vers l'adresse " + adres + "\n", e);
            return false;
        }
    }

    /**
     * cette fonction permet de vérifiè si l'adresse pris en paramettre fait
     * partie des adresses d'une des interface réseau
     *
     * @param adresse
     * @return true si une des interfaces réseaux à l'adresse pris en paramettre
     */
    public boolean verrifieAdresseMachine(String adresse) {
        try {
            // Énumération de toutes les cartes réseau. 
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface interfaceN = (NetworkInterface) interfaces.nextElement();
                //System.out.println("----> " + interfaceN.getDisplayName()); 
                Enumeration<InetAddress> iEnum = interfaceN.getInetAddresses();
                while (iEnum.hasMoreElements()) {
                    InetAddress inetAddress = iEnum.nextElement();
                    String adresseCourante = inetAddress.getHostAddress();
                    //System.out.println(adresseCourante);
                    if (adresseCourante.equals(adresse)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            System.out.println("pas de carte réseau.");
            logger.log(Level.SEVERE, null, e);
            return false;
        }
    }

    /**
     * cette fonction permet d'envoyer une alerte au serveur et de stopper la
     * tache si le serveur à repondu
     *
     * @param cle
     * @param code
     */
    public void envoiAlerteAuServeur(JobKey cle, int code) {
        try {
            if (!ClientMonitoring.wsServeur.traitementAlerteTache(new Integer(cle.getName()), code)) {
                logger.log(Level.SEVERE, " le serveur n'a pas pus traiter le problème consulter les log serveur pour plus de détail");
            } else {//on stope la tache dans le cas où le serveur à bien traité le pb
                arreterJob(cle);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "impossible de contacter le serveur \n" + e);
        }
    }

    /**
     * permet de faire le telnet
     *
     * @param adresseEtPort contiend l'adresse et le port séparé par une virgule
     * "," exemple: "41.204.94.29,8282"
     * @return
     */
    public boolean telnet(String adresseEtPort) {
        try {
            TelnetClient telnet = new TelnetClient();
            String tab[] = adresseEtPort.split(",");
            String adresse = tab[0];
            int port = new Integer(tab[1]);
            logger.log(Level.INFO, "Telne à l'adresse <<" + adresse + ">> et au port <<" + port + ">>");
            telnet.connect(adresse, port);
            if (telnet.isConnected()) {
                telnet.disconnect();
            }
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "impossible de faire le telnet à l'adresse <<" + adresseEtPort + ">>\n", e);
            return false;
        }
    }

    public Date dateDernierFichier(String repertoire) {
        try {
            File file = new File(repertoire);
            if (!file.exists() || file.isFile()) {
                logger.log(Level.SEVERE,"repertoire <<" + repertoire + ">> invalide");
                return null;
            }
            File[] listFichier = file.listFiles();
            int nbFichierDuRepertoire = listFichier.length;
            if (nbFichierDuRepertoire > 0) {//il y'a au moins un fichier dans le repertoire
                File lePlusRescent = listFichier[0];
                for (int i = 1; i < nbFichierDuRepertoire; i++) {
                    File fichierCourant = listFichier[i];
                    if (fichierCourant.lastModified() > lePlusRescent.lastModified()) {
                        lePlusRescent = fichierCourant;
                    }
                    //System.out.println(fichierCourant.getName() + " -->" + fichierCourant.lastModified());
                }
                System.out.println("le plus rescent des fichiers est: " + lePlusRescent.getName());
                return new Date(lePlusRescent.lastModified());
            } else {
                logger.log(Level.SEVERE,"repertoire <<" + repertoire + ">> es vide");
                return null;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, null,e);
            return null;
        }
    }
    
    /**
     * cette fonction verrifie qu'il existe un job qui à le nom et le groupe passé en paramettre
     * @return true si le job existe sur la machine
     */
    public boolean jobExiste(String name, String group){
        try {
            return SCHEDULER.checkExists(new JobKey(name, group));
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
