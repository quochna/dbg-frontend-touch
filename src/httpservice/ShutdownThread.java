/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package httpservice;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;

/**
 *
 * @author hainpt
 */
public class ShutdownThread extends Thread
{

    private Server server;
    private static Logger ClassLogger = Logger.getLogger(ShutdownThread.class);

    public ShutdownThread(Server server)
    {
        this.server = server;
    }

    @Override
    public void run()
    {
        ClassLogger.info("Waiting for shut down!");
         try
         {			
            // Add code before shutdown here
            server.stop();
         } 
         catch (Exception ex)
         {
             ClassLogger.error(ex.getMessage());
         }
         
         ClassLogger.info("Server shutted down!");
    }
		
}
