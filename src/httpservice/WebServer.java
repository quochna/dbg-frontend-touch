package httpservice;

import dbg.frontend.config.DbgFrontEndConfig;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import com.vng.jcore.common.Config;
import dbg.frontend.touch.DbgFrontendCore;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class WebServer extends Thread
{
    private static Logger ClassLogger = Logger.getLogger(WebServer.class);
    
    @Override
    public void run()
    {
        try
        {
            this.startWebServer();
        }
        catch(Exception ex)
        {
            ClassLogger.error("Web server error", ex);
            ClassLogger.error("Web server error RootCause: " +  ExceptionUtils.getRootCauseMessage(ex));
            ClassLogger.error("Web server error StackTrace: " +  ExceptionUtils.getStackTrace(ex));         
        }		
    }
    
    public void startWebServer() throws Exception
    {
        
        Server server = new Server();

        int listenPort = Integer.parseInt(Config.getParam("jetty", "listenPort"));
        int maxThreads = Integer.parseInt(Config.getParam("jetty", "maxThreads"));
        int minThreads = Integer.parseInt(Config.getParam("jetty", "minThreads"));
        int acceptors = Integer.valueOf(Config.getParam("jetty", "acceptors"));
        
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(minThreads);
        threadPool.setMaxThreads(maxThreads);
        server.setThreadPool(threadPool);
        

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(listenPort);
        connector.setMaxIdleTime(60000);
        //connector.setConfidentialPort(8443);
        connector.setStatsOn(false);
        connector.setLowResourcesConnections(20000);
        connector.setLowResourcesMaxIdleTime(5000);
        connector.setAcceptors(acceptors);
        
        server.setConnectors(new Connector[]{connector});
        
        
       
          
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/"); 
        context.getSessionHandler().getSessionManager().setMaxInactiveInterval(DbgFrontEndConfig.SessionTimeoutSeconds);
        //handlers.setHandlers(new Handler[]{context, new DefaultHandler()});
//        ServletHolder servletHolder = new ServletHolder(dbg.frontend.touch.SelectChannelController.class);
//        servletHolder.setInitOrder(1);
//        context.addServlet(servletHolder, "/chonkenhthanhtoan");
//        
//        servletHolder = new ServletHolder(dbg.frontend.touch.ChargeController.class);
//        servletHolder.setInitOrder(1);
//        context.addServlet(servletHolder, "/thanhtoan");
//        
//        servletHolder = new ServletHolder(dbg.frontend.touch.ResultController.class);
//        servletHolder.setInitOrder(1);
//        context.addServlet(servletHolder, "/ketqua");
//        
//        servletHolder = new ServletHolder(dbg.frontend.touch.AsyncResultController.class);
//        servletHolder.setInitOrder(1);
//        context.addServlet(servletHolder, "/async");
//        
//        
//        servletHolder = new ServletHolder(dbg.frontend.touch.Pay123ResultController.class);
//        servletHolder.setInitOrder(1);
//        context.addServlet(servletHolder, "/pay123result");
//   
      
        
       
        
        //Set max global session timeout
       
        //End
        
        
        String a = DbgFrontEndConfig.SystemUrl ;
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setResourceBase(DbgFrontEndConfig.SystemPublicPath);
        HandlerList handlers = new HandlerList();
        
        context.addServlet(DbgFrontendCore.class,  "/*");

        handlers.setHandlers(new Handler[]{resourceHandler, context});
        
        server.setHandler(handlers);
        
        
        server.setStopAtShutdown(true);
        server.setGracefulShutdown(1000);//1 giay se dong
        server.setSendServerVersion(false);
        
        ShutdownThread obj = new ShutdownThread(server);
        Runtime.getRuntime().addShutdownHook(obj);
        
        server.start();
        server.join();


    }

}