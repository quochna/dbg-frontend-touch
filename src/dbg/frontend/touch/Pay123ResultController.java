/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch;

import dbg.frontend.touch.entity.SessionResultInfo;
import dbg.frontend.touch.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dbg.frontend.config.DbgFrontEndConfig;
import dbg.frontend.touch.entity.LogEntity;
import static dbg.frontend.utils.common.getRequestUrl;
import dbg.response.TransStatusResp;
import hapax.Template;
import hapax.TemplateDataDictionary;
import hapax.TemplateDictionary;
import hapax.TemplateException;
import hapax.TemplateLoader;
import hapax.TemplateResourceLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

/**
 *
 * @author bangdq
 */
public class Pay123ResultController extends DbgFrontendCore {

    private static Logger logger = Logger.getLogger(Pay123ResultController.class);
    private final String ITEM_SEPARATE = "\\|";
    private final String PARAM_STATS = "stats";
    private final Monitor readStats = new Monitor();
    private static final Pay123ResultController instance = new Pay123ResultController();

    public static Pay123ResultController getInstance() {
        return instance;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
        LogEntity logEntity = new LogEntity();
        try {
            logEntity.startTime = System.nanoTime();
            logEntity.userAgent = request.getHeader("User-Agent");
            logEntity.requestUrl = getRequestUrl(request);
            processRequest(logEntity, request, response);

        } catch (Exception ex) {
            logEntity.exception = ex.getMessage() + "|" + ExceptionUtils.getStackTrace(ex);
            logger.error(ex.toString());
            echoAndStats(logEntity, render123PayExceptionErrorByTemplate(logEntity, request), response);
        }
    }

    protected void processRequest(LogEntity logEntity, HttpServletRequest request, HttpServletResponse response) throws TException, TemplateException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        long startTime = System.nanoTime();
        String stats = request.getParameter(PARAM_STATS);

        if (stats != null && stats.equals(PARAM_STATS)) {
            this.echo(this.readStats.dumpHtmlStats(), response);
            return;
        }

        // TODO : decode to check params
        echoAndStats(logEntity, renderByTemplate(logEntity, request, response), response);
    }

    private void echoAndStats(LogEntity logEntity, String html, HttpServletResponse response) {
        this.echo(html, response);
        logEntity.endTime = System.currentTimeMillis();
    }

    private String renderByTemplate(LogEntity logEntity, HttpServletRequest request, HttpServletResponse response)
            throws TemplateException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException {

        TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
        Template template = templateLoader.getTemplate("master");
        TemplateDataDictionary dic = TemplateDictionary.create();

        dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
        dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
        dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
//        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);

        dic.showSection("asyncresult");
        dic.setVariable("ASYNC_RESULT_URL", DbgFrontEndConfig.AsyncResultUrl);
        //transactionID 123Pay provide when Redirect
        dic.setVariable("transid", request.getParameter("transactionID"));

        dic.setVariable("numberofcalling", String.valueOf(DbgFrontEndConfig.Pay123numberofcallingasync));
        dic.setVariable("intervalcallinginseconds", String.valueOf(1000 * DbgFrontEndConfig.Pay123intervalcallinginsecondsasync));

        SetValuesForRedirectInformation(request, dic);
        //dic.setVariable("STATUS_BAR", GetStep3BarHTML());

        dic.showSection("statusbar");
        dic.addSection("STEP3");

        String status = request.getParameter("status");
        if (status != null) {
            queryATMTransStatus(logEntity, request.getParameter("transactionID"));
        }

        return template.renderToString(dic);

    }

    private void SetValuesForRedirectInformation(HttpServletRequest request, TemplateDataDictionary dic) {

        HttpSession session = request.getSession();
        //transactionID 123Pay provide when Redirect
        String sskey = DbgFrontEndConfig.Pay123sessionkey
                + request.getParameter("transactionID");
        SessionResultInfo ssResultInfo = (SessionResultInfo) session.getAttribute(sskey);
        if (ssResultInfo != null) {
            String strAppID = ssResultInfo.appid;
            String strAppServerID = ssResultInfo.appserverid;
            int appID = Integer.parseInt(strAppID);
            if (DbgFrontEndConfig.AppEntityMap.get(appID) != null) {
                dic.setVariable("appName", DbgFrontEndConfig.AppEntityMap.get(appID).appDesc);
            }

            String key = DbgFrontEndConfig.CreateAppServerKey(strAppServerID, strAppID);
            dic.setVariable("_n_url_redirect", ssResultInfo.urlredirect);
            dic.setVariable("_n_tranxid", ssResultInfo.transid);
            dic.setVariable("_n_state", "billed");
            dic.setVariable("_n_apptranxid", ssResultInfo.apptransid);
            dic.setVariable("_n_platform", ssResultInfo.pl);
            dic.setVariable("_n_netamount", "");
            dic.setVariable("_n_pmc", ssResultInfo.pmcid);
            dic.setVariable("_n_grossamount", "");
            dic.setVariable("appid", strAppID);
            dic.setVariable("pmcid", ssResultInfo.pmcid);
            //added by BANGDQ for Special Layout 30/10/2014 13:20:10
            //SetSpecialLayOut(request, dic,strAppID);
        }

    }

    public String render123PayExceptionErrorByTemplate(LogEntity logEntity, HttpServletRequest request) {
        try {

            TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
            Template template = templateLoader.getTemplate("errorpage");
            TemplateDataDictionary dic = TemplateDictionary.create();
            dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
            dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
            dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
//        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);

            dic.setVariable("message", DbgFrontEndConfig.Exception);

            dic.setVariable("_n_error_code", "0");
            dic.setVariable("_n_error_msg", DbgFrontEndConfig.Exception);

            HttpSession session = request.getSession();
            //transactionID 123Pay provide when Redirect
            String sskey = DbgFrontEndConfig.Pay123sessionkey
                    + request.getParameter("transactionID");
            SessionResultInfo ssResultInfo = (SessionResultInfo) session.getAttribute(sskey);
            if (ssResultInfo != null) {
                String strAppID = ssResultInfo.appid;
                String strAppServerID = ssResultInfo.appserverid;
                int appID = Integer.parseInt(strAppID);
                if (DbgFrontEndConfig.AppEntityMap.get(appID) != null) {
                    dic.setVariable("appName", DbgFrontEndConfig.AppEntityMap.get(appID).appDesc);
                }

                String key = DbgFrontEndConfig.CreateAppServerKey(strAppServerID, strAppID);
                dic.setVariable("_n_url_redirect", ssResultInfo.urlredirect);
                dic.setVariable("_n_tranxid", ssResultInfo.transid);
                dic.setVariable("_n_state", "billed");
                dic.setVariable("_n_apptranxid", ssResultInfo.apptransid);
                dic.setVariable("_n_platform", ssResultInfo.pl);
                dic.setVariable("_n_netamount", "");
                dic.setVariable("_n_pmc", ssResultInfo.pmcid);
                dic.setVariable("_n_grossamount", "");
                dic.setVariable("appid", strAppID);
                dic.setVariable("pmcid", ssResultInfo.pmcid);
                dic.setVariable("transid", request.getParameter("transactionID"));
                //added by BANGDQ for Special Layout 30/10/2014 13:20:10
                //SetSpecialLayOut(request, dic,strAppID);
            }

            return template.renderToString(dic);
        } catch (Exception ex) {
            logEntity.exception = ex.getMessage() + "|" + ExceptionUtils.getStackTrace(ex);
            logger.error(ex.getMessage());
            return DbgFrontEndConfig.Exception;
        }
    }

    public TransStatusResp queryATMTransStatus(LogEntity logEntity, String transID) throws UnsupportedEncodingException, IOException {

        TransStatusResp resp = null;
        int timeout = DbgFrontEndConfig.CallApiTimeoutSeconds * 1000;
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setStaleConnectionCheckEnabled(true)
                .setSocketTimeout(timeout)
                .build();
        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build()) {

            HttpPost httpPost = new HttpPost(DbgFrontEndConfig.atmquerystatusUrl);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("transid", transID));
            nvps.add(new BasicNameValuePair("feclientid", String.valueOf(DbgFrontEndConfig.WebFeClientID)));
            SimpleDateFormat formatSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            formatSDF.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            Date date = new Date(System.currentTimeMillis());
            nvps.add(new BasicNameValuePair("reqdate", formatSDF.format(date)));
            //Hash256(feClientID + transID + format(reqDate, yyyy-MM-dd HH:mm:ss.SSS) + hashKey)
            try {
                String data = String.format("%s%s%s%s", String.valueOf(DbgFrontEndConfig.WebFeClientID),
                        transID, formatSDF.format(date), DbgFrontEndConfig.WebFeHashKey);
                String sigdata = dbg.util.HashUtil.hashSHA256(data);
                nvps.add(new BasicNameValuePair("sig", sigdata));
            } catch (Exception ex) {
                logEntity.exception = ex.getMessage() + "|" + ExceptionUtils.getStackTrace(ex);
                logger.error(ex.toString());
            }

            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();

                InputStream inputStream = entity.getContent();
                String sResponse = IOUtils.toString(inputStream, "UTF-8");

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setDateFormat(DbgFrontEndConfig.DateTimeFormatString);
                Gson gson = gsonBuilder.create();

                resp = gson.fromJson(sResponse, TransStatusResp.class);
            }
        }

        return resp;
    }

}
