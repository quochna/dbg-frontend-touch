/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch;

import dbg.entity.AppData;
import dbg.entity.MiniAppServerEntity;

import dbg.frontend.config.DbgFrontEndConfig;

//import dbg.frontend.touch.entity.SSO3Account;
import dbg.util.AES256Algorithm;
import hapax.Template;
import hapax.TemplateDataDictionary;
import hapax.TemplateDictionary;

import hapax.TemplateLoader;
import hapax.TemplateResourceLoader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author hainpt
 */
public class DbgFrontendCore extends HttpServlet {

    private static final Logger logger = Logger.getLogger(DbgFrontendCore.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            List<String> params = parseUriRequest(request);
            
            String method = params.get(0);
            DbgFrontendCore controller = getController(method);
            controller.handleRequest(request, response);

        } catch (Exception ex) {
        }
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("DbgFrontendCore.handleRequest => UnsupportedOperationException");
    }

    private DbgFrontendCore getController(String method) {
        switch (method) {
            case "chonkenhthanhtoan":
                return dbg.frontend.touch.edit.SelectChannelController.getInstance();
            case "thanhtoan":
                return dbg.frontend.touch.edit.ChargeController.getInstance();
            case "ketqua":
                return dbg.frontend.touch.edit.ResultController.getInstance();
            case "async":
                return AsyncResultController.getInstance();
            case "pay123result":
                return Pay123ResultController.getInstance();
            default:
                return null;
        }
    }

    protected List<String> parseUriRequest(HttpServletRequest req) {
        List<String> result = new ArrayList();
        try {
            String uripath = req.getRequestURI();
            String[] splitArray = uripath.split("/");
            for (int i = 1; i < splitArray.length; i++) {
                result.add(splitArray[i]);
            }
        } catch (Exception ex) {
            logger.error("parseUriRequest exception " + ex.getMessage(), ex);
        }
        return result;
    }

    protected void echo(Object text, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            response.setContentType("text/html;charset=UTF-8");
            out = response.getWriter();
            if (out != null) {
                out.print(text);
                out.close();
            }
        } catch (IOException ex) {
            logger.error(ex.toString());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

//    protected SSO3Account getSSO3Account(HttpServletRequest req) throws IOException, Exception {
//        SSO3Account account = null;
//        req.setAttribute("login", false);
//        String vngAuth = null;
//        Cookie[] cookies = req.getCookies();
//        for (Cookie cookie : cookies) {
//            String cName = cookie.getName();
//            if (cName.compareTo("vngauth") == 0) {
//                vngAuth = cookie.getValue();
//                if ("null".equals(vngAuth)) {
//                    vngAuth = null;
//                }
//            }
//        }
//
//        if (vngAuth != null) {
//            try {
//
//                VngAuthClient auth = VngAuthClient.getInstance(DbgFrontEndConfig.VNGAuthMasterService,
//                        DbgFrontEndConfig.VNGAuthSlaveService, DbgFrontEndConfig.VNGAuthAuth,
//                        DbgFrontEndConfig.VNGAuthSource);
//                Map<String, String> result = auth.getUserLoggedin(vngAuth);
//                // Session id is exist
//                if (result != null) {
//                    account = new SSO3Account();
//                    account.userName = result.get("username");
//                    account.userId = result.get("uin");
//
//                    req.setAttribute("userName", account.userName);
//                    req.setAttribute("userId", result.get("uin"));
//                    req.setAttribute("login", true);
//                    //isLoginUser = true;
//                }
//            } catch (Exception ex) {
//
//                //TODO: LogService.writeException(ex.getMessage(), ex);
//                logger.error(String.format("getSSO3Account->StackTrace:%s", ExceptionUtils.getStackTrace(ex)));
//                logger.error(String.format("getSSO3Account->RootCause:%s", ExceptionUtils.getRootCause(ex)));
//                throw ex;
//            }
//        }
//        return account;
//    }

//    protected String getUserName(HttpServletRequest req) {
//        String vngAuth = null;
//        Cookie[] cookies = req.getCookies();
//        for (Cookie cookie : cookies) {
//            String cName = cookie.getName();
//            if (cName.compareTo("vngauth") == 0) {
//                vngAuth = cookie.getValue();
//                if ("null".equals(vngAuth)) {
//                    vngAuth = null;
//                }
//            }
//        }
//
//        if (vngAuth != null) {
//            try {
//                VngAuthClient auth = VngAuthClient.getInstance(DbgFrontEndConfig.VNGAuthMasterService,
//                        DbgFrontEndConfig.VNGAuthSlaveService, DbgFrontEndConfig.VNGAuthAuth,
//                        DbgFrontEndConfig.VNGAuthSource);
//                Map<String, String> result = auth.getUserLoggedin(vngAuth);
//                // Session id is exist
//                if (result != null) {
//                    return result.get("username");
//                }
//            } catch (Exception ex) {
//                logger.error("VngAuthClient get userLogin : " + ex.getMessage());
//            }
//        }
//        return null;
//    }

//    protected boolean storeZingMePaymentHidenInfo(HttpServletRequest request, String addinfo) {
//
//        try {
//            PaymentHidenInfo info = getDefaultZingmePaymentHidenInfo(request, addinfo);
//            HttpSession session = request.getSession();
//            session.setAttribute(String.format("%s_%s", "sso3", info.transID), info);
//            session.setMaxInactiveInterval(DbgFrontEndConfig.SessionTimeoutSeconds);
//            return true;
//        } catch (Exception ex) {
//            logger.error(String.format("storeZingMePaymentHidenInfo->Error:%s", ex.toString()));
//            return false;
//        }
//    }

//    protected PaymentHidenInfo getDefaultZingmePaymentHidenInfo(HttpServletRequest request, String addinfo) {
//
//        PaymentHidenInfo info = null;
//        boolean isSessionExpired = false;
//        String transID = null;
//        transID = request.getParameter("transid");
//        if (transID != null) {
//            HttpSession session = request.getSession();
//            info = (PaymentHidenInfo) session.getAttribute(String.format("%s_%s", "sso3", transID));
//            if (info == null) {
//                isSessionExpired = true;
//            }
//        }
//
//        if (info == null) {
//
//            info = new PaymentHidenInfo();
//            info.isSessionExpired = isSessionExpired;
//            String refererUrl = request.getHeader("Referer");
//            if (refererUrl == null && "".equals(refererUrl)) {
//                refererUrl = "";
//            }
//
//            info.refererUrl = refererUrl;
//            info.appID = request.getParameter("appid");
//            info.appData = request.getParameter("appdata");
//            info.appServerID = request.getParameter("appserverid");
//            info.appTransID = request.getParameter("apptransid");
//            if (transID != null) {
//                info.transID = transID;
//            } else {
//                info.transID = request.getParameter("transid");
//            }
//            info.devicePlatform = request.getParameter("pl");
//            info.billResult = AppRedirectParamKey.BILL_RESULT.getDefaultValue();
//            info.billState = AppRedirectParamKey.BILL_STATE.getDefaultValue();
//            info.errorCode = AppRedirectParamKey.ERROR_CODE.getDefaultValue();
//            info.errorMsg = AppRedirectParamKey.ERROR_MSG.getDefaultValue();
//            info.netAmount = AppRedirectParamKey.NET_AMOUNT.getDefaultValue();
//            info.addInfo = addinfo;
//            info.redirectUrl = request.getParameter("url_redirect");
//            int pmcID = -1;
//
//            try {
//                pmcID = NumberUtils.toInt(request.getParameter("pmcid"), pmcID);
//                if (!DbgFrontEndConfig.PmcEntityMap.containsKey(pmcID)) {
//                    pmcID = -1;
//                }
//            } catch (NumberFormatException ex) {
//                logger.error(ex.toString());
//            }
//
//            if (DbgFrontEndConfig.PmcEntityMap.containsKey(pmcID)) {
//                info.pmcName = DbgFrontEndConfig.PmcEntityMap.get(pmcID).pmcDesc;
//            }
//
//            info.pmcID = pmcID;
//        }
//
//        return info;
//
//    }

    public String formatcomma(long value) {
        DecimalFormat myFormatter = new DecimalFormat("#,###");
        String output = myFormatter.format(value);
        return output;
    }

    public String formatcomma(double value) {
        DecimalFormat myFormatter = new DecimalFormat("#,###");
        String output = myFormatter.format(value);
        return output;
    }

    public String formatcomma(String value) {
        String output = value;
        if (value != null && !value.trim().equals("")) {
            try {
                long lvalue = new Long(value);
                DecimalFormat myFormatter = new DecimalFormat("#,###");
                output = myFormatter.format(lvalue);
            } catch (Exception e) {

            }
        }
        return output;
    }

    public String formatdot(long value) {
        DecimalFormat myFormatter = new DecimalFormat("#,###");
        String output = myFormatter.format(value);
        if (output != null) {
            output = output.replace(",", ".");
        }
        return output;
    }

    public String renderExceptionErrorByTemplate(HttpServletRequest request) {
        try {

            TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
            Template template = templateLoader.getTemplate("errorpage");
            TemplateDataDictionary dic = TemplateDictionary.create();
            dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
            dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
            dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
            dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);
            dic.setVariable("apptransid", request.getParameter("apptransid"));
            dic.setVariable("message", DbgFrontEndConfig.Exception);
            dic.setVariable("transid", request.getParameter("transid"));
            dic.setVariable("_n_error_code", "0");
            dic.setVariable("_n_error_msg", DbgFrontEndConfig.Exception);
            dic.setVariable("appid", request.getParameter("appid"));

            String strAppID = request.getParameter("appid");
            String strAppServerID = request.getParameter("appserverid");
            String key = DbgFrontEndConfig.CreateAppServerKey(strAppServerID, strAppID);

            String url = request.getParameter("url_redirect");
            if (url != null && !url.trim().equals("") && !url.trim().equals("#")) {
                dic.setVariable("_n_url_redirect", url);
            } else {
                if (DbgFrontEndConfig.AppServerEntityMap.containsKey(key)) {
                    MiniAppServerEntity entity = DbgFrontEndConfig.AppServerEntityMap.get(key);
                    if (entity != null) {
                        dic.setVariable("_n_url_redirect", entity.appRedirectUrl);
                    }
                } else {
                    dic.setVariable("_n_url_redirect", "");

                }
            }
            dic.setVariable("_n_tranxid", request.getParameter("transid"));
            dic.setVariable("_n_state", "billing");
            dic.setVariable("_n_apptranxid", request.getParameter("apptransid"));
            dic.setVariable("_n_platform", request.getParameter("pl"));
            dic.setVariable("_n_netamount", "");
            dic.setVariable("appserverid", request.getParameter("appserverid"));
            dic.setVariable("_n_pmc", request.getParameter("pmcid"));
            dic.setVariable("_n_grossamount", "");
            //added by BANGDQ for Special Layout 30/10/2014 13:20:10
            //SetSpecialLayOut(request, dic);
            
            return template.renderToString(dic);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return DbgFrontEndConfig.Exception;
        }
    }

    public void SaveBackButtonHTML(HttpServletRequest request, TemplateDataDictionary dic) {
        Object strIsDirect = request.getAttribute("_isdirect");
        if (strIsDirect != null) {
            dic.setVariable("_isdirect", strIsDirect.toString());
        } else {
            Object strParam = request.getParameter("_isdirect");
            if (strParam != null) {
                dic.setVariable("_isdirect", strParam.toString());
            }
        }
    }

    public boolean GetBackButtonHTML(HttpServletRequest request) {
        Object strIsDirect = request.getAttribute("_isdirect");
        boolean html = false;
        if (strIsDirect != null && strIsDirect.equals("true")) {
            html = false;
        } else {
            Object strParam = request.getParameter("_isdirect");
            if (strParam != null && strParam.equals("true")) {
                html = false;
            } else {
                html = true;
            }
        }
        return html;
    }

//    public void SetSpecialLayOut(HttpServletRequest request, TemplateDataDictionary dic) {
//        String appID = request.getParameter("appid");
//        if (appID != null) {
//            dic.setVariable("CUSTOM_CSS_LINK", getCUSTOM_CSS_LINK(appID));
//            dic.setVariable("CUSTOM_CSS_DBG_WRAPPER", getCUSTOM_CSS_DBG_WRAPPER(appID));
//            dic.setVariable("CUSTOM_CSS_DBG_FOOTER", getCUSTOM_CSS_DBG_FOOTER(appID));
//        }
//
//    }
//
//    public void SetSpecialLayOut(HttpServletRequest request, TemplateDataDictionary dic, String appID) {
//
//        if (appID != null) {
//            dic.setVariable("CUSTOM_CSS_LINK", getCUSTOM_CSS_LINK(appID));
//            dic.setVariable("CUSTOM_CSS_DBG_WRAPPER", getCUSTOM_CSS_DBG_WRAPPER(appID));
//            dic.setVariable("CUSTOM_CSS_DBG_FOOTER", getCUSTOM_CSS_DBG_FOOTER(appID));
//        }
//
//    }

//    private String getCUSTOM_CSS_DBG_FOOTER(String appID) {
//        String returnval = "";
//        if (appID != null) {
//            if (appID.trim().equalsIgnoreCase(DbgFrontEndConfig.GunnyProductID)) {
//                returnval = "<div class=\"fl\"><h1 class=\"zdbg_logo zdbgsprt\">Zing Cổng thanh toán</h1></div>";
//            } else if (appID.trim().equalsIgnoreCase(DbgFrontEndConfig.DotaTKProductID)) {
//                returnval = "<div class=\"fl\"><h1 class=\"zdbg_logo zdbgsprt\">Zing Cổng thanh toán</h1></div>";
//            } else if (appID.trim().equalsIgnoreCase(DbgFrontEndConfig.CCTalkAppID)) {
//                returnval = "<div class=\"fl\"><h1 class=\"zdbg_logo zdbgsprt\">Zing Cổng thanh toán</h1></div>";
//            }
//        }
//        return returnval;
//    }

//    private String getCUSTOM_CSS_LINK(String appID) {
//        String returnval = "";
//        if (appID != null) {
//            if (appID.trim().equalsIgnoreCase(DbgFrontEndConfig.GunnyProductID)) {
//                returnval = "<link href=\"" + DbgFrontEndConfig.StaticContentUrl + "/css/gunny/css/screen.css\"  rel=\"stylesheet\" type=\"text/css\" />";
//            } else if (appID.trim().equalsIgnoreCase(DbgFrontEndConfig.DotaTKProductID)) {
//                returnval = "<link href=\"" + DbgFrontEndConfig.StaticContentUrl + "/css/dotatk/css/screen.css\"  rel=\"stylesheet\" type=\"text/css\" />";
//            } else if (appID.trim().equalsIgnoreCase(DbgFrontEndConfig.CCTalkAppID)) {
//                returnval = "<link href=\"" + DbgFrontEndConfig.StaticContentUrl + "/css/talktv/css/screen.css\"  rel=\"stylesheet\" type=\"text/css\" />";
//            }
//        }
//        return returnval;
//    }

//    private String getCUSTOM_CSS_DBG_WRAPPER(String appID) {
//        String returnval = "";
//        if (appID != null) {
//            if (appID.trim().equalsIgnoreCase(DbgFrontEndConfig.GunnyProductID)) {
//                returnval = "gunny";
//            } else if (appID.trim().equalsIgnoreCase(DbgFrontEndConfig.DotaTKProductID)) {
//                returnval = "dotatk";
//            } else if (appID.trim().equalsIgnoreCase(DbgFrontEndConfig.CCTalkAppID)) {
//                returnval = "talktv";
//            }
//        }
//        return returnval;
//    }

    public boolean StoreCardSerial(HttpServletRequest request, SessionCard entity) {
        try {
            HttpSession session = request.getSession();
            session.setAttribute("cardserial", entity);
            session.setMaxInactiveInterval(DbgFrontEndConfig.SessionTimeoutSeconds);
            return true;
        } catch (Exception ex) {
            logger.error(String.format("StoreCardSerial StackTrace:%s", ExceptionUtils.getStackTrace(ex)));
            logger.error(String.format("StoreCardSerial RootCause:%s", ExceptionUtils.getRootCause(ex)));
            return false;
        }
    }

    public SessionCard GetStoreCardSerial(HttpServletRequest request) {
        SessionCard sessionCarSerial = null;
        try {
            HttpSession session = request.getSession();
            Object objserial = session.getAttribute("cardserial");
            if (objserial != null) {
                sessionCarSerial = (SessionCard) objserial;
            }

        } catch (Exception ex) {
            logger.error(String.format("GetStoreCardSerial StackTrace:%s", ExceptionUtils.getStackTrace(ex)));
            logger.error(String.format("GetStoreCardSerial RootCause:%s", ExceptionUtils.getRootCause(ex)));

        }
        return sessionCarSerial;
    }

    public void ClearStoreCardSerial(HttpServletRequest request, String transID) {
        try {
            HttpSession session = request.getSession();
            SessionCard sessionCarSerial = null;
            Object objserial = session.getAttribute("cardserial");
            if (objserial != null) {
                sessionCarSerial = (SessionCard) objserial;
                if (sessionCarSerial.transID.equalsIgnoreCase(transID)) {
                    session.removeAttribute("cardserial");
                }
            }

        } catch (Exception ex) {
            logger.error(String.format("ClearStoreCardSerial StackTrace:%s", ExceptionUtils.getStackTrace(ex)));
            logger.error(String.format("ClearStoreCardSerial RootCause:%s", ExceptionUtils.getRootCause(ex)));

        }
    }

    public AppData DecryptAppData(int appID, String appData) {
        AppData appdata = null;
        if (DbgFrontEndConfig.AppEntityMap.containsKey(appID)) {
            AES256Algorithm _aes256 = new AES256Algorithm();
            String sDecrypted = null;
            try {
                sDecrypted = _aes256.decrypt(DbgFrontEndConfig.AppEntityMap.get(appID).appKey1, appData);
                if (sDecrypted != null) {
                    appdata = AppData.fromJsonString(sDecrypted);
                }
            } catch (Exception ex) {
                logger.error(String.format("DecryptAppData StackTrace:%s", ExceptionUtils.getStackTrace(ex)));
                logger.error(String.format("DecryptAppData RootCause:%s", ExceptionUtils.getRootCause(ex)));
            }
        }
        return appdata;
    }

    public String getClientIP(HttpServletRequest request) {

        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
            
        }

        return ipAddress;
    }

    public boolean IsZaloApp(String appID) {
        boolean result = false;
        if (DbgFrontEndConfig.ZaloAppIDs.contains(appID)) {
            result = true;
        }
        return result;
    }

    public String GetIsDirectValue(HttpServletRequest request) {
        Object strIsDirect = request.getAttribute("_isdirect");
        String returnval = "";
        if (strIsDirect != null) {
            returnval = strIsDirect.toString();
        } else {
            Object strParam = request.getParameter("_isdirect");
            if (strParam != null) {
                returnval = strParam.toString();
            }
        }
        return returnval;
    }

    public Integer getVisaMasterJCBMinMoney(int pmcID) {
        int minmoney = 0;
        if (DbgFrontEndConfig.VisaMasterMinMoneys.containsKey(pmcID)) {
            minmoney = DbgFrontEndConfig.VisaMasterMinMoneys.get(pmcID);
        }
        return minmoney;
    }

//    public BalanceResp getZingXuBallance(String AccountName) throws
//            UnsupportedEncodingException,
//            IOException,
//            Exception {
//        BalanceResp sResponse = null;
//        int timeout = DbgFrontEndConfig.CallApiTimeoutSeconds * 1000;
//        RequestConfig defaultRequestConfig = RequestConfig.custom()
//                .setConnectTimeout(timeout)
//                .setConnectionRequestTimeout(timeout)
//                .setStaleConnectionCheckEnabled(true)
//                .setSocketTimeout(timeout)
//                .build();
//        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build()) {
//
//            HttpPost httpPost = new HttpPost(DbgFrontEndConfig.GetBalanceUrl);
//            List<NameValuePair> nvps = new ArrayList<>();
//            nvps.add(new BasicNameValuePair("accountname", AccountName));
//            nvps.add(new BasicNameValuePair("feclientid",
//                    String.valueOf(DbgFrontEndConfig.WebFeClientID)));
//            SimpleDateFormat formatSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//            formatSDF.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//            Date date = new Date(System.currentTimeMillis());
//            nvps.add(new BasicNameValuePair("reqdate", formatSDF.format(date)));
//            //Hash256(feClientID + transID + format(reqDate, yyyy-MM-dd HH:mm:ss.SSS) + hashKey)
//            try {
//                String data = String.format("%s%s%s%s", AccountName,
//                        String.valueOf(DbgFrontEndConfig.WebFeClientID),
//                        formatSDF.format(date), DbgFrontEndConfig.WebFeHashKey);
//                String sigdata = dbg.util.HashUtil.hashSHA256(data);
//                nvps.add(new BasicNameValuePair("sig", sigdata));
//            } catch (Exception ex) {
//                logger.error(ex.toString());
//            }
//            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
//            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
//                HttpEntity entity = response.getEntity();
//                InputStream inputStream = entity.getContent();
//                String data = IOUtils.toString(inputStream, "UTF-8");
//                sResponse = BalanceResp.fromJsonString(data);
//            }
//        }
//
//        if (sResponse != null) {
//            if (sResponse.ReturnCode == -888888 || sResponse.ReturnCode == 0) {
//                throw new Exception("{-888888} Unable to get ballance of account :" + AccountName);
//            }
//
//        } else {
//            throw new Exception("Unable to get ballance of account :" + AccountName);
//        }
//        return sResponse;
//    }
//
//    public ThapPhongBalanceResp getThapPhongBallance(String AccountName) throws
//            UnsupportedEncodingException,
//            IOException,
//            Exception {
//        ThapPhongBalanceResp sResponse = null;
//        int timeout = DbgFrontEndConfig.CallApiTimeoutSeconds * 1000;
//        RequestConfig defaultRequestConfig = RequestConfig.custom()
//                .setConnectTimeout(timeout)
//                .setConnectionRequestTimeout(timeout)
//                .setStaleConnectionCheckEnabled(true)
//                .setSocketTimeout(timeout)
//                .build();
//        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build()) {
//
//            HttpPost httpPost = new HttpPost(DbgFrontEndConfig.ThapPhongGetBalanceURL);
//            List<NameValuePair> nvps = new ArrayList<>();
//            nvps.add(new BasicNameValuePair("accountname", AccountName));
//            nvps.add(new BasicNameValuePair("feclientid",
//                    String.valueOf(DbgFrontEndConfig.WebFeClientID)));
//            SimpleDateFormat formatSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//            formatSDF.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//            Date date = new Date(System.currentTimeMillis());
//            nvps.add(new BasicNameValuePair("reqdate", formatSDF.format(date)));
//            try {
//                String data = String.format("%s%s%s%s", AccountName,
//                        String.valueOf(DbgFrontEndConfig.WebFeClientID),
//                        formatSDF.format(date), DbgFrontEndConfig.WebFeHashKey);
//                String sigdata = dbg.util.HashUtil.hashSHA256(data);
//                nvps.add(new BasicNameValuePair("sig", sigdata));
//            } catch (Exception ex) {
//                logger.error(ex.toString());
//            }
//            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
//            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
//                HttpEntity entity = response.getEntity();
//                InputStream inputStream = entity.getContent();
//                String data = IOUtils.toString(inputStream, "UTF-8");
//                sResponse = ThapPhongBalanceResp.fromJsonString(data);
//            }
//        }
//
//        if (sResponse != null) {
//            if (sResponse.ReturnCode == -888888 || sResponse.ReturnCode == 0) {
//                throw new Exception("{-888888} Unable to get ballance of account :" + AccountName);
//            }
//
//        } else {
//            throw new Exception("Unable to get ballance of account :" + AccountName);
//        }
//        return sResponse;
//    }

    public boolean checkDuplicateTransID(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            String transID = request.getParameter("transid");
            Object duptranid = null;
            if (session != null) {
                duptranid = session.getAttribute(DbgFrontEndConfig.TransIDsessionkey + "" + transID);
            }
            if (duptranid != null && duptranid.toString().equalsIgnoreCase(transID)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            logger.error(ex.toString());
            return false;
        }
    }

}
