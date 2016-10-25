/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.ws;

import classeServeur.Tache;
import clientmonitoring.BeanClient;
import java.util.Map;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import org.quartz.JobKey;

@WebService
public class WSClientMonitoring {


    //BeanClient beanClient = new BeanClient();
    @WebMethod
    public String hello(@WebParam(name = "name") String txt) {
        //logger.log(Level.SEVERE, "le message de test");
        //arreterLeScheduler();
        return "Agent " + txt + " ! version= 2.03 IP=" + BeanClient.ADRESSE_MACHINE + " Port:" + BeanClient.PORT_MACHINE + " Nom:" + BeanClient.NOM_MACHINE + " OS:" + BeanClient.OS_MACHINE;
    }

    @WebMethod
    public boolean demarerMetAJourOUStopperTache(@WebParam(name = "idtache") int idtache) {
        Tache tache = BeanClient.wsServeur.getTache(idtache);
        return BeanClient.demarerMetAJourOUStopperTache(tache);
    }

    @WebMethod
    public boolean redemarerTachePrincipaleEtSousTache() {
        return BeanClient.redemarerTachePrincipaleEtSousTache();
    }

    @WebMethod
    public boolean jobExiste(@WebParam(name = "name") String name, @WebParam(name = "group") String group) {
        return BeanClient.jobExiste(name, group);
    }

    @WebMethod
    public boolean executeJob(@WebParam(name = "idTache") int idTache, @WebParam(name = "idMachine") int idMachine) {
        
        return BeanClient.executeJob(JobKey.jobKey(idTache + "", idMachine + ""));
    }
    
    @WebMethod
    public String TestTache(@WebParam(name = "idtache") int idtache) {
        Tache tache = BeanClient.wsServeur.getTache(idtache);
        return BeanClient.testTache(tache);
    }
    
    
}
