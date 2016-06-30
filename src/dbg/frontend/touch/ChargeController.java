/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch;

import dbg.entity.AppData;
import dbg.entity.MiniAppServerEntity;
import dbg.entity.PaymentHidenInfo;
import dbg.entity.PmcEntity;
import dbg.enums.PMCIDEnum;
import dbg.frontend.config.DbgFrontEndConfig;
import hapax.Template;
import hapax.TemplateDataDictionary;
import hapax.TemplateDictionary;
import hapax.TemplateException;
import hapax.TemplateLoader;
import hapax.TemplateResourceLoader;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

/**
 *
 * @author hainpt
 */
public class ChargeController extends DbgFrontendCore {

    private static final Logger logger = Logger.getLogger(ChargeController.class);
    private final String PARAM_STATS = "stats";
    private final Monitor readStats = new Monitor();
    private static final ChargeController instance = new ChargeController();

    public static ChargeController getInstance() {
        return instance;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
        long startTime = System.nanoTime();
        try {

            int pmcID = -1;
            String strpmcid = request.getParameter("pmcid");
            try {
                if (strpmcid != null && !strpmcid.equals("")) {
                    pmcID = Integer.parseInt(request.getParameter("pmcid"));
                }

            } catch (NumberFormatException ex) {
            }
            Map<Integer, PmcEntity> PmcEntityMap = DbgFrontEndConfig.GetPmcSupportByAppID(request.getParameter("appid"));
            PmcEntity pmcEntity = PmcEntityMap.get(pmcID);
            //if (DbgFrontEndConfig.PmcEntityMap.containsKey(pmcID))
            if (pmcEntity != null) {
                processRequest(request, response);
            } else {   //if it is bank channel
                if (DbgFrontEndConfig.BankEntityMap.containsKey(pmcID)) {
                    processRequest(request, response);
                } else {

                    String msg2 = "Kênh thanh toán này chưa được hỗ trợ, vui lòng quay lại sau!";
                    echoAndStats(startTime, renderPreCheckDataErrorByTemplate(request, msg2, "0"), response);
                }
            }

        } catch (Exception ex) {
            logger.error(ex.toString());
            logger.error("ChargeController error RootCause: " + ExceptionUtils.getRootCauseMessage(ex));
            logger.error("ChargeController error StackTrace: " + ExceptionUtils.getStackTrace(ex));
            echoAndStats(startTime, renderExceptionErrorByTemplate(request), response);

        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws TException, TemplateException, ServletException, IOException, Exception {
        long startTime = System.nanoTime();
        String stats = request.getParameter(PARAM_STATS);

        if (stats != null && stats.equals(PARAM_STATS)) {
            this.echo(this.readStats.dumpHtmlStats(), response);
            return;
        }

        // TODO : decode to check params
        echoAndStats(startTime, renderByTemplate(request, response), response);
    }

    private void echoAndStats(long startTime, String html, HttpServletResponse response) {
        this.echo(html, response);
        this.readStats.addMicro((System.nanoTime() - startTime) / 1000);
    }

    private String renderByTemplate(HttpServletRequest request, HttpServletResponse response) throws TemplateException, ServletException, TException, IOException, Exception {

        TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
        Template template = templateLoader.getTemplate("master");
        TemplateDataDictionary dic = TemplateDictionary.create();
        dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
        dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
        dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);
        dic.setVariable("apptransid", request.getParameter("apptransid"));
        dic.setVariable("appid", request.getParameter("appid"));
        int pmcID = -1;
        int appID = -1;
        PaymentHidenInfo info = new PaymentHidenInfo();
        try {
            pmcID = Integer.parseInt(request.getParameter("pmcid"));
            appID = Integer.parseInt(request.getParameter("appid"));
        } catch (NumberFormatException ex) {
            logger.error("parseInt pmcID and appID  error: " + ex.toString());
        }
        switch (pmcID) {
            case -1:
                dic.showSection("notify");
                String msg = "Kênh thanh toán không hợp lệ, vui lòng chọn lại!";
                dic.setVariable("message", msg);
                dic.setVariable("transid", request.getParameter("transid"));
                SetValuesErrorForRedirectInformationNotify(request, dic, msg);
                break;
            case 1:  //ZingCard      
                renderCaseZingCard(request, dic, pmcID, appID);
                break;
            case 2: //Mobi 
            case 3: //Vina
            case 4: //Viettel
            case 12://Mobi PayDirect
            case 13://Vina PayDirect
            case 14://Viettel Paydirect
            case 15://Mobi VDC
            case 16://Vina VDC   
            case 17://Direct Viettel              
                renderCaseTelCoCard(request, dic, pmcID, appID);
                break;
//            case 7://Zingxu           
//                renderCaseZingXu(request, response,dic,pmcID, appID, info);
//                break;           
            case 18: //TKDienThoai Mobi          
                renderCaseTKDienThoaiMobi(request, dic, pmcID, appID);
                break;
            case 19:
            case 20:
            case 21:
                renderCase12PayVISAMASTER(request, dic, pmcID, appID);
                break;
//            case 22:
//                renderCaseThapPhongWallet(request, response,dic,pmcID, appID, info);
//                break; 
            default: {
                if (DbgFrontEndConfig.BankEntityMap.containsKey(pmcID)) {
                    renderCase12PayATM(request, dic, pmcID, appID);
                } else {
                    dic.showSection("notify");
                    String msg2 = "Kênh thanh toán này chưa được hỗ trợ, vui lòng quay lại sau!";
                    dic.setVariable("message", msg2);
                    dic.setVariable("transid", request.getParameter("transid"));
                    SetValuesErrorForRedirectInformationNotify(request, dic, msg2);
                }
                break;
            }
        }
        //dic.setVariable("STATUS_BAR", GetStep2BarHTML());
        dic.showSection("statusbar");
        dic.addSection("STEP2");
        if (pmcID != 7 && pmcID != 22)//ZingXu and ThapPhongWallet
        {
            SetValuesForRedirectInformation(request, dic);
        }
        //added by BANGDQ save BackButton
        SaveBackButtonHTML(request, dic);
        //added by BANGDQ for Special Layout 30/10/2014 13:20:10
        //SetSpecialLayOut(request, dic);        
        return template.renderToString(dic);
    }

    private void renderCaseZingCard(HttpServletRequest request, TemplateDataDictionary dic, int pmcID, int appID) {
        if (appID == -1) {
            dic.showSection("notify");
            String msg1 = "Thông tin ứng dụng không hợp lệ!";
            dic.setVariable("message", msg1);
            dic.setVariable("transid", request.getParameter("transid"));
            SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
        } else {

            dic.setVariable("PMC_CHARGE_HEADER", DbgFrontEndConfig.Get_pmc_chargemsg(pmcID));
            dic.showSection("chargecard");
            dic.showSection("charge");
            Map<Integer, PmcEntity> PmcEntityMap = DbgFrontEndConfig.GetPmcSupportByAppID(request.getParameter("appid"));
            PmcEntity pmcEntity = PmcEntityMap.get(pmcID);
            if (pmcEntity != null) {

                dic.setVariable("pmcname", pmcEntity.pmcDesc);
            }

            if (DbgFrontEndConfig.AppEntityMap.get(appID) != null) {
                dic.setVariable("appname", DbgFrontEndConfig.AppEntityMap.get(appID).appDesc);
            }
            dic.setVariable("transid", request.getParameter("transid"));
            dic.setVariable("appid", request.getParameter("appid"));
            dic.setVariable("appdata", request.getParameter("appdata"));
            dic.setVariable("pmcid", request.getParameter("pmcid"));
            if (GetBackButtonHTML(request)) {
                dic.addSection("BACK_BTN");
            }
            //added by BANGDQ auto input cardserial when input wrong
            SetCardSerialForWrongInput(request, dic, pmcID);

        }
    }

    private void renderCaseTelCoCard(HttpServletRequest request, TemplateDataDictionary dic, int pmcID, int appID) {
        if (appID == -1) {
            dic.showSection("notify");
            String msg1 = "Thông tin ứng dụng không hợp lệ!";
            dic.setVariable("message", msg1);
            dic.setVariable("transid", request.getParameter("transid"));
            SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
        } else {
            dic.setVariable("PMC_CHARGE_HEADER", DbgFrontEndConfig.Get_pmc_chargemsg(pmcID));
            dic.showSection("chargetelcocard");
            dic.showSection("charge");

            dic.setVariable("tco_patternCardCode", DbgFrontEndConfig.TelcoCardSerialValidatorPatterns.get(pmcID));
            dic.setVariable("tco_patternCardPassword", DbgFrontEndConfig.TelcoCardPasswordValidatorPatterns.get(pmcID));
            Map<Integer, PmcEntity> PmcEntityMap = DbgFrontEndConfig.GetPmcSupportByAppID(request.getParameter("appid"));
            PmcEntity pmcEntity = PmcEntityMap.get(pmcID);
            //if (DbgFrontEndConfig.PmcEntityMap.get(pmcID) != null)
            if (pmcEntity != null) {
                //dic.setVariable("pmcname", DbgFrontEndConfig.PmcEntityMap.get(pmcID).pmcDesc);
                dic.setVariable("pmcname", pmcEntity.pmcDesc);
            }
            if (DbgFrontEndConfig.AppEntityMap.get(appID) != null) {
                dic.setVariable("appname", DbgFrontEndConfig.AppEntityMap.get(appID).appDesc);
            }
            dic.setVariable("transid", request.getParameter("transid"));
            dic.setVariable("appid", request.getParameter("appid"));
            dic.setVariable("appdata", request.getParameter("appdata"));
            dic.setVariable("pmcid", request.getParameter("pmcid"));
            if (GetBackButtonHTML(request)) {
                dic.addSection("BACK_BTN");
            }
            //added by BANGDQ auto input cardserial when input wrong
            SetCardSerialForWrongInput(request, dic, pmcID);
        }
    }

//    private void renderCaseZingXu(HttpServletRequest request, HttpServletResponse response, TemplateDataDictionary dic, int pmcID, int appID,PaymentHidenInfo info ) throws ServletException, IOException, TemplateException, Exception
//    {
//        int vnd= 100;
//        if (appID == -1)
//        {
//            dic.showSection("notify");
//            String msg1 = "Thông tin ứng dụng không hợp lệ!";
//            dic.setVariable("message", msg1);
//            dic.setVariable("transid", request.getParameter("transid"));
//            SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
//        }
//        else
//        {
//            if(IsZaloApp(String.valueOf(appID)))
//            {
//                //Change for zalo to get info of wphone         
//                String  _n_platform = request.getParameter("pl") ;
//                if(_n_platform!=null && !_n_platform.isEmpty())
//                {
//                    if(_n_platform.equalsIgnoreCase("wphone") ||
//                         _n_platform.equalsIgnoreCase("wp"))        
//                    {
//                        _n_platform = "wp";
//                    }
//                }
//                RequestDispatcher rd = ContextHandler.getCurrentContext().getRequestDispatcher("/zalochargezingxu?"
//                        +"pl=" + _n_platform +  "&url_redirect=" + getRedirectURL(request) );                                    
//                if (rd != null)
//                {                            
//                    rd.forward(request, response);
//                }
//                else
//                {
//                    processRequest(request, response);
//                }                         
//
//            }
//            else
//            {
//
//                SSO3Account account = getSSO3Account(request);
//                info = getDefaultZingmePaymentHidenInfo(request, "");
//                info.pmcID= PMCIDEnum.ZING_XU.getValue();
//                if (DbgFrontEndConfig.PmcEntityMap.containsKey(pmcID)) {
//                    info.pmcName = DbgFrontEndConfig.PmcEntityMap.get(pmcID).pmcDesc;
//                }
//                storeZingMePaymentHidenInfo(request, "");
//                dic.setVariable("urlbase", DbgFrontEndConfig.SystemUrl);
//                //Set for add more ZingXu      
//
//                if (account != null)
//                {                        
//                   try
//                   {    //BANGDQ added for decypt appdata to check flow and chargeAmt to fill chargeAmt
//
//                        AppData _appdata = DecryptAppData(appID, info.appData);
//                        long zingxuamt = _appdata.chargeAmt /vnd;
//                        if(_appdata!=null)
//                        { 
//                            if(_appdata.flow == 2)
//                            {                                    
//                                if(zingxuamt > 0                                            
//                                        && zingxuamt <= DbgFrontEndConfig.MaxZingXu                                           
//                                        && (zingxuamt*vnd== _appdata.chargeAmt)
//                                        && zingxuamt >= DbgFrontEndConfig.MinZingXu)
//                                {                                    
//                                    BalanceResp balance = getZingXuBallance(account.userName);
//
//                                    dic.showSection("chargezingxu");                    
//                                    dic.showSection("charge");
//                                    dic.setVariable("_waitingToRedirect2AppInterval",String.valueOf(DbgFrontEndConfig.AutoRedirectMiliSeconds));
//                                    dic.setVariable("transid", info.transID);
//                                    dic.setVariable("appid", info.appID);
//                                    dic.setVariable("appdata", info.appData);
//                                    dic.setVariable("pmcid", String.valueOf(info.pmcID));
//                                    dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
//                                    dic.setVariable("accountname", account.userName);
//                                    dic.setVariable("pl", info.devicePlatform);
//                                    dic.setVariable("minzingxu", String.valueOf(DbgFrontEndConfig.MinZingXu));
//
//                                    if (balance != null)
//                                    {
//                                        dic.setVariable("xubalance", String.valueOf(balance.Cash));
//                                        dic.setVariable("fmxubalance", formatcomma(balance.Cash));
//                                    }
//                                    //BANGDQ added for decypt appdata to check flow and chargeAmt to fill chargeAmt
//                                    dic.setVariable("zingxuamt", formatcomma(zingxuamt));
//                                    dic.setVariable("disable", "readonly = \"readonly\"");
//
//                                    RequestDispatcher rd = ContextHandler.getCurrentContext().getRequestDispatcher("/zingxuconfirm?zingxuamt="
//                                            +formatcomma(zingxuamt)+"&pl=" + info.pl                                                                                                                   
//                                            +"&appserverid="+info.appServerID +"&appdata=" + info.appData
//                                            +"&url_redirect=" + getRedirectURL(request));
//
//                                    if (rd != null)
//                                    {
//
//                                        rd.forward(request, response);
//                                    }                                       
//
//                                }
//                                else
//                                {
//                                    dic.showSection("notify");
//                                    String msg1 = "Số lượng Zing Xu thanh toán không hợp lệ!";
//                                    dic.setVariable("message", msg1);
//                                    dic.setVariable("transid", request.getParameter("transid"));
//                                    SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
//
//                                }
//
//                            }else //Flow = 1
//                            {                   
//
//                                BalanceResp balance = getZingXuBallance(account.userName);
//                                dic.showSection("chargezingxu");                    
//                                dic.showSection("charge");
//                                dic.setVariable("_waitingToRedirect2AppInterval",String.valueOf(DbgFrontEndConfig.AutoRedirectMiliSeconds));
//                                dic.setVariable("transid", info.transID);
//                                dic.setVariable("appid", info.appID);
//                                dic.setVariable("appdata", info.appData);
//                                dic.setVariable("pmcid", String.valueOf(info.pmcID));
//                                dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
//                                dic.setVariable("accountname", account.userName);
//                                dic.setVariable("pl", info.devicePlatform);
//                                dic.setVariable("minzingxu", String.valueOf(DbgFrontEndConfig.MinZingXu));
//
//                                if (balance != null)
//                                {
//                                    dic.setVariable("xubalance", String.valueOf(balance.Cash));
//                                    dic.setVariable("fmxubalance", formatcomma(balance.Cash));
//                                }
//                                if(zingxuamt > 0 
//                                        && zingxuamt <= DbgFrontEndConfig.MaxZingXu 
//                                        && zingxuamt >= DbgFrontEndConfig.MinZingXu)
//                                {
//                                   //BANGDQ added for decypt appdata to check flow and chargeAmt to fill chargeAmt
//                                   dic.setVariable("zingxuamt", formatcomma(zingxuamt));
//                                }   
//                                 // For Add more xu
//                                dic.setVariable("zpv2url", DbgFrontEndConfig.AddMoreZingXuURL);      
//
//                                String cbString = String.format("%s/thanhtoan?transid=%s&pmcid=%s&appid=%s&_isdirect=%s",
//                                                DbgFrontEndConfig.SystemUrl, request.getParameter("transid"), 7, 
//                                                request.getParameter("appid"), GetIsDirectValue(request));
//                                 URLCodec _encode = new URLCodec();                                             
//                                String cbStringEncode =  _encode.encode(cbString);
//                                String zpv2params = String.format("acc=%s&cb=%s",account.userName,cbStringEncode);
//                                dic.setVariable("zpv2params", zpv2params);
//                            }
//                        }                             
//                        else
//                        {
//                            dic.showSection("notify");
//                            String msg1 = "Thông tin ứng dụng không hợp lệ!";
//                            dic.setVariable("message", msg1);
//                            dic.setVariable("transid", request.getParameter("transid"));
//                            SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
//                        }                          
//                    }
//                    catch (Exception ex)
//                    {
//                        dic.setVariable("xubalance", "0");
//                        dic.setVariable("fmxubalance", "0");
//                        logger.error("getBallance  error: " + ex.toString());
//                        //added when error get balance                            
//
//                        dic.showSection("notify");
//                        String msg2 = "Hệ thống đang có lỗi, vui lòng thực hiện giao dịch lại sau!";
//                        dic.setVariable("message", msg2);
//                        dic.setVariable("transid", request.getParameter("transid"));
//                        SetValuesErrorForRedirectInformationNotify(request, dic, msg2);
//
//
//                    }
//                }
//                else
//                {
//                    RequestDispatcher rd = ContextHandler.getCurrentContext().getRequestDispatcher("/loginzingme?"
//                        + "url_redirect=" + getRedirectURL(request));                                    
//                    if (rd != null)
//                    {                            
//                        rd.forward(request, response);
//                    }
//                    else
//                    {
//                        processRequest(request, response);
//                    }
//                }
//            }
//        }
//        
//        SetValuesZingXuForRedirectInformation(info, dic, String.valueOf(pmcID));
//        
//    }
    private void renderCaseTKDienThoaiMobi(HttpServletRequest request, TemplateDataDictionary dic, int pmcID, int appID) {
        if (appID == -1) {
            dic.showSection("notify");
            String msg1 = "Thông tin ứng dụng không hợp lệ!";
            dic.setVariable("message", msg1);
            dic.setVariable("transid", request.getParameter("transid"));
            SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
        } else {
            AppData _appdata = DecryptAppData(appID, request.getParameter("appdata"));
            if (_appdata != null) {
                if (_appdata.flow == 2) {
                    long chargeamt = _appdata.chargeAmt;
                    if (chargeamt > 0 && DbgFrontEndConfig.TKDienthoaiMobiChargeAmts.contains(String.valueOf(chargeamt))) {
                        dic.setVariable("PMC_CHARGE_HEADER", DbgFrontEndConfig.Get_pmc_chargemsg(pmcID));
                        dic.showSection("chargetkmobi");
                        dic.showSection("charge");
                        Map<Integer, PmcEntity> PmcEntityMap = DbgFrontEndConfig.GetPmcSupportByAppID(request.getParameter("appid"));
                        PmcEntity pmcEntity = PmcEntityMap.get(pmcID);

                        RenderChargeAmtSelectBox(dic, String.valueOf(chargeamt), true);

                        if (pmcEntity != null) {
                            dic.setVariable("pmcname", pmcEntity.pmcDesc);
                        }
                        if (DbgFrontEndConfig.AppEntityMap.get(appID) != null) {
                            dic.setVariable("appname", DbgFrontEndConfig.AppEntityMap.get(appID).appDesc);
                        }
                        dic.setVariable("transid", request.getParameter("transid"));
                        dic.setVariable("appid", request.getParameter("appid"));
                        dic.setVariable("appdata", request.getParameter("appdata"));
                        dic.setVariable("pmcid", request.getParameter("pmcid"));
                        //dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
                        if (GetBackButtonHTML(request)) {
                            dic.addSection("BACK_BTN");
                        }
                    } else {
                        dic.showSection("notify");
                        String msg1 = "Số tiền thanh toán không hợp lệ!";
                        dic.setVariable("message", msg1);
                        dic.setVariable("transid", request.getParameter("transid"));
                        SetValuesErrorForRedirectInformationNotify(request, dic, msg1);

                    }

                } else //Flow = 1
                {

                    dic.setVariable("PMC_CHARGE_HEADER", DbgFrontEndConfig.Get_pmc_chargemsg(pmcID));
                    dic.showSection("chargetkmobi");
                    dic.showSection("charge");
                    Map<Integer, PmcEntity> PmcEntityMap = DbgFrontEndConfig.GetPmcSupportByAppID(request.getParameter("appid"));
                    PmcEntity pmcEntity = PmcEntityMap.get(pmcID);

                    RenderChargeAmtSelectBox(dic, null, false);

                    if (pmcEntity != null) {
                        dic.setVariable("pmcname", pmcEntity.pmcDesc);
                    }
                    if (DbgFrontEndConfig.AppEntityMap.get(appID) != null) {
                        dic.setVariable("appname", DbgFrontEndConfig.AppEntityMap.get(appID).appDesc);
                    }
                    dic.setVariable("transid", request.getParameter("transid"));
                    dic.setVariable("appid", request.getParameter("appid"));
                    dic.setVariable("appdata", request.getParameter("appdata"));
                    dic.setVariable("pmcid", request.getParameter("pmcid"));
                    //dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
                    if (GetBackButtonHTML(request)) {
                        dic.addSection("BACK_BTN");
                    }
                }

            } else {
                dic.showSection("notify");
                String msg1 = "Thông tin ứng dụng không hợp lệ!";
                dic.setVariable("message", msg1);
                dic.setVariable("transid", request.getParameter("transid"));
                SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
            }
        }
    }

    private void renderCase12PayATM(HttpServletRequest request, TemplateDataDictionary dic, int pmcID, int appID) {
        //123Pay Banking

        dic.setVariable("chargeamtcalculated", "0");
        AppData _appdata = DecryptAppData(appID, request.getParameter("appdata"));
        if (_appdata != null) {
            if (_appdata.flow == 2) {

                if (_appdata.chargeAmt > 0 && (_appdata.chargeAmt % DbgFrontEndConfig.ATMChargeAmtMod == 0)
                        && _appdata.chargeAmt >= DbgFrontEndConfig.MinAtmMoney
                        && _appdata.chargeAmt <= DbgFrontEndConfig.MaxAtmMoney) {
                    dic.showSection("chargeatm");
                    dic.showSection("charge");
                    dic.setVariable("chargeamtcalculated", "0");
                    dic.setVariable("chargeamt", formatcomma(_appdata.chargeAmt));

                    if (DbgFrontEndConfig.BankEntityMap.get(pmcID) != null) {
                        dic.setVariable("bankname", DbgFrontEndConfig.BankEntityMap.get(pmcID).mobiname);
                        dic.setVariable("banklogo", DbgFrontEndConfig.BankEntityMap.get(pmcID).logoUrlTouch);
                    }
                    if (DbgFrontEndConfig.AppEntityMap.get(appID) != null) {
                        dic.setVariable("appname", DbgFrontEndConfig.AppEntityMap.get(appID).appDesc);
                        //   CustomPMCCostRateEnum appCostRate = CustomPMCCostRateEnum.fromInt(
                        //          DbgFrontEndConfig.AppEntityMap.get(appID).isCustomPmcCostRate);
                        String StrisCustomPmcCostRate = String.valueOf(DbgFrontEndConfig.AppEntityMap.get(appID).isCustomPmcCostRate);
                        if (DbgFrontEndConfig.ATMDiscount.containsKey(StrisCustomPmcCostRate))//Apps that have discount percent config
                        {
                            ///subtract charge AMT before submit   
                            double discountpercent = Double.valueOf(DbgFrontEndConfig.ATMDiscount.get(StrisCustomPmcCostRate));

                            dic.setVariable("discount_rate", String.valueOf(discountpercent));
                            long realmoney = (long) (_appdata.chargeAmt * (100 - discountpercent) / 100);
                            dic.setVariable("chargeamtcalculated", String.valueOf(realmoney));
                            dic.setVariable("chargeamterror", "Giảm " + discountpercent + " %. Bạn chỉ phải trả " + formatcomma(realmoney) + " VNĐ");
                        } else //Ko giam gia doi voi appCostRate!=INTERNAL
                        {   ///subtract charge AMT before submit                            
                            dic.setVariable("discount_rate", "0");
                            long realmoney = (_appdata.chargeAmt);
                            dic.setVariable("chargeamtcalculated", String.valueOf(realmoney));
                        }
                    }
                    dic.setVariable("transid", request.getParameter("transid"));
                    dic.setVariable("appid", request.getParameter("appid"));
                    dic.setVariable("appdata", request.getParameter("appdata"));
                    dic.setVariable("pmcid", request.getParameter("pmcid"));
                    dic.setVariable("minatmmoney", String.valueOf(DbgFrontEndConfig.MinAtmMoney));
                    dic.setVariable("maxatmmoney", String.valueOf(DbgFrontEndConfig.MaxAtmMoney));

                    //dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
                    if (GetBackButtonHTML(request)) {
                        dic.addSection("BACK_BTN");
                    }

                    dic.setVariable("disable", "readonly = \"readonly\"");

                } else {
                    //Ngoài giới hạn thanh toán / giao dịch
                    String msg1 = "Ngoài giới hạn thanh toán / giao dịch!";
                    if (_appdata.chargeAmt % DbgFrontEndConfig.ATMChargeAmtMod != 0) {
                        msg1 = "Số tiền phải là bội số của 10,000";
                    } else if (_appdata.chargeAmt < DbgFrontEndConfig.MinAtmMoney) {
                        msg1 = "Số tiền phải lớn hơn hoặc bằng " + formatcomma(DbgFrontEndConfig.MinAtmMoney) + " VNĐ";
                    } else if (_appdata.chargeAmt > DbgFrontEndConfig.MaxAtmMoney) {
                        msg1 = "Số tiền phải nhỏ hơn hoặc bằng " + formatcomma(DbgFrontEndConfig.MaxAtmMoney) + " VNĐ";
                    }

                    dic.showSection("notify");

                    dic.setVariable("message", msg1);
                    dic.setVariable("transid", request.getParameter("transid"));
                    SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
                }
            } else //Flow = 1                            
            {
                dic.showSection("chargeatm");
                dic.showSection("charge");
                dic.setVariable("chargeamtcalculated", "0");
                if (DbgFrontEndConfig.BankEntityMap.get(pmcID) != null) {
                    dic.setVariable("bankname", DbgFrontEndConfig.BankEntityMap.get(pmcID).mobiname);
                    dic.setVariable("banklogo", DbgFrontEndConfig.BankEntityMap.get(pmcID).logoUrlTouch);
                }
                if (DbgFrontEndConfig.AppEntityMap.get(appID) != null) {
                    dic.setVariable("appname", DbgFrontEndConfig.AppEntityMap.get(appID).appDesc);
                    //     CustomPMCCostRateEnum appCostRate = CustomPMCCostRateEnum.fromInt(
                    //             DbgFrontEndConfig.AppEntityMap.get(appID).isCustomPmcCostRate);

                    String StrisCustomPmcCostRate = String.valueOf(DbgFrontEndConfig.AppEntityMap.get(appID).isCustomPmcCostRate);
                    if (DbgFrontEndConfig.ATMDiscount.containsKey(StrisCustomPmcCostRate))//Apps that have discount percent config
                    {
                        ///subtract charge AMT before submit  
                        double discountpercent = Double.valueOf(DbgFrontEndConfig.ATMDiscount.get(StrisCustomPmcCostRate));
                        dic.setVariable("discount_rate", String.valueOf(discountpercent));

                        if (_appdata.chargeAmt > 0 && (_appdata.chargeAmt % DbgFrontEndConfig.ATMChargeAmtMod == 0)
                                && _appdata.chargeAmt >= DbgFrontEndConfig.MinAtmMoney
                                && _appdata.chargeAmt <= DbgFrontEndConfig.MaxAtmMoney) {
                            long realmoney = (long) (_appdata.chargeAmt * (100 - discountpercent) / 100);
                            dic.setVariable("chargeamtcalculated", String.valueOf(realmoney));
                            dic.setVariable("chargeamterror", "Giảm " + discountpercent + " %. Bạn chỉ phải trả " + formatcomma(realmoney) + " VNĐ");
                        }
                    } else //Ko giam gia doi voi appCostRate!=INTERNAL
                    {   ///subtract charge AMT before submit                            
                        dic.setVariable("discount_rate", "0");
                        if (_appdata.chargeAmt > 0
                                && _appdata.chargeAmt >= DbgFrontEndConfig.MinAtmMoney
                                && _appdata.chargeAmt <= DbgFrontEndConfig.MaxAtmMoney) {
                            long realmoney = (_appdata.chargeAmt);
                            dic.setVariable("chargeamtcalculated", String.valueOf(realmoney));
                        }
                    }
                }
                dic.setVariable("transid", request.getParameter("transid"));
                dic.setVariable("appid", request.getParameter("appid"));
                dic.setVariable("appdata", request.getParameter("appdata"));
                dic.setVariable("pmcid", request.getParameter("pmcid"));
                dic.setVariable("minatmmoney", String.valueOf(DbgFrontEndConfig.MinAtmMoney));
                dic.setVariable("maxatmmoney", String.valueOf(DbgFrontEndConfig.MaxAtmMoney));

                //dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
                if (GetBackButtonHTML(request)) {
                    dic.addSection("BACK_BTN");
                }

                if (_appdata.chargeAmt > 0 && (_appdata.chargeAmt % DbgFrontEndConfig.ATMChargeAmtMod == 0)
                        && _appdata.chargeAmt >= DbgFrontEndConfig.MinAtmMoney
                        && _appdata.chargeAmt <= DbgFrontEndConfig.MaxAtmMoney) {
                    //BANGDQ added for decypt appdata to check flow and chargeAmt to fill chargeAmt
                    dic.setVariable("chargeamt", formatcomma(_appdata.chargeAmt));
                }
            }
        } else {
            dic.showSection("notify");
            String msg1 = "Thông tin ứng dụng không hợp lệ!";
            dic.setVariable("message", msg1);
            dic.setVariable("transid", request.getParameter("transid"));
            SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
        }

    }

    private void renderCase12PayVISAMASTER(HttpServletRequest request, TemplateDataDictionary dic, int pmcID, int appID) {
        //123Pay VISA MASTER

        dic.setVariable("chargeamtcalculated", "0");
        AppData _appdata = DecryptAppData(appID, request.getParameter("appdata"));
        if (_appdata != null) {
            if (_appdata.flow == 2) {

                if (_appdata.chargeAmt > 0 && (_appdata.chargeAmt % DbgFrontEndConfig.ATMChargeAmtMod == 0)
                        && _appdata.chargeAmt >= getVisaMasterJCBMinMoney(pmcID)
                        && _appdata.chargeAmt <= DbgFrontEndConfig.MaxAtmMoney) {
                    dic.showSection("chargeatm");
                    dic.showSection("charge");
                    dic.setVariable("chargeamtcalculated", "0");
                    dic.setVariable("chargeamt", formatcomma(_appdata.chargeAmt));

                    if (pmcID == PMCIDEnum.VISA_123PAY.getValue()) {
                        dic.setVariable("bankname", "Visa Card");
                        dic.setVariable("banklogo", "credit visa");
                    } else if (pmcID == PMCIDEnum.MASTER_123PAY.getValue()) {
                        dic.setVariable("bankname", "Master Card");
                        dic.setVariable("banklogo", "credit mastercard");
                    } else if (pmcID == PMCIDEnum.JCB_123PAY.getValue()) {
                        dic.setVariable("bankname", "JCB Card");
                        dic.setVariable("banklogo", "credit jcb");
                    }

                    if (DbgFrontEndConfig.AppEntityMap.get(appID) != null) {

                        dic.setVariable("appname", DbgFrontEndConfig.AppEntityMap.get(appID).appDesc);
                        //  CustomPMCCostRateEnum appCostRate = CustomPMCCostRateEnum.fromInt(
                        //         DbgFrontEndConfig.AppEntityMap.get(appID).isCustomPmcCostRate);
                        String keyCostRate = String.format("%s%s", String.valueOf(pmcID), String.valueOf(DbgFrontEndConfig.AppEntityMap.get(appID).isCustomPmcCostRate));
                        if (DbgFrontEndConfig.VisaMasterDiscount.containsKey(keyCostRate))//Apps of VNG
                        {
                            ///subtract charge AMT before submit     
                            double discountpercent = new Double(DbgFrontEndConfig.VisaMasterDiscount.get(keyCostRate));
                            dic.setVariable("discount_rate", String.valueOf(discountpercent));
                            long realmoney = (long) (_appdata.chargeAmt * (100 - discountpercent) / 100);
                            dic.setVariable("chargeamtcalculated", String.valueOf(realmoney));
                            if (discountpercent > 0) {
                                dic.setVariable("chargeamterror", "Giảm " + discountpercent + " %. Bạn chỉ phải trả " + formatcomma(realmoney) + " VNĐ");
                            }
                        } else //Ko giam gia doi voi appCostRate!=INTERNAL
                        {   ///subtract charge AMT before submit                            
                            dic.setVariable("discount_rate", "0");
                            long realmoney = (_appdata.chargeAmt);
                            dic.setVariable("chargeamtcalculated", String.valueOf(realmoney));
                        }
                    }
                    dic.setVariable("transid", request.getParameter("transid"));
                    dic.setVariable("appid", request.getParameter("appid"));
                    dic.setVariable("appdata", request.getParameter("appdata"));
                    dic.setVariable("pmcid", request.getParameter("pmcid"));
                    dic.setVariable("minatmmoney", String.valueOf(getVisaMasterJCBMinMoney(pmcID)));
                    dic.setVariable("maxatmmoney", String.valueOf(DbgFrontEndConfig.MaxAtmMoney));

                    //dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
                    if (GetBackButtonHTML(request)) {
                        dic.addSection("BACK_BTN");
                    }
                    dic.setVariable("disable", "readonly = \"readonly\"");

                } else {
                    //Ngoài giới hạn thanh toán / giao dịch
                    String msg1 = "Ngoài giới hạn thanh toán / giao dịch!";
                    if (_appdata.chargeAmt % DbgFrontEndConfig.ATMChargeAmtMod != 0) {
                        msg1 = "Số tiền phải là bội số của 10,000";
                    } else if (_appdata.chargeAmt < getVisaMasterJCBMinMoney(pmcID)) {
                        msg1 = "Số tiền phải lớn hơn hoặc bằng " + formatcomma(getVisaMasterJCBMinMoney(pmcID)) + " VNĐ";
                    } else if (_appdata.chargeAmt > DbgFrontEndConfig.MaxAtmMoney) {
                        msg1 = "Số tiền phải nhỏ hơn hoặc bằng " + formatcomma(DbgFrontEndConfig.MaxAtmMoney) + " VNĐ";
                    }

                    dic.showSection("notify");
                    dic.setVariable("message", msg1);
                    dic.setVariable("transid", request.getParameter("transid"));
                    SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
                }
            } else //Flow = 1                            
            {
                dic.showSection("chargeatm");
                dic.showSection("charge");
                dic.setVariable("chargeamtcalculated", "0");

                if (pmcID == PMCIDEnum.VISA_123PAY.getValue()) {
                    dic.setVariable("bankname", "Visa Card");
                    dic.setVariable("banklogo", "credit visa");
                } else if (pmcID == PMCIDEnum.MASTER_123PAY.getValue()) {
                    dic.setVariable("bankname", "Master Card");
                    dic.setVariable("banklogo", "credit mastercard");
                } else if (pmcID == PMCIDEnum.JCB_123PAY.getValue()) {
                    dic.setVariable("bankname", "JCB Card");
                    dic.setVariable("banklogo", "credit jcb");
                }

                if (DbgFrontEndConfig.AppEntityMap.get(appID) != null) {
                    dic.setVariable("appname", DbgFrontEndConfig.AppEntityMap.get(appID).appDesc);
                    //   CustomPMCCostRateEnum appCostRate = CustomPMCCostRateEnum.fromInt(
                    //            DbgFrontEndConfig.AppEntityMap.get(appID).isCustomPmcCostRate);
                    String keyCostRate = String.format("%s%s", String.valueOf(pmcID), String.valueOf(DbgFrontEndConfig.AppEntityMap.get(appID).isCustomPmcCostRate));
                    if (DbgFrontEndConfig.VisaMasterDiscount.containsKey(keyCostRate))//Apps of VNG
                    {
                        ///subtract charge AMT before submit     
                        double discountpercent = new Double(DbgFrontEndConfig.VisaMasterDiscount.get(keyCostRate));
                        dic.setVariable("discount_rate", String.valueOf(discountpercent));

                        if (_appdata.chargeAmt > 0 && (_appdata.chargeAmt % DbgFrontEndConfig.ATMChargeAmtMod == 0)
                                && _appdata.chargeAmt >= getVisaMasterJCBMinMoney(pmcID)
                                && _appdata.chargeAmt <= DbgFrontEndConfig.MaxAtmMoney) {

                            long realmoney = (long) (_appdata.chargeAmt * (100 - discountpercent) / 100);
                            dic.setVariable("chargeamtcalculated", String.valueOf(realmoney));
                            if (discountpercent > 0) {
                                dic.setVariable("chargeamterror", "Giảm " + discountpercent + " %. Bạn chỉ phải trả " + formatcomma(realmoney) + " VNĐ");
                            }
                        }
                    } else //Ko giam gia doi voi appCostRate!=INTERNAL
                    {   ///subtract charge AMT before submit                            
                        dic.setVariable("discount_rate", "0");
                        if (_appdata.chargeAmt > 0
                                && _appdata.chargeAmt >= getVisaMasterJCBMinMoney(pmcID)
                                && _appdata.chargeAmt <= DbgFrontEndConfig.MaxAtmMoney) {
                            long realmoney = (_appdata.chargeAmt);
                            dic.setVariable("chargeamtcalculated", String.valueOf(realmoney));
                        }
                    }
                }
                dic.setVariable("transid", request.getParameter("transid"));
                dic.setVariable("appid", request.getParameter("appid"));
                dic.setVariable("appdata", request.getParameter("appdata"));
                dic.setVariable("pmcid", request.getParameter("pmcid"));
                dic.setVariable("minatmmoney", String.valueOf(getVisaMasterJCBMinMoney(pmcID)));
                dic.setVariable("maxatmmoney", String.valueOf(DbgFrontEndConfig.MaxAtmMoney));

                //dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
                if (GetBackButtonHTML(request)) {
                    dic.addSection("BACK_BTN");
                }

                if (_appdata.chargeAmt > 0 && (_appdata.chargeAmt % DbgFrontEndConfig.ATMChargeAmtMod == 0)
                        && _appdata.chargeAmt >= getVisaMasterJCBMinMoney(pmcID)
                        && _appdata.chargeAmt <= DbgFrontEndConfig.MaxAtmMoney) {
                    //BANGDQ added for decypt appdata to check flow and chargeAmt to fill chargeAmt
                    dic.setVariable("chargeamt", formatcomma(_appdata.chargeAmt));
                }
            }
        } else {
            dic.showSection("notify");
            String msg1 = "Thông tin ứng dụng không hợp lệ!";
            dic.setVariable("message", msg1);
            dic.setVariable("transid", request.getParameter("transid"));
            SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
        }

    }

//    private void renderCaseThapPhongWallet(HttpServletRequest request, HttpServletResponse response, TemplateDataDictionary dic, int pmcID, int appID,PaymentHidenInfo info ) throws ServletException, IOException, TemplateException, Exception
//    {
//        int vnd = 100;
//        
//        if (appID == -1)
//        {
//            dic.showSection("notify");
//            String msg1 = "Thông tin ứng dụng không hợp lệ!";
//            dic.setVariable("message", msg1);
//            dic.setVariable("transid", request.getParameter("transid"));
//            SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
//        }
//        else
//        {      
//
//            SSO3Account account = getSSO3Account(request);
//            info = getDefaultZingmePaymentHidenInfo(request, "");
//            info.pmcID= PMCIDEnum.ThapPhong_Wallet.getValue();
//            if (DbgFrontEndConfig.PmcEntityMap.containsKey(pmcID)) {
//                info.pmcName = DbgFrontEndConfig.PmcEntityMap.get(pmcID).pmcDesc;
//            }
//            storeZingMePaymentHidenInfo(request, "");
//            dic.setVariable("urlbase", DbgFrontEndConfig.SystemUrl);
//            //Set for add more ZingXu      
//
//            if (account != null)
//            {                        
//               try
//               {    //BANGDQ added for decypt appdata to check flow and chargeAmt to fill chargeAmt
//
//                    AppData _appdata = DecryptAppData(appID, info.appData);
//                    long thapphongxuamt = _appdata.chargeAmt /vnd;
//                    if(_appdata!=null)
//                    { 
//                        if(_appdata.flow == 2)
//                        {                                    
//                            if(thapphongxuamt > 0                                            
//                                    && thapphongxuamt <= DbgFrontEndConfig.ThapPhongMaxXu                                           
//                                    && (thapphongxuamt*vnd== _appdata.chargeAmt)
//                                    && thapphongxuamt >= DbgFrontEndConfig.ThapPhongMinXu)
//                            {                                    
//                                ThapPhongBalanceResp balance = getThapPhongBallance(account.userName);
//
//                                dic.showSection("chargethapphong");                    
//                                dic.showSection("charge");
//                                dic.setVariable("_waitingToRedirect2AppInterval",String.valueOf(DbgFrontEndConfig.AutoRedirectMiliSeconds));
//                                dic.setVariable("transid", info.transID);
//                                dic.setVariable("appid", info.appID);
//                                dic.setVariable("appdata", info.appData);
//                                dic.setVariable("pmcid", String.valueOf(info.pmcID));
//                                dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
//                                dic.setVariable("accountname", account.userName);
//                                dic.setVariable("pl", info.devicePlatform);
//                                dic.setVariable("minthapphongxu", String.valueOf(DbgFrontEndConfig.ThapPhongMinXu));
//
//                                if (balance != null)
//                                {
//                                    dic.setVariable("thapphongbalance", String.valueOf(balance.Balance));
//                                    dic.setVariable("fmthapphongbalance", formatcomma(balance.Balance));
//                                }
//                                //BANGDQ added for decypt appdata to check flow and chargeAmt to fill chargeAmt
//                                dic.setVariable("thapphongxuamt", formatcomma(thapphongxuamt));
//                                dic.setVariable("disable", "readonly = \"readonly\"");
//
//                                RequestDispatcher rd = ContextHandler.getCurrentContext().getRequestDispatcher("/thapphongconfirm?thapphongxuamt="
//                                        +formatcomma(thapphongxuamt)+"&pl=" + info.pl                                                                                                                   
//                                        +"&appserverid="+info.appServerID +"&appdata=" + info.appData
//                                        +"&url_redirect=" + getRedirectURL(request));
//
//                                if (rd != null)
//                                {
//
//                                    rd.forward(request, response);
//                                }                                       
//
//                            }
//                            else
//                            {
//                                dic.showSection("notify");
//                                String msg1 = "Số lượng Xu thanh toán không hợp lệ!";
//                                dic.setVariable("message", msg1);
//                                dic.setVariable("transid", request.getParameter("transid"));
//                                SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
//
//                            }
//
//                        }else //Flow = 1
//                        {                   
//
//                            ThapPhongBalanceResp balance = getThapPhongBallance(account.userName);
//                            dic.showSection("chargethapphong");                    
//                            dic.showSection("charge");
//                            dic.setVariable("_waitingToRedirect2AppInterval",String.valueOf(DbgFrontEndConfig.AutoRedirectMiliSeconds));
//                            dic.setVariable("transid", info.transID);
//                            dic.setVariable("appid", info.appID);
//                            dic.setVariable("appdata", info.appData);
//                            dic.setVariable("pmcid", String.valueOf(info.pmcID));
//                            dic.setVariable("BACK_BTN", GetBackButtonHTML(request));
//                            dic.setVariable("accountname", account.userName);
//                            dic.setVariable("pl", info.devicePlatform);
//                            dic.setVariable("minthapphongxu", String.valueOf(DbgFrontEndConfig.ThapPhongMinXu));
//
//                            if (balance != null)
//                            {
//                                dic.setVariable("thapphongbalance", String.valueOf(balance.Balance));
//                                dic.setVariable("fmthapphongbalance", formatcomma(balance.Balance));
//                            }
//                            if(thapphongxuamt > 0 
//                                    && thapphongxuamt <= DbgFrontEndConfig.ThapPhongMaxXu 
//                                    && thapphongxuamt >= DbgFrontEndConfig.ThapPhongMinXu)
//                            {
//                               //BANGDQ added for decypt appdata to check flow and chargeAmt to fill chargeAmt
//                               dic.setVariable("thapphongxuamt", formatcomma(thapphongxuamt));
//                            }   
//                             // For Add more xu
//                            dic.setVariable("tpmorexuurl", DbgFrontEndConfig.ThapPhongMoreXuURL);      
//
//                            String cbString = String.format("%s/thanhtoan?transid=%s&pmcid=%s&appid=%s&_isdirect=%s",
//                                            DbgFrontEndConfig.SystemUrl, request.getParameter("transid"), 22, 
//                                            request.getParameter("appid"), GetIsDirectValue(request));
//                             URLCodec _encode = new URLCodec();                                             
//                            String cbStringEncode =  _encode.encode(cbString);
//                            String tpmorexuparams = String.format("acc=%s&cb=%s",account.userName,cbStringEncode);
//                            dic.setVariable("tpmorexuparams", tpmorexuparams);
//                        }
//                    }                             
//                    else
//                    {
//                        dic.showSection("notify");
//                        String msg1 = "Thông tin ứng dụng không hợp lệ!";
//                        dic.setVariable("message", msg1);
//                        dic.setVariable("transid", request.getParameter("transid"));
//                        SetValuesErrorForRedirectInformationNotify(request, dic, msg1);
//                    }                          
//                }
//                catch (Exception ex)
//                {
//                    dic.setVariable("thapphongbalance", "0");
//                    dic.setVariable("fmthapphongbalance", "0");
//                    logger.error("getBallance  error: " + ex.toString());
//                    //added when error get balance                            
//
//                    dic.showSection("notify");
//                    String msg2 = "Hệ thống đang có lỗi, vui lòng thực hiện giao dịch lại sau!";
//                    dic.setVariable("message", msg2);
//                    dic.setVariable("transid", request.getParameter("transid"));
//                    SetValuesErrorForRedirectInformationNotify(request, dic, msg2);
//
//
//                }
//            }
//            else
//            {
//                RequestDispatcher rd = ContextHandler.getCurrentContext().getRequestDispatcher("/loginzingme?"
//                    + "url_redirect=" + getRedirectURL(request));                                    
//                if (rd != null)
//                {                            
//                    rd.forward(request, response);
//                }
//                else
//                {
//                    processRequest(request, response);
//                }
//            }
//            
//        }
//        
//        SetValuesZingXuForRedirectInformation(info, dic, String.valueOf(pmcID));
//        
//    }   
//   
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
        dic.setVariable("apptransid", request.getParameter("apptransid"));

        dic.setVariable("_n_pmc", request.getParameter("pmcid"));
        dic.setVariable("_n_grossamount", "");
        dic.setVariable("appid", request.getParameter("appid"));

    }

    private void SetValuesZingXuForRedirectInformation(PaymentHidenInfo info, TemplateDataDictionary dic, String pmcid) {

        String strAppID = info.appID;
        String strAppServerID = info.appServerID;
        String key = DbgFrontEndConfig.CreateAppServerKey(strAppServerID, strAppID);
        String url = info.redirectUrl;
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
        dic.setVariable("_n_tranxid", info.transID);
        dic.setVariable("_n_state", "billing");
        dic.setVariable("_n_apptranxid", info.appTransID);
        dic.setVariable("_n_platform", info.pl);
        dic.setVariable("_n_netamount", "");
        dic.setVariable("appserverid", info.appServerID);
        dic.setVariable("apptransid", info.appTransID);
        dic.setVariable("_n_pmc", pmcid);
        dic.setVariable("appid", info.appID);

        dic.setVariable("_n_grossamount", "");
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

    private String renderPreCheckDataErrorByTemplate(HttpServletRequest request, String strError, String errorCode) throws TemplateException {

        TemplateLoader templateLoader = TemplateResourceLoader.create("view/");
        Template template = templateLoader.getTemplate("master");
        TemplateDataDictionary dic = TemplateDictionary.create();
        dic.setVariable("PAYTITLE", DbgFrontEndConfig.MasterFormTitle);
        dic.setVariable("PAYURL", DbgFrontEndConfig.SystemUrl);
        dic.setVariable("STATIC_URL", DbgFrontEndConfig.StaticContentUrl);
        dic.setVariable("SYSTEM_CREDITS_URL", DbgFrontEndConfig.SystemCreditsUrl);
        dic.setVariable("apptransid", request.getParameter("apptransid"));

        //dic.setVariable("STATUS_BAR", GetStep2BarHTML());
        dic.showSection("statusbar");
        dic.addSection("STEP2");
        dic.showSection("notify");

        dic.setVariable("message", strError);
        dic.setVariable("transid", request.getParameter("transid"));
        dic.setVariable("appid", request.getParameter("appid"));

        SetValuesForRedirectInformation(request, dic);
        dic.setVariable("_n_error_code", errorCode);
        dic.setVariable("_n_error_msg", strError);

        //added by BANGDQ for Special Layout 30/10/2014 13:20:10
        //SetSpecialLayOut(request, dic);
        return template.renderToString(dic);
    }

    private void SetCardSerialForWrongInput(HttpServletRequest request, TemplateDataDictionary dic, int pmcID) {
        SessionCard cardEntity = GetStoreCardSerial(request);
        if (cardEntity != null && cardEntity.pmcID.equalsIgnoreCase(String.valueOf(pmcID))) {
            dic.setVariable("cardserial", cardEntity.CardSerial);
        }
    }

    private String getRedirectURL(HttpServletRequest request) {

        String strAppID = request.getParameter("appid");
        String strAppServerID = request.getParameter("appserverid");
        String key = DbgFrontEndConfig.CreateAppServerKey(strAppServerID, strAppID);

        String url = request.getParameter("url_redirect");
        if (url != null && !url.trim().equals("") && !url.trim().equals("#")) {
            return url;
        } else {
            if (DbgFrontEndConfig.AppServerEntityMap.containsKey(key)) {
                MiniAppServerEntity entity = DbgFrontEndConfig.AppServerEntityMap.get(key);
                if (entity != null) {
                    url = entity.appRedirectUrl;
                }
            } else {
                url = "";
            }
        }
        return url;
    }

    private void RenderChargeAmtSelectBox(TemplateDataDictionary dic, String chargeAmt, boolean isReadOnly) {

        dic.setVariable("patternphonenumber", DbgFrontEndConfig.TKMobiPatternValidate);

        if (!DbgFrontEndConfig.TKDienthoaiMobiChargeAmts.isEmpty()) {
            for (int i = 0; i < DbgFrontEndConfig.TKDienthoaiMobiChargeAmts.size(); i++) {

                String value = DbgFrontEndConfig.TKDienthoaiMobiChargeAmts.get(i);
                if (!isReadOnly) {
                    TemplateDataDictionary itemGrpDic = dic.addSection("SELECTAMT");
                    if (chargeAmt != null && chargeAmt.equals(value)) {

                        itemGrpDic.setVariable("selected", "selected");

                    }
                    itemGrpDic.setVariable("keyamt", value);
                    itemGrpDic.setVariable("valueamt", formatcomma(value));
                } else {
                    if (chargeAmt != null && chargeAmt.equals(value)) {
                        TemplateDataDictionary itemGrpDic = dic.addSection("SELECTAMT");
                        itemGrpDic.setVariable("selected", "selected");
                        itemGrpDic.setVariable("keyamt", value);
                        itemGrpDic.setVariable("valueamt", formatcomma(value));

                    }
                }

            }

        }

    }

}
