package main;

import httpservice.WebServer;
import java.io.File;
import com.vng.jcore.common.LogUtil;
import dbg.frontend.config.DbgFrontEndConfig;
//import dbg.frontend.touch.DbgTestToolConfig;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class Main
{
    private static Logger ClassLogger = Logger.getLogger(Main.class);
    
    public static void main(String[] args) throws Exception
    {
        try
        {
            LogUtil.init();     
            InitBusiness();
            
            String pidFile = System.getProperty("pidfile");
            if (pidFile != null)
            {
                new File(pidFile).deleteOnExit();
            }
            
//            if (System.getProperty("foreground") == null)
//            {
//                System.out.close();
//                System.err.close();
//            }
            
            WebServer webserver = new WebServer();
            webserver.start();

        }
        catch (Throwable e)
        {
            ClassLogger.error("Exception at start up: " + e.getMessage());
            ClassLogger.error("Exception at start up RootCause: " +  ExceptionUtils.getRootCauseMessage(e));
            ClassLogger.error("Exception at start up StackTrace: " +  ExceptionUtils.getStackTrace(e));         
            System.exit(3);
        }
    }
    
    public static void InitBusiness()
    {
        // Init cac business khoi dau de chay nhanh		
        if (DbgFrontEndConfig.loadConfigs() == false)
           System.exit(1);
    }
}
