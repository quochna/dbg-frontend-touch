/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dbg.frontend.config.DbgFrontEndConfig;
import dbg.entity.ChargeInfo;
import dbg.entity.MiniAppServerEntity;
import dbg.enums.PMCIDEnum;
import dbg.enums.TransStatusEnum;
import dbg.frontend.touch.entity.SSO3Account;
import dbg.request.SubmitTransReq;
import dbg.response.SubmitTransResp;
import dbg.response.SubmitValidateTransResp;

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
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
public class ResultController extends DbgFrontendCore
{

    private static Logger logger = Logger.getLogger(ResultController.class);
    private final String ITEM_SEPARATE = "\\|";
    private final String PARAM_STATS = "stats";
    private final Monitor readStats = new Monitor();
    private static final ResultController instance = new ResultController();
    public static ResultController getInstance()
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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws TException, TemplateException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException, Exception
    {
        long startTime = System.nanoTime();
        String stats = request.getParameter(PARAM_STATS);

        if (stats != null && stats.equals(PARAM_STATS))
        {
            this.echo(this.readStats.dumpHtmlStats(), response);
            return;
        }

        // TODO : decode to check params
        echoAndStats(startTime, renderByTemplate(request, response), response);
    }

    private void echoAndStats(long startTime, String html, HttpServletResponse response)
    {
        this.echo(html, response);
        this.readStats.addMicro((System.nanoTime() - startTime) / 1000);
    }

    private String renderByTemplate(HttpServletRequest request, HttpServletResponse response)
            throws TemplateException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException, Exception
    {

        TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
        Template template = templateLoader.getTemplate("master");
        TemplateDataDictionary dic = TemplateDictionary.create();

        dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
        dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
        dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);


        SubmitTransReq req = new SubmitTransReq();

        req.transID = request.getParameter("transid");
        req.appID = request.getParameter("appid");
        req.appData = request.getParameter("appdata");

        req.envID = String.valueOf(DbgFrontEndConfig.EnvID);
        req.feClientID = String.valueOf(DbgFrontEndConfig.WebFeClientID);
        req.clientIP = getClientIP(request);

        //Special case for ATM range id > 100
        int pmcID = getPmcID(request);
        ChargeInfo cardInfo = new ChargeInfo();
        if (DbgFrontEndConfig.BankEntityMap.containsKey(pmcID)
            || pmcID == PMCIDEnum.VISA_123PAY.getValue()
            || pmcID == PMCIDEnum.MASTER_123PAY.getValue() 
            || pmcID == PMCIDEnum.JCB_123PAY.getValue() )
        {
            
            //added charge Amt to addinfo for validations with flow = 2
            String chargeAMT = request.getParameter("chargeamt");
            if(chargeAMT != null && !chargeAMT.trim().equals(""))
            {
                req.addInfo = chargeAMT.replaceAll(",", "");
            }            
            String amt = request.getParameter("chargeamtcalculated");
            if (amt != null && !amt.trim().equals(""))
            {
                try
                {
                    cardInfo.chargeAmount = new Long(amt.trim().replaceAll(",", ""));
                }
                catch (Exception ex)
                {
                    cardInfo.chargeAmount = -1L;
                }
            }
            if( pmcID == PMCIDEnum.VISA_123PAY.getValue())             
                
            {
               req.pmcID = String.valueOf(PMCIDEnum.VISA_123PAY.getValue()); 
               cardInfo.bankCode = DbgFrontEndConfig.Visa_Master_BankCode_123PAY;
               
            }else if(pmcID == PMCIDEnum.MASTER_123PAY.getValue())
            {
               req.pmcID = String.valueOf(PMCIDEnum.MASTER_123PAY.getValue()); 
               cardInfo.bankCode = DbgFrontEndConfig.Visa_Master_BankCode_123PAY;
            }else if(pmcID == PMCIDEnum.JCB_123PAY.getValue())
            {
               req.pmcID = String.valueOf(PMCIDEnum.JCB_123PAY.getValue()); 
               cardInfo.bankCode = DbgFrontEndConfig.Visa_Master_BankCode_123PAY; 
            }            
            else  //Normal BANKS
            {
               req.pmcID = String.valueOf(dbg.enums.PMCIDEnum.PAY123.getValue()); 
               cardInfo.bankCode = DbgFrontEndConfig.BankEntityMap.get(pmcID).code;
            }
            
            cardInfo.cancelURL = DbgFrontEndConfig.Pay123CancelURL;
            cardInfo.redirectURL = DbgFrontEndConfig.Pay123RedirectURL;
            cardInfo.errorURL = DbgFrontEndConfig.Pay123ErrorURL;

        }
        else
        {
            req.pmcID = request.getParameter("pmcid");
            cardInfo.cardSerial = request.getParameter("cardserial");
            if (cardInfo.cardSerial != null)
            {
                cardInfo.cardSerial = cardInfo.cardSerial.toUpperCase();
                //Added by BangDQ for fill cardserial when input wrong cardcode
                SessionCard  entity = new SessionCard();
                entity.pmcID = req.pmcID;
                entity.CardSerial = cardInfo.cardSerial;
                entity.transID = req.transID;
                StoreCardSerial(request, entity);
            }
            cardInfo.cardPassword = request.getParameter("cardpassword");
            if (cardInfo.cardPassword != null)
            {
                cardInfo.cardPassword = cardInfo.cardPassword.toUpperCase();
            }
            if (req.pmcID.equals("7"))//Zingxu
            {
                String amt = request.getParameter("zingxuamt");
                if (amt != null && !amt.trim().equals(""))
                {
                    try
                    {
                        cardInfo.chargeAmount = new Long(amt.trim().replaceAll(",", ""));
                    }
                    catch (Exception ex)
                    {
                        cardInfo.chargeAmount = -1L;
                    }
                }
                SSO3Account account = getSSO3Account(request);
                if (account != null)
                {
                    req.addInfo = account.userName + ";" + account.userId;
                }

            }
            if (req.pmcID.equals("22"))//Thap Phong
            {
                String amt = request.getParameter("thapphongxuamt");
                if (amt != null && !amt.trim().equals(""))
                {
                    try
                    {
                        cardInfo.chargeAmount = new Long(amt.trim().replaceAll(",", ""));
                    }
                    catch (Exception ex)
                    {
                        cardInfo.chargeAmount = -1L;
                    }
                }
                SSO3Account account = getSSO3Account(request);
                if (account != null)
                {
                    req.addInfo = account.userName + ";" + account.userId;
                }

            }
        }

        String json = cardInfo.toJsonString();
        boolean isException = false;
        try
        {
            req.pmcData = new dbg.util.AES256Algorithm().encrypt(DbgFrontEndConfig.WebFeAesKey, json);
            
            
            
            
        }
        catch (Exception ex)
        {
            isException = true;
            logger.error(ex.toString());
        }

        if (isException)
        {
            dic.showSection("notify");
            dic.setVariable("message", DbgFrontEndConfig.MaintainMsg);
            dic.setVariable("transid", request.getParameter("transid"));            
            SetValuesForRedirectInformationForNotify(request, dic, "0", DbgFrontEndConfig.MaintainMsg);
        }
        else
        {
            String data = String.format("%s%s%s%s%s%s%s%s", req.transID, req.appID, req.appData,
                    req.pmcID, req.envID, req.feClientID, req.pmcData,
                    DbgFrontEndConfig.WebFeHashKey);
          
            req.sig = dbg.util.HashUtil.hashSHA256(data);
            
           
            if (checkDuplicateTransID(request))
            {
                String resturnMSG = "Giao dịch này đã hết hạn.<br/> Vui lòng quay lại ứng dụng để thực hiện giao dịch khác!";
                int errorCode = -19; //Duplicate TransactionID
                dic.showSection("notify");
                dic.setVariable("message", resturnMSG);
                dic.setVariable("transid", request.getParameter("transid"));
                SetValuesForRedirectInformationForNotify(request, dic, String.valueOf(errorCode), resturnMSG);
            }
            else
            {
                if(req.pmcID.equals("7") && 
                        cardInfo.chargeAmount < DbgFrontEndConfig.MinZingXu)
                {
                    String resturnMSG = "Số ZingXu phải lớn hơn hoặc bằng "+DbgFrontEndConfig.MinZingXu+".<br/> Vui lòng quay lại ứng dụng để thực hiện giao dịch khác!";
                    int errorCode = -66; //ZX_CHARGE_AMT_INVALID(-66)
                    dic.showSection("notify");
                    dic.setVariable("message", resturnMSG);
                    dic.setVariable("transid", request.getParameter("transid"));
                    SetValuesForRedirectInformationForNotify(request, dic, String.valueOf(errorCode), resturnMSG);
                }
                else                    
                {               
                    //Save TransationID first
                    SaveSessionTransID(request);
                    //Call Submit validation ATM 
                    if (DbgFrontEndConfig.BankEntityMap.containsKey(pmcID)
                        || pmcID == PMCIDEnum.VISA_123PAY.getValue() 
                        || pmcID == PMCIDEnum.MASTER_123PAY.getValue() 
                        || pmcID == PMCIDEnum.JCB_123PAY.getValue()) //ATM case
                    {
                        //Submit ATM transaction
                        SubmitValidateTransResp resp = submitValidateATMTrans(req);
                        if (resp.returnCode != TransStatusEnum.WAIT_FOR_CHARGE.getValue())
                        {
                            dic.showSection("notify");
                            dic.setVariable("message", resp.returnMessage);
                            dic.setVariable("transid", request.getParameter("transid"));
                            SetValuesForRedirectInformationForNotify(request, dic, String.valueOf(resp.returnCode), resp.returnMessage);
                        }
                        else //Submit request to Pay123 URL                     
                        {
                            SaveSessionInfomation(request);
                            response.sendRedirect(resp.redirectURL);
                        }

                    }                    
                    else //Normal Case
                    {
                        //Submit normal transaction 
                        SubmitTransResp resp = submitTrans(req);
                        if (resp.returnCode != TransStatusEnum.IN_VALIDATION_QUEUE.getValue()
                                && resp.returnCode != TransStatusEnum.SUCCESSFUL.getValue())
                        {
                            dic.showSection("notify");
                            dic.setVariable("message", resp.returnMessage);
                            dic.setVariable("transid", request.getParameter("transid"));
                            SetValuesForRedirectInformationForNotify(request, dic, String.valueOf(resp.returnCode), resp.returnMessage);
                        }
                        else
                        {
                            dic.showSection("asyncresult");
                            dic.setVariable("ASYNC_RESULT_URL", DbgFrontEndConfig.AsyncResultUrl);
                            dic.setVariable("transid", req.transID);
                            int appID = Integer.parseInt(request.getParameter("appid"));
                            if (DbgFrontEndConfig.AppEntityMap.get(appID) != null)
                            {
                                dic.setVariable("appName", DbgFrontEndConfig.AppEntityMap.get(appID).appDesc);
                            }
                            dic.setVariable("numberofcalling", String.valueOf(DbgFrontEndConfig.numberofcallingasync));
                            dic.setVariable("intervalcallinginseconds", String.valueOf(DbgFrontEndConfig.intervalcallinginsecondsasync * 1000));
                            SetValuesForRedirectInformation(request, dic);

                        }
                    }
                
                }
                
            }
        }
        //dic.setVariable("STATUS_BAR", GetStep3BarHTML());
        dic.showSection("statusbar");
        dic.addSection("STEP3");

         //added by BANGDQ for Special Layout 30/10/2014 13:20:10
        //SetSpecialLayOut(request, dic);
        
        return template.renderToString(dic);
    }

    public SubmitTransResp submitTrans(SubmitTransReq req) throws UnsupportedEncodingException, IOException
    {
        SubmitTransResp resp = null;

        int timeout = DbgFrontEndConfig.CallApiTimeoutSeconds * 1000;
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setStaleConnectionCheckEnabled(true)
                  .setSocketTimeout(timeout)
                .build();
        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build())
        {

            HttpPost httpPost = new HttpPost(DbgFrontEndConfig.SubmitTransUrl);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("transid", req.transID));
            nvps.add(new BasicNameValuePair("appid", req.appID));
            nvps.add(new BasicNameValuePair("appdata", req.appData));
            nvps.add(new BasicNameValuePair("pmcid", req.pmcID));
            nvps.add(new BasicNameValuePair("envid", req.envID));
            nvps.add(new BasicNameValuePair("feclientid", req.feClientID));
            nvps.add(new BasicNameValuePair("pmcdata", req.pmcData));
            nvps.add(new BasicNameValuePair("clientIP", req.clientIP));
            nvps.add(new BasicNameValuePair("addInfo", req.addInfo));
            nvps.add(new BasicNameValuePair("sig", req.sig));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));


            try (CloseableHttpResponse response = httpclient.execute(httpPost))
            {
                HttpEntity entity = response.getEntity();

                InputStream inputStream = entity.getContent();
                String sResponse = IOUtils.toString(inputStream, "UTF-8");

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setDateFormat(DbgFrontEndConfig.DateTimeFormatString);
                Gson gson = gsonBuilder.create();

                resp = gson.fromJson(sResponse, SubmitTransResp.class);
            }
        }

        return resp;
    }

    public SubmitValidateTransResp submitValidateATMTrans(SubmitTransReq req)
            throws UnsupportedEncodingException, IOException
    {
        SubmitValidateTransResp resp = null;
        int timeout = DbgFrontEndConfig.CallApiTimeoutSeconds * 1000;
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setStaleConnectionCheckEnabled(true)
                  .setSocketTimeout(timeout)
                .build();
        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build())
        {

            HttpPost httpPost = new HttpPost(DbgFrontEndConfig.GetValidateATMUrl);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("transid", req.transID));
            nvps.add(new BasicNameValuePair("appid", req.appID));
            nvps.add(new BasicNameValuePair("appdata", req.appData));
            nvps.add(new BasicNameValuePair("pmcid", req.pmcID));
            nvps.add(new BasicNameValuePair("envid", req.envID));
            nvps.add(new BasicNameValuePair("feclientid", req.feClientID));
            nvps.add(new BasicNameValuePair("pmcdata", req.pmcData));
            nvps.add(new BasicNameValuePair("clientIP", req.clientIP));
            nvps.add(new BasicNameValuePair("addInfo", req.addInfo));
            nvps.add(new BasicNameValuePair("sig", req.sig));

            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));


            try (CloseableHttpResponse response = httpclient.execute(httpPost))
            {
                HttpEntity entity = response.getEntity();

                InputStream inputStream = entity.getContent();
                String sResponse = IOUtils.toString(inputStream, "UTF-8");

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setDateFormat(DbgFrontEndConfig.DateTimeFormatString);
                Gson gson = gsonBuilder.create();

                resp = gson.fromJson(sResponse, SubmitValidateTransResp.class);
            }
        }

        return resp;
    }

    

    private void SetValuesForRedirectInformation(HttpServletRequest request, TemplateDataDictionary dic)
    {

        String strAppID = request.getParameter("appid");
        String strAppServerID = request.getParameter("appserverid");
        String key = DbgFrontEndConfig.CreateAppServerKey(strAppServerID, strAppID);

        String url = request.getParameter("url_redirect");
        if (url != null && !url.trim().equals(""))
        {
            dic.setVariable("_n_url_redirect", url);

        }
        else
        {
            if (DbgFrontEndConfig.AppServerEntityMap.containsKey(key))
            {
                MiniAppServerEntity entity = DbgFrontEndConfig.AppServerEntityMap.get(key);
                if (entity != null)
                {
                    dic.setVariable("_n_url_redirect", entity.appRedirectUrl);

                }
            }
            else
            {
                dic.setVariable("_n_url_redirect", "");

            }
        }
        dic.setVariable("_n_tranxid", request.getParameter("transid"));
        dic.setVariable("_n_state", "billed");
        dic.setVariable("_n_apptranxid", request.getParameter("apptransid"));
        dic.setVariable("_n_platform", request.getParameter("pl"));
        dic.setVariable("_n_netamount", "");


        dic.setVariable("_n_pmc", request.getParameter("pmcid"));
        dic.setVariable("_n_grossamount", "");
        dic.setVariable("appid", request.getParameter("appid"));
        dic.setVariable("pmcid", request.getParameter("pmcid"));



    }

    private void SetValuesForRedirectInformationForNotify(HttpServletRequest request, TemplateDataDictionary dic, String errorCode, String errormsg)
    {

        String strAppID = request.getParameter("appid");
        String strAppServerID = request.getParameter("appserverid");
        String key = DbgFrontEndConfig.CreateAppServerKey(strAppServerID, strAppID);
        String url = request.getParameter("url_redirect");
        if (url != null && !url.trim().equals(""))
        {
            dic.setVariable("_n_url_redirect", url);

        }
        else
        {
            if (DbgFrontEndConfig.AppServerEntityMap.containsKey(key))
            {
                MiniAppServerEntity entity = DbgFrontEndConfig.AppServerEntityMap.get(key);
                if (entity != null)
                {
                    dic.setVariable("_n_url_redirect", entity.appRedirectUrl);

                }
            }
            else
            {
                dic.setVariable("_n_url_redirect", "");

            }
        }
        dic.setVariable("_n_tranxid", request.getParameter("transid"));
        dic.setVariable("_n_state", "billed");
        dic.setVariable("_n_apptranxid", request.getParameter("apptransid"));
        dic.setVariable("_n_platform", request.getParameter("pl"));
        dic.setVariable("_n_netamount", "");
        dic.setVariable("_n_error_code", errorCode);
        dic.setVariable("_n_error_msg", errormsg);

        String pmcid = request.getParameter("pmcid");
        if (pmcid != null && !pmcid.trim().equals(""))
        {
            try
            {
                int n_pmcid = Integer.parseInt(pmcid);
                if (n_pmcid > 100)
                {
                    n_pmcid = dbg.enums.PMCIDEnum.PAY123.getValue();
                }
                dic.setVariable("_n_pmc", String.valueOf(n_pmcid));
                dic.setVariable("pmcid", String.valueOf(n_pmcid));
            }
            catch (Exception ex)
            {
            }

        }
        else
        {
            dic.setVariable("_n_pmc", request.getParameter("pmcid"));
            dic.setVariable("pmcid", request.getParameter("pmcid"));
        }

        dic.setVariable("_n_grossamount", "");
        dic.setVariable("appid",request.getParameter("appid"));
     


    }

    private void SaveSessionInfomation(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        SessionResultInfo ssResultInfo = new SessionResultInfo();
        String strAppID = request.getParameter("appid");
        String strAppServerID = request.getParameter("appserverid");
        ssResultInfo.appid = strAppID;
        ssResultInfo.appserverid = strAppServerID;
        String key = DbgFrontEndConfig.CreateAppServerKey(strAppServerID, strAppID);
        String url = request.getParameter("url_redirect");
        if (url != null && !url.trim().equals("") &&!url.trim().equals("#"))
        {
            ssResultInfo.url_redirect = url;

        }
        else
        {
            if (DbgFrontEndConfig.AppServerEntityMap.containsKey(key))
            {
                MiniAppServerEntity entity = DbgFrontEndConfig.AppServerEntityMap.get(key);
                if (entity != null)
                {
                    ssResultInfo.url_redirect = entity.appRedirectUrl;

                }
            }
            else
            {
                ssResultInfo.url_redirect = "";

            }
        }

        ssResultInfo.transid = request.getParameter("transid");
        ssResultInfo.billed = "billed";
        ssResultInfo.apptransid = request.getParameter("apptransid");

        ssResultInfo.pl = request.getParameter("pl");

        ssResultInfo.netamount = "";


        ssResultInfo.pmcid = request.getParameter("pmcid");
        ssResultInfo.grossamount = "";

        String sskey = DbgFrontEndConfig.Pay123sessionkey + ssResultInfo.transid;
        session.setAttribute(sskey, ssResultInfo);
        session.setMaxInactiveInterval(DbgFrontEndConfig.SessionTimeoutSeconds);

    }

    

    private void SaveSessionTransID(HttpServletRequest request)
    {
        try
        {
            HttpSession session = request.getSession();
            String transID = request.getParameter("transid");
            if (session != null)
            {
                session.setAttribute(DbgFrontEndConfig.TransIDsessionkey + "" + transID, transID);
                session.setMaxInactiveInterval(DbgFrontEndConfig.SessionTimeoutSeconds);
            }
        }
        catch (Exception ex)
        {
            logger.error(ex.toString());
        }
    }

    private int getPmcID(HttpServletRequest request)
    {
        int pmcID = -1;
        String strpmcid = request.getParameter("pmcid");
        try
        {
            if (strpmcid != null && !strpmcid.equals(""))
            {
                pmcID = Integer.parseInt(request.getParameter("pmcid"));
            }

        }
        catch (NumberFormatException ex)
        {
            logger.error(ex.toString());
        }
        return pmcID;
    }

   
}
