package com.titanjr.checkstand.controller;

import com.fangcang.titanjr.common.bean.ValidateResponse;
import com.fangcang.titanjr.common.enums.OrderStatusEnum;
import com.fangcang.titanjr.common.util.CommonConstant;
import com.fangcang.titanjr.common.util.GenericValidate;
import com.fangcang.titanjr.common.util.Wxutil;
import com.fangcang.titanjr.dto.bean.PayMethodConfigDTO;
import com.fangcang.titanjr.dto.bean.SysConfig;
import com.fangcang.titanjr.dto.bean.TransOrderDTO;
import com.fangcang.titanjr.dto.request.TransOrderRequest;
import com.fangcang.titanjr.service.TitanFinancialUtilService;
import com.fangcang.titanjr.service.TitanOrderService;
import com.fangcang.util.StringUtil;
import com.titanjr.checkstand.constants.PayTypeEnum;
import com.titanjr.checkstand.constants.RSErrorCodeEnum;
import com.titanjr.checkstand.constants.RequestTypeEnum;
import com.titanjr.checkstand.constants.SysConstant;
import com.titanjr.checkstand.dto.GateWayConfigDTO;
import com.titanjr.checkstand.dto.TitanPayDTO;
import com.titanjr.checkstand.request.RBQuickPayRequest;
import com.titanjr.checkstand.request.TLNetBankPayRequest;
import com.titanjr.checkstand.request.TLQrCodePayRequest;
import com.titanjr.checkstand.respnse.TitanQrCodePayResponse;
import com.titanjr.checkstand.respnse.TitanQuickPayResponse;
import com.titanjr.checkstand.service.RBQuickPayService;
import com.titanjr.checkstand.service.TLPaymentService;
import com.titanjr.checkstand.strategy.StrategyFactory;
import com.titanjr.checkstand.strategy.pay.PayRequestStrategy;
import com.titanjr.checkstand.util.CommonUtil;
import com.titanjr.checkstand.util.SignMsgBuilder;
import com.titanjr.checkstand.util.WebUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhaoshan on 2017/10/19.
 */
@Controller
@RequestMapping(value = "/pay")
public class PaymentController extends BaseController {
    /** 
	 * 
	 */
	private static final long serialVersionUID = 2974835411917209774L;
	private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
	
	@Resource
    private TLPaymentService tlPaymentService;
	
	@Resource
	private RBQuickPayService rbQuickPayService;
	
	@Resource
	private TitanOrderService titanOrderService;
	
	@Resource 
	private TitanFinancialUtilService titanFinancialUtilService;
	

    /**
     * 所有支付入口，进入后分配到具体的支付接口返回不同的参数
     * @param request
     * @param attr
     * @throws Exception
     */
    @RequestMapping(value = "/entrance", method = {RequestMethod.GET, RequestMethod.POST})
    public String entrance(HttpServletRequest request, RedirectAttributes attr, Model model) {
        
        try {
        	
			PayTypeEnum payTypeEnum = PayTypeEnum.getPayTypeEnum(request.getParameter("payType"));
			if(payTypeEnum == null){
				logger.error("【支付】参数错误，未找到对应的支付方式，payType={}", request.getParameter("payType"));
				return super.payFailedCallback(model);
			}
			
			//根据支付方式来判定走到具体哪个接口
			PayRequestStrategy payRequestStrategy =  StrategyFactory.getPayRequestStrategy(payTypeEnum);
			if(payRequestStrategy == null){
				logger.error("【支付】失败，获取相应的支付策略为空");
				return super.payFailedCallback(model);
			}
			
			String redirectUrl = payRequestStrategy.redirectResult(request);
			super.resetParameter(request,attr);
			
			return "forward:" + redirectUrl;
			
		} catch (Exception e) {
			logger.error("【checkstand支付】异常：", e);
			return super.payFailedCallback(model);
			
		}
        
    }
    
    
    /**
     * 网银支付
     * @author Jerry
     * @date 2017年11月27日 上午11:03:53
     */
    @RequestMapping(value = "/netBankPay", method = {RequestMethod.GET, RequestMethod.POST})
    public String netBankPay(HttpServletRequest request, Model model) {
    	
        try {
        	
			TitanPayDTO payDTO = WebUtils.switch2RequestDTO(TitanPayDTO.class, request);
			ValidateResponse res = GenericValidate.validateNew(payDTO);
			if (!res.isSuccess()){
				logger.error("【通联-网银支付】参数错误：{}", res.getReturnMessage());
				return super.payFailedCallback(model);
			}
			if(!isOrderCanPay(payDTO)){
				return super.payFailedCallback(model);
			}
			
			String configKey = SysConstant.TL_NETBANK_MERCHANT +"_" + PayTypeEnum.PERSON_EBANK.combPayType + 
					"_" + SysConstant.TL_CHANNEL_CODE + "_" + RequestTypeEnum.GATEWAY_PAY_QUERY_REFUND.getKey();
			GateWayConfigDTO gateWayConfigDTO = SysConstant.gateWayConfigMap.get(configKey);
			if(gateWayConfigDTO == null){
				logger.error("【通联-扫码支付】失败，获取网关配置为空，configKey={}，orderNo={}", configKey, payDTO.getOrderNo());
				return super.payFailedCallback(model);
			}
			
			PayTypeEnum payTypeEnum = PayTypeEnum.getPayTypeEnum(payDTO.getPayType().toString());
			TLNetBankPayRequest tlNetBankPayRequest = new TLNetBankPayRequest();
			tlNetBankPayRequest.setInputCharset("1");
			PayMethodConfigDTO payMethodConfigDTO = titanFinancialUtilService.getPayMethodConfigDTO(null);
			tlNetBankPayRequest.setPickupUrl(payMethodConfigDTO.getTl_NetBankPay_ConfirmPageurl());
			tlNetBankPayRequest.setReceiveUrl(payMethodConfigDTO.getTl_NetBankPay_Notifyurl());
			tlNetBankPayRequest.setVersion(SysConstant.TL_NETBANK_PAY_VERSION);
			tlNetBankPayRequest.setLanguage("1");
			tlNetBankPayRequest.setSignType("0");
			tlNetBankPayRequest.setMerchantId(SysConstant.TL_NETBANK_MERCHANT);
			tlNetBankPayRequest.setOrderNo(payDTO.getOrderNo());
			tlNetBankPayRequest.setOrderAmount(payDTO.getOrderAmount());
			tlNetBankPayRequest.setOrderCurrency("0");
			tlNetBankPayRequest.setOrderDatetime(payDTO.getOrderTime());
			tlNetBankPayRequest.setPayType(payTypeEnum.channelTypeKey);
			//tlNetBankPayRequest.setIssuerId(payDTO.getBankInfo());
			tlNetBankPayRequest.setIssuerId("vbank");
			tlNetBankPayRequest.setSignMsg(SignMsgBuilder.getNetBanPaySignMsg(tlNetBankPayRequest));
			tlNetBankPayRequest.setServerUrl(gateWayConfigDTO.getGateWayUrl());
			logger.info("【通联-网银支付】请求参数：{}", tlNetBankPayRequest.toString());
			
			model.addAttribute("tlNetBankPayRequest", tlNetBankPayRequest);
			return "payment/tl_gateWayPay";
			
		} catch (Exception e) {
			
			logger.error("【通联-网银支付】异常：", e);
			return super.payFailedCallback(model);
			
		}
        
    }
    

    /**
     * 第三方扫码支付
     * @author Jerry
     * @date 2017年11月27日 上午11:15:27
     */
    @ResponseBody
    @RequestMapping(value = "/qrCodePay", method = {RequestMethod.GET, RequestMethod.POST})
    public TitanQrCodePayResponse qrCodePay(HttpServletRequest request, Model model) {
		
		TitanQrCodePayResponse titanQrPayResponse = new TitanQrCodePayResponse();
    	try {
			
			TitanPayDTO payDTO = WebUtils.switch2RequestDTO(TitanPayDTO.class, request);
			ValidateResponse res = GenericValidate.validateNew(payDTO);
			if (!res.isSuccess()){
				logger.error("【通联-扫码支付】参数错误：{}", res.getReturnMessage());
				titanQrPayResponse.putErrorResult(RSErrorCodeEnum.PRAM_ERROR);
				return titanQrPayResponse;
			}
			if(!isOrderCanPay(payDTO)){
				titanQrPayResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
				return titanQrPayResponse;
			}
			PayTypeEnum payTypeEnum = PayTypeEnum.getPayTypeEnum(payDTO.getPayType().toString());
			
			TLQrCodePayRequest tlQrCodePayRequest = new TLQrCodePayRequest();
			tlQrCodePayRequest.setCusid(SysConstant.TL_QRCODE_CUSTID);
			tlQrCodePayRequest.setVersion(SysConstant.TL_QRCODE_VERSION);
			tlQrCodePayRequest.setTrxamt(Integer.parseInt(payDTO.getOrderAmount().toString()));
			tlQrCodePayRequest.setReqsn(payDTO.getOrderNo());
			tlQrCodePayRequest.setPaytype(payTypeEnum.channelTypeKey);
			tlQrCodePayRequest.setRandomstr(CommonUtil.getValidatecode(8));
			PayMethodConfigDTO payMethodConfigDTO = titanFinancialUtilService.getPayMethodConfigDTO(null);
			tlQrCodePayRequest.setNotify_url(payMethodConfigDTO.getTl_QrCodePay_Notifyurl());
			tlQrCodePayRequest.setRequestType(RequestTypeEnum.PUBLIC_PAY.getKey());
			
			titanQrPayResponse = tlPaymentService.qrCodePay(tlQrCodePayRequest);
			
			titanQrPayResponse.setPayType(payDTO.getPayType().toString());
			titanQrPayResponse.setOrderAmount(payDTO.getOrderAmount().toString());
			titanQrPayResponse.setOrderTime(payDTO.getOrderTime());
			SysConfig config = titanFinancialUtilService.querySysConfig();
			titanQrPayResponse.setSignMsg(SignMsgBuilder.tlQrCodePayResponseSignMsg(titanQrPayResponse, config.getRsCheckKey()));
			
			return titanQrPayResponse;
			
		} catch (Exception e) {
			
			logger.error("【通联-扫码支付】异常：", e);
			titanQrPayResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return titanQrPayResponse;
		}
        
    }
    
    
    @ResponseBody
    @RequestMapping(value = "/wechatPay")
    public TitanQrCodePayResponse wechatPay(HttpServletRequest request, Model model) {
		
		TitanQrCodePayResponse titanQrPayResponse = new TitanQrCodePayResponse();
    	try {
			
			TitanPayDTO payDTO = WebUtils.switch2RequestDTO(TitanPayDTO.class, request);
			ValidateResponse res = GenericValidate.validateNew(payDTO);
			if (!res.isSuccess()){
				logger.error("【通联-微信公众号支付】参数错误：{}", res.getReturnMessage());
				titanQrPayResponse.putErrorResult(RSErrorCodeEnum.PRAM_ERROR);
				return titanQrPayResponse;
			}
			if(!isOrderCanPay(payDTO)){
				titanQrPayResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
				return titanQrPayResponse;
			}
			PayTypeEnum payTypeEnum = PayTypeEnum.getPayTypeEnum(payDTO.getPayType().toString());
			
			TLQrCodePayRequest tlQrCodePayRequest = new TLQrCodePayRequest();
			tlQrCodePayRequest.setCusid(SysConstant.TL_NETBANK_MERCHANT);
			tlQrCodePayRequest.setVersion(SysConstant.TL_QRCODE_VERSION);
			tlQrCodePayRequest.setTrxamt(Integer.parseInt(payDTO.getOrderAmount().toString()));
			tlQrCodePayRequest.setReqsn(payDTO.getOrderNo());
			tlQrCodePayRequest.setPaytype(payTypeEnum.channelTypeKey);
			tlQrCodePayRequest.setRandomstr(CommonUtil.getValidatecode(8));
			tlQrCodePayRequest.setBody(payDTO.getProductName());
			tlQrCodePayRequest.setAcct(payDTO.getOpenId());
			PayMethodConfigDTO payMethodConfigDTO = titanFinancialUtilService.getPayMethodConfigDTO(null);
			tlQrCodePayRequest.setNotify_url(payMethodConfigDTO.getTl_WechatPay_Notifyurl());
			tlQrCodePayRequest.setRequestType(RequestTypeEnum.PUBLIC_PAY.getKey());
			
			titanQrPayResponse = tlPaymentService.wechatPay(tlQrCodePayRequest);
			titanQrPayResponse.setPayType(payDTO.getPayType().toString());
			titanQrPayResponse.setOrderAmount(payDTO.getOrderAmount().toString());
			titanQrPayResponse.setOrderTime(payDTO.getOrderTime());
			SysConfig config = titanFinancialUtilService.querySysConfig();
			titanQrPayResponse.setSignMsg(SignMsgBuilder.tlQrCodePayResponseSignMsg(titanQrPayResponse, config.getRsCheckKey()));
			
			return titanQrPayResponse;
			
		} catch (Exception e) {
			
			logger.error("【通联-微信公众号支付】异常：", e);
			titanQrPayResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return titanQrPayResponse;
		}
        
    }
	
	
	/**
    * 快捷支付-签约支付
    * @author Jerry
    * @date 2017年11月27日 上午11:30:27
    */
   @ResponseBody
   @RequestMapping(value = "/quickPay", method = {RequestMethod.GET, RequestMethod.POST})
   public TitanQuickPayResponse quickPay(HttpServletRequest request, Model model) {
   	
   		TitanQuickPayResponse titanQuickPayResponse = new TitanQuickPayResponse();
   	
   		try {
   		
			TitanPayDTO payDTO = WebUtils.switch2RequestDTO(TitanPayDTO.class, request);
			ValidateResponse res = GenericValidate.validateNew(payDTO);
			if (!res.isSuccess() || !StringUtil.isValidString(payDTO.getPayerAccountType())){
				logger.error("【融宝-快捷支付】参数错误：{}，payerAccountType={}", res.getReturnMessage(), payDTO.getPayerAccountType());
				titanQuickPayResponse.putErrorResult(RSErrorCodeEnum.PRAM_ERROR);
				return titanQuickPayResponse;
			}
			if(!isOrderCanPay(payDTO)){
				titanQuickPayResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
				return titanQuickPayResponse;
			}
			
			RBQuickPayRequest rbQuickPayRequest = new RBQuickPayRequest();
			//已绑卡签约
			if(StringUtil.isValidString(payDTO.getBindCardId())){
				rbQuickPayRequest.setBindCardId(payDTO.getBindCardId());
			}
			//不是已绑卡签约
			if(!StringUtil.isValidString(payDTO.getBindCardId())){
				rbQuickPayRequest.setCard_no(payDTO.getPayerAcount());
				rbQuickPayRequest.setOwner(payDTO.getPayerName());
				rbQuickPayRequest.setCert_type("01");
				rbQuickPayRequest.setCert_no(payDTO.getIdCode());
				rbQuickPayRequest.setPhone(payDTO.getPayerPhone());
			}
			rbQuickPayRequest.setMerchant_id(SysConstant.RB_QUICKPAY_MERCHANT);
			rbQuickPayRequest.setOrder_no(payDTO.getOrderNo());
			rbQuickPayRequest.setTranstime(payDTO.getOrderTime());//2015-03-06 12:24:59
			rbQuickPayRequest.setCurrency("156");
			rbQuickPayRequest.setTotal_fee(Integer.parseInt(payDTO.getOrderAmount().toString()));
			rbQuickPayRequest.setTitle("支付");//需要pay-app传过来
			rbQuickPayRequest.setBody("泰坦金融");//需要pay-app传过来
			rbQuickPayRequest.setMember_id("4534535er4");//需要pay-app传过来
			rbQuickPayRequest.setTerminal_type(payDTO.getTerminalType());
			rbQuickPayRequest.setTerminal_info(payDTO.getTerminalInfo());
			rbQuickPayRequest.setMember_ip(payDTO.getTerminalIp());
			rbQuickPayRequest.setSeller_email(SysConstant.RB_SELLER_EMAIL);
			PayMethodConfigDTO payMethodConfigDTO = titanFinancialUtilService.getPayMethodConfigDTO(null);
			rbQuickPayRequest.setNotify_url(payMethodConfigDTO.getRb_QuickPay_Notifyurl());
			rbQuickPayRequest.setToken_id(CommonUtil.getUUID());
			rbQuickPayRequest.setVersion(SysConstant.RB_VERSION);
			rbQuickPayRequest.setSign_type(SysConstant.RB_SIGN_TYPE);
			rbQuickPayRequest.setRequestType(RequestTypeEnum.QUICK_BIND_PAY.getKey());
			if(!StringUtil.isValidString(rbQuickPayRequest.getBindCardId())){
				//储蓄卡
				if(SysConstant.CARD_TYPE_DESPOSIT.equals(payDTO.getPayerAccountType())){
					rbQuickPayRequest.setRequestType(RequestTypeEnum.QUICK_PAY_DEPOSIT.getKey());
				//信用卡
		       	}else if(SysConstant.CARD_TYPE_CREDIT.equals(payDTO.getPayerAccountType())){
		       		if(!StringUtil.isValidString(payDTO.getSafetyCode()) || !StringUtil.isValidString(payDTO.getValidthru())){
		       			logger.error("【融宝-快捷支付】参数错误：safetyCode={}，validthru={}", payDTO.getSafetyCode(), payDTO.getValidthru());
		   				titanQuickPayResponse.putErrorResult(RSErrorCodeEnum.PRAM_ERROR);
		   				return titanQuickPayResponse;
		       		}
		       		rbQuickPayRequest.setCvv2(payDTO.getSafetyCode());
		       		rbQuickPayRequest.setValidthru(payDTO.getValidthru());
		       		rbQuickPayRequest.setRequestType(RequestTypeEnum.QUICK_PAY_CREDIT.getKey());
		       	}
			}
			
			titanQuickPayResponse = rbQuickPayService.contractPay(rbQuickPayRequest);
			//融宝没有返回的信息，自己设置进去
			titanQuickPayResponse.setOrderAmount(payDTO.getOrderAmount().toString());
			titanQuickPayResponse.setOrderTime(payDTO.getOrderTime());
			titanQuickPayResponse.setPayType(payDTO.getPayType().toString());
			return titanQuickPayResponse;
			
		} catch (Exception e) {
			
			logger.error("【融宝-快捷支付】异常：", e);
			titanQuickPayResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return titanQuickPayResponse;
			
		}
       
   }
    
    
    /**
     * 获取二维码
     * @author Jerry
     * @date 2017年12月20日 下午6:02:26
     */
	@RequestMapping("wxPicture")
	public void getWxPicture(String url,HttpServletResponse response){
		try{
			Wxutil.createRqCode(url, CommonConstant.RQ_WIDTH, CommonConstant.RQ_HEIGH, response.getOutputStream());
		}catch(Exception e){
			logger.error("微信生成图片错误：", e);
		}
	}
	
	
	/**
	 * 交易单存在并且可以支付
	 * @author Jerry
	 * @date 2018年1月12日 上午10:27:26
	 */
	private boolean isOrderCanPay(TitanPayDTO payDTO){
		TransOrderRequest transOrderRequest = new TransOrderRequest();
		transOrderRequest.setOrderid(payDTO.getOrderNo());
		TransOrderDTO transOrderDTO = titanOrderService.queryTransOrderDTO(transOrderRequest);
		if(transOrderDTO == null){
			logger.error("【支付】失败：交易单不存在，orderNo={}", payDTO.getOrderNo());
			return false;
		}
		if(OrderStatusEnum.isPaySuccess(transOrderDTO.getStatusid())){
			logger.error("【支付】失败：交易单不是可支付状态，orderNo={}", payDTO.getOrderNo());
			return false;
		}
		return true;
	}

}
