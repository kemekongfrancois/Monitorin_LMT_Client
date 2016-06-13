/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitoring.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public class WSClientMonitoring {
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
       
        return "Hello " + txt + " ! le client es là";
    }
}
