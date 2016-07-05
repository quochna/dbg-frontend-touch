///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package dbg.frontend.touch;
//
//import org.apache.log4j.Logger;
//import com.vng.jcore.common.Config;
//import dbg.client.DbgClientConfig;
//
///**
// *
// * @author hainpt
// */
//public class DbgTestToolConfig {
//
//    private static final Logger logger = Logger.getLogger(DbgTestToolConfig.class);
//    //public static int APP_ID;
//    //public static String KEY_1;
//    //public static String KEY_2;
//    //public static String GET_TRANSID_URL;
//    //public static String SUBMIT_TRANS_URL;
//    //public static String GET_TRANS_HISTORY_URL;
//    public static String TEMPLATE_PATH;
//    public static String SYSTEM_PUBLIC_PATH;
//    public static String MASTER_FORM_TITLE;
//    public static String SYSTEM_URL;
//    public static String STATIC_CONTENT_URL;
//    public static String SYSTEM_CREDITS_URL;
//    public static String MAINTAIN_MSG;
////    public static String SubmitTransUrl;
//    public static String SubmittransThanhtoanurl;
//    public static String TransHistoryUrl;
//    public static DbgClientConfig DBG_CLIENT_CONFIG;
//    public static String CacheHost = "";
//    public static String CachePort = "";
//    public static String CacheSource = "";
//    public static String CacheAuth = "";
//    public static String ErrorInfoCacheHost = "";
//    public static String ErrorInfoCachePort = "";
//    public static String ErrorInfoCacheSource = "";
//    public static String ErrorInfoCacheAuth = "";
//    public static String CardLockedInfoCacheHost = "";
//    public static String CardLockedInfoCachePort = "";
//    public static String CardLockedInfoCacheSource = "";
//    public static String CardLockedInfoCacheAuth = "";
//    public static Integer maxcounter = 2;
//    public static Integer diffinminute = 5;
//    public static Integer lockinminute = 10;
//    public static String receiveMoSecretKey = "";
//    
//    public static String gettransstatus="";
//
//    public static boolean loadConfigs() {
//
//        boolean result = true;
//        try {
//            DBG_CLIENT_CONFIG = new DbgClientConfig();
//            CacheHost = Config.getParam("cacheService", "host");
//            CachePort = Config.getParam("cacheService", "port");
//            CacheSource = Config.getParam("cacheService", "source");
//            CacheAuth = Config.getParam("cacheService", "auth");
//
//            DBG_CLIENT_CONFIG.appID = Integer.parseInt(Config.getParam("dbgclient", "appid"));
//            DBG_CLIENT_CONFIG.key1 = Config.getParam("dbgclient", "key1");
//            DBG_CLIENT_CONFIG.key2 = Config.getParam("dbgclient", "key2");
//            DBG_CLIENT_CONFIG.hashKey = Config.getParam("dbgclient", "hashkey");
//            DBG_CLIENT_CONFIG.apiBaseUrl = Config.getParam("dbgclient", "apiBaseUrl");
//
////            SubmitTransUrl = Config.getParam("dbgclient", "submittransurl");
//            TransHistoryUrl = Config.getParam("dbgclient", "transHistoryUrl");
//            gettransstatus = Config.getParam("dbgclient", "gettransstatus");
//
//
//            SubmittransThanhtoanurl = Config.getParam("dbgclient", "SubmittransThanhtoanurl");
//
//            logger.info("AppID: " + DBG_CLIENT_CONFIG.appID);
//            logger.info("Key1 : " + DBG_CLIENT_CONFIG.key1);
//            logger.info("Hash key: " + DBG_CLIENT_CONFIG.hashKey);
//            logger.info("Api Base Url: " + DBG_CLIENT_CONFIG.apiBaseUrl);
//
//            SYSTEM_PUBLIC_PATH = Config.getParam("system", "system-public-path");
//            SYSTEM_URL = Config.getParam("system", "system-url");
//            MASTER_FORM_TITLE = Config.getParam("system", "master-form-title");
//            STATIC_CONTENT_URL = Config.getParam("system", "static-content-url");
//
//            MAINTAIN_MSG = Config.getParam("system", "maintain-msg");
//            SYSTEM_CREDITS_URL = Config.getParam("system", "credits-url");
//
//            ErrorInfoCacheHost = Config.getParam("errorInfoCache", "host");
//            ErrorInfoCachePort = Config.getParam("errorInfoCache", "port");
//            ErrorInfoCacheSource = Config.getParam("errorInfoCache", "source");
//            ErrorInfoCacheAuth = Config.getParam("errorInfoCache", "auth");
//
//            CardLockedInfoCacheHost = Config.getParam("cardLockedInfoCache", "host");
//            CardLockedInfoCachePort = Config.getParam("cardLockedInfoCache", "port");
//            CardLockedInfoCacheSource = Config.getParam("cardLockedInfoCache", "source");
//            CardLockedInfoCacheAuth = Config.getParam("cardLockedInfoCache", "auth");
//
//            receiveMoSecretKey = Config.getParam("sms", "receiveMoSecretKey");
//
//        } catch (Exception ex) {
//            logger.error("Exception: " + ex.toString());
//            result = false;
//        }
//        return result;
//    }
//
//    public static void init() {
//    }
//}
