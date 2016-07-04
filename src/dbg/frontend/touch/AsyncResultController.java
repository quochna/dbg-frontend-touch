/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch;

import dbg.frontend.touch.*;
import dbg.frontend.config.DbgFrontEndConfig;
import dbg.frontend.touch.entity.LogEntity;
import static dbg.frontend.utils.common.getRequestUrl;
import static dbg.frontend.utils.common.sendPost;
import dbg.response.TransStatusResp;
import hapax.TemplateException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

/**
 *
 * @author hainpt
 */
public class AsyncResultController extends DbgFrontendCore {

    private static Logger logger = Logger.getLogger(AsyncResultController.class);
    private final String ITEM_SEPARATE = "\\|";
    private final String PARAM_STATS = "stats";
    private final Monitor readStats = new Monitor();
    private static final AsyncResultController instance = new AsyncResultController();

    public static AsyncResultController getInstance() {
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
            echoAndStats(logEntity, renderExceptionErrorByTemplate(request), response);
        }
    }

    protected void processRequest(LogEntity logEntity, HttpServletRequest request, HttpServletResponse response)
            throws TException, TemplateException, UnsupportedEncodingException, IOException, ParseException {
        long startTime = System.nanoTime();
        String stats = request.getParameter(PARAM_STATS);
        if (stats != null && stats.equals(PARAM_STATS)) {
            this.echo(this.readStats.dumpHtmlStats(), response);
            return;
        }
        // TODO : decode to check params
        echoAndStats(logEntity, renderByTemplate(logEntity, request), response);
    }

    private void echoAndStats(LogEntity logEntity, String html, HttpServletResponse response) {
        this.echo(html, response);
        logEntity.endTime = System.currentTimeMillis();
    }

    private String renderByTemplate(LogEntity logEntity, HttpServletRequest request)
            throws TemplateException, UnsupportedEncodingException,
            IOException, ParseException {

        return getTransStatus(logEntity, request);
    }

    public String getTransStatus(LogEntity logEntity, HttpServletRequest request) throws UnsupportedEncodingException, IOException, ParseException {
        String sResponse = null;
        String transID = request.getParameter("transid");
        int timeout = DbgFrontEndConfig.CallApiTimeoutSeconds * 1000;

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

        try {

            sResponse = sendPost(nvps, DbgFrontEndConfig.GetTransStatusUrl, timeout);

        } catch (Exception ex) {
            logEntity.exception = ex.getMessage() + "|" + ExceptionUtils.getStackTrace(ex);
            logger.error(ex.toString());
        }

        //Added by BangDQ for clear session card when transStatus = 1;
        try {
            TransStatusResp transResp = TransStatusResp.fromJsonString(sResponse);
            if (transResp != null && !transResp.isProcessing) {
                if (transResp.transStatus == 1)//Success then clear
                {
                    ClearStoreCardSerial(request, transID);
                }
            }
        } catch (Exception ex) {
            logEntity.exception = ex.getMessage() + "|" + ExceptionUtils.getStackTrace(ex);
        }

        return sResponse;

    }
}
