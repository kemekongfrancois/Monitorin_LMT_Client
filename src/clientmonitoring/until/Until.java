/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.until;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author KEF10
 */
public class Until {
    private static final String fichieLog = "log.txt";
    private static final String fichieLogErreur = "logErreur.txt";

    static Logger logger= clientmonitoring.ClientMonitoring.LOGGER;
    /**
     * cette fonction permt d'initialisé la gestion des fichier de log 
     * @param logger_ 
     */
    public static void initialisationGestionFichierLog(Logger logger_){
        try {
            Handler fh = new FileHandler(fichieLog,false); //Le fichier est recréé (false) ou repris tel quel (true)
            fh.setFormatter(new SimpleFormatter());//on defini ici que les données écrit dans le fichier seront du text
            logger_.addHandler(fh);//on ajouter le 
            
            Handler fhErreur = new FileHandler(fichieLogErreur,false);
            fhErreur.setFormatter(new SimpleFormatter());
            fhErreur.setLevel(Level.SEVERE);//cette instruction permet de dire que seul les alerte de niveau "SEVERE = Niveau le plus élevé" seront écri dans le fichier que represente ce Handler
            logger_.addHandler(fhErreur);
            
        } catch (IOException ex) {
            Logger.getLogger(Until.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Until.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * cette fonction prend en entre un fichier et retourne son contenu dans une liste
     * chaque élèment de la liste es une ligne du fichier
     *
     * @param nomFichier
     * @return "null" si le fichier n'existe pas
     */
    public static List<String> lectureFichier(String nomFichier) {

        File f = new File(nomFichier);
        if (f.exists()) {//on fait le traitement que si le fichier existe
            try {
                System.out.println("----------debut du fichier \" "+ nomFichier +"\" ------------");
                List<String> result = new ArrayList<>();
                try (Scanner scanner = new Scanner(f)) {
                    // On boucle sur chaque champ detecté
                    String ligne;
                    while (scanner.hasNextLine()) {
                        ligne = scanner.nextLine();
                        result.add(ligne);
                        System.out.println("***" + ligne);
                    }
                }
                System.out.println("----------fin du fichier \" "+ nomFichier +"\" ------------");

                return result;
            } catch (FileNotFoundException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }else{
            logger.log(Level.SEVERE, nomFichier + ": ce fichier n'existe pas");
        }
        return null;//
    }

    /**
     * cette fonction permet de vérifiè si l'adresse pris en paramettre fait
     * partie des adresses d'une des interface réseau
     *
     * @param adresse
     * @return true si une des interfaces réseaux à l'adresse pris en paramettre
     */
    public static boolean verrifieAdresseMachine(String adresse) {
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

}
