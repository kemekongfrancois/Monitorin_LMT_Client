/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring;

import classeServeur.Machine;
import classeServeur.WsMonitoring;
import classeServeur.WsMonitoring_Service;
import clientmonitoring.until.Until;
import clientmonitoring.ws.WSClientMonitoring;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.Endpoint;

/**
 *
 * @author KEF10
 */
public class ClientMonitoring {

    public static Logger LOGGER = Logger.getLogger(Class.class.getName());

    public ClientMonitoring() {
        Until.initialisationGestionFichierLog(LOGGER);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ClientMonitoring client = new ClientMonitoring();
        BeanClient beanClient = new BeanClient();
        if (beanClient.initialisation() && beanClient.demarerWSClientEtScheduler()) {
            beanClient.redemarerTachePrincipaleEtSousTache();

        }

    }
}
