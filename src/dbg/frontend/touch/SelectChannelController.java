/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch;

import dbg.entity.BankEntity;
import dbg.entity.MiniAppServerEntity;
import dbg.entity.PmcEntity;
import dbg.entity.PmcGroupEntity;
import dbg.enums.PMCIDEnum;
import dbg.frontend.config.DbgFrontEndConfig;
import dbg.frontend.touch.*;
import dbg.frontend.touch.entity.LogEntity;
import static dbg.frontend.utils.common.getRequestUrl;
import hapax.Template;
import hapax.TemplateDataDictionary;
import hapax.TemplateDictionary;
import hapax.TemplateException;
import hapax.TemplateLoader;
import hapax.TemplateResourceLoader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.eclipse.jetty.server.handler.ContextHandler;

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
        LogEntity logEntity = new LogEntity();

        try {
            logEntity.startTime = System.nanoTime();
            logEntity.userAgent = request.getHeader("User-Agent");
            logEntity.requestUrl = getRequestUrl(request);

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

            if (pmcID != -1) {
                if (StrisBack.equals("")) {
                    //Check PMCID is 123 Pay
                    request.setAttribute("_isdirect", "true");
                    if (pmcID == dbg.enums.PMCIDEnum.PAY123.getValue()) {

                        echoAndStats(logEntity, renderPMCByTemplate(request, dbg.enums.PMCIDGroupEnum.PAY123.getValue()), response);

                    } else {
                        //Change for zalo to get info of wphone         
                        String _n_platform = request.getParameter("pl");
                        if (_n_platform != null && !_n_platform.isEmpty()) {
                            if (_n_platform.equalsIgnoreCase("wphone")
                                    || _n_platform.equalsIgnoreCase("wp")) {
                                _n_platform = "wp";
                            }
                        }
                        RequestDispatcher rd = ContextHandler.getCurrentContext().getRequestDispatcher("/thanhtoan?" + "pl=" + _n_platform);
                        if (rd != null) {

                            rd.forward(request, response);
                        } else {
                            processRequest(logEntity, request, response);
                        }
                    }

                } else {
                    processRequest(logEntity, request, response);

                }
            } else {
                processRequest(logEntity, request, response);
            }

        } catch (Exception ex) {
            logger.error(ex.toString(), ex);
            logEntity.exception = ex.getMessage() + "|" + ExceptionUtils.getStackTrace(ex);
            echoAndStats(logEntity, renderExceptionErrorByTemplate(request), response);
        } finally {
            //ghi log//todo
            logger.info(logEntity.toJsonString());
        }

    }

    protected void processRequest(LogEntity logEntity, HttpServletRequest request, HttpServletResponse response) throws TException, TemplateException, ServletException, IOException, Exception {

        long startTime = System.nanoTime();
        String stats = request.getParameter(PARAM_STATS);
        if (stats != null && stats.equals(PARAM_STATS)) {
            this.echo(this.readStats.dumpHtmlStats(), response);
            return;
        }
        String step = request.getParameter(PARAM_STEP);
        if (step != null) {
            if (step.equalsIgnoreCase("pmcgrp")) {
                echoAndStats(logEntity, renderPMCGRPByTemplate(request), response);

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
                    logEntity.exception = ex.getMessage() + "|" + ExceptionUtils.getStackTrace(ex);
                }

                Map<Integer, PmcEntity> PmcEntityMap = DbgFrontEndConfig.GetPmcSupportByAppID(request.getParameter("appid"));
                PmcEntity pmcEntity = PmcEntityMap.get(pmcID);
                if (pmcEntity != null) {

                    echoAndStats(logEntity, renderPMCByTemplateIsBack(request, pmcEntity.pmcGroupID), response);
                } else {
                    //Banking ATM
                    echoAndStats(logEntity, renderPMCByTemplateIsBack(request, dbg.enums.PMCIDGroupEnum.PAY123.getValue()), response);
                }

            } else {
                echoAndStats(logEntity, renderPMCByTemplate(request), response);
            }

        } else {
            echoAndStats(logEntity, renderPMCGRPByTemplate(request), response);

        }

    }

    private void echoAndStats(LogEntity logEntity, String html, HttpServletResponse response) {
        logEntity.endTime = System.currentTimeMillis();
        this.echo(html, response);
        //logger.info...
//        this.readStats.addMicro((System.nanoTime() - logEntity.startTime) / 1000);
    }

    private String renderPMCGRPByTemplate(HttpServletRequest request) throws TemplateException, Exception {

        TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
        Template template = templateLoader.getTemplate("master");
        TemplateDataDictionary dic = TemplateDictionary.create();

        dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
        dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
        dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
//        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);
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

        dic.setVariable("userid", request.getParameter("userid"));
        dic.setVariable("platform", request.getParameter("platform"));
        dic.setVariable("flow", request.getParameter("flow"));
        dic.setVariable("itemid", request.getParameter("itemid"));
        dic.setVariable("itemname", request.getParameter("itemname"));
        dic.setVariable("itemquantity", request.getParameter("itemquantity"));
        dic.setVariable("chargeamt", request.getParameter("chargeamt"));
        dic.setVariable("serverid", request.getParameter("serverid"));
        dic.setVariable("apptransid", request.getParameter("apptransid"));
        dic.setVariable("appTest", request.getParameter("appTest"));

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
//        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);
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

        dic.setVariable("userid", request.getParameter("userid"));
        dic.setVariable("platform", request.getParameter("platform"));
        dic.setVariable("flow", request.getParameter("flow"));
        dic.setVariable("itemid", request.getParameter("itemid"));
        dic.setVariable("itemname", request.getParameter("itemname"));
        dic.setVariable("itemquantity", request.getParameter("itemquantity"));
        dic.setVariable("chargeamt", request.getParameter("chargeamt"));
        dic.setVariable("serverid", request.getParameter("serverid"));
        dic.setVariable("apptransid", request.getParameter("apptransid"));
        dic.setVariable("appTest", request.getParameter("appTest"));

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
//        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);
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

        dic.setVariable("userid", request.getParameter("userid"));
        dic.setVariable("platform", request.getParameter("platform"));
        dic.setVariable("flow", request.getParameter("flow"));
        dic.setVariable("itemid", request.getParameter("itemid"));
        dic.setVariable("itemname", request.getParameter("itemname"));
        dic.setVariable("itemquantity", request.getParameter("itemquantity"));
        dic.setVariable("chargeamt", request.getParameter("chargeamt"));
        dic.setVariable("serverid", request.getParameter("serverid"));
        dic.setVariable("apptransid", request.getParameter("apptransid"));
        dic.setVariable("appTest", request.getParameter("appTest"));

        dic.setVariable("step", "pmcgrp");
        String pmcgrpheader = DbgFrontEndConfig.Get_pmcgrp_headermsg(String.valueOf(PmcGrpID));
        if (pmcgrpheader != null && !pmcgrpheader.trim().equals("")) {
            dic.setVariable("PMC_HEADER", pmcgrpheader);
        } else {
            dic.setVariable("PMC_HEADER", "Chọn phương thức thanh toán");
        }
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
        String strAppServerID = request.getParameter("serverid");
        String key = DbgFrontEndConfig.CreateAppServerKey(strAppServerID, strAppID);

        String url = request.getParameter("urlredirect");
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
        dic.setVariable("serverid", request.getParameter("serverid"));

        dic.setVariable("_n_pmc", "");
        dic.setVariable("_n_grossamount", "");

    }

    private String renderPMCByTemplate(HttpServletRequest request, int pmcGroupID)
            throws TemplateException {

        TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
        Template template = templateLoader.getTemplate("master");
        TemplateDataDictionary dic = TemplateDictionary.create();

        dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
        dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
        dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
//        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);

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

        dic.setVariable("userid", request.getParameter("userid"));
        dic.setVariable("platform", request.getParameter("platform"));
        dic.setVariable("flow", request.getParameter("flow"));
        dic.setVariable("itemid", request.getParameter("itemid"));
        dic.setVariable("itemname", request.getParameter("itemname"));
        dic.setVariable("itemquantity", request.getParameter("itemquantity"));
        dic.setVariable("chargeamt", request.getParameter("chargeamt"));
        dic.setVariable("serverid", request.getParameter("serverid"));
        dic.setVariable("apptransid", request.getParameter("apptransid"));
        dic.setVariable("appTest", request.getParameter("appTest"));

        dic.setVariable("step", "pmcgrp");
        String pmcgrpheader = DbgFrontEndConfig.Get_pmcgrp_headermsg(String.valueOf(pmcGroupID));
        if (pmcgrpheader != null && !pmcgrpheader.trim().equals("")) {
            dic.setVariable("PMC_HEADER", pmcgrpheader);
        } else {
            dic.setVariable("PMC_HEADER", "Chọn phương thức thanh toán");
        }
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
        String strAppServerID = request.getParameter("serverid");
        String key = DbgFrontEndConfig.CreateAppServerKey(strAppServerID, strAppID);
        String url = request.getParameter("urlredirect");

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
