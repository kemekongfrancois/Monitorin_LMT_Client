/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.ws;

import clientmonitoring.BeanClient;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import org.quartz.JobKey;
import org.quartz.Scheduler;


@WebService
public class WsDesFonctionsDisponible {
    
    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;
    Scheduler scheduler = clientmonitoring.BeanClient.SCHEDULER;
    
    @WebMethod
    public String processusWindowsEnFonctionnement(@WebParam(name = "nomProcessus") String nomProcessus, @WebParam(name = "ndTentative") int ndTentative) {
        return BeanClient.verifiProcessusWindows(nomProcessus, ndTentative);
    }

    @WebMethod
    public long tailleFichier(@WebParam(name = "cheminFichier") String nomFichier) {
        return BeanClient.tailleFichier(nomFichier);
    }

    @WebMethod
    public int pourcentageOccupationDD(@WebParam(name = "partition") String lettreDD) {
        return BeanClient.pourcentageOccupationDD(lettreDD);
    }

    @WebMethod
    public boolean pinger(@WebParam(name = "adresse") String adres, @WebParam(name = "nbTentative") int nbTentative) {
        return BeanClient.pinger(adres, nbTentative);
    }

    @WebMethod
    public boolean telnet(@WebParam(name = "adresseEtPort") String adresseEtPort, @WebParam(name = "nbDeTentative") int nbDeTentative) {
        return BeanClient.telnet(adresseEtPort, nbDeTentative);
    }

    @WebMethod
    public String verifiService(@WebParam(name = "nomService") String nomService) {
        return BeanClient.verifiService(nomService);
    }

    @WebMethod
    public boolean demarerService(@WebParam(name = "nomService") String nomService) {
        return BeanClient.demarerService(nomService);
    }

    @WebMethod
    public List<String> executeCommand(@WebParam(name = "commande") String commande) {
        return BeanClient.executeCommand(commande);
    }

    @WebMethod
    public Date dateDernierFichier(@WebParam(name = "repertoire") String repertoire) {
        return BeanClient.dateDernierFichier(repertoire);
    }
    
    @WebMethod
    public int uptimeMachine(){
        return BeanClient.uptimeMachine();
    }

    
}
