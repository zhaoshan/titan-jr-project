/** 
 * CopyRright ©2017 深圳市天下房仓科技有限公司 All Right Reserved.
 * 
 * @fileName QuickpayController.java
 * @author Jerry
 * @date 2018年1月3日 下午6:32:55  
 */
package com.titanjr.checkstand.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fangcang.titanjr.common.bean.ValidateResponse;
import com.fangcang.titanjr.common.util.GenericValidate;
import com.fangcang.titanjr.dto.bean.TitanOrderPayDTO;
import com.fangcang.titanjr.service.TitanOrderService;
import com.titanjr.checkstand.constants.BusiCodeEnum;
import com.titanjr.checkstand.constants.RSErrorCodeEnum;
import com.titanjr.checkstand.constants.RequestTypeEnum;
import com.titanjr.checkstand.constants.SysConstant;
import com.titanjr.checkstand.dto.TitanCardAuthDTO;
import com.titanjr.checkstand.dto.TitanCardBINQueryDTO;
import com.titanjr.checkstand.dto.TitanPayConfirmDTO;
import com.titanjr.checkstand.dto.TitanReSendMsgDTO;
import com.titanjr.checkstand.dto.TitanUnBindCardDTO;
import com.titanjr.checkstand.request.RBCardAuthRequest;
import com.titanjr.checkstand.request.RBCardBINQueryRequest;
import com.titanjr.checkstand.request.RBQuickPayConfirmRequest;
import com.titanjr.checkstand.request.RBReSendMsgRequest;
import com.titanjr.checkstand.request.RBUnBindCardRequest;
//import com.titanjr.checkstand.respnse.TitanCardAuthResponse;
import com.titanjr.checkstand.respnse.TitanCardBINQueryResponse;
import com.titanjr.checkstand.respnse.TitanPayConfirmResponse;
import com.titanjr.checkstand.respnse.TitanReSendMsgResponse;
import com.titanjr.checkstand.respnse.TitanUnBindCardResponse;
import com.titanjr.checkstand.service.RBQuickPayService;
import com.titanjr.checkstand.strategy.StrategyFactory;
import com.titanjr.checkstand.strategy.quickPay.QuickPayStrategy;
import com.titanjr.checkstand.util.JRBeanUtils;
import com.titanjr.checkstand.util.WebUtils;

/**
 * 快捷支付
 * @author Jerry
 * @date 2018年1月3日 下午6:32:55  
 */
@Controller
@RequestMapping(value = "/quick")
public class QuickpayController extends BaseController {

	/** 
	 * 
	 */
	private static final long serialVersionUID = 4464610222937616026L;
	private final static Logger logger = LoggerFactory.getLogger(QuickpayController.class);
	
	@Resource
	private RBQuickPayService rbQuickPayService;
	
	@Resource
	private TitanOrderService titanOrderService;
	
	
	/**
	 * 快捷支付相关的请求都从这里进来
	 * @author Jerry
	 * @date 2018年1月3日 下午6:36:15
	 */
	@RequestMapping(value = "/entrance", method = {RequestMethod.GET, RequestMethod.POST})
    public String entrance(HttpServletRequest request, RedirectAttributes attr, Model model) throws Exception {
        
        BusiCodeEnum busiCodeEnum = JRBeanUtils.getBusiCode(request);
        if(busiCodeEnum == null){
        	logger.error("【融宝-快捷服务】参数错误，未找到对应的业务代码，busiCodeEnum={}", busiCodeEnum);
        	return super.payFailedCallback(model);
        }
        
        //根据业务代码来判定走到具体哪个接口
        QuickPayStrategy quickPayStrategy =  StrategyFactory.getQuickpayStrategy(busiCodeEnum);
        if(quickPayStrategy == null){
			logger.error("【融宝-快捷服务】失败，获取相应的业务策略为空");
			return super.payFailedCallback(model);
		}
        
        String redirectUrl = quickPayStrategy.redirectResult(request);
        super.resetParameter(request,attr);
        
        return "redirect:" + redirectUrl;
        
    }
    
    
    /**
     * 卡BIN查询
     * @author Jerry
     * @date 2018年1月4日 下午5:40:40
     */
    @ResponseBody
    @RequestMapping(value = "/cardBINQuery", method = {RequestMethod.GET, RequestMethod.POST})
    public TitanCardBINQueryResponse cardBINQuery(HttpServletRequest request, Model model) {
    	
    	TitanCardBINQueryResponse titanCardBINQueryResponse = new TitanCardBINQueryResponse();
    	
    	try {
    		
    		TitanCardBINQueryDTO titanCardBINQueryDTO = WebUtils.switch2RequestDTO(TitanCardBINQueryDTO.class, request);
			ValidateResponse res = GenericValidate.validateNew(titanCardBINQueryDTO);
			if (!res.isSuccess()){
				logger.error("【融宝-卡BIN查询】参数错误：{}", res.getReturnMessage());
				titanCardBINQueryResponse.putErrorResult(RSErrorCodeEnum.PRAM_ERROR);
				return titanCardBINQueryResponse;
			}
			
			RBCardBINQueryRequest rbCardBINQueryRequest = new RBCardBINQueryRequest();
			rbCardBINQueryRequest.setCard_no(titanCardBINQueryDTO.getCardNo());
			rbCardBINQueryRequest.setMerchant_id(SysConstant.RB_QUICKPAY_MERCHANT);
			rbCardBINQueryRequest.setVersion(SysConstant.RB_VERSION);
			rbCardBINQueryRequest.setSign_type(SysConstant.RB_SIGN_TYPE);
			rbCardBINQueryRequest.setRequestType(RequestTypeEnum.QUICK_CARDBIN_QUERY.getKey());
			
			titanCardBINQueryResponse = rbQuickPayService.cardBINQuery(rbCardBINQueryRequest);
			return titanCardBINQueryResponse;
			
		} catch (Exception e) {
			
			logger.error("【融宝-卡BIN查询】异常：", e);
			titanCardBINQueryResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return titanCardBINQueryResponse;
			
		}
        
    }
    
    
    /**
     * 确认支付
     * @author Jerry
     * @date 2018年1月5日 下午2:04:59
     */
    @ResponseBody
    @RequestMapping(value = "/payConfirm", method = {RequestMethod.GET, RequestMethod.POST})
    public TitanPayConfirmResponse payConfirm(HttpServletRequest request, Model model) {
    	
    	TitanPayConfirmResponse titanPayConfirmResponse = new TitanPayConfirmResponse();
    	
    	try {
    		
			TitanPayConfirmDTO titanPayConfirmDTO = WebUtils.switch2RequestDTO(TitanPayConfirmDTO.class, request);
			ValidateResponse res = GenericValidate.validateNew(titanPayConfirmDTO);
			if (!res.isSuccess()){
				logger.error("【融宝-确认支付】参数错误：{}", res.getReturnMessage());
				titanPayConfirmResponse.putErrorResult(RSErrorCodeEnum.PRAM_ERROR);
				return titanPayConfirmResponse;
			}
			
			RBQuickPayConfirmRequest rbQuickPayConfirmRequest = new RBQuickPayConfirmRequest();
			rbQuickPayConfirmRequest.setOrder_no(titanPayConfirmDTO.getOrderNo());
			rbQuickPayConfirmRequest.setCheck_code(titanPayConfirmDTO.getCheckCode());
			rbQuickPayConfirmRequest.setMerchant_id(SysConstant.RB_QUICKPAY_MERCHANT);
			rbQuickPayConfirmRequest.setVersion(SysConstant.RB_VERSION);
			rbQuickPayConfirmRequest.setSign_type(SysConstant.RB_SIGN_TYPE);
			rbQuickPayConfirmRequest.setRequestType(RequestTypeEnum.QUICK_PAY_CONFIRM.getKey());
			
			titanPayConfirmResponse = rbQuickPayService.payConfirm(rbQuickPayConfirmRequest);
			return titanPayConfirmResponse;
			
		} catch (Exception e) {
			
			logger.error("【融宝-确认支付】异常：", e);
			titanPayConfirmResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return titanPayConfirmResponse;
			
		}
        
    }
    
    
    /**
     * 重发验证码
     * @author Jerry
     * @date 2018年1月5日 下午3:23:46
     */
    @ResponseBody
    @RequestMapping(value = "/reSendMsg", method = {RequestMethod.GET, RequestMethod.POST})
    public TitanReSendMsgResponse reSendMsg(HttpServletRequest request, Model model) {
    	
    	TitanReSendMsgResponse titanReSendMsgResponse = new TitanReSendMsgResponse();
    	
    	try {
    		
    		//校验参数
			TitanReSendMsgDTO titanReSendMsgDTO = WebUtils.switch2RequestDTO(TitanReSendMsgDTO.class, request);
			ValidateResponse res = GenericValidate.validateNew(titanReSendMsgDTO);
			if (!res.isSuccess()){
				logger.error("【融宝-重发验证码】参数错误：{}", res.getReturnMessage());
				titanReSendMsgResponse.putErrorResult(RSErrorCodeEnum.PRAM_ERROR);
				return titanReSendMsgResponse;
			}
			
			//查询充值单（需要得到手机号）
			TitanOrderPayDTO titanOrderPayDTO = new TitanOrderPayDTO();
			titanOrderPayDTO.setMerchantNo(titanReSendMsgDTO.getMerchantNo());
			titanOrderPayDTO.setOrderNo(titanReSendMsgDTO.getOrderNo());
			titanOrderPayDTO = titanOrderService.getTitanOrderPayDTO(titanOrderPayDTO);
			if(titanOrderPayDTO == null){
				logger.error("【融宝-重发验证码】失败，查询充值单为空，orderNo={}", titanReSendMsgDTO.getOrderNo());
				titanReSendMsgResponse.putErrorResult(RSErrorCodeEnum.build("查询充值单为空"));
				return titanReSendMsgResponse;
			}
			
			//封装参数调service
			RBReSendMsgRequest rbReSendMsgRequest = new RBReSendMsgRequest();
			rbReSendMsgRequest.setOrder_no(titanReSendMsgDTO.getOrderNo());
			rbReSendMsgRequest.setPhone(titanOrderPayDTO.getPayerPhone());
			rbReSendMsgRequest.setMerchant_id(SysConstant.RB_QUICKPAY_MERCHANT);
			rbReSendMsgRequest.setVersion(SysConstant.RB_VERSION);
			rbReSendMsgRequest.setSign_type(SysConstant.RB_SIGN_TYPE);
			rbReSendMsgRequest.setRequestType(RequestTypeEnum.QUICK_MSG_SEND.getKey());
			
			titanReSendMsgResponse = rbQuickPayService.reSendMsg(rbReSendMsgRequest);
			return titanReSendMsgResponse;
			
		} catch (Exception e) {
			
			logger.error("【融宝-重发验证码】异常：", e);
			titanReSendMsgResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return titanReSendMsgResponse;
			
		}
        
    }
    
    
    /**
     * 卡密鉴权-返回页面（此功能后期优化）
     * @author Jerry
     * @date 2018年1月8日 下午5:13:42
     */
    @ResponseBody
    @RequestMapping(value = "/cardAuth", method = {RequestMethod.GET, RequestMethod.POST})
    public String cardAuth(HttpServletRequest request, Model model) {
    	
    	//TitanCardAuthResponse titanCardAuthResponse = new TitanCardAuthResponse();
    	
    	try {
    		
			TitanCardAuthDTO titanCardAuthDTO = WebUtils.switch2RequestDTO(TitanCardAuthDTO.class, request);
			ValidateResponse res = GenericValidate.validateNew(titanCardAuthDTO);
			if (!res.isSuccess()){
				logger.error("【融宝-卡密鉴权】参数错误：{}", res.getReturnMessage());
				/*titanCardAuthResponse.putErrorResult(RSErrorCodeEnum.PRAM_ERROR);
				return titanCardAuthResponse;*/
				return "【融宝-卡密鉴权】参数错误："+res.getReturnMessage();
			}
			
			RBCardAuthRequest rbCardAuthRequest = new RBCardAuthRequest();
			rbCardAuthRequest.setMember_id("12345");//pay-app需要新增这个参数
			rbCardAuthRequest.setBind_id("343545");//需要查询融宝绑卡列表，通过cardNo和merber_id匹配bind_id（后续：无法实现，绑卡查询返回的卡号不是完全的卡号）
			rbCardAuthRequest.setOrder_no(titanCardAuthDTO.getOrderNo());
			rbCardAuthRequest.setTerminal_type(titanCardAuthDTO.getTerminalType());
			rbCardAuthRequest.setReturn_url(titanCardAuthDTO.getCardCheckPageUrl());//用pay-app的地址
			rbCardAuthRequest.setNotify_url(titanCardAuthDTO.getCardChecknotifyUrl());//用支付路由的地址
			rbCardAuthRequest.setMerchant_id(SysConstant.RB_QUICKPAY_MERCHANT);
			rbCardAuthRequest.setVersion(SysConstant.RB_VERSION);
			rbCardAuthRequest.setSign_type(SysConstant.RB_SIGN_TYPE);
			rbCardAuthRequest.setRequestType(RequestTypeEnum.QUICK_CARD_AUTH.getKey());
			
			/*titanCardAuthResponse = rbQuickPayService.cardAuth(rbCardAuthRequest);
			return titanCardAuthResponse;*/
			
			return rbQuickPayService.cardAuth(rbCardAuthRequest);
			
		} catch (Exception e) {
			
			logger.error("【融宝-卡密鉴权】异常：", e);
			/*titanCardAuthResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return titanCardAuthResponse;*/
			return "【融宝-卡密鉴权】异常";
			
		}
        
    }
    
    
    /**
     * 解绑卡
     * @author Jerry
     * @date 2018年1月9日 上午11:53:37
     */
    @ResponseBody
    @RequestMapping(value = "/unBindCard", method = {RequestMethod.GET, RequestMethod.POST})
    public TitanUnBindCardResponse unBindCard(HttpServletRequest request, Model model) {
    	
    	TitanUnBindCardResponse titanUnBindCardResponse = new TitanUnBindCardResponse();
    	
    	try {
    		
			TitanUnBindCardDTO titanUnBindCardDTO = WebUtils.switch2RequestDTO(TitanUnBindCardDTO.class, request);
			ValidateResponse res = GenericValidate.validateNew(titanUnBindCardDTO);
			if (!res.isSuccess()){
				logger.error("【融宝-解绑卡】参数错误：{}", res.getReturnMessage());
				titanUnBindCardResponse.putErrorResult(RSErrorCodeEnum.PRAM_ERROR);
				return titanUnBindCardResponse;
			}
			
			RBUnBindCardRequest rbUnBindCardRequest = new RBUnBindCardRequest();
			rbUnBindCardRequest.setMember_id(titanUnBindCardDTO.getUserId());
			rbUnBindCardRequest.setBind_id("343545");//需要查询本地快捷支付历史卡（泰坦金融保存常用卡的时候要保存bind_id）
			rbUnBindCardRequest.setMerchant_id(SysConstant.RB_QUICKPAY_MERCHANT);
			rbUnBindCardRequest.setVersion(SysConstant.RB_VERSION);
			rbUnBindCardRequest.setSign_type(SysConstant.RB_SIGN_TYPE);
			rbUnBindCardRequest.setRequestType(RequestTypeEnum.QUICK_UNBIND.getKey());
			
			titanUnBindCardResponse = rbQuickPayService.unBindCard(rbUnBindCardRequest);
			return titanUnBindCardResponse;
			
		} catch (Exception e) {
			
			logger.error("【融宝-解绑卡】异常：", e);
			titanUnBindCardResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return titanUnBindCardResponse;
			
		}
        
    }
    

}