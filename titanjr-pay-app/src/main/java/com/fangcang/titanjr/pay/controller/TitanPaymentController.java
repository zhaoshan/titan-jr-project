
package com.fangcang.titanjr.pay.controller;

import com.alibaba.fastjson.JSON;
import com.fangcang.titanjr.common.enums.*;
import com.fangcang.titanjr.common.util.*;
import com.fangcang.titanjr.dto.PaySourceEnum;
import com.fangcang.titanjr.dto.bean.OrderExceptionDTO;
import com.fangcang.titanjr.dto.bean.RechargeDataDTO;
import com.fangcang.titanjr.dto.bean.TransOrderDTO;
import com.fangcang.titanjr.dto.bean.TransOrderInfo;
import com.fangcang.titanjr.enums.PayTypeEnum;
import com.fangcang.titanjr.dto.request.ConfirmOrdernQueryRequest;
import com.fangcang.titanjr.dto.request.RechargeResultConfirmRequest;
import com.fangcang.titanjr.dto.request.TitanPaymentRequest;
import com.fangcang.titanjr.dto.request.TransOrderRequest;
import com.fangcang.titanjr.dto.request.TransferRequest;
import com.fangcang.titanjr.dto.response.AccountCheckResponse;
import com.fangcang.titanjr.dto.response.ConfirmOrdernQueryResponse;
import com.fangcang.titanjr.dto.response.LocalAddTransOrderResponse;
import com.fangcang.titanjr.dto.response.QrCodeResponse;
import com.fangcang.titanjr.dto.response.RechargeResponse;
import com.fangcang.titanjr.dto.response.TransOrderCreateResponse;
import com.fangcang.titanjr.dto.response.TransferResponse;
import com.fangcang.titanjr.pay.constant.TitanConstantDefine;
import com.fangcang.titanjr.pay.req.CreateTitanRateRecordReq;
import com.fangcang.titanjr.pay.req.TitanRateComputeReq;
import com.fangcang.titanjr.pay.services.TitanPaymentService;
import com.fangcang.titanjr.pay.services.TitanRateService;
import com.fangcang.titanjr.pay.services.TitanTradeService;
import com.fangcang.titanjr.service.TitanCashierDeskService;
import com.fangcang.titanjr.service.TitanFinancialAccountService;
import com.fangcang.titanjr.service.TitanFinancialTradeService;
import com.fangcang.titanjr.service.TitanFinancialUtilService;
import com.fangcang.titanjr.service.TitanOrderService;
import com.fangcang.util.StringUtil;

import net.sf.json.JSONSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Controller
@RequestMapping("/payment")
public class TitanPaymentController extends BaseController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private static final Log log = LogFactory.getLog(TitanPaymentController.class);
	
	@Resource
	private TitanFinancialTradeService titanFinancialTradeService;
	
	@Resource
	private TitanPaymentService titanPaymentService;
	
	@Resource
	private TitanOrderService titanOrderService;
	
	@Resource
	private TitanFinancialAccountService titanFinancialAccountService;
	
	@Resource
	private TitanTradeService financialTradeService;
	
	@Resource
	private TitanRateService titanRateService;

	@Resource
	private TitanCashierDeskService titanCashierDeskService;
	
	
	private static Map<String,Object> mapLock = new  ConcurrentHashMap<String, Object>();
	/**
	 * 消息回调接口
	 * @param rechargeResultConfirmRequest
	 * @param response
	 * @throws IOException
	 */
	@ResponseBody
    @RequestMapping(value = "notify")
    public void payResultConfirm(RechargeResultConfirmRequest rechargeResultConfirmRequest,HttpServletResponse response) throws IOException{
		String orderNo = rechargeResultConfirmRequest.getOrderNo();
		if(!StringUtil.isValidString(orderNo)){
			log.error("RS callback is fail");
			return;
		}

		response.getWriter().print("returnCode=000000&returnMsg=成功");
		response.flushBuffer();
		log.info(orderNo+"订单状态："+rechargeResultConfirmRequest.getPayStatus());
		
		String sign  =titanPaymentService.getSign(rechargeResultConfirmRequest);
		String signMsg = rechargeResultConfirmRequest.getSignMsg();
    	if(!MD5.MD5Encode(sign, "UTF-8").equals(signMsg)){
    	   log.error("signature verification is fail,The data is:"+JsonConversionTool.toJson(rechargeResultConfirmRequest));
   		   return;
    	}
		
    	if(!CommonConstant.PAY_SUCCESS.equals(rechargeResultConfirmRequest.getPayStatus())){
    		log.error("recharge is fail");
    		return ;
    	}
    	
    	//查询订单
    	try{
    		lockOutTradeNoList(orderNo);
        	TransOrderRequest transOrderRequest = new TransOrderRequest();
    		transOrderRequest.setOrderid(orderNo);
    		TransOrderDTO transOrderDTO = titanOrderService.queryTransOrderDTO(transOrderRequest);
        	if(null == transOrderDTO){
        		log.error("the object is null");
        		unlockOutTradeNoList(orderNo);
        		return ;
        	}
        	
        	PayerTypeEnum payerType = PayerTypeEnum.getPayerTypeEnumByKey(transOrderDTO.getPayerType());
        	
        	//validate transfer order 
        	boolean validateResult = titanPaymentService.validateIsConfirmed(transOrderDTO.getTransid());
			if(!validateResult){
				log.error("该订单已转帐成功");
				unlockOutTradeNoList(orderNo);
				return ;
			}
        	
        	// update recharge order
			int row = titanOrderService.updateTitanOrderPayreq(orderNo,ReqstatusEnum.RECHARFE_SUCCESS.getStatus()+"");
        	if(row<1){
        		log.error("更新充值单失败");	
        		OrderExceptionDTO orderExceptionDTO = new OrderExceptionDTO(orderNo, "充值成功修改充值单失败", OrderExceptionEnum.OrderPay_Update, JSON.toJSONString(orderNo));
        		titanOrderService.saveOrderException(orderExceptionDTO);
        	}
        	
        	OrderStatusEnum orderStatusEnum = OrderStatusEnum.RECHARGE_SUCCESS;
        	if(!PayerTypeEnum.RECHARGE.key.equals(payerType.getKey())&&!validateOrderStatus(orderNo)){//非充值的需要发出三次确认订单成功到帐
    			log.error("实在没办法,钱没到账，不能转账");
    			orderStatusEnum = OrderStatusEnum.ORDER_FAIL;
    			if(CommonConstant.RS_FANGCANG_USER_ID.equals(transOrderDTO.getPayermerchant())){//中间账户的延时到帐就是失败
    				orderStatusEnum = OrderStatusEnum.ORDER_DELAY;
    			}
    			titanPaymentService.updateOrderStatus(transOrderDTO.getTransid(),orderStatusEnum);

    			return ;
    		}
        	
        	if(PayerTypeEnum.RECHARGE.key.equals(payerType.getKey())){//如果是充值则置订单为成功
        		orderStatusEnum= OrderStatusEnum.ORDER_SUCCESS;
        	}else{//不是充值操作，就需要转帐
        		TransferRequest transferRequest = titanPaymentService.convertToTransferRequest(transOrderDTO);
	        	log.info("begin to transfer:"+JsonConversionTool.toJson(transferRequest));
	        	TransferResponse transferResponse = titanFinancialTradeService.transferAccounts(transferRequest);
	        	log.info("the result of transfer:"+JsonConversionTool.toJson(transferResponse));
	        	
	        	if(!transferResponse.isResult()){//transfer fail
	        		orderStatusEnum = OrderStatusEnum.ORDER_FAIL;
	        	}else{//transfer success
	        		orderStatusEnum = OrderStatusEnum.TRANSFER_SUCCESS;
	        		financialTradeService.notifyPayResult(transOrderDTO.getUserorderid());
	        		
	        		if(CommonConstant.FREEZE_ORDER.equals(transOrderDTO.getIsEscrowedPayment())){//需要进行冻结操作
	        			log.info("begin to freeze:"+transferRequest);
	        			boolean freezeSuccess = titanPaymentService.freezeAccountBalance(transferRequest,orderNo);
	        			log.info("the result of freeze:"+freezeSuccess);
    					//update the status of the order
    					if(freezeSuccess){//freeze order is success
    						orderStatusEnum = OrderStatusEnum.FREEZE_SUCCESS;
    					}else{
    						log.error("update the status of the order was failed,the msg is "+JsonConversionTool.toJson(transferRequest));
    						orderStatusEnum = OrderStatusEnum.FREEZE_FAIL;
    						OrderExceptionDTO orderExceptionDTO = new OrderExceptionDTO(orderNo, "转账成功冻结失败", OrderExceptionEnum.Freeze_Insert, JSON.toJSONString(transferRequest));
        	        		titanOrderService.saveOrderException(orderExceptionDTO);
    					}
	        		}
	        		
	        		//save the trade account
					if(payerType.isAddAccountHistory()){
						titanPaymentService.addAccountHistory(transOrderDTO);
					}
	        	}
        	}
        	
        	//udate the status being success if the transfer is success but that is not freeze 
			if(orderStatusEnum.getStatus().equals(OrderStatusEnum.TRANSFER_SUCCESS.getStatus())){
				orderStatusEnum = OrderStatusEnum.ORDER_SUCCESS;
			}
			
			log.info("update the status of the order:"+JsonConversionTool.toJson(orderStatusEnum));
			boolean updateStatus = titanPaymentService.updateOrderStatus(transOrderDTO.getTransid(),orderStatusEnum);
			
			if(!updateStatus){//udate the status was failed 
				log.error("冻结成功修改订单状态失败：update the status of the order were failed");
				OrderExceptionDTO orderExceptionDTO = new OrderExceptionDTO(orderNo, "冻结成功修改订单状态失败", OrderExceptionEnum.TransOrder_update, JSON.toJSONString(transOrderDTO.getTransid()));
        		titanOrderService.saveOrderException(orderExceptionDTO);
			}
        	
    	}catch(Exception e){
            log.error("" ,e);    		
    	}finally{
    		unlockOutTradeNoList(orderNo);
    	}
    	
	}
	
	private boolean validateOrderStatus(String orderNo){

		ConfirmOrdernQueryRequest request = new ConfirmOrdernQueryRequest();
		request.setOrderNo(orderNo);
		request.setMerchantcode(CommonConstant.RS_FANGCANG_CONST_ID);
		
		ConfirmOrdernQueryResponse response = null;
		
		TransOrderInfo order = null;
		// 重试三次
		for (int i = 0; i < 3; i++) {

			response = titanFinancialTradeService.ordernQuery(request);

			if (response == null || !response.isResult()
					|| null == response.getTransOrderInfos()
					|| response.getTransOrderInfos().size() != 1) {
				log.error("confirem ordern query is null");
				try {//线程等待
					if(i<2){
						Thread.sleep(500 * (2<<i));
					}
				} catch (InterruptedException e) {
					log.error("", e);
				}
				continue;
			}

			order = response.getTransOrderInfos().get(0);

			log.info(orderNo + "订单状态:" + i + ":" + order.getOrderstatus());

			if (CommonConstant.RS_ORDER_STATUS.equals(order.getOrderstatus())) {
				return true;
			}
			try {
				if(i<2){
					Thread.sleep(500 * (2<<i));
				}
				
			} catch (InterruptedException e) {
				log.error("", e);
			}
		}
		return false;
	}
	
	@RequestMapping("notifyPayResult")
	public void notifyPayResult(String userOrderId){
		financialTradeService.notifyPayResult(userOrderId);
	}
	
	/**
	 * 只有转账操作的controller
	 * @param request
	 * @param titanPaymentRequest
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/showTitanPayPage")
	public String showTitanPayPage(HttpServletRequest request,TitanPaymentRequest titanPaymentRequest) throws Exception{
		log.info("账户余额请求参数:"+JsonConversionTool.toJson(titanPaymentRequest));
		if(null == titanPaymentRequest || !StringUtil.isValidString(titanPaymentRequest.getTradeAmount())
				||!StringUtil.isValidString(titanPaymentRequest.getPayOrderNo())){
			return toMsgJson(TitanMsgCodeEnum.PARAMETER_VALIDATION_FAILED);
		}
		
	    Map<String,String> validResult = this.validPaymentData(titanPaymentRequest);
        if(!CommonConstant.OPERATE_SUCCESS.equals(validResult.get(CommonConstant.RESULT))){//合规性验证
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("result", validResult.get(CommonConstant.RESULT));
    		map.put("resultMsg", validResult.get(CommonConstant.RETURN_MSG));
        	return JSONSerializer.toJSON(map).toString();
        }
		
        log.info("the params of local order:"+JsonConversionTool.toJson(titanPaymentRequest));
		LocalAddTransOrderResponse localOrderResp = titanFinancialTradeService.addLocalTransOrder(titanPaymentRequest);
        log.info("the result of local order:"+JsonConversionTool.toJson(localOrderResp));
		
        if (!localOrderResp.isResult()) {
        	log.error("the result of local order was failed");
        	Map<String, Object> map = new HashMap<String, Object>();
    		map.put("result", localOrderResp.getReturnCode());
    		map.put("resultMsg", localOrderResp.getReturnMessage());
    		return JSONSerializer.toJSON(map).toString();
        }
        titanPaymentRequest.setOrderid(localOrderResp.getOrderNo());
        TransferRequest transferRequest = this.convertToTransferRequest(titanPaymentRequest);
        //存在安全隐患，如果余额支付两次会不会存在重复支付，转帐操作中的锁在集群中已经不起作用了
        TransferResponse transferResponse = titanFinancialTradeService.transferAccounts(transferRequest);
		
		TransOrderRequest transOrderRequest = new TransOrderRequest();
		transOrderRequest.setOrderid(localOrderResp.getOrderNo());
		TransOrderDTO transOrder= titanOrderService.queryTransOrderDTO(transOrderRequest);
		OrderStatusEnum orderStatusEnum = OrderStatusEnum.ORDER_IN_PROCESS;
        
		if(!transferResponse.isResult()){
			log.error("transfer is failed");
			orderStatusEnum = OrderStatusEnum.ORDER_FAIL;
			boolean updateStatus = titanPaymentService.updateOrderStatus(transOrder.getTransid(),orderStatusEnum);
			if(!updateStatus){
				OrderExceptionDTO orderExceptionDTO = new OrderExceptionDTO(transOrder.getOrderid(), "转账失败修改订单状态失败", OrderExceptionEnum.TransOrder_update, JSON.toJSONString(transOrder.getTransid()));
				titanOrderService.saveOrderException(orderExceptionDTO);
			}
			return toMsgJson(TitanMsgCodeEnum.TRANSFER_FAIL);
		}
		
		financialTradeService.notifyPayResult(localOrderResp.getUserOrderId());
		orderStatusEnum = OrderStatusEnum.ORDER_SUCCESS;
		if(CommonConstant.FREEZE_ORDER.equals(transOrder.getIsEscrowedPayment())){
			boolean freezeSuccess = titanPaymentService.freezeAccountBalance(transferRequest,localOrderResp.getOrderNo());
			//修改订单状态
			if(freezeSuccess){//冻结成功改变订单状态
				orderStatusEnum = OrderStatusEnum.FREEZE_SUCCESS;
			}else{
				log.error("freeze the order was failed");
				orderStatusEnum =OrderStatusEnum.FREEZE_FAIL;
				OrderExceptionDTO orderExceptionDTO = new OrderExceptionDTO(localOrderResp.getOrderNo(), "转账成功冻结失败", OrderExceptionEnum.Freeze_Insert, JSON.toJSONString(transferRequest));
        		titanOrderService.saveOrderException(orderExceptionDTO);
			}
		}
		
		//中间账户不计历史
		if(!transOrder.getUserid().equals(titanFinancialAccountService.getDefaultPayerConfig().getUserId())){
			titanPaymentService.addAccountHistory(transOrder);
		}
		
		boolean updateStatus = titanPaymentService.updateOrderStatus(transOrder.getTransid(),orderStatusEnum);
		if(!updateStatus){
			OrderExceptionDTO orderExceptionDTO = new OrderExceptionDTO(transOrder.getOrderid(), "冻结成功修改订单状态失败", OrderExceptionEnum.TransOrder_update, JSON.toJSONString(transOrder.getTransid()));
			titanOrderService.saveOrderException(orderExceptionDTO);
		}
		
		return toMsgJson(TitanMsgCodeEnum.TITAN_SUCCESS,transOrder.getOrderid());
	}
	

	private TransferRequest convertToTransferRequest(TitanPaymentRequest titanPaymentRequest){
		TransferRequest transferRequest = new TransferRequest();
		transferRequest.setCreator(titanPaymentRequest.getCreator());
    	transferRequest.setUserid(titanPaymentRequest.getUserid());										//转出的用户
    	transferRequest.setRequestno(OrderGenerateService.genResquestNo());									//业务订单号
    	transferRequest.setRequesttime(DateUtil.sdf4.format(new Date()));				//请求时间
    	transferRequest.setAmount(NumberUtil.covertToCents(titanPaymentRequest.getTradeAmount()));										//金额 必须是分
    	transferRequest.setUserfee("0");									
    	transferRequest.setUserrelateid(titanPaymentRequest.getUserrelateid());	                   //接收方用户Id
    	transferRequest.setOrderid(titanPaymentRequest.getOrderid());
		return transferRequest;
	}
	
	@RequestMapping("qrCodePayment")
	public String qrCodePayment(HttpServletRequest request,TitanPaymentRequest titanPaymentRequest,Model model) throws Exception{
		this.packageRechargeData(request, titanPaymentRequest, model);
		return CommonConstant.PAY_WX;
	}
	
	/**
	 * 需要充值的接口
	 * @param request
	 * @param paymentRequest
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("packageRechargeData")
	public String packageRechargeData(HttpServletRequest request,TitanPaymentRequest titanPaymentRequest,Model model) throws Exception{
		log.info("网银支付请求参数:"+JsonConversionTool.toJson(titanPaymentRequest));
		model.addAttribute(CommonConstant.RESULT, CommonConstant.OPERATE_FAIL);
		if(null == titanPaymentRequest || !StringUtil.isValidString(titanPaymentRequest.getTradeAmount()) 
				|| !StringUtil.isValidString(titanPaymentRequest.getPayAmount())){
			model.addAttribute(CommonConstant.RETURN_MSG, "参数错误");
			return CommonConstant.GATE_WAY_PAYGE;
		}
		
		if(!titanPaymentRequest.getPaySource().equals(PaySourceEnum.RECHARDE.getDeskCode()) )
		{
	        Map<String,String> validResult = this.validPaymentData(titanPaymentRequest);
	        if(!CommonConstant.OPERATE_SUCCESS.equals(validResult.get(CommonConstant.RESULT))){//合规性验证
	        	model.addAttribute(CommonConstant.RETURN_MSG, validResult.get(CommonConstant.RETURN_MSG));
				return CommonConstant.GATE_WAY_PAYGE;
	        }
		}
		// 确认收银台支付类型是否存在
		CashierItemTypeEnum cashierItemTypeEnum = CashierItemTypeEnum
				.getCashierItemTypeEnumByKey(titanPaymentRequest
						.getLinePayType());

		if (cashierItemTypeEnum == null) {
			model.addAttribute(CommonConstant.RETURN_MSG,
					TitanMsgCodeEnum.PARAMETER_VALIDATION_FAILED.getKey());
			return CommonConstant.GATE_WAY_PAYGE;
		}
		
		// 开始计算并设置费率
		TitanRateComputeReq computeReq = new TitanRateComputeReq();
		computeReq.setAmount(titanPaymentRequest.getPayAmount());
		computeReq.setItemTypeEnum(cashierItemTypeEnum);
		computeReq.setUserId(titanPaymentRequest.getUserid());
		if(TitanConstantDefine.EXTERNAL_PAYMENT_ACCOUNT.equals(titanPaymentRequest.getUserid())){
			computeReq.setUserId(titanPaymentRequest.getUserrelateid());
		}
		
		//设置费率信息
		titanPaymentRequest = titanRateService.setRateAmount(computeReq,
				titanPaymentRequest);
        TransOrderCreateResponse transOrderCreateResponse = titanFinancialTradeService.createRsOrder(titanPaymentRequest);
        if(!transOrderCreateResponse.isResult() || !StringUtil.isValidString(transOrderCreateResponse.getOrderNo()) ){
        	log.error("call createTitanTransOrder fail msg["+ JsonConversionTool.toJson(transOrderCreateResponse)+"]");
        	model.addAttribute("msg", transOrderCreateResponse.getReturnMessage());
        	return CommonConstant.GATE_WAY_PAYGE;
        }
        
        //设置支付方式
    	if(StringUtil.isValidString(titanPaymentRequest.getLinePayType())){
    		titanPaymentRequest.setPayType(PayTypeEnum.getPayTypeEnum(titanPaymentRequest.getLinePayType()));
    		if(CommonConstant.ALIPAY.equals(titanPaymentRequest.getBankInfo())){
    			titanPaymentRequest.setPayType(PayTypeEnum.ALIPAY_URL);
    		}
    	}
    	titanPaymentRequest.setOrderid(transOrderCreateResponse.getOrderNo());
    	
    	RechargeResponse rechargeResponse = titanPaymentService.packageRechargeData(titanPaymentRequest);
    	if(!rechargeResponse.isResult()){
    		model.addAttribute(CommonConstant.RETURN_MSG, rechargeResponse.getReturnMessage());
    		return CommonConstant.GATE_WAY_PAYGE;
    	}
		
		CreateTitanRateRecordReq req = new CreateTitanRateRecordReq();
		req.setAmount(Long.parseLong(NumberUtil.covertToCents(computeReq
				.getAmount())));
		req.setReceivablefee(Long.parseLong(titanPaymentRequest
				.getReceivablefee()));
		req.setReceivedfee(Long.parseLong(titanPaymentRequest.getReceivedfee()));
		req.setStanderdfee(Long.parseLong(titanPaymentRequest.getStandfee()));
		req.setPayType(Integer.parseInt(cashierItemTypeEnum.itemCode));
		if (StringUtil.isValidString(titanPaymentRequest.getPaySource())) {
			req.setUsedFor(Integer.parseInt(titanPaymentRequest.getPaySource()));
		}
		req.setUserId(computeReq.getUserId());
		req.setReceivableRate(titanPaymentRequest.getReceivablerate());
		req.setReceivedRate(titanPaymentRequest.getExecutionrate());
		req.setStandardRate(titanPaymentRequest.getStandardrate());
		req.setRateType(titanPaymentRequest.getRateType());
		req.setOrderNo(transOrderCreateResponse.getOrderNo());
		req.setCreator(computeReq.getUserId());
		titanRateService.addRateRecord(req);
		titanPaymentService.saveCommonPayMethod(titanPaymentRequest);
		//如果是扫码支付则调用httpClient接口进行
		if(PayTypeEnum.WECHAT_URL.getLinePayType().equals(titanPaymentRequest.getLinePayType())){
			return weChat(rechargeResponse, model);
		}
		
		return cyberBank(rechargeResponse,model);
    	
	}
	
	private String weChat(RechargeResponse rechargeResponse,Model model) throws Exception{
		RechargeDataDTO rechargeDataDTO = rechargeResponse.getRechargeDataDTO();
		if(PayTypeEnum.ALIPAY_URL.key.equals(rechargeDataDTO.getPayType())){
			rechargeDataDTO.setExpand2(CommonConstant.ALIPAY);
		}
		QrCodeResponse response = titanFinancialTradeService.getQrCodeUrl(rechargeDataDTO);
		if(!response.isResult()){
			log.error("第三方支付获取地址失败");
			model.addAttribute(CommonConstant.RETURN_MSG, TitanMsgCodeEnum.QR_EXCEPTION.getKey());
			return CommonConstant.PAY_WX;
		}
		model.addAttribute(CommonConstant.RESULT, CommonConstant.RETURN_SUCCESS);
		model.addAttribute(CommonConstant.QRCODE,response.getQrCodeDTO());
		return CommonConstant.PAY_WX;
	}
	
	//网银支付
	private String cyberBank(RechargeResponse rechargeResponse,Model model){
		model.addAttribute(CommonConstant.RESULT, CommonConstant.RETURN_SUCCESS);
    	model.addAttribute("rechargeDataDTO", rechargeResponse.getRechargeDataDTO());
    	log.info("支付请求的参数如下:"+JsonConversionTool.toJson(rechargeResponse.getRechargeDataDTO()));
    	//保存常用的支付方式
		return CommonConstant.GATE_WAY_PAYGE;
	}
 	
    private Map<String,String> validPaymentData(TitanPaymentRequest titanPaymentRequest){
    	Map<String,String> resultMap = new HashMap<String, String>();
    	//判断收款方是否存在
    	AccountCheckResponse  accountCheckResponse = titanPaymentService.accountIsExist(titanPaymentRequest.getRecieveOrgName(),
				titanPaymentRequest.getRecieveTitanCode());
		
		if(null ==accountCheckResponse){
			resultMap.put(CommonConstant.RETURN_MSG, "收款方不存在");
			resultMap.put(CommonConstant.RESULT, CommonConstant.OPERATE_FAIL);
			return resultMap;
		}
		
		titanPaymentRequest.setUserrelateid(accountCheckResponse.getUserid());
		//TODO  支付请求发生时需要确定金额
		
		//交易金额
		BigDecimal tradeAmount = new BigDecimal(titanPaymentRequest.getTradeAmount());
		//在线支付金额
		BigDecimal  payAmount = new BigDecimal(titanPaymentRequest.getPayAmount());
		
		if(tradeAmount.subtract(payAmount).compareTo(BigDecimal.ZERO)==1){//有转账金额,需要输入密码
			boolean isAllowNoPwdPay = titanPaymentService.isAllowNoPwdPay(titanPaymentRequest.getUserid(), titanPaymentRequest.getTradeAmount());
	        if(!isAllowNoPwdPay){//不允许免密支付，需要输入密码
	        	boolean isTrue = titanPaymentService.checkPwd(titanPaymentRequest.getPayPassword(), titanPaymentRequest.getFcUserid());
	            if(!isTrue){
	    			resultMap.put(CommonConstant.RETURN_MSG, "付款密码不正确");
	    			resultMap.put(CommonConstant.RESULT, CommonConstant.OPERATE_FAIL);
	    			return resultMap;
	            }
	        }
		}
		
		if(StringUtil.isValidString(titanPaymentRequest.getUserrelateid())){//收款方不为空，则判断是否自己给自己付款
			if(titanPaymentRequest.getUserrelateid().equals(titanPaymentRequest.getUserid())){
    			resultMap.put(CommonConstant.RETURN_MSG, "收款账户不能和付款账户相同");
    			resultMap.put(CommonConstant.RESULT, CommonConstant.OPERATE_FAIL);
    			return resultMap;
			}
		}
		resultMap.put(CommonConstant.RESULT, CommonConstant.OPERATE_SUCCESS);
		return resultMap;
    }	
    

	@RequestMapping("payConfirmPage")
	public String payConfirmPage(RechargeResultConfirmRequest rechargeResultConfirmRequest,Model model) throws NamingException{
		
		return titanPaymentService.payConfirmPage(rechargeResultConfirmRequest,model);
	}
	
	@RequestMapping("confirmOrder")
	@ResponseBody
    public Map<String,String> confirmOrder(String orderNo){
		Map<String,String> resultMap = new HashMap<String, String>();
		resultMap.put(CommonConstant.RETURN_MSG, titanOrderService.confirmOrderStatus(orderNo));
    	return resultMap;
    }
	
	@RequestMapping("wxPicture")
	public void getWxPicture(String url,HttpServletResponse response){
		try{
			Wxutil.createRqCode(url, 170, 170, response.getOutputStream());
		}catch(Exception e){
			log.error("微信生成图片错误："+e.getMessage());
		}
	}
	 private void lockOutTradeNoList(String out_trade_no) throws InterruptedException {
			synchronized (mapLock) {
				if(mapLock.containsKey(out_trade_no)) {
					synchronized (mapLock.get(out_trade_no)) 
					{
						mapLock.get(out_trade_no).wait();
					}
				}
				else{
					mapLock.put(out_trade_no, new Object());
				}
				
			}
		}
		
		private void unlockOutTradeNoList(String out_trade_no) {
			if(mapLock.containsKey(out_trade_no)){
				synchronized (mapLock.get(out_trade_no)) {
					mapLock.remove(out_trade_no).notifyAll();
				}
			}
		}
	    
}
