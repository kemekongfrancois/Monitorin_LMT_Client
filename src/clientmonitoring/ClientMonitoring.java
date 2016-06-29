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
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.Endpoint;

/**
 *
 * @author KEF10
 */
public class ClientMonitoring {

    public static final String OSWINDOWS = "Windows";
    public static final String OSLinux = "Linux";
    public static final String TACHE_DD = "surveiller_dd";
    public static final String ALERTE = "ALERTE";
    public static final String START = "START";
    public static final String STOP = "STOP";
    public static final String PB = "PB";
    public static final String KO = "KO";
    public static final String OK = "OK";
    public static final String TACHE_PROCESSUS = "surveiller_processus";
    public static final String TACHE_SERVICE = "surveiller_service";
    public static final String TACHE_PING = "ping";
    public static final String TACHE_FICHIER_EXISTE = "surveille fichier existe";
    public static final String TACHE_TAILLE_FICHIER = "surveille taille fichier";
    public static final String TACHE_TELNET = "telnet";

    public static final int NB_LIGNE_FICHIER_CONF = 4;
    public static final String ficfierConfig = "parametre.txt";

    public static WsMonitoring wsServeur;
    private String ADRESSE_SERVEUR = "";
    private String PORT_SERVEUR;
    private String ADRESSE_MACHINE = "";
    private String PORT_MACHINE;

    public static String OS_MACHINE;
    private String nomMachine;

    public static Logger LOGGER = Logger.getLogger(Class.class.getName());

    public ClientMonitoring() {
        Until.initialisationGestionFichierLog(LOGGER);
    }

    /**
     * cette fonction permet d'initialisé le client et certain paramètre il
     * démarer le Scheduler
     */
    public boolean initialisation() {
        try {
        //---------on recupere le type d'OS du système------
        OS_MACHINE = System.getProperty("os.name");
        if (OS_MACHINE.contains(OSWINDOWS)) {
            OS_MACHINE = OSWINDOWS;
        } else {
            OS_MACHINE = OSLinux;
        }

        //----- on recuper le nom de la machine------------
        nomMachine = System.getProperty("user.name");

        //------------on recuper les paramettre contenue dans le fichie de paramettre-------------
        List<String> parmettre = Until.lectureFichier(ficfierConfig);//lecture du fichier de paramettre

        if (parmettre.size() >= NB_LIGNE_FICHIER_CONF) {
            int i = 0;
            ADRESSE_SERVEUR = parmettre.get(i++);
            PORT_SERVEUR = parmettre.get(i++);
            ADRESSE_MACHINE = parmettre.get(i++);
            PORT_MACHINE = parmettre.get(i++);
        } else {
            LOGGER.log(Level.SEVERE, "le fichier de configuration es incorect");
            return false;
        }
        BeanClient beanClient = new BeanClient();
        //------on verifie que l'adresse de la machine es valide---------
        if (!beanClient.verrifieAdresseMachine(ADRESSE_MACHINE)) {
            LOGGER.log(Level.SEVERE, "l'adresse de la machine ne correspond à aucune interface réseau: "+ADRESSE_MACHINE);
            return false;
        }
        
            //------------on initialise le webService qui vas envoyer des requette au serveur----------
            URL url = new URL("http://" + ADRESSE_SERVEUR + ":" + PORT_SERVEUR + "/projetMonitoring-war/WsMonitoring?wsdl");
            WsMonitoring_Service service = new WsMonitoring_Service(url);
            wsServeur = service.getWsMonitoringPort();

            //------------on démarer le Scheduler----------
            if (!beanClient.demarerLeScheduler()) {
                return false;
            }

            return true;
            //--------- on recupere les paramettre de la machine---------
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Adresse du serveur ou port invalide ou un autre PB\n", ex);
            return false;
        }

    }

    public void demarerTachePrincipaleEtSOusTache() {
        Machine machine = wsServeur.verifiOuCreerMachine(ADRESSE_MACHINE, PORT_MACHINE, OS_MACHINE, nomMachine);
        BeanClient lesfonction = new BeanClient();
        lesfonction.redemarerTachePrincipaleEtSousTache(machine, wsServeur.getListTacheMachine(machine.getAdresseIP()));
    }

    public void demarerWSClient() {
        //String URL = "http://"+ADRESSE_MACHINE+":8080/";
        String URL = "http://" + ADRESSE_MACHINE + ":" + PORT_MACHINE + "/";
        Endpoint.publish(URL, new WSClientMonitoring());
        LOGGER.log(Level.INFO, "Web Service démarer: " + URL);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ClientMonitoring client = new ClientMonitoring();

        if (client.initialisation()) {
            client.demarerTachePrincipaleEtSOusTache();
            client.demarerWSClient();
        }

    }
}
