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

/**
 *
 * @author KEF10
 */
public class ClientMonitoring {
    public static final String OSWINDOWS = "Windows";
    public static final String OSLinux= "Linux";
    public static final String ficfierConfig = "parametre.txt"; 
    
    public static WsMonitoring ws;
    public static Machine machine;
    String adresseServeur="";
    String adresseMachine="";
    String PORTWSCLIENT;

    public ClientMonitoring() {
        initialisation();
    }
    
    /**
     * cette fonction permet d'initialisé le client et certain paramètre globeaux
     */
    public void initialisation(){
        //---------on recupere le type d'OS du système------
        String typeOS = System.getProperty("os.name");
        if(typeOS.contains(OSWINDOWS)) typeOS = OSWINDOWS;
        else typeOS = OSLinux;
        
        //----- on recuper le nom de la machine------------
        String nomMachine = System.getProperty("user.name");
        
        //------------on recuper les paramettre contenue dans le fichie de paramettre-------------
        List<String> parmettre = Until.lectureFichier(ficfierConfig);//lecture du fichier de paramettre
       
        if(parmettre.size()>0){
            adresseServeur = parmettre.get(0);
            adresseMachine = parmettre.get(1);
            PORTWSCLIENT = parmettre.get(2);
        }
        
        //------------on initialise le webService----------
        URL url=null;
        try {
            url = new URL("http://"+adresseServeur+":8080/projetMonitoring-war/WsMonitoring?wsdl");
        } catch (MalformedURLException ex) {
            Logger.getLogger(ClientMonitoring.class.getName()).log(Level.SEVERE, null, ex);
            Until.savelog("Adresse du serveur invalide", Until.fichieLog);
        }
        WsMonitoring_Service service  = new WsMonitoring_Service(url);
        ws = service.getWsMonitoringPort();
       
        //--------- on recupere les paramettre de la machine---------
        machine = ws.verifiOuCreerMachine(adresseServeur, typeOS, nomMachine);
    }
    
    public void demarerTachePrincipale(){
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("machine", machine);
            
            // define the job and tie it to our MyJob class
            JobDetail jobDetaille = newJob(JobPrincipale.class)
                    .withIdentity("job1", "group1")
                    .usingJobData("jobSays", machine.getAdresseIP())
                    .usingJobData("userId", machine.getNomMachine())
                    .usingJobData(data)
                    .build();
            
            // Trigger the job to run now, and then repeat every 40 seconds
            Trigger trigger = newTrigger()
                    .withIdentity("trigger1", "group1")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(machine.getPeriodeDeCheck())
                            //.withRepeatCount(2))
                            .repeatForever())
                    .build();
            
            /*
            Trigger trigger = newTrigger()
            .withIdentity("trigger1", "group1")
            .startNow()
            .withSchedule(cronSchedule("2 * * * * ?"))
            .build();
            */
            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(jobDetaille, trigger);
            
        } catch (SchedulerException ex) {
            Logger.getLogger(ClientMonitoring.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void demarerWS(){
        //String URL = "http://"+adresseMachine+":8080/";
        String URL = "http://"+adresseMachine+":"+PORTWSCLIENT+"/";
        Endpoint.publish(URL, new WSClientMonitoring());
        System.out.println("Web Service démarer: "+URL);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ClientMonitoring client = new ClientMonitoring();
        client.demarerWS();
        
        client.demarerTachePrincipale();
        
        
    }
}
