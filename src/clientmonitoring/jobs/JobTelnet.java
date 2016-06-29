/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.jobs;

import clientmonitoring.BeanClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 *
 * @author KEF10
 */
public class JobTelnet implements Job {

    Logger logger = clientmonitoring.ClientMonitoring.LOGGER;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("------effectué Telnet: ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String adresseAEtPort = dataMap.getString("adresseAEtPort");
        JobKey cle = context.getJobDetail().getKey();

        BeanClient beanClient = new BeanClient();
        if (beanClient.telnet(adresseAEtPort)) {
            logger.log(Level.INFO, "le telnet vers <<" + adresseAEtPort + ">> es OK");
        } else {
            logger.log(Level.SEVERE, "impossible de contacter :<<" + adresseAEtPort + ">>");
            beanClient.envoiAlerteAuServeur(cle, 0);
        }
    }
}
