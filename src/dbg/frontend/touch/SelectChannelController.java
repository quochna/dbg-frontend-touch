/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dbg.entity.BankEntity;
import dbg.entity.MiniAppServerEntity;
import dbg.frontend.config.DbgFrontEndConfig;
import dbg.entity.PmcEntity;
import dbg.entity.PmcGroupEntity;
import dbg.enums.PMCIDEnum;
import dbg.request.SubmitTransReq;
import dbg.response.SubmitTransResp;

import hapax.Template;
import hapax.TemplateDataDictionary;
import hapax.TemplateDictionary;
import hapax.TemplateException;
import hapax.TemplateLoader;
import hapax.TemplateResourceLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;
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

/**
 *
 * @author hainpt
 */
public class SelectChannelController extends DbgFrontendCore {

    private static Logger logger = Logger.getLogger(SelectChannelController.class);
    private final String ITEM_SEPARATE = "\\|";
    private final String PARAM_STATS = "stats";
    private final String PARAM_STEP = "step";
    private final Monitor readStats = new Monitor();
    private static final SelectChannelController instance = new SelectChannelController();

    public static SelectChannelController getInstance() {
        return instance;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
        long startTime = System.nanoTime();
        try {
            Integer pmcID = -1;
            String pmcIDStr = request.getParameter("pmcid");
            String StrisBack = request.getParameter("isBack");

            if (StrisBack == null) {
                StrisBack = "";
            }
            try {
                if (pmcIDStr != null && !pmcIDStr.trim().equals("")) {
                    pmcID = Integer.parseInt(pmcIDStr);
                }
            } catch (NumberFormatException ex) {
                logger.error("format" + pmcIDStr + ex.toString());
            }

            SubmitTransResp resp = PreCheckAppData(request);
            if (resp != null) {
                if (resp.returnCode == 1) {
                    if (pmcID != -1) {
                        if (StrisBack.equals("")) {
                            //Check PMCID is 123 Pay
                            request.setAttribute("_isdirect", "true");
                            if (pmcID == dbg.enums.PMCIDEnum.PAY123.getValue()) {

                                echoAndStats(startTime, renderPMCByTemplate(request, dbg.enums.PMCIDGroupEnum.PAY123.getValue()), response);

                            } 
//                            else {
//                                //Change for zalo to get info of wphone         
//                                String _n_platform = request.getParameter("pl");
//                                if (_n_platform != null && !_n_platform.isEmpty()) {
//                                    if (_n_platform.equalsIgnoreCase("wphone")
//                                            || _n_platform.equalsIgnoreCase("wp")) {
//                                        _n_platform = "wp";
//                                    }
//                                }
//                                RequestDispatcher rd = ContextHandler.getCurrentContext().getRequestDispatcher("/thanhtoan?" + "pl=" + _n_platform);
//                                if (rd != null) {
//
//                                    rd.forward(request, response);
//                                } else {
//                                    processRequest(request, response);
//                                }
//                            }
                        } else {
                            processRequest(request, response);

                        }
                    } else {
                        processRequest(request, response);
                    }
                } else {
                    echoAndStats(startTime, renderPreCheckDataErrorByTemplate(request, resp.returnMessage, String.valueOf(resp.returnCode)), response);

                }
            } else {
                String msg = "Hệ thống đang có lỗi, giao dịch thất bại!";
                echoAndStats(startTime, renderPreCheckDataErrorByTemplate(request, msg, "0"), response);
            }
        } catch (Exception ex) {
            logger.error(ex.toString());
            echoAndStats(startTime, renderExceptionErrorByTemplate(request), response);
        }

    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws TException, TemplateException, ServletException, IOException, Exception {

        long startTime = System.nanoTime();
        String stats = request.getParameter(PARAM_STATS);
        if (stats != null && stats.equals(PARAM_STATS)) {
            this.echo(this.readStats.dumpHtmlStats(), response);
            return;
        }
        String step = request.getParameter(PARAM_STEP);
        if (step != null) {
            if (step.equalsIgnoreCase("pmcgrp")) {
                echoAndStats(startTime, renderPMCGRPByTemplate(request), response);

            } else if (step.equalsIgnoreCase("pmc")) {

                //get back the pmcGroupID and Set to the pmcid
                Integer pmcID = -1;
                String pmcIDStr = request.getParameter("pmcid");
                try {
                    if (pmcIDStr != null && !pmcIDStr.trim().equals("")) {
                        pmcID = Integer.parseInt(pmcIDStr);
                    }
                } catch (NumberFormatException ex) {
                    //logger.error(ex.toString());
                }

                Map<Integer, PmcEntity> PmcEntityMap = DbgFrontEndConfig.GetPmcSupportByAppID(request.getParameter("appid"));
                PmcEntity pmcEntity = PmcEntityMap.get(pmcID);
                if (pmcEntity != null) {

                    echoAndStats(startTime, renderPMCByTemplateIsBack(request, pmcEntity.pmcGroupID), response);
                } else {
                    //Banking ATM
                    echoAndStats(startTime, renderPMCByTemplateIsBack(request, dbg.enums.PMCIDGroupEnum.PAY123.getValue()), response);
                }

            } else {
                echoAndStats(startTime, renderPMCByTemplate(request), response);
            }

        } else {
            echoAndStats(startTime, renderPMCGRPByTemplate(request), response);

        }

    }

    private void echoAndStats(long startTime, String html, HttpServletResponse response) {
        this.echo(html, response);
        this.readStats.addMicro((System.nanoTime() - startTime) / 1000);
    }

    private String renderPMCGRPByTemplate(HttpServletRequest request) throws TemplateException, Exception {

        TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
        Template template = templateLoader.getTemplate("master");
        TemplateDataDictionary dic = TemplateDictionary.create();

        dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
        dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
        dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);
        dic.showSection("selectchannel");
        dic.showSection("selectcardlayout");

        Iterator<Integer> keySetIterator = DbgFrontEndConfig.PmcGrpEntityMap.keySet().iterator();
        boolean isFirst = true;
        while (keySetIterator.hasNext()) {
            Integer key = keySetIterator.next();
            PmcGroupEntity pmcgrpEntity = DbgFrontEndConfig.PmcGrpEntityMap.get(key);
            //  List<PmcEntity> listPMC = DbgFrontEndConfig.getPmc(key);
            List<PmcEntity> listPMC = DbgFrontEndConfig.getPmc(key, request.getParameter("appid"));

            if (listPMC.size() == 1) {
                if (key == dbg.enums.PMCIDGroupEnum.PAY123.getValue())//ATM 
                {
                    //Code here get Bank channel
                    TemplateDataDictionary itemGrpDic = dic.addSection("PMCGRP");
                    itemGrpDic.setVariable("pmcgrpid", String.valueOf(pmcgrpEntity.pmcGroupID));
                    itemGrpDic.setVariable("pmcids", DbgFrontEndConfig.BanksIDs);
                } else {

                    TemplateDataDictionary itemGrpDic = dic.addSection("PMCGRP");
                    itemGrpDic.setVariable("pmcgrpid", String.valueOf(pmcgrpEntity.pmcGroupID));
                    itemGrpDic.setVariable("pmcids", String.valueOf(listPMC.get(0).pmcID));
                }

            } else if (listPMC.size() > 1) //change not appID not apply pmcid
            {

                TemplateDataDictionary itemGrpDic = dic.addSection("PMCGRP");
                itemGrpDic.setVariable("pmcgrpid", String.valueOf(pmcgrpEntity.pmcGroupID));
                itemGrpDic.setVariable("pmcids", GetPMCIDs(listPMC));
            }

            if (pmcgrpEntity != null && pmcgrpEntity.pmcGroupName != null
                    && !pmcgrpEntity.pmcGroupName.equalsIgnoreCase("SMS")
                    && listPMC.size() > 0) {
                TemplateDataDictionary itemDic = dic.addSection("PMC");
                itemDic.setVariable("pmcid", String.valueOf(pmcgrpEntity.pmcGroupID));
                itemDic.setVariable("pmcname", pmcgrpEntity.pmcGroupDesc);

                if (isFirst == true) {
                    itemDic.setVariable("checked", "checked");
                    itemDic.setVariable("selected", "selected");
                    isFirst = false;
                } else {
                    itemDic.setVariable("checked", "");

                }
            }
        }

        dic.setVariable("transid", request.getParameter("transid"));
        dic.setVariable("appid", request.getParameter("appid"));

        dic.setVariable("appdata", request.getParameter("appdata"));

        //dic.setVariable("XACNHAN_BTN", "<p class=\"btn02\"><a  class=\"zdbgbtn\" href=\"javascript:;\" onclick=\"submitForm();\" >Xác nhận</a></p>");
        dic.addSection("XACNHAN_BTN_FORM");
        
        dic.setVariable("actionpost", "chonkenhthanhtoan");
        dic.setVariable("step", " ");
        dic.setVariable("PMC_HEADER", "Chọn phương thức thanh toán");
        //dic.setVariable("STATUS_BAR", GetStep1BarHTML());
        dic.showSection("statusbar");
        dic.addSection("STEP1");

        //added new for information gen call redirect
        SetValuesForRedirectInformation(request, dic);
        //added by BANGDQ for Special Layout 30/10/2014 13:20:10
        // SetSpecialLayOut(request, dic);

        return template.renderToString(dic);
    }

    private String renderPMCByTemplate(HttpServletRequest request) throws TemplateException {

        TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
        Template template = templateLoader.getTemplate("master");
        TemplateDataDictionary dic = TemplateDictionary.create();

        dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
        dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
        dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);
        dic.showSection("selectchannel");

        // Iterator<Integer> keySetIterator = DbgFrontEndConfig.PmcEntityMap.keySet().iterator();
        Map<Integer, PmcEntity> PmcEntityMap = DbgFrontEndConfig.GetPmcSupportByAppID(request.getParameter("appid"));
        Iterator<Integer> keySetIterator = PmcEntityMap.keySet().iterator();

        boolean isFirst = true;
        int pmcGroupID = Integer.parseInt(request.getParameter("pmcid"));
        //Build special case for ATM
        if (pmcGroupID == dbg.enums.PMCIDGroupEnum.PAY123.getValue())//ATM
        {
            dic.showSection("selectatmlayout");
            //Code here build bank layout
            Iterator<Integer> keybankSetIterator = DbgFrontEndConfig.BankEntityMap.keySet().iterator();
            while (keybankSetIterator.hasNext()) {
                Integer key = keybankSetIterator.next();
                BankEntity bankEntity = DbgFrontEndConfig.BankEntityMap.get(key);
                if (bankEntity != null && bankEntity.code != null) {
                    TemplateDataDictionary itemDic = dic.addSection("PMC");
                    itemDic.setVariable("pmcid", String.valueOf(bankEntity.id));
                    itemDic.setVariable("pmcname", bankEntity.mobiname);
                    itemDic.setVariable("banklogo", bankEntity.logoUrlTouch);

                    if (isFirst == true) {
                        itemDic.setVariable("checked", "checked");
                        itemDic.setVariable("selected", "selected");
                        isFirst = false;
                    } else {
                        itemDic.setVariable("checked", "");
                    }

                }
            }
        } else if (pmcGroupID == dbg.enums.PMCIDGroupEnum.VISA_MASTER.getValue()) {
            dic.showSection("selectatmlayout");
            while (keySetIterator.hasNext()) {
                Integer key = keySetIterator.next();
                PmcEntity pmcEntity = PmcEntityMap.get(key);
                if (pmcEntity != null && pmcEntity.pmcName != null
                        && pmcGroupID == pmcEntity.pmcGroupID && !pmcEntity.pmcName.equals("SMS")) {
                    TemplateDataDictionary itemDic = dic.addSection("PMC");
                    itemDic.setVariable("pmcid", String.valueOf(pmcEntity.pmcID));
                    itemDic.setVariable("pmcname", pmcEntity.pmcDesc);
                    if (pmcEntity.pmcID == PMCIDEnum.VISA_123PAY.getValue()) {

                        itemDic.setVariable("banklogo", "credit visa");
                    } else if (pmcEntity.pmcID == PMCIDEnum.MASTER_123PAY.getValue()) {

                        itemDic.setVariable("banklogo", "credit mastercard");
                    } else if (pmcEntity.pmcID == PMCIDEnum.JCB_123PAY.getValue()) {
                        itemDic.setVariable("banklogo", "credit jcb");
                    }

                    if (isFirst == true) {
                        itemDic.setVariable("checked", "checked");
                        itemDic.setVariable("selected", "selected");
                        isFirst = false;
                    } else {
                        itemDic.setVariable("checked", "");
                    }
                }
            }
        } else {
            dic.showSection("selectcardlayout");
            while (keySetIterator.hasNext()) {
                Integer key = keySetIterator.next();
                // PmcEntity pmcEntity = DbgFrontEndConfig.PmcEntityMap.get(key);
                PmcEntity pmcEntity = PmcEntityMap.get(key);
                if (pmcEntity != null && pmcEntity.pmcName != null
                        && pmcGroupID == pmcEntity.pmcGroupID && !pmcEntity.pmcName.equals("SMS")) {
                    TemplateDataDictionary itemDic = dic.addSection("PMC");
                    itemDic.setVariable("pmcid", String.valueOf(pmcEntity.pmcID));
                    itemDic.setVariable("pmcname", pmcEntity.pmcDesc);
                    if (isFirst == true) {
                        itemDic.setVariable("checked", "checked");
                        itemDic.setVariable("selected", "selected");
                        isFirst = false;
                    } else {
                        itemDic.setVariable("checked", "");
                    }
                }
            }

        }

        //dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
        if (GetBackButtonHTML(request)) {
            dic.addSection("BACK_BTN");
        }
   //     dic.setVariable("XACNHAN_BTN", "<p class=\"btn02\"> <a  class=\"zdbgbtn\" href=\"javascript:;\" onclick=\"document.getElementById('frmSelectChannel').submit();\" >Xác nhận</a></p>");
        dic.addSection("XACNHAN_BTN");
        
        dic.setVariable("transid", request.getParameter("transid"));
        dic.setVariable("appid", request.getParameter("appid"));
        dic.setVariable("appdata", request.getParameter("appdata"));

        dic.setVariable("actionpost", "thanhtoan");
        dic.setVariable("step", "pmcgrp");
        String pmcgrpheader = DbgFrontEndConfig.Get_pmcgrp_headermsg(request.getParameter("pmcid"));
        if (pmcgrpheader != null && pmcgrpheader.trim() != "") {
            dic.setVariable("PMC_HEADER", pmcgrpheader);
        } else {
            dic.setVariable("PMC_HEADER", "Chọn phương thức thanh toán");
        }
        //dic.setVariable("STATUS_BAR", GetStep1BarHTML());
        dic.showSection("statusbar");
        dic.addSection("STEP1");
        //added new for information gen call redirect
        SetValuesForRedirectInformation(request, dic);

        //added by BANGDQ for Special Layout 30/10/2014 13:20:10
        //SetSpecialLayOut(request, dic);
        return template.renderToString(dic);
    }

    private String renderPMCByTemplateIsBack(HttpServletRequest request, int PmcGrpID) throws TemplateException {

        TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
        Template template = templateLoader.getTemplate("master");
        TemplateDataDictionary dic = TemplateDictionary.create();

        dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
        dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
        dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);
        dic.showSection("selectchannel");

        //Iterator<Integer> keySetIterator = DbgFrontEndConfig.PmcEntityMap.keySet().iterator();
        Map<Integer, PmcEntity> PmcEntityMap = DbgFrontEndConfig.GetPmcSupportByAppID(request.getParameter("appid"));
        Iterator<Integer> keySetIterator = PmcEntityMap.keySet().iterator();

        boolean isFirst = true;
        int pmcGroupID = PmcGrpID;

        if (PmcGrpID == dbg.enums.PMCIDGroupEnum.PAY123.getValue()) {

            dic.showSection("selectatmlayout");
            //Code here build bank layout
            Iterator<Integer> keybankSetIterator = DbgFrontEndConfig.BankEntityMap.keySet().iterator();
            while (keybankSetIterator.hasNext()) {
                Integer key = keybankSetIterator.next();
                BankEntity bankEntity = DbgFrontEndConfig.BankEntityMap.get(key);
                if (bankEntity != null && bankEntity.code != null) {
                    TemplateDataDictionary itemDic = dic.addSection("PMC");
                    itemDic.setVariable("pmcid", String.valueOf(bankEntity.id));
                    itemDic.setVariable("pmcname", bankEntity.mobiname);
                    itemDic.setVariable("banklogo", bankEntity.logoUrlTouch);
                    if (isFirst == true) {
                        itemDic.setVariable("checked", "checked");
                        itemDic.setVariable("selected", "selected");
                        isFirst = false;
                    } else {
                        itemDic.setVariable("checked", "");
                    }

                }

            }

        } else if (pmcGroupID == dbg.enums.PMCIDGroupEnum.VISA_MASTER.getValue()) {
            dic.showSection("selectatmlayout");
            while (keySetIterator.hasNext()) {
                Integer key = keySetIterator.next();
                PmcEntity pmcEntity = PmcEntityMap.get(key);
                if (pmcEntity != null && pmcEntity.pmcName != null
                        && pmcGroupID == pmcEntity.pmcGroupID && !pmcEntity.pmcName.equals("SMS")) {
                    TemplateDataDictionary itemDic = dic.addSection("PMC");
                    itemDic.setVariable("pmcid", String.valueOf(pmcEntity.pmcID));
                    itemDic.setVariable("pmcname", pmcEntity.pmcDesc);
                    if (pmcEntity.pmcID == PMCIDEnum.VISA_123PAY.getValue()) {

                        itemDic.setVariable("banklogo", "credit visa");
                    } else if (pmcEntity.pmcID == PMCIDEnum.MASTER_123PAY.getValue()) {

                        itemDic.setVariable("banklogo", "credit mastercard");
                    } else if (pmcEntity.pmcID == PMCIDEnum.JCB_123PAY.getValue()) {
                        itemDic.setVariable("banklogo", "credit jcb");
                    }

                    if (isFirst == true) {
                        itemDic.setVariable("checked", "checked");
                        itemDic.setVariable("selected", "selected");
                        isFirst = false;
                    } else {
                        itemDic.setVariable("checked", "");
                    }
                }
            }
        } else {
            dic.showSection("selectcardlayout");
            while (keySetIterator.hasNext()) {
                Integer key = keySetIterator.next();
              // PmcEntity pmcEntity = DbgFrontEndConfig.PmcEntityMap.get(key);
                // Map<Integer, PmcEntity>  PmcEntityMap = DbgFrontEndConfig.GetPmcSupportByAppID(request.getParameter("appid"));
                PmcEntity pmcEntity = PmcEntityMap.get(key);
                if (pmcEntity != null && pmcEntity.pmcName != null
                        && pmcGroupID == pmcEntity.pmcGroupID && !pmcEntity.pmcName.equals("SMS")) {
                    TemplateDataDictionary itemDic = dic.addSection("PMC");
                    itemDic.setVariable("pmcid", String.valueOf(pmcEntity.pmcID));
                    itemDic.setVariable("pmcname", pmcEntity.pmcDesc);
                    if (isFirst == true) {
                        itemDic.setVariable("checked", "checked");
                        itemDic.setVariable("selected", "selected");
                        isFirst = false;
                    } else {
                        itemDic.setVariable("checked", "");
                    }
                }
            }
        }

        //dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
        if (GetBackButtonHTML(request)) {
            dic.addSection("BACK_BTN");
        }
  //      dic.setVariable("XACNHAN_BTN", "<p class=\"btn02\"> <a  class=\"zdbgbtn\" href=\"javascript:;\" onclick=\"document.getElementById('frmSelectChannel').submit();\" >Xác nhận</a></p>");

        dic.addSection("XACNHAN_BTN");
        
        dic.setVariable("transid", request.getParameter("transid"));
        dic.setVariable("appid", request.getParameter("appid"));
        dic.setVariable("appdata", request.getParameter("appdata"));

        dic.setVariable("actionpost", "thanhtoan");
        dic.setVariable("step", "pmcgrp");
        String pmcgrpheader = DbgFrontEndConfig.Get_pmcgrp_headermsg(String.valueOf(PmcGrpID));
        if (pmcgrpheader != null && !pmcgrpheader.trim().equals("")) {
            dic.setVariable("PMC_HEADER", pmcgrpheader);
        } else {
            dic.setVariable("PMC_HEADER", "Chọn phương thức thanh toán");
        }
        //dic.setVariable("STATUS_BAR", GetStep1BarHTML());
        dic.showSection("statusbar");
        dic.addSection("STEP1");
        //added new for information gen call redirect
        SetValuesForRedirectInformation(request, dic);
        //added save back button
        SaveBackButtonHTML(request, dic);

        //added by BANGDQ for Special Layout 30/10/2014 13:20:10
        //SetSpecialLayOut(request, dic);
        return template.renderToString(dic);
    }

    private String GetPMCIDs(List<PmcEntity> listPMC) {

        String retunval = "";
        if (listPMC != null) {
            for (int i = 0; i < listPMC.size(); i++) {
                retunval += String.valueOf(listPMC.get(i).pmcID) + ",";
            }
        }
        if (retunval.length() > 0) {
            retunval = retunval.substring(0, retunval.length() - 1);
        }
        return retunval;
    }

    private void SetValuesForRedirectInformation(HttpServletRequest request, TemplateDataDictionary dic) {

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

        dic.setVariable("_n_pmc", "");
        dic.setVariable("_n_grossamount", "");

    }

    private String renderPreCheckDataErrorByTemplate(HttpServletRequest request, String strError, String errorCode) throws TemplateException {

        TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
        Template template = templateLoader.getTemplate("master");
        TemplateDataDictionary dic = TemplateDictionary.create();
        dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
        dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
        dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);
        dic.setVariable("apptransid", request.getParameter("apptransid"));

        //dic.setVariable("STATUS_BAR", GetStep1BarHTML());
        dic.showSection("statusbar");
        dic.addSection("STEP1");
        dic.showSection("notify");

        dic.setVariable("message", strError);
        dic.setVariable("transid", request.getParameter("transid"));

        SetValuesForRedirectInformation(request, dic);
        dic.setVariable("_n_error_code", errorCode);
        dic.setVariable("_n_error_msg", strError);
        //added by BANGDQ for Special Layout 30/10/2014 13:20:10
        //SetSpecialLayOut(request, dic);

        return template.renderToString(dic);

    }

    private SubmitTransResp PreCheckAppData(HttpServletRequest request) {
        SubmitTransResp resp = null;
        SubmitTransReq req = new SubmitTransReq();
        req.transID = request.getParameter("transid");
        req.appID = request.getParameter("appid");
        req.appData = request.getParameter("appdata");
        req.pmcID = "-1";
        req.envID = String.valueOf(DbgFrontEndConfig.EnvID);
        req.feClientID = String.valueOf(DbgFrontEndConfig.WebFeClientID);
        try {

            String data = String.format("%s%s%s%s%s%s%s%s", req.transID, req.appID, req.appData,
                    req.pmcID, req.envID, req.feClientID, req.pmcData,
                    DbgFrontEndConfig.WebFeHashKey);
            req.sig = dbg.util.HashUtil.hashSHA256(data);
            resp = submitPreCheckTrans(req);

        } catch (Exception ex) {
            logger.error(ex.toString());
        }
        return resp;

    }

    private SubmitTransResp submitPreCheckTrans(SubmitTransReq req) throws UnsupportedEncodingException, IOException {
        SubmitTransResp resp = null;
        int timeout = DbgFrontEndConfig.CallApiTimeoutSeconds * 1000;
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setStaleConnectionCheckEnabled(true)
                .setSocketTimeout(timeout)
                .build();
        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build()) {

            HttpPost httpPost = new HttpPost(DbgFrontEndConfig.PreCheckTransUrl);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("transid", req.transID));
            nvps.add(new BasicNameValuePair("appid", req.appID));
            nvps.add(new BasicNameValuePair("appdata", req.appData));
            nvps.add(new BasicNameValuePair("pmcid", req.pmcID));
            nvps.add(new BasicNameValuePair("envid", req.envID));
            nvps.add(new BasicNameValuePair("feclientid", req.feClientID));
            nvps.add(new BasicNameValuePair("pmcdata", req.pmcData));
            nvps.add(new BasicNameValuePair("sig", req.sig));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
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

    private String renderPMCByTemplate(HttpServletRequest request, int pmcGroupID)
            throws TemplateException {

        TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
        Template template = templateLoader.getTemplate("master");
        TemplateDataDictionary dic = TemplateDictionary.create();

        dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
        dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
        dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);

        //Iterator<Integer> keySetIterator = DbgFrontEndConfig.PmcEntityMap.keySet().iterator();
        Map<Integer, PmcEntity> PmcEntityMap = DbgFrontEndConfig.GetPmcSupportByAppID(request.getParameter("appid"));
        Iterator<Integer> keySetIterator = PmcEntityMap.keySet().iterator();
        boolean isFirst = true;
        if (PmcEntityMap.size() > 0) {

            //Build special case for ATM
            if (pmcGroupID == dbg.enums.PMCIDGroupEnum.PAY123.getValue())//ATM
            {
                int Pay123PmcID = dbg.enums.PMCIDEnum.PAY123.getValue();
                if (PmcEntityMap.containsKey(Pay123PmcID)) {
                    dic.showSection("selectchannel");
                    dic.showSection("selectatmlayout");
                    //Code here build bank layout
                    Iterator<Integer> keybankSetIterator = DbgFrontEndConfig.BankEntityMap.keySet().iterator();
                    while (keybankSetIterator.hasNext()) {
                        Integer key = keybankSetIterator.next();
                        BankEntity bankEntity = DbgFrontEndConfig.BankEntityMap.get(key);
                        if (bankEntity != null && bankEntity.code != null) {
                            TemplateDataDictionary itemDic = dic.addSection("PMC");
                            itemDic.setVariable("pmcid", String.valueOf(bankEntity.id));
                            itemDic.setVariable("pmcname", bankEntity.mobiname);
                            itemDic.setVariable("banklogo", bankEntity.logoUrlTouch);

                            if (isFirst == true) {
                                itemDic.setVariable("checked", "checked");
                                itemDic.setVariable("selected", "selected");
                                isFirst = false;
                            } else {
                                itemDic.setVariable("checked", "");
                            }

                        }
                    }

                } else {
                    dic.showSection("notify");
                    String msg2 = "Kênh thanh toán này chưa được hỗ trợ, vui lòng quay lại sau!";
                    dic.setVariable("message", msg2);
                    dic.setVariable("transid", request.getParameter("transid"));
                    SetValuesErrorForRedirectInformationNotify(request, dic, msg2);

                }

            } else {
                dic.showSection("selectchannel");
                dic.showSection("selectcardlayout");
                while (keySetIterator.hasNext()) {
                    Integer key = keySetIterator.next();
                   // PmcEntity pmcEntity = DbgFrontEndConfig.PmcEntityMap.get(key);
                    // Map<Integer, PmcEntity>  PmcEntityMap = DbgFrontEndConfig.GetPmcSupportByAppID(request.getParameter("appid"));
                    PmcEntity pmcEntity = PmcEntityMap.get(key);

                    if (pmcEntity != null && pmcEntity.pmcName != null
                            && pmcGroupID == pmcEntity.pmcGroupID && !pmcEntity.pmcName.equals("SMS")) {
                        TemplateDataDictionary itemDic = dic.addSection("PMC");
                        itemDic.setVariable("pmcid", String.valueOf(pmcEntity.pmcID));
                        itemDic.setVariable("pmcname", pmcEntity.pmcDesc);
                        if (isFirst == true) {
                            itemDic.setVariable("checked", "checked");
                            itemDic.setVariable("selected", "selected");
                            isFirst = false;
                        } else {
                            itemDic.setVariable("checked", "");
                        }
                    }
                }

            }
        } else {
            dic.showSection("notify");
            String msg2 = "Kênh thanh toán này chưa được hỗ trợ, vui lòng quay lại sau!";
            dic.setVariable("message", msg2);
            dic.setVariable("transid", request.getParameter("transid"));
            SetValuesErrorForRedirectInformationNotify(request, dic, msg2);
        }

        //dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
        if (GetBackButtonHTML(request)) {
            dic.addSection("BACK_BTN");
        }

        dic.setVariable("transid", request.getParameter("transid"));
        dic.setVariable("appid", request.getParameter("appid"));
        dic.setVariable("appdata", request.getParameter("appdata"));
        dic.addSection("XACNHAN_BTN");
        dic.setVariable("actionpost", "thanhtoan");
        dic.setVariable("step", "pmcgrp");
        String pmcgrpheader = DbgFrontEndConfig.Get_pmcgrp_headermsg(String.valueOf(pmcGroupID));
        if (pmcgrpheader != null && !pmcgrpheader.trim().equals("")) {
            dic.setVariable("PMC_HEADER", pmcgrpheader);
        } else {
            dic.setVariable("PMC_HEADER", "Chọn phương thức thanh toán");
        }
        // dic.setVariable("STATUS_BAR", GetStep1BarHTML());
        dic.showSection("statusbar");
        dic.addSection("STEP1");
        //added new for information gen call redirect
        SetValuesForRedirectInformation(request, dic);

        //added by BANGDQ
        SaveBackButtonHTML(request, dic);

       //added by BANGDQ for Special Layout 30/10/2014 13:20:10
        //SetSpecialLayOut(request, dic);
        return template.renderToString(dic);
    }

    private void SetValuesErrorForRedirectInformationNotify(HttpServletRequest request, TemplateDataDictionary dic, String msg) {
        dic.setVariable("_n_error_code", "0");
        dic.setVariable("_n_error_msg", msg);
        //added by BangDQ
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
        dic.setVariable("appid", request.getParameter("appid"));

    }
}
