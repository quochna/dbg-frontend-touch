/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch;

import dbg.frontend.config.DbgFrontEndConfig;
import dbg.response.TransStatusResp;
import hapax.TemplateException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
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
 * @author hainpt
 */
public class AsyncResultController extends DbgFrontendCore
{

    private static Logger logger = Logger.getLogger(AsyncResultController.class);
    private final String ITEM_SEPARATE = "\\|";
    private final String PARAM_STATS = "stats";
    private final Monitor readStats = new Monitor();
    private static final AsyncResultController instance = new AsyncResultController();

    public static AsyncResultController getInstance()
    {
        return instance;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
    {
        
        long startTime = System.nanoTime();
        try
        {
            
            
            processRequest(request, response);
            
        }
        catch (Exception ex)
        {
            logger.error(ex.toString());
           echoAndStats(startTime, renderExceptionErrorByTemplate(request), response);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws TException, TemplateException, UnsupportedEncodingException, IOException, ParseException
    {
        long startTime = System.nanoTime();
        String stats = request.getParameter(PARAM_STATS);
        if (stats != null && stats.equals(PARAM_STATS))
        {
            this.echo(this.readStats.dumpHtmlStats(), response);
            return;
        }
        // TODO : decode to check params
        echoAndStats(startTime, renderByTemplate(request), response);
    }

    private void echoAndStats(long startTime, String html, HttpServletResponse response)
    {
        this.echo(html, response);
        this.readStats.addMicro((System.nanoTime() - startTime) / 1000);
    }

    private String renderByTemplate(HttpServletRequest request)
            throws TemplateException, UnsupportedEncodingException,
            IOException, ParseException
    {

        return getTransStatus(request);
    }

    public String getTransStatus(HttpServletRequest request) throws UnsupportedEncodingException, IOException, ParseException
    {
        String sResponse = null;
        String transID = request.getParameter("transid");
        int timeout = DbgFrontEndConfig.CallApiTimeoutSeconds * 1000;
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setStaleConnectionCheckEnabled(true)
                .setSocketTimeout(timeout)
                .build();
        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build())
        {

            HttpPost httpPost = new HttpPost(DbgFrontEndConfig.GetTransStatusUrl);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("transid", transID));

            nvps.add(new BasicNameValuePair("feclientid", String.valueOf(DbgFrontEndConfig.WebFeClientID)));
            SimpleDateFormat formatSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            formatSDF.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            Date date = new Date(System.currentTimeMillis());
            nvps.add(new BasicNameValuePair("reqdate", formatSDF.format(date)));
            //Hash256(feClientID + transID + format(reqDate, yyyy-MM-dd HH:mm:ss.SSS) + hashKey)
            try
            {
                String data = String.format("%s%s%s%s", String.valueOf(DbgFrontEndConfig.WebFeClientID),
                        transID, formatSDF.format(date), DbgFrontEndConfig.WebFeHashKey);
                String sigdata = dbg.util.HashUtil.hashSHA256(data);
                nvps.add(new BasicNameValuePair("sig", sigdata));

            }
            catch (Exception ex)
            {
                logger.error(ex.toString());
            }

            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

            try (CloseableHttpResponse response = httpclient.execute(httpPost))
            {
                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();
                sResponse = IOUtils.toString(inputStream, "UTF-8");

            }
        }
        //Added by BangDQ for clear session card when transStatus = 1;
        try{
            TransStatusResp  transResp = TransStatusResp.fromJsonString(sResponse);
            if(transResp!=null && !transResp.isProcessing)
            {
                if(transResp.transStatus==1)//Success then clear
                {
                    ClearStoreCardSerial(request, transID);
                }
            }
        }catch ( Exception ex)
        {
            
        }
        
        return sResponse;

    }
}
