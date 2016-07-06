/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import com.vng.jcore.common.Config;
import dbg.entity.BankEntity;
import dbg.request.AppReq;
import dbg.request.AppServerReq;
import dbg.response.AppResp;
import dbg.response.AppServerResp;
import dbg.entity.MiniAppEntity;
import dbg.entity.PmcEntity;
import dbg.entity.PmcGroupEntity;
import dbg.request.PmcReq;
import dbg.response.PmcResp;
import dbg.request.PmcGroupReq;
import dbg.response.PmcGroupResp;
import dbg.util.DateTimeUtil;
import dbg.util.HashUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import dbg.entity.MiniAppServerEntity;
import dbg.request.BankReq;
import dbg.response.BankResp;
import java.text.ParseException;

/**
 *
 * @author hainpt
 */
public class DbgFrontEndConfig {

    private static final Logger logger = Logger.getLogger(DbgFrontEndConfig.class);
    public final static String DateTimeFormatString = "yyyy-MM-dd HH:mm:ss.SSS";
    public static String SystemPublicPath;
    public static String MasterFormTitle;
    public static String SystemUrl;
    public static String StaticContentUrl;
//    public static String SystemCreditsUrl;
    public static String MaintainMsg;
    public static int WebFeClientID;
    public static String WebFeHashKey;
    public static String WebFeAesKey;
    public static int EnvID;
    public static int Platform = 1;
    public static String GetPmcUrl;
    public static String GetPmcGroupUrl;
    public static String GetAppUrl;
    public static String Getappserverurl;
    public static String SubmitTransUrl;
    public static String GetTransStatusUrl;
    public static String AsyncResultUrl;
    public static String PreCheckTransUrl;
    public static Map<Integer, PmcEntity> PmcEntityMap = null;
    public static Map<Integer, MiniAppEntity> AppEntityMap = null;
    public static Map<Integer, PmcGroupEntity> PmcGrpEntityMap = null;
    public static Map<String, MiniAppServerEntity> AppServerEntityMap = null;
    public static int numberofcallingasync = 20;
    public static int intervalcallinginsecondsasync = 10;
    public static Map<String, String> PmcGrpHeaders = null;
    public static Map<String, String> PmcHeaders = null;
    public static Map<Integer, String> TelcoCardSerialValidatorPatterns = null;
    public static Map<Integer, String> TelcoCardPasswordValidatorPatterns = null;
    //added on 12/06/1024
    public static Map<Integer, BankEntity> BankEntityMap = null;
    public static String GetBankUrl;
    public static String BanksIDs = "";
    public static String GetValidateATMUrl = "";
    public static String BaseDbgApiUrl = "";
    public static String Pay123CancelURL = "";
    public static String Pay123RedirectURL = "";
    public static String Pay123ErrorURL = "";
    public static int Pay123numberofcallingasync = 20;
    public static int Pay123intervalcallinginsecondsasync = 10;
    public static String Pay123sessionkey = "pay123";
    public static String atmquerystatusUrl = "";
    public static String TransIDsessionkey = "transid";
    public static String GetBalanceUrl = "";
    public static String VNGAuthMasterService;
    public static String VNGAuthSlaveService;
    public static String VNGAuthAuth;
    public static String VNGAuthSource;
    public static int CallApiTimeoutSeconds = 10;
    public static int SessionTimeoutSeconds = 900;
    public static int AutoRedirectMiliSeconds = 60000;
    public static int MinAtmMoney = 9700;
    public static int MaxAtmMoney = 10000000;
    public static int MinZingXu = 100;
    public static int MaxZingXu = 10000;
    public static String Exception = "Hệ thống đang có lỗi, giao dịch thất bại!";

    public static Map<String, String> ATMDiscount = null;

    //For TKmobi    
    public static List<String> TKDienthoaiMobiChargeAmts = null;
    public static String TKMobiPatternValidate = "";

    //For Static Content 
    public static String _KenhThanhToanChuaHoTro = "Kênh thanh toán này chưa được hỗ trợ, vui lòng quay lại sau!";

    //For VISA, MASTER 123PAY
    public static String Visa_Master_BankCode_123PAY = "123PCC";

    //For Discount Percents
    public static Map<String, String> VisaMasterDiscount = null;

    //For min visamaster validations
    public static Map<Integer, Integer> VisaMasterMinMoneys = null;

    //For ATM chia het cho
    public static int ATMChargeAmtMod = 10000;

    //Passport API
    public static String PassportLoginURL = "";
    public static String PassportVerifyURL = "";
    public static String PassPortProxyHost = "";
    public static int PassPortProxyPort = 8088;
    public static boolean PassPortIsLoginUseProxy = false;
    public static boolean PassPortIsVerifyUseProxy = false;
    public static String PassportApiKey = "";
    public static String PassportPriKey = "";
    public static String PassportPID = "";

    public static boolean loadConfigs() {
        boolean result = true;
        try {
            SystemPublicPath = Config.getParam("system", "system-public-path");
            SystemUrl = Config.getParam("system", "system-url");
            MasterFormTitle = Config.getParam("system", "master-form-title");
            StaticContentUrl = Config.getParam("system", "static-content-url");
            MaintainMsg = Config.getParam("system", "maintain-msg");
//            SystemCreditsUrl = Config.getParam("system", "credits-url");
            AsyncResultUrl = Config.getParam("system", "AsyncResultUrl");

            WebFeClientID = Integer.parseInt(Config.getParam("frontend", "webfeclientid"));
            WebFeHashKey = Config.getParam("frontend", "webfehashkey");
            WebFeAesKey = Config.getParam("frontend", "webfeaeskey");
            EnvID = Integer.parseInt(Config.getParam("frontend", "envid"));

            CallApiTimeoutSeconds = Integer.parseInt(Config.getParam("timeout", "callapiseconds"));
            logger.info(String.format("CallApiTimeoutSeconds: %d", CallApiTimeoutSeconds));
            SessionTimeoutSeconds = Integer.parseInt(Config.getParam("timeout", "sessiontimeoutinseconds"));
            logger.info(String.format("SessionTimeoutSeconds: %d", SessionTimeoutSeconds));

            AutoRedirectMiliSeconds = Integer.parseInt(Config.getParam("timeout", "autoredirectmiliseconds"));
            logger.info(String.format("AutoRedirectMiliSeconds: %d", AutoRedirectMiliSeconds));
            //Load DBG API URL

            BaseDbgApiUrl = Config.getParam("dbgapi", "dbgapibaseurl");
            logger.info(String.format("BaseDbgApiUrl: %s", BaseDbgApiUrl));

            GetPmcUrl = BaseDbgApiUrl + Config.getParam("dbgapi", "getpmcurl");
            logger.info(String.format("GetPmcUrl: %s", BaseDbgApiUrl));

            GetPmcGroupUrl = BaseDbgApiUrl + Config.getParam("dbgapi", "getpmcgroupurl");
            logger.info(String.format("GetPmcGroupUrl: %s", GetPmcGroupUrl));

            GetAppUrl = BaseDbgApiUrl + Config.getParam("dbgapi", "getappurl");
            logger.info(String.format("GetAppUrl: %s", GetAppUrl));

            SubmitTransUrl = BaseDbgApiUrl + Config.getParam("dbgapi", "submittransurl");
            logger.info(String.format("SubmitTransUrl: %s", SubmitTransUrl));

            GetTransStatusUrl = BaseDbgApiUrl + Config.getParam("dbgapi", "gettransstatus");
            logger.info(String.format("GetTransStatusUrl: %s", GetTransStatusUrl));

            Getappserverurl = BaseDbgApiUrl + Config.getParam("dbgapi", "getappserverurl");
            logger.info(String.format("Getappserverurl: %s", Getappserverurl));

            PreCheckTransUrl = BaseDbgApiUrl + Config.getParam("dbgapi", "prechecktransurl");
            logger.info(String.format("PreCheckTransUrl: %s", PreCheckTransUrl));

            GetBankUrl = BaseDbgApiUrl + Config.getParam("dbgapi", "getbankurl");
            logger.info(String.format("GetBankUrl: %s", GetBankUrl));

            GetValidateATMUrl = BaseDbgApiUrl + Config.getParam("dbgapi", "getvalidateatmurl");
            logger.info(String.format("GetValidateATMUrl: %s", GetValidateATMUrl));

            atmquerystatusUrl = BaseDbgApiUrl + Config.getParam("dbgapi", "atmquerystatusUrl");
            logger.info(String.format("atmquerystatusUrl: %s", atmquerystatusUrl));

            GetBalanceUrl = BaseDbgApiUrl + Config.getParam("dbgapi", "getbalanceurl");
            logger.info(String.format("GetBalanceURL: %s", GetBalanceUrl));

            //End Load DBG API URL
            //Load 123 Pay Redirect URL
            Pay123CancelURL = SystemUrl + Config.getParam("123payatm", "cancelurl");
            logger.info(String.format("Pay123CancelURL: %s", Pay123CancelURL));
            Pay123ErrorURL = SystemUrl + Config.getParam("123payatm", "errorurl");
            logger.info(String.format("Pay123ErrorURL: %s", Pay123ErrorURL));
            Pay123RedirectURL = SystemUrl + Config.getParam("123payatm", "redirecturl");
            logger.info(String.format("Pay123RedirectURL: %s", Pay123RedirectURL));

            MinAtmMoney = Integer.parseInt(Config.getParam("123payatm", "minatmmoney"));
            logger.info(String.format("MinAtmMoney: %s", MinAtmMoney));
            MaxAtmMoney = Integer.parseInt(Config.getParam("123payatm", "maxatmmoney"));
            logger.info(String.format("MaxAtmMoney: %s", MaxAtmMoney));

//            MinZingXu = Integer.parseInt(Config.getParam("zingxu", "minzingxu"));
//            logger.info("MinZingXu: " +  MinZingXu);
//            
//            MaxZingXu = Integer.parseInt(Config.getParam("zingxu", "maxzingxu"));
//            logger.info("MaxZingXu: " +  MaxZingXu);
            //End Load 123 Pay Redirect URL
            logger.info(String.format("WebFeClientID: %d", WebFeClientID));
            logger.info(String.format("WebFeHashKey: %s", WebFeHashKey));

            logger.info(String.format("AsyncResultUrl: %s", AsyncResultUrl));

            logger.info("Loading Pmc List");
            PmcEntityMap = new HashMap<>();
            List<PmcEntity> pmcList = getPmc();
            for (PmcEntity p : pmcList) {
                PmcEntityMap.put(p.pmcID, p);
                logger.info(p.toJsonString());
            }

            logger.info("Loading App List");
            AppEntityMap = new HashMap<>();
            List<MiniAppEntity> appList = getApp();
            for (MiniAppEntity app : appList) {
                AppEntityMap.put(app.appID, app);
                logger.info(app.toJsonString());
            }

            logger.info("Loading PmcGrp List");
            PmcGrpEntityMap = new HashMap<>();
            List<PmcGroupEntity> pmcgrpList = getPmcGrp();
            for (PmcGroupEntity p : pmcgrpList) {
                PmcGrpEntityMap.put(p.pmcGroupID, p);
                logger.info(p.toJsonString());
            }

            logger.info("Loading App Server List");
            AppServerEntityMap = new HashMap<>();
            List<MiniAppServerEntity> appServerList = getAppServer();
            for (MiniAppServerEntity app : appServerList) {

                AppServerEntityMap.put(CreateAppServerKey(app.appServerID, app.appID), app);
                logger.info(app.toJsonString());
            }

            logger.info("Loading asyncgetresult");

            numberofcallingasync = Integer.parseInt(Config.getParam("asyncgetresult", "numberofcalling"));
            logger.info("Loading numberofcalling :" + numberofcallingasync);
            intervalcallinginsecondsasync = Integer.parseInt(Config.getParam("asyncgetresult", "intervalcallinginseconds"));
            logger.info("Loading intervalcallinginseconds :" + intervalcallinginsecondsasync);

            //Begin Pay123 load asyn result
            logger.info("Loading Pay123asyncgetresult");

            Pay123numberofcallingasync = Integer.parseInt(Config.getParam("123payatm", "numberofcalling"));
            logger.info("Loading Pay123numberofcallingasync :" + Pay123numberofcallingasync);
            Pay123intervalcallinginsecondsasync = Integer.parseInt(Config.getParam("123payatm", "intervalcallinginseconds"));
            logger.info("Loading Pay123intervalcallinginsecondsasync :" + Pay123intervalcallinginsecondsasync);

            //End Pay123 load async result
            LoadPmcGrpHeaders();

            //added on 24/03/2014 for load TcolCard perttern
            TelcoCardSerialValidatorPatterns = loadCarValidatorPatterns("123paytelco",
                    "cardSerialValidationPatterns", ";", ":");
            TelcoCardPasswordValidatorPatterns = loadCarValidatorPatterns("123paytelco",
                    "cardPasswordValidationPatterns", ";", ":");

            //added on 12/06/2014 for ATM
            logger.info("Loading Banks  List");
            BankEntityMap = new HashMap<>();
            List<BankEntity> bankList = getBanks();
            for (BankEntity en : bankList) {
                if (en.status && en.supportTouch) {

                    BankEntityMap.put(en.id, en);
                    logger.info(en.toJsonString());
                    BanksIDs += String.valueOf(en.id) + ",";
                }
            }
            if (BanksIDs.length() > 0) {
                BanksIDs = BanksIDs.substring(0, BanksIDs.length() - 1);
            }
            logger.info("Loading BankIDs :" + BanksIDs);

            String masterPort = Config.getParam("vngauth", "master_port");
            String masterHost = Config.getParam("vngauth", "master_host");
            VNGAuthMasterService = masterHost + ":" + masterPort;
            logger.info("VNGAuthMasterService: " + VNGAuthMasterService);

            String slaveHost = Config.getParam("vngauth", "slave_host");
            String slavePort = Config.getParam("vngauth", "slave_port");
            VNGAuthSlaveService = slaveHost + ":" + slavePort;
            logger.info("VNGAuthSlaveService: " + VNGAuthSlaveService);
            VNGAuthAuth = Config.getParam("vngauth", "auth");
            logger.info("VNGAuthAuth: " + VNGAuthAuth);
            VNGAuthSource = Config.getParam("vngauth", "source");
            logger.info("VNGAuthSource: " + VNGAuthSource);

            //ATM Discountpercent
            ATMDiscount = loadAtmDisCountPercents("123payatm", "discountpercent", ";", ":");

            //For TKMobi
            LoadTKDienthoaiMobiChargeAmts();
            TKMobiPatternValidate = Config.getParam("tkdienthoaimobi", "patternvalidate");

            //For Zalo App
            // LoadZaloAppIDs();
            //for add More ZingXu
//            AddMoreZingXuURL = Config.getParam("zpayv2", "addthemzxurl");
//            logger.info("AddMoreZingXuURL: " + AddMoreZingXuURL);
            //Load VisaMaster discountPercent         
            VisaMasterDiscount = loadVisaMasterDisCountPercents("visamasterdiscount", "discountpercents", ";", ":");

            //Load VisaMasterMinMoneys
            VisaMasterMinMoneys = loadVisaMasterMinMoneys("visamaster", "minmoneys", ";", ":");

            //Vi Thap Phong   
//            ThapPhongMoreXuURL = Config.getParam("thapphongwallet", "addmoreurl");
//            logger.info("ThapPhongMoreXuURL: " + ThapPhongMoreXuURL);
//            ThapPhongMinXu = Integer.parseInt(Config.getParam("thapphongwallet", "minxu"));
//            logger.info("ThapPhongMinXu: " + ThapPhongMinXu); 
//            ThapPhongMaxXu = Integer.parseInt(Config.getParam("thapphongwallet", "maxxu"));
//            logger.info("ThapPhongMaxXu: " + ThapPhongMaxXu); 
//            
//            ThapPhongGetBalanceURL = BaseDbgApiUrl + Config.getParam("dbgapi", "getthapphongbalanceurl");
//            logger.info("ThapPhongGetBalanceURL: " + ThapPhongGetBalanceURL); 
//            
            //For CCTalk
//            CCTalkAppID = Config.getParam("cctalk", "appid");  
            //For PassPort API
//             PassportLoginURL= Config.getParam("passportapi", "loginurl");
//             logger.info("PassportLoginURL: " + PassportLoginURL); 
//             
//             PassPortProxyHost=Config.getParam("passportapi", "proxyhost");
//             logger.info("PassPortProxyHost: " + PassPortProxyHost); 
//             
//             PassPortProxyPort=Integer.parseInt(Config.getParam("passportapi", "proxyport"));
//            logger.info("PassPortProxyPort: " + PassPortProxyPort); 
//            
//             
//             PassPortIsLoginUseProxy=Boolean.parseBoolean(Config.getParam("passportapi", "loginuseproxy"));
//             logger.info("PassPortIsLoginUseProxy: " + PassPortIsLoginUseProxy); 
//             
//             
//             PassportVerifyURL = Config.getParam("passportapi", "verifyurl");             
//             logger.info("PassportVerifyURL: " + PassportVerifyURL); 
//             
//             
//             PassPortIsVerifyUseProxy = Boolean.parseBoolean(Config.getParam("passportapi", "verifyuseproxy"));
//            logger.info("PassPortIsVerifyUseProxy: " + PassPortIsVerifyUseProxy); 
//             
//             PassportApiKey = Config.getParam("passportapi", "apikey");
//             logger.info("PassportApiKey: " + PassportApiKey); 
//             
//             PassportPriKey = Config.getParam("passportapi", "prikey");
//             logger.info("PassportPriKey: " + PassportPriKey); 
//             
//             PassportPID = Config.getParam("passportapi", "productid");
//              logger.info("PassportPID: " + PassportPID); 
//             
        } catch (Exception ex) {
            logger.error("Exception: " + ex.toString());
            result = false;
        }

        return result;
    }

    public static List<PmcEntity> getPmc() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateTimeFormatString);

        PmcResp resp = null;

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            PmcReq request = new PmcReq();
            request.feClientID = WebFeClientID;
            request.reqDate = DateTimeUtil.getCurDateWithMilisec();
            String data = String.format("%s%s%s", request.feClientID,
                    simpleDateFormat.format(request.reqDate), WebFeHashKey);
            request.sig = dbg.util.HashUtil.hashSHA256(data);

            HttpPost httpPost = new HttpPost(GetPmcUrl);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("feclientid", String.valueOf(request.feClientID)));
            nvps.add(new BasicNameValuePair("reqdate", simpleDateFormat.format(request.reqDate)));
            nvps.add(new BasicNameValuePair("sig", request.sig));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();

                InputStream inputStream = entity.getContent();
                String sResponse = IOUtils.toString(inputStream, "UTF-8");

                JsonElement jsonElement = new JsonParser().parse(sResponse);
                JsonObject jsonObj = jsonElement.getAsJsonObject();

                resp = new PmcResp();
                resp.returnCode = jsonObj.get("returnCode").getAsInt();

                if (resp.returnCode != 1) {
                    return null;
                }

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setDateFormat(DbgFrontEndConfig.DateTimeFormatString);
                Gson gson = gsonBuilder.create();

                Type collectionType = new TypeToken<List<PmcEntity>>() {
                }.getType();
                resp.pmcList = gson.fromJson(jsonObj.get("pmcList").toString(), collectionType);
            }
        }

        return resp.pmcList;
    }

    public static List<MiniAppEntity> getApp() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateTimeFormatString);

        AppResp resp = null;

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            AppReq request = new AppReq();
            request.feClientID = WebFeClientID;
            request.reqDate = DateTimeUtil.getCurDateWithMilisec();
            String data = String.format("%s%s%s", request.feClientID,
                    simpleDateFormat.format(request.reqDate), WebFeHashKey);
            request.sig = HashUtil.hashSHA256(data);

            HttpPost httpPost = new HttpPost(GetAppUrl);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("feclientid", String.valueOf(request.feClientID)));
            nvps.add(new BasicNameValuePair("reqdate", simpleDateFormat.format(request.reqDate)));
            nvps.add(new BasicNameValuePair("sig", request.sig));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();

                InputStream inputStream = entity.getContent();
                String sResponse = IOUtils.toString(inputStream, "UTF-8");

                JsonElement jsonElement = new JsonParser().parse(sResponse);
                JsonObject jsonObj = jsonElement.getAsJsonObject();

                resp = new AppResp();
                resp.returnCode = jsonObj.get("returnCode").getAsInt();

                if (resp.returnCode != 1) {
                    return null;
                }

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setDateFormat(DbgFrontEndConfig.DateTimeFormatString);
                Gson gson = gsonBuilder.create();

                Type collectionType = new TypeToken<List<MiniAppEntity>>() {
                }.getType();
                resp.appList = gson.fromJson(jsonObj.get("appList").toString(), collectionType);
            }
        }

        return resp.appList;
    }

    public static List<PmcGroupEntity> getPmcGrp() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateTimeFormatString);

        PmcGroupResp resp = null;

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            PmcGroupReq request = new PmcGroupReq();
            request.feClientID = WebFeClientID;
            request.reqDate = DateTimeUtil.getCurDateWithMilisec();
            String data = String.format("%s%s%s", request.feClientID,
                    simpleDateFormat.format(request.reqDate), WebFeHashKey);
            request.sig = dbg.util.HashUtil.hashSHA256(data);

            HttpPost httpPost = new HttpPost(GetPmcGroupUrl);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("feclientid", String.valueOf(request.feClientID)));
            nvps.add(new BasicNameValuePair("reqdate", simpleDateFormat.format(request.reqDate)));
            nvps.add(new BasicNameValuePair("sig", request.sig));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();

                InputStream inputStream = entity.getContent();
                String sResponse = IOUtils.toString(inputStream, "UTF-8");

                JsonElement jsonElement = new JsonParser().parse(sResponse);
                JsonObject jsonObj = jsonElement.getAsJsonObject();

                resp = new PmcGroupResp();
                resp.returnCode = jsonObj.get("returnCode").getAsInt();

                if (resp.returnCode != 1) {
                    return null;
                }

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setDateFormat(DbgFrontEndConfig.DateTimeFormatString);
                Gson gson = gsonBuilder.create();

                Type collectionType = new TypeToken<List<PmcGroupEntity>>() {
                }.getType();
                resp.pmcGroupList = gson.fromJson(jsonObj.get("pmcGroupList").toString(), collectionType);
            }
        }

        return resp.pmcGroupList;
    }

    public static List<PmcEntity> getPmc(int GroupID) throws Exception {
        List<PmcEntity> list = new ArrayList<PmcEntity>();
        if (PmcEntityMap != null) {
            Iterator<Integer> keySetIterator = PmcEntityMap.keySet().iterator();
            while (keySetIterator.hasNext()) {
                Integer key = keySetIterator.next();
                PmcEntity pmcEntity = DbgFrontEndConfig.PmcEntityMap.get(key);
                if (pmcEntity != null && pmcEntity.pmcGroupID == GroupID) {
                    list.add(pmcEntity);
                }
            }
        }
        return list;
    }

    public static List<MiniAppServerEntity> getAppServer() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateTimeFormatString);

        AppServerResp resp = null;

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            AppServerReq request = new AppServerReq();
            request.feClientID = WebFeClientID;
            request.reqDate = DateTimeUtil.getCurDateWithMilisec();
            String data = String.format("%s%s%s", request.feClientID,
                    simpleDateFormat.format(request.reqDate), WebFeHashKey);
            request.sig = HashUtil.hashSHA256(data);

            HttpPost httpPost = new HttpPost(Getappserverurl);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("feclientid", String.valueOf(request.feClientID)));
            nvps.add(new BasicNameValuePair("reqdate", simpleDateFormat.format(request.reqDate)));
            nvps.add(new BasicNameValuePair("sig", request.sig));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();

                InputStream inputStream = entity.getContent();
                String sResponse = IOUtils.toString(inputStream, "UTF-8");

                JsonElement jsonElement = new JsonParser().parse(sResponse);
                JsonObject jsonObj = jsonElement.getAsJsonObject();

                resp = new AppServerResp();
                resp.returnCode = jsonObj.get("returnCode").getAsInt();

                if (resp.returnCode != 1) {
                    return null;
                }

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setDateFormat(DbgFrontEndConfig.DateTimeFormatString);
                Gson gson = gsonBuilder.create();

                Type collectionType = new TypeToken<List<MiniAppServerEntity>>() {
                }.getType();
                resp.appServerList = gson.fromJson(jsonObj.get("appServerList").toString(), collectionType);
            }
        }

        return resp.appServerList;
    }

    public static String CreateAppServerKey(String appServerID, int appID) {
        String key = String.format("%s_%s", appServerID, appID);
        return key.trim().toLowerCase();
    }

    public static String CreateAppServerKey(String appServerID, String appID) {
        String key = String.format("%s_%s", appServerID, appID);
        return key.trim().toLowerCase();
    }

    public static String GetPMCName(String pmcID) {
        String PMCName = "";
        if (pmcID != null && pmcID.trim() != "") {
            PmcEntity pmcEntity = DbgFrontEndConfig.PmcEntityMap.get(Integer.parseInt(pmcID));
            if (pmcEntity != null) {
                PMCName = pmcEntity.pmcName;
            }
        }
        return PMCName;
    }

    public static void LoadPmcGrpHeaders() {
        PmcGrpHeaders = new HashMap<>();
        PmcHeaders = new HashMap<>();
        logger.info("--->LoadPmcGrpHeaders :");
        if (PmcGrpEntityMap != null) {
            Iterator<Integer> keySetIterator = PmcGrpEntityMap.keySet().iterator();
            while (keySetIterator.hasNext()) {
                Integer key = keySetIterator.next();
                PmcGroupEntity pmcgrpEntity = DbgFrontEndConfig.PmcGrpEntityMap.get(key);
                if (pmcgrpEntity != null) {
                    String pmcgrp_headermsg = Config.getParam("pmcgrpheader", Create_pmcgrp_headermsg_Key(key));

                    if (pmcgrp_headermsg != null) {
                        PmcGrpHeaders.put(Create_pmcgrp_headermsg_Key(key), pmcgrp_headermsg);
                        logger.info(Create_pmcgrp_headermsg_Key(key) + " : " + pmcgrp_headermsg);
                    }

                }
            }

        }
        logger.info("--->LoadPmcHeaders :");
        if (PmcEntityMap != null) {
            Iterator<Integer> keySetIterator = PmcEntityMap.keySet().iterator();
            while (keySetIterator.hasNext()) {
                Integer key = keySetIterator.next();
                PmcEntity pmcEntity = DbgFrontEndConfig.PmcEntityMap.get(key);
                if (pmcEntity != null) {
                    String pmc_headermsg = Config.getParam("pmcgrpheader", Create_pmc_chargemsg_Key(key));

                    if (pmc_headermsg != null) {
                        PmcHeaders.put(Create_pmc_chargemsg_Key(key), pmc_headermsg);
                        logger.info(Create_pmc_chargemsg_Key(key) + " : " + pmc_headermsg);
                    }

                }
            }

        }

    }

    public static String Create_pmcgrp_headermsg_Key(Integer pmcgrpid) {

        return "pmcgrp_headermsg_" + String.valueOf(pmcgrpid);
    }

    public static String Create_pmcgrp_headermsg_Key(String pmcgrpid) {

        return "pmcgrp_headermsg_" + (pmcgrpid);
    }

    public static String Create_pmc_chargemsg_Key(Integer pmcid) {
        return "pmc_chargemsg_" + String.valueOf(pmcid);

    }

    public static String Get_pmcgrp_headermsg(String pmcgrpid) {
        String PMCgrpHeadermsg = "";
        if (PmcGrpHeaders != null && pmcgrpid != null) {
            PMCgrpHeadermsg = PmcGrpHeaders.get(Create_pmcgrp_headermsg_Key(pmcgrpid));

        }
        return PMCgrpHeadermsg;
    }

    public static String Get_pmc_chargemsg(Integer pmcid) {
        String PMCChargemsg = "";
        if (PmcHeaders != null) {
            if (PmcEntityMap != null && PmcEntityMap.containsKey(pmcid)) {
                PmcEntity entity = PmcEntityMap.get(pmcid);
                if (entity != null) {
                    PMCChargemsg = PmcHeaders.get(Create_pmc_chargemsg_Key(entity.pmcID));
                }

            }

        }
        return PMCChargemsg;
    }

    private static Map<Integer, String> loadCarValidatorPatterns(String section, String paramName,
            String secSeperator, String eleSeperator) {

        logger.info("--->loadTelcoCarValidatorPatterns :" + paramName);
        Map<Integer, String> configMap = new HashMap<>();
        String strmaxcounters = Config.getParam(section, paramName);
        if (strmaxcounters != null && strmaxcounters.length() > 0) {
            String[] values = strmaxcounters.split(secSeperator);
            if (values != null && values.length > 0) {
                for (int i = 0; i < values.length; i++) {
                    String[] str = values[i].split(eleSeperator);
                    if (str.length == 2) {
                        configMap.put(Integer.parseInt(str[0]), str[1]);
                        logger.info(str[0] + " : " + str[1]);
                    }
                }
            }
        }
        return configMap;
    }

    public static List<BankEntity> getBanks() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateTimeFormatString);

        BankResp resp = null;

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            BankReq request = new BankReq();
            request.feClientID = WebFeClientID;
            request.reqDate = DateTimeUtil.getCurDateWithMilisec();
            String data = String.format("%s%s%s", request.feClientID,
                    simpleDateFormat.format(request.reqDate), WebFeHashKey);
            request.sig = dbg.util.HashUtil.hashSHA256(data);

            HttpPost httpPost = new HttpPost(GetBankUrl);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("feclientid", String.valueOf(request.feClientID)));
            nvps.add(new BasicNameValuePair("reqdate", simpleDateFormat.format(request.reqDate)));
            nvps.add(new BasicNameValuePair("sig", request.sig));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();

                InputStream inputStream = entity.getContent();
                String sResponse = IOUtils.toString(inputStream, "UTF-8");

                JsonElement jsonElement = new JsonParser().parse(sResponse);
                JsonObject jsonObj = jsonElement.getAsJsonObject();

                resp = new BankResp();
                resp.returnCode = jsonObj.get("returnCode").getAsInt();

                if (resp.returnCode != 1) {
                    return null;
                }

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setDateFormat(DbgFrontEndConfig.DateTimeFormatString);
                Gson gson = gsonBuilder.create();

                Type collectionType = new TypeToken<List<BankEntity>>() {
                }.getType();
                resp.bankList = gson.fromJson(jsonObj.get("bankList").toString(), collectionType);
            }
        }

        return resp.bankList;
    }

    public static Map<Integer, PmcEntity> GetPmcSupportByAppID(String strappID) {
        Map<Integer, PmcEntity> result = new HashMap<>();
        Integer appID = -1;
        if (strappID != null && !strappID.trim().equals("")) {
            try {
                appID = Integer.parseInt(strappID);

            } catch (Exception ex) {

            }
        }

        MiniAppEntity appEntity = AppEntityMap.get(appID);

        if (PmcEntityMap != null) {
            Iterator<Integer> keySetIterator = PmcEntityMap.keySet().iterator();
            while (keySetIterator.hasNext()) {
                Integer key = keySetIterator.next();
                PmcEntity pmcEntity = DbgFrontEndConfig.PmcEntityMap.get(key);
                if (pmcEntity != null) {
                    if (appEntity != null && appEntity.notApliedPMCList != null
                            && appEntity.notApliedPMCList.contains(key)) {
                        //Not add to PMC list
                    } else {
                        result.put(key, pmcEntity);
                    }
                }
            }
        }
        return result;
    }

    public static List<PmcEntity> getPmc(int GroupID, String strappID) throws Exception {
        List<PmcEntity> list = new ArrayList<PmcEntity>();
        Map<Integer, PmcEntity> mapPMCs = GetPmcSupportByAppID(strappID);
        if (mapPMCs != null) {
            Iterator<Integer> keySetIterator = mapPMCs.keySet().iterator();
            while (keySetIterator.hasNext()) {
                Integer key = keySetIterator.next();
                PmcEntity pmcEntity = mapPMCs.get(key);
                if (pmcEntity != null && pmcEntity.pmcGroupID == GroupID) {
                    list.add(pmcEntity);
                }
            }
        }
        return list;
    }

    private static void LoadTKDienthoaiMobiChargeAmts() {

        logger.info("--->LoadTKDienthoaiMobiChargeAmts :");
        TKDienthoaiMobiChargeAmts = new ArrayList<>();

        String strchargeamnts = Config.getParam("tkdienthoaimobi", "chargeamts");
        if (strchargeamnts != null) {
            strchargeamnts = strchargeamnts.trim();
            String[] parts = strchargeamnts.split(",");
            if (parts != null && parts.length > 0) {
                for (int i = 0; i < parts.length; i++) {
                    TKDienthoaiMobiChargeAmts.add(parts[i]);
                    logger.info("-->>Value :  " + parts[i]);
                }
            }
        }
    }

    //Load ATM discounts
    private static Map<String, String> loadAtmDisCountPercents(String section, String paramName,
            String secSeperator, String eleSeperator) {

        logger.info("--->loadAtmDisCountPercents :" + paramName);
        Map<String, String> configMap = new HashMap<>();
        String strmaxcounters = Config.getParam(section, paramName);
        if (strmaxcounters != null && strmaxcounters.length() > 0) {
            String[] values = strmaxcounters.split(secSeperator);
            if (values != null && values.length > 0) {
                for (int i = 0; i < values.length; i++) {
                    String[] str = values[i].split(eleSeperator);
                    if (str.length == 2) {

                        configMap.put(String.format("%s", str[0]), str[1]);
                        logger.info(String.format("CustomPMCCostRate %s", str[0]) + " : " + str[1]);
                    }
                }
            }
        }
        return configMap;
    }

    //Load VisaMasterJCB discounts
    private static Map<String, String> loadVisaMasterDisCountPercents(String section, String paramName,
            String secSeperator, String eleSeperator) {

        logger.info("--->loadVisaMasterDisCountPercents :" + paramName);
        Map<String, String> configMap = new HashMap<>();
        String strmaxcounters = Config.getParam(section, paramName);
        if (strmaxcounters != null && strmaxcounters.length() > 0) {
            String[] values = strmaxcounters.split(secSeperator);
            if (values != null && values.length > 0) {
                for (int i = 0; i < values.length; i++) {
                    String[] str = values[i].split(eleSeperator);
                    if (str.length == 3) {

                        configMap.put(String.format("%s%s", str[0], str[1]), str[2]);
                        logger.info(String.format("Appid_CustomPMCCostRate %s%s", str[0], str[1]) + " : " + str[2]);
                    }
                }
            }
        }
        return configMap;
    }

    private static Map<Integer, Integer> loadVisaMasterMinMoneys(String section, String paramName,
            String secSeperator, String eleSeperator) {

        logger.info("--->loadVisaMasterMinMoneys :" + paramName);
        Map<Integer, Integer> configMap = new HashMap<>();
        String strmaxcounters = Config.getParam(section, paramName);
        if (strmaxcounters != null && strmaxcounters.length() > 0) {
            String[] values = strmaxcounters.split(secSeperator);
            if (values != null && values.length > 0) {
                for (int i = 0; i < values.length; i++) {
                    String[] str = values[i].split(eleSeperator);
                    if (str.length == 2) {
                        configMap.put(Integer.parseInt(str[0].trim()), Integer.parseInt(str[1].trim()));
                        logger.info(str[0] + " : " + str[1]);
                    }
                }
            }
        }
        return configMap;
    }

}
