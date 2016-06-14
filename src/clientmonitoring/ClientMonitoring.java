/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring;

import classeServeur.Machine;
import classeServeur.WsMonitoring;
import classeServeur.WsMonitoring_Service;
import clientmonitoring.jobs.JobPrincipale;
import clientmonitoring.until.Until;
import clientmonitoring.ws.WSClientMonitoring;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.Endpoint;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobBuilder.newJob;

/**
 *
 * @author KEF10
 */
public class ClientMonitoring {

    public static final String OSWINDOWS = "Windows";
    public static final String OSLinux = "Linux";
    public static final String ficfierConfig = "parametre.txt";

    public static Scheduler SCHEDULER;
    public static WsMonitoring ws;
    public static Machine machine;
    public static String ADRESSE_SERVEUR = "";
    public static String PORT_SERVEUR;
    public static String ADRESSE_MACHINE = "";
    public static String PORT_MACHINE;

    public ClientMonitoring() {
        initialisation();
        
    }

    /**
     * cette fonction permet d'initialisé le client et certain paramètre
     * globeaux
     */
    public void initialisation() {
        //---------on recupere le type d'OS du système------
        String typeOS = System.getProperty("os.name");
        if (typeOS.contains(OSWINDOWS)) {
            typeOS = OSWINDOWS;
        } else {
            typeOS = OSLinux;
        }

        //----- on recuper le nom de la machine------------
        String nomMachine = System.getProperty("user.name");

        //------------on recuper les paramettre contenue dans le fichie de paramettre-------------
        List<String> parmettre = Until.lectureFichier(ficfierConfig);//lecture du fichier de paramettre

        if (parmettre.size() > 0) {
            int i = 0;
            ADRESSE_SERVEUR = parmettre.get(i++);
            PORT_SERVEUR = parmettre.get(i++);
            ADRESSE_MACHINE = parmettre.get(i++);
            PORT_MACHINE = parmettre.get(i++);
        }

        //------------on initialise le webService----------
        try {
            URL url = new URL("http://" + ADRESSE_SERVEUR + ":" + PORT_SERVEUR + "/projetMonitoring-war/WsMonitoring?wsdl");
            WsMonitoring_Service service = new WsMonitoring_Service(url);
            ws = service.getWsMonitoringPort();

            //--------- on recupere les paramettre de la machine---------
            machine = ws.verifiOuCreerMachine(ADRESSE_SERVEUR, PORT_MACHINE, typeOS, nomMachine);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ClientMonitoring.class.getName()).log(Level.SEVERE, null, ex);
            Until.savelog("Adresse du serveur ou port invalide\n" + ex, Until.fichieLog);
        }

    }

    public void demarerTachePrincipale() {
        try {
            SCHEDULER = StdSchedulerFactory.getDefaultScheduler();
            SCHEDULER.start();
            JobDataMap data = new JobDataMap();
            data.put("machine", machine);

            String groupe = machine.getAdresseIP();
            String indentifiant = machine.getAdresseIP();
            // define the job and tie it to our MyJob class
            JobDetail jobDetaille = newJob(JobPrincipale.class)
                    .withIdentity(indentifiant,groupe)
                    .usingJobData(data)
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(indentifiant,groupe)
                    .startNow()
                    .withSchedule(cronSchedule(machine.getPeriodeDeCheck()))
                    .build();

            // Tell quartz to schedule the job using our trigger
            SCHEDULER.scheduleJob(jobDetaille, trigger);
        } catch (SchedulerException ex) {
            Logger.getLogger(ClientMonitoring.class.getName()).log(Level.SEVERE, null, ex);
            Until.savelog("pb lors de l'éxécution du scheduler \n" + ex, Until.fichieLog);
        }
    }

    public void demarerWS() {
        //String URL = "http://"+ADRESSE_MACHINE+":8080/";
        String URL = "http://" + ADRESSE_MACHINE + ":" + PORT_MACHINE + "/";
        Endpoint.publish(URL, new WSClientMonitoring());
        System.out.println("Web Service démarer: " + URL);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ClientMonitoring client = new ClientMonitoring();

        client.demarerTachePrincipale();
        client.demarerWS();

    }
}
