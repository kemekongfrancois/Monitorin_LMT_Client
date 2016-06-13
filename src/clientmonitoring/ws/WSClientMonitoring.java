/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.ws;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

@WebService
public class WSClientMonitoring {
    Scheduler scheduler = clientmonitoring.ClientMonitoring.SCHEDULER;
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
        try {
            scheduler.shutdown();
        } catch (SchedulerException ex) {
            Logger.getLogger(WSClientMonitoring.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Hello " + txt + " !";
    }
}
