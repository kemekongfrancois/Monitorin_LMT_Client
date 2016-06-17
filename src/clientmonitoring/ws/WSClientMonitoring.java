/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.ws;

import classeServeur.Tache;
import clientmonitoring.BeanClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

@WebService
public class WSClientMonitoring {

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;
    Scheduler scheduler = clientmonitoring.BeanClient.SCHEDULER;
    
    BeanClient beanClient = new BeanClient();

    public String hello(@WebParam(name = "name") String txt) {
        //logger.log(Level.SEVERE, "le message de test");
        arreterLeScheduler();
        return "Hello je suis le WSClient " + txt + " !";
    }

    @WebMethod
    public boolean arreterLeScheduler() {
        return beanClient.arreterLeScheduler();
    }
    
    @WebMethod
    public boolean demarerMetAJourOUStopperTache(@WebParam(name = "tache")Tache tache) {
        return beanClient.demarerMetAJourOUStopperTache(tache);
    }
    
    @WebMethod
    public boolean demarerLeScheduler(){
        return beanClient.demarerLeScheduler();
    }
}
