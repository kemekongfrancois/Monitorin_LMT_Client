/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.ws;

import classeServeur.Tache;
import clientmonitoring.BeanClient;
import java.util.Date;
import java.util.List;
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
    
    //BeanClient beanClient = new BeanClient();

    @WebMethod
    public String hello(@WebParam(name = "name") String txt) {
        //logger.log(Level.SEVERE, "le message de test");
        //arreterLeScheduler();
        return "Hello je suis le WSClient " + txt + " ! version= 1.03 IP="+BeanClient.ADRESSE_MACHINE+" Port:"+BeanClient.PORT_MACHINE+" Nom:"+BeanClient.NOM_MACHINE+" OS:"+BeanClient.OS_MACHINE;
    }

    
    
    @WebMethod
    public boolean demarerMetAJourOUStopperTache(@WebParam(name = "idtache")int idtache) {
        Tache tache  = BeanClient.wsServeur.getTache(idtache);
        return BeanClient.demarerMetAJourOUStopperTache(tache);
    }
    
   
    
    @WebMethod
    public String processusWindowsEnFonctionnement(@WebParam(name = "nomProcessus")String nomProcessus) {
        return BeanClient.verifiProcessusWindows(nomProcessus,0);
    }
    
    @WebMethod
    public long tailleFichier(@WebParam(name = "cheminFichier")String nomFichier) {
        return BeanClient.tailleFichier(nomFichier);
    }
    
    @WebMethod
    public int pourcentageOccupationDD(@WebParam(name = "partition")String lettreDD) {
        return BeanClient.pourcentageOccupationDD(lettreDD);
    }
    
    @WebMethod
    public boolean pinger(@WebParam(name = "adresse")String adres, @WebParam(name = "nbTentative")int nbTentative) {
        return BeanClient.pinger(adres, nbTentative);
    }
    
    @WebMethod
    public boolean telnet(@WebParam(name = "adresseEtPort")String adresseEtPort) {
        return BeanClient.telnet(adresseEtPort);
    }
    
    @WebMethod
    public String verifiService(@WebParam(name = "nomService")String nomService) {
        return BeanClient.verifiService(nomService);
    }
    
    @WebMethod
    public boolean demarerService(@WebParam(name = "nomService")String nomService) {
        return BeanClient.demarerService(nomService);
    }
    
    @WebMethod
    public List<String> executeCommand(@WebParam(name = "commande")String commande) {
        return BeanClient.executeCommand(commande);
    }
    
    @WebMethod
    public Date dateDernierFichier(@WebParam(name = "repertoire")String repertoire) {
        return BeanClient.dateDernierFichier(repertoire);
    }
    
    @WebMethod
    public boolean jobExiste(@WebParam(name = "name")String name, @WebParam(name = "group")String group){
        return BeanClient.jobExiste(name, group);
    }
    @WebMethod
    public boolean redemarerTachePrincipaleEtSousTache() {
        return BeanClient.redemarerTachePrincipaleEtSousTache();
    }
    
}
