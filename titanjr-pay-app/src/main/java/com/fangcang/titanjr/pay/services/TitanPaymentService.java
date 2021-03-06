package com.fangcang.titanjr.pay.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.fangcang.titanjr.enums.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.fangcang.titanjr.common.enums.BusinessLog;
import com.fangcang.titanjr.common.enums.FreezeTypeEnum;
import com.fangcang.titanjr.common.enums.OrderKindEnum;
import com.fangcang.titanjr.common.enums.OrderStatusEnum;
import com.fangcang.titanjr.common.enums.PayerTypeEnum;
import com.fangcang.titanjr.common.enums.QuickPayBankEnum;
import com.fangcang.titanjr.common.enums.TitanjrVersionEnum;
import com.fangcang.titanjr.common.enums.TransferReqEnum;
import com.fangcang.titanjr.common.util.CommonConstant;
import com.fangcang.titanjr.common.util.DateUtil;
import com.fangcang.titanjr.common.util.JsonConversionTool;
import com.fangcang.titanjr.common.util.OrderGenerateService;
import com.fangcang.titanjr.common.util.Tools;
import com.fangcang.titanjr.dto.bean.AccountHistoryDTO;
import com.fangcang.titanjr.dto.bean.CommonPayMethodDTO;
import com.fangcang.titanjr.dto.bean.PayMethodConfigDTO;
import com.fangcang.titanjr.dto.bean.SysConfig;
import com.fangcang.titanjr.dto.bean.TitanOrderPayDTO;
import com.fangcang.titanjr.dto.bean.TitanTransferDTO;
import com.fangcang.titanjr.dto.bean.TitanUserBindInfoDTO;
import com.fangcang.titanjr.dto.bean.TransOrderDTO;
import com.fangcang.titanjr.dto.bean.gateway.CommonPayHistoryDTO;
import com.fangcang.titanjr.dto.request.AccountCheckRequest;
import com.fangcang.titanjr.dto.request.AccountHistoryRequest;
import com.fangcang.titanjr.dto.request.AddPayLogRequest;
import com.fangcang.titanjr.dto.request.FinancialOrganQueryRequest;
import com.fangcang.titanjr.dto.request.JudgeAllowNoPwdPayRequest;
import com.fangcang.titanjr.dto.request.PayMethodConfigRequest;
import com.fangcang.titanjr.dto.request.RechargeRequest;
import com.fangcang.titanjr.dto.request.RechargeResultConfirmRequest;
import com.fangcang.titanjr.dto.request.TitanPaymentRequest;
import com.fangcang.titanjr.dto.request.TransOrderRequest;
import com.fangcang.titanjr.dto.request.TransferRequest;
import com.fangcang.titanjr.dto.response.AccountCheckResponse;
import com.fangcang.titanjr.dto.response.AllowNoPwdPayResponse;
import com.fangcang.titanjr.dto.response.FinancialOrganResponse;
import com.fangcang.titanjr.dto.response.FreezeAccountBalanceResponse;
import com.fangcang.titanjr.dto.response.RechargeResponse;
import com.fangcang.titanjr.service.BusinessLogService;
import com.fangcang.titanjr.service.TitanCashierDeskService;
import com.fangcang.titanjr.service.TitanFinancialAccountService;
import com.fangcang.titanjr.service.TitanFinancialOrganService;
import com.fangcang.titanjr.service.TitanFinancialTradeService;
import com.fangcang.titanjr.service.TitanFinancialUserService;
import com.fangcang.titanjr.service.TitanFinancialUtilService;
import com.fangcang.titanjr.service.TitanOrderService;
import com.fangcang.util.StringUtil;

@Component
public class TitanPaymentService {
	private static final Log log = LogFactory.getLog(TitanPaymentService.class);

	@Resource
	private TitanFinancialAccountService titanFinancialAccountService;

	@Resource
	private TitanFinancialTradeService titanFinancialTradeService;

	@Resource
	private TitanFinancialUserService titanFinancialUserService;

	@Resource
	private TitanOrderService titanOrderService;

	@Resource
	private TitanCashierDeskService titanCashierDeskService;

	@Resource
	private TitanFinancialOrganService titanFinancialOrganService;

	@Resource
	private TitanFinancialUtilService titanFinancialUtilService;
	
	@Resource
	private BusinessLogService businessLogService;
	
	 public AccountCheckResponse accountIsExist(String orgName,String titanCode ){
	    	if(StringUtil.isValidString(orgName)&&
	    			StringUtil.isValidString(titanCode)){
	    		AccountCheckRequest accountCheckRequest = new AccountCheckRequest();
	    		accountCheckRequest.setOrgName(orgName);
	    		accountCheckRequest.setTitanCode(titanCode);
	    		accountCheckRequest.setStatusId(1);//just query active org
	    		AccountCheckResponse accountCheckResponse = titanFinancialAccountService.checkTitanCode(accountCheckRequest);
	    		if(accountCheckResponse.isCheckResult()){
	    		   return accountCheckResponse;
	    		}
	    	}
			return null;
	    } 
	 
	 public boolean isAllowNoPwdPay(String userid,String totalAmount){
			JudgeAllowNoPwdPayRequest judgeAllowNoPwdPayRequest = new JudgeAllowNoPwdPayRequest();
			judgeAllowNoPwdPayRequest.setMoney(totalAmount);
			judgeAllowNoPwdPayRequest.setUserid(userid);
			AllowNoPwdPayResponse allowNoPwdPayResponse = titanFinancialTradeService.isAllowNoPwdPay(judgeAllowNoPwdPayRequest);
		    if(allowNoPwdPayResponse.isAllowNoPwdPay()){
		    	return true;
		    }
			return false;
		}
	 
	 public boolean checkPwd(String pwd, String fcUserId, String partenerOrgCode){
		 if(!StringUtil.isValidString(pwd) || !StringUtil.isValidString(fcUserId)){
			 return false;
		 }
		 
		 TitanUserBindInfoDTO titanUserBindInfoDTO = new TitanUserBindInfoDTO();
		 titanUserBindInfoDTO.setFcuserid(Long.parseLong(fcUserId));
		 titanUserBindInfoDTO.setMerchantcode(partenerOrgCode);
		 titanUserBindInfoDTO = titanFinancialUserService.getUserBindInfoByFcuserid(titanUserBindInfoDTO);
		 
		 if(null == titanUserBindInfoDTO || null ==titanUserBindInfoDTO.getTfsuserid()){
			 return false;
		 }
		 
		 return titanFinancialUserService.checkPayPassword(pwd,titanUserBindInfoDTO.getTfsuserid().toString());
	 }
	   

	public RechargeResponse packageRechargeData(
			TitanPaymentRequest titanPaymentRequest) {
		RechargeRequest rechargeRequest = this
				.convertToRechargePageRequest(titanPaymentRequest);
		return titanFinancialTradeService.packageRechargeData(rechargeRequest);
	}

	private RechargeRequest convertToRechargePageRequest(
			TitanPaymentRequest titanPaymentRequest) {
		RechargeRequest rechargeRequest = new RechargeRequest();
		// 根据系统生成的单号在本地查询订单信息
		TransOrderRequest transOrderRequest = new TransOrderRequest();
		transOrderRequest.setOrderid(titanPaymentRequest.getOrderid());
		// 获取本地落单信息
		// 为什么取第一个单，因为若单是成功或者失败，在下单的时候就获取不到单号，只有在处理中的单才能
		TransOrderDTO transOrderDTO = titanOrderService
				.queryTransOrderDTO(transOrderRequest);

		if (null == transOrderDTO || transOrderDTO.getAmount() == null) {
			return rechargeRequest;
		}
		rechargeRequest.setTransorderid(transOrderDTO.getTransid());
		rechargeRequest.setOrderAmount(transOrderDTO.getAmount().toString());
		rechargeRequest.setProductName(transOrderDTO.getGoodsname());
		rechargeRequest.setUserid(transOrderDTO.getUserid());
		rechargeRequest.setOrderNo(titanPaymentRequest.getOrderid());
		rechargeRequest.setProductNum(CommonConstant.DEFAULT_PRODUCT_NUM);
		rechargeRequest.setAmtType(AmtTypeEnum.RMB.getKey());
		rechargeRequest.setPayerAcount(titanPaymentRequest.getPayerAcount());
		rechargeRequest.setBankInfo(titanPaymentRequest.getBankInfo());
		rechargeRequest.setOrderTime(DateUtil.sdf5.format(new Date()));
		rechargeRequest.setOrderExpireTime(CommonConstant.DEFAULT_EXPIRE_TIME);
		rechargeRequest.setSignType(SignTypeEnum.MD5.getKey());
		rechargeRequest.setBusiCode(BusiCodeEnum.MerchantOrder.getKey());
		rechargeRequest.setOrderMark(OrderMarkEnum.InsideOrder.getKey());
		rechargeRequest.setCharset(CharsetEnum.UTF_8.getKey());
		rechargeRequest.setReceivablefee(Long.parseLong(titanPaymentRequest
				.getReceivablefee()));
		rechargeRequest.setReceivedfee(Long.parseLong(titanPaymentRequest
				.getReceivedfee()));
		rechargeRequest.setStandfee(Long.parseLong(titanPaymentRequest
				.getStandfee()));
		rechargeRequest.setStandardrate(titanPaymentRequest.getStandardrate());
		rechargeRequest
				.setExecutionrate(titanPaymentRequest.getExecutionrate());
		rechargeRequest.setRatetype(titanPaymentRequest.getRateType());

		if (titanPaymentRequest.getPayType() != null) {
			rechargeRequest.setPayType(titanPaymentRequest.getPayType()
					.getKey());
		}
		PayMethodConfigRequest payMethodConfigRequest = new PayMethodConfigRequest();
		payMethodConfigRequest.setUserId(titanPaymentRequest.getUserid());
		PayMethodConfigDTO payMethodConfigDTO = titanFinancialUtilService
				.getPayMethodConfigDTO(payMethodConfigRequest);
		if (payMethodConfigDTO != null) {
			rechargeRequest.setPageUrl(payMethodConfigDTO.getPageurl());
			rechargeRequest.setNotifyUrl(payMethodConfigDTO.getNotifyurl());
		}
		//新版收银台增加参数
		rechargeRequest.setVersion(titanPaymentRequest.getRsVersion());
		rechargeRequest.setIdCode(titanPaymentRequest.getIdCode());
		rechargeRequest.setPayerAccountType(titanPaymentRequest.getPayerAccountType());
		rechargeRequest.setPayerName(titanPaymentRequest.getPayerName());
		rechargeRequest.setPayerPhone(titanPaymentRequest.getPayerPhone());
		rechargeRequest.setSafetyCode(titanPaymentRequest.getSafetyCode());
		rechargeRequest.setValidthru(titanPaymentRequest.getValidthru());

		return rechargeRequest;
	}

	public void saveCommonPayMethod(TitanPaymentRequest titanPaymentRequest) {
		try {
			CommonPayMethodDTO commonPayMethodDTO = new CommonPayMethodDTO();
			if (StringUtil.isValidString(titanPaymentRequest.getDeskId())) {
				commonPayMethodDTO.setDeskid(Integer
						.parseInt(titanPaymentRequest.getDeskId()));
			}
			commonPayMethodDTO.setBankname(titanPaymentRequest.getBankInfo());
			commonPayMethodDTO.setCreator(titanPaymentRequest.getCreator());
			commonPayMethodDTO.setCreatetime(new Date());
			if (StringUtil.isValidString(titanPaymentRequest.getLinePayType())) {
				commonPayMethodDTO.setPaytype(Integer
						.parseInt(titanPaymentRequest.getLinePayType()));
			}
			titanCashierDeskService.saveCommonPayMethod(commonPayMethodDTO);
		} catch (Exception e) {
			log.error("保存常用的支付方式失败" + e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	public void saveCommonPayHistory(TitanPaymentRequest titanPaymentRequest) {
		try {
			boolean isSaveHistory = false;
			
			if(StringUtil.isValidString(titanPaymentRequest.getFcUserid()) && 
					StringUtil.isValidString(titanPaymentRequest.getPartnerOrgCode())){
				
				/*UserBindInfoDTO userBindInfoDTO = null;
				UserBindInfoRequest userBindInfoRequest = new UserBindInfoRequest();
				userBindInfoRequest.setMerchantcode(titanPaymentRequest.getPartnerOrgCode());
				userBindInfoRequest.setFcuserid(Long.valueOf(titanPaymentRequest.getFcUserid()));
				log.info("保存快捷支付常用卡历史记录，查询UserBindInfo请求参数：" + Tools.gsonToString(userBindInfoRequest));
				UserBindInfoResponse userBindInfoResponse = titanFinancialUserService.queryUserBindInfoDTO(userBindInfoRequest);
				log.info("保存快捷支付常用卡历史记录，查询UserBindInfo返回结果：" + Tools.gsonToString(userBindInfoResponse));
				
				if(CollectionUtils.isNotEmpty(userBindInfoResponse.getPaginationSupport().getItemList())){
					userBindInfoDTO = userBindInfoResponse.getPaginationSupport().getItemList().get(0);
				}
				if(userBindInfoDTO != null && titanPaymentRequest.getFcUserid().equals(String.valueOf(
						userBindInfoDTO.getFcUserId()))){
					isSaveHistory = true;
				}*/
				
				//只要传了第三方商家ID和用户ID就可以保存快捷支付历史，不需要校验绑定信息
				isSaveHistory = true;
				
			}
			
			log.info("=============>>isSaveHistory：" + isSaveHistory);
			if(isSaveHistory){
				
				CommonPayHistoryDTO commonPayHistoryDTO = new CommonPayHistoryDTO();
				commonPayHistoryDTO.setOrgcode(titanPaymentRequest.getUserid());
				commonPayHistoryDTO.setFcuserid(titanPaymentRequest.getFcUserid());
				commonPayHistoryDTO.setDeskid(titanPaymentRequest.getDeskId());
				commonPayHistoryDTO.setPaytype(titanPaymentRequest.getPayType().getLinePayType());
				commonPayHistoryDTO.setBankinfo(titanPaymentRequest.getBankInfo());
				commonPayHistoryDTO.setBankname(QuickPayBankEnum.getBankName(titanPaymentRequest.getBankInfo(), 
						titanPaymentRequest.getPayerAccountType()));
				commonPayHistoryDTO.setPayername(titanPaymentRequest.getPayerName());
				commonPayHistoryDTO.setPayeracount(titanPaymentRequest.getPayerAcount());
				commonPayHistoryDTO.setPayeraccounttype(titanPaymentRequest.getPayerAccountType());
				commonPayHistoryDTO.setPayerphone(titanPaymentRequest.getPayerPhone());
				commonPayHistoryDTO.setIdcode(titanPaymentRequest.getIdCode());
				commonPayHistoryDTO.setSafetycode(titanPaymentRequest.getSafetyCode());
				commonPayHistoryDTO.setValidthru(titanPaymentRequest.getValidthru());
				commonPayHistoryDTO.setCreator(titanPaymentRequest.getCreator());
				
				titanCashierDeskService.saveCommonPayHistory(commonPayHistoryDTO);
			}
			
		} catch (Exception e) {
			log.error("保存常用的支付方式失败" + e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public boolean validateIsConfirmed(Integer transId) {
		TitanTransferDTO titanTransferDTO = new TitanTransferDTO();
		titanTransferDTO.setTransorderid(transId);
		List<TitanTransferDTO> titanTransferDTOList = titanOrderService
				.getTitanTransferDTOList(titanTransferDTO);
		if (titanTransferDTOList != null && titanTransferDTOList.size() > 0) {
			for (TitanTransferDTO transferDTO : titanTransferDTOList) {
				if (transferDTO.getStatus() != null) {
					if (TransferReqEnum.TRANSFER_SUCCESS.getStatus() == transferDTO
							.getStatus().intValue()) {
						return false;
					}
				}

			}
		}
		return true;
	}

	public TransferRequest convertToTransferRequest(TransOrderDTO transOrderDTO) {
		TransferRequest transferRequest = new TransferRequest();
		transferRequest.setCreator(transOrderDTO.getCreator());
		transferRequest.setUserid(transOrderDTO.getUserid()); // 转出的用户
		transferRequest.setRequestno(OrderGenerateService.genResquestNo()); // 业务订单号
		transferRequest.setRequesttime(DateUtil.sdf4.format(new Date())); // 请求时间
		PayerTypeEnum payerTypeEnum = PayerTypeEnum
				.getPayerTypeEnumByKey(transOrderDTO.getPayerType());
		if (transOrderDTO.getTradeamount() != null) {
			String amount = transOrderDTO.getTradeamount().toString();
			if (payerTypeEnum != null && transOrderDTO.getReceivedfee() != null) {
				if (!payerTypeEnum.isNeedPayerInfo()) {// 收款方出手续费的，交易金额减去手续费
					amount = new BigDecimal(transOrderDTO.getTradeamount())
							.subtract(
									new BigDecimal(transOrderDTO
											.getReceivedfee())).toString();
				}
			}
			transferRequest.setAmount(amount);
		}
		transferRequest.setUserfee("0");
		transferRequest.setOrderid(transOrderDTO.getOrderid());
		transferRequest.setUserrelateid(transOrderDTO.getUserrelateid());
		transferRequest.setProductId(transOrderDTO.getProductid());
		return transferRequest;
	}
	
	public TransferRequest getRevenueAccountTransferRequest(TransOrderDTO transOrderDTO){
		TransferRequest transferRequest = new TransferRequest();
		transferRequest.setCreator(transOrderDTO.getCreator());
		transferRequest.setUserid(transOrderDTO.getUserid()); // 转出的用户
		transferRequest.setProductId(transOrderDTO.getProductid());
		transferRequest.setRequestno(OrderGenerateService.genResquestNo()); // 业务订单号
		transferRequest.setRequesttime(DateUtil.sdf4.format(new Date())); // 请求时间
		transferRequest.setAmount(String.valueOf(transOrderDTO.getReceivedfee()));
		transferRequest.setUserfee("0");
		transferRequest.setOrderid(transOrderDTO.getOrderid());
		transferRequest.setUserrelateid(CommonConstant.RS_FANGCANG_USER_ID); // 转入的用户
		transferRequest.setInterproductid(CommonConstant.RS_FANGCANG_PRODUCT_ID_229);
		return transferRequest;
	}

	/**
	 * 
	 * @author Jerry
	 * @date 2017年8月9日 下午2:40:13
	 * @param transferRequest transOrderDTO
	 * @return -1冻结失败   0不需要冻结	1冻结付款方 2冻结在收款方
	 */
	public int freezeAccountBalance(TransferRequest transferRequest,
			TransOrderDTO transOrderDTO) {
		int freezeStatus = -1;
		try {
			RechargeResultConfirmRequest rechargeResultConfirmRequest = new RechargeResultConfirmRequest();
			rechargeResultConfirmRequest.setOrderNo(transOrderDTO.getOrderid());
			rechargeResultConfirmRequest.setPayAmount(transferRequest
					.getAmount());
			rechargeResultConfirmRequest.setOrderAmount(transferRequest
					.getAmount());
			
			if(FreezeTypeEnum.UNFREEZE.getKey().equals(transOrderDTO.getFreezeType())){
				log.info("转账到收款方，不冻结,订单号："+transOrderDTO.getOrderid());
				return 0;//不需要冻结，直接返回
			}else if(FreezeTypeEnum.FREEZE_PAYER.getKey().equals(transOrderDTO.getFreezeType())){
				//冻结在付款方,冻结金额需要考虑手续费
				if(transOrderDTO.getReceivedfee() != null && transOrderDTO.getReceivedfee() > 0){
					log.info("冻结方案是3，转账（冻结）金额需要加上手续费，订单号："+transOrderDTO.getOrderid());
					rechargeResultConfirmRequest.setPayAmount(String.valueOf(Integer.parseInt(transferRequest.getAmount()) + transOrderDTO.getReceivedfee()));
				}
				rechargeResultConfirmRequest.setUserid(transferRequest.getUserid());
				freezeStatus = 1;
				log.info("不转账，资金冻结在付款方，订单号："+transOrderDTO.getOrderid());
			}else{
				rechargeResultConfirmRequest.setUserid(transferRequest.getUserrelateid());//冻结在收款方
				freezeStatus = 2;
				log.info("转账到收款方，资金冻结在收款方，订单号："+transOrderDTO.getOrderid());
			}
			
			FreezeAccountBalanceResponse freezeAccountBalanceResponse = titanFinancialAccountService
					.freezeAccountBalance(rechargeResultConfirmRequest);
			if (!freezeAccountBalanceResponse.isFreezeSuccess()) {
				freezeStatus = -1;//冻结失败
				log.error("冻结失败，原因：" + freezeAccountBalanceResponse.getReturnMessage());
			}
			
		} catch (Exception e) {
			freezeStatus = -1;
			log.error("冻结余额失败,订单号："+transOrderDTO.getOrderid(), e);
		}
		return freezeStatus;
	}

	// 更新订单状态
	public boolean updateOrderStatus(TransOrderDTO transOrderDTO,
			OrderStatusEnum orderStatusEnum) {
		try {
			TransOrderDTO transOrderReq = new TransOrderDTO();
			transOrderReq.setStatusid(orderStatusEnum.getStatus());
			transOrderReq.setTransid(transOrderDTO.getTransid());
			transOrderReq.setFreezeAt(transOrderDTO.getFreezeAt());
			boolean flag = titanOrderService.updateTransOrder(transOrderReq);
			return flag;
		} catch (Exception e) {
			log.error("更新订单失败" + e.getMessage(), e);
		}
		return false;
	}

	public String payConfirmPage(
			RechargeResultConfirmRequest rechargeResultConfirmRequest,
			Model model) {

		if (StringUtil.isValidString(rechargeResultConfirmRequest.getOrderNo())
				|| StringUtil.isValidString(rechargeResultConfirmRequest
						.getPayOrderNo())) {

			TransOrderRequest transOrderRequest = new TransOrderRequest();
			if (StringUtil.isValidString(rechargeResultConfirmRequest
					.getOrderNo())) {
				transOrderRequest.setOrderid(rechargeResultConfirmRequest
						.getOrderNo());
			}
			if (StringUtil.isValidString(rechargeResultConfirmRequest
					.getPayOrderNo())
					&& (!StringUtil.isValidString(rechargeResultConfirmRequest
							.getOrderNo()))) {
				transOrderRequest.setPayorderno(rechargeResultConfirmRequest
						.getPayOrderNo());
			}
			TransOrderDTO transOrderDTO = titanOrderService
					.queryTransOrderDTO(transOrderRequest);
			if (transOrderDTO != null) {
				businessLogService.addPayLog(new AddPayLogRequest(BusinessLog.PayStep.PayConfirmPage, OrderKindEnum.TransOrderId, transOrderDTO.getTransid()+""));
				model.addAttribute("transOrderDTO", transOrderDTO);
				FinancialOrganQueryRequest organQueryRequest = new FinancialOrganQueryRequest();
				organQueryRequest.setOrgCode(transOrderDTO.getPayeemerchant());
				FinancialOrganResponse financialOrganResponse = titanFinancialOrganService
						.queryBaseFinancialOrgan(organQueryRequest);
				if (financialOrganResponse.isResult()) {
					model.addAttribute("financialOrganDTO",
							financialOrganResponse.getFinancialOrganDTO());
				}

				if (StringUtil.isValidString(rechargeResultConfirmRequest
						.getPayOrderNo())
						&& (!StringUtil
								.isValidString(rechargeResultConfirmRequest
										.getOrderNo()))) {
					model.addAttribute("payType", "贷款支付");
					return "checkstand-pay/loanPayResult";
				} else {
					model.addAttribute("payType", "网银支付");
					if (!StringUtil.isValidString(rechargeResultConfirmRequest
							.getPayStatus())) {// 判断是本地回调

						boolean paySuccess = OrderStatusEnum
								.isPaySuccess(transOrderDTO.getStatusid());
						rechargeResultConfirmRequest
								.setOrderPayTime(DateUtil.sdf5
										.format(transOrderDTO.getCreatetime()));
						rechargeResultConfirmRequest.setPayMsg("付款失败");
						if (paySuccess) {
							rechargeResultConfirmRequest.setPayStatus("3");
							rechargeResultConfirmRequest.setPayMsg("付款成功");
							PayerTypeEnum payerTypeEnum = PayerTypeEnum
									.getPayerTypeEnumByKey(transOrderDTO.getPayerType());
							Long receivedfee = 0L;
							//新版收银台，付款方出手续费时，余额支付的实付金额需要加上手续费
							if (TitanjrVersionEnum.isVersion2(transOrderDTO.getVersion()) 
									&& transOrderDTO.getReceivedfee() != null 
									&& payerTypeEnum != null && payerTypeEnum.isNeedPayerInfo()) {
								receivedfee = transOrderDTO.getReceivedfee();
							}
							rechargeResultConfirmRequest.setPayAmount(
									new BigDecimal(transOrderDTO.getTradeamount() + receivedfee).toString());
						}
						model.addAttribute("payType", "余额支付");
					}

					TitanOrderPayDTO payOrder = new TitanOrderPayDTO();
					payOrder.setOrderNo(rechargeResultConfirmRequest
							.getOrderNo());
					TitanOrderPayDTO payOrderDTO = titanOrderService
							.getTitanOrderPayDTO(payOrder);
					if (payOrderDTO != null) {
						if (PayTypeEnum.ALIPAY_URL.getKey().equals(
								payOrderDTO.getPayType())) {
							rechargeResultConfirmRequest
									.setPayAmount(new BigDecimal(transOrderDTO
											.getAmount()).toString());
							model.addAttribute("payType", "支付宝支付");
						} else if (PayTypeEnum.WECHAT_URL.getKey().equals(
								payOrderDTO.getPayType())) {
							rechargeResultConfirmRequest
									.setPayAmount(new BigDecimal(transOrderDTO
											.getAmount()).toString());
							model.addAttribute("payType", "微信支付");
						}else if(PayTypeEnum.QUICK_PAY_NEW.getKey().equals(payOrderDTO.getPayType())){
							model.addAttribute("payType", "快捷支付");
						}
					}

					if (StringUtil.isValidString(rechargeResultConfirmRequest
							.getExpand())
							&& rechargeResultConfirmRequest.getExpand().equals(
									CommonConstant.ORDER_DELAY)) {
						rechargeResultConfirmRequest.setPayStatus("3");
						rechargeResultConfirmRequest.setPayMsg("延迟到账，稍后查询");
					}
				}
			} else {
				log.error("显示支付结果页，订单没有找到，参数transOrderRequest："
						+ Tools.gsonToString(transOrderRequest));
			}
		} else {
			log.error("显示支付结果页,订单不存在，参数rechargeResultConfirmRequest:"
					+ Tools.gsonToString(rechargeResultConfirmRequest));
		}
		model.addAttribute("rechargeResultConfirmRequest",
				rechargeResultConfirmRequest);
		return "checkstand-pay/payResult";
	}

	public void addAccountHistory(TransOrderDTO transOrderDTO) {
		// 记录收款历史
		AccountHistoryRequest accountHistoryRequest = null;
		AccountHistoryDTO accountHistoryDTO = convertToAccountHistoryDTO(transOrderDTO);
		if (accountHistoryDTO != null) {
			accountHistoryRequest = new AccountHistoryRequest();
			accountHistoryRequest.setAccountHistoryDTO(accountHistoryDTO);
			titanFinancialAccountService
					.addAccountHistory(accountHistoryRequest);
		}
	}

	private AccountHistoryDTO convertToAccountHistoryDTO(
			TransOrderDTO transOrderDTO) {
		if (null == transOrderDTO
				|| !StringUtil.isValidString(transOrderDTO.getBusinessinfo())) {
			return null;
		}
		AccountHistoryDTO accountHistoryDTO = null;
		Map<String, String> bussinessInfoMap = JsonConversionTool.toObject(
				transOrderDTO.getBusinessinfo(), Map.class);
		if (bussinessInfoMap != null) {
			String inAccountCode = bussinessInfoMap.get("inAccountCode");
			String outAccountCode = bussinessInfoMap.get("outAccountCode");
			if(StringUtil.isValidString(inAccountCode)&&StringUtil.isValidString(outAccountCode)){
				accountHistoryDTO = new AccountHistoryDTO();
				accountHistoryDTO.setInaccountcode(inAccountCode);
				accountHistoryDTO.setOutaccountcode(outAccountCode);
				accountHistoryDTO.setPayeeuserid(transOrderDTO.getPayeemerchant());
				accountHistoryDTO.setPayeruserid(transOrderDTO.getPayermerchant());
				accountHistoryDTO.setCreatetime(new Date());
			}
		}
		
		return accountHistoryDTO;
	}

	public String getSign(
			RechargeResultConfirmRequest rechargeResultConfirmRequest) {
		StringBuffer stringBuffer = new StringBuffer();
		if (rechargeResultConfirmRequest != null) {
			stringBuffer.append("merchantNo=");
			stringBuffer.append(rechargeResultConfirmRequest.getMerchantNo());
			stringBuffer.append("&payType=");
			stringBuffer.append(rechargeResultConfirmRequest.getPayType());
			stringBuffer.append("&orderNo=");
			stringBuffer.append(rechargeResultConfirmRequest.getOrderNo());
			stringBuffer.append("&payOrderNo=");
			stringBuffer.append(rechargeResultConfirmRequest.getPayOrderNo());
			stringBuffer.append("&payStatus=");
			stringBuffer.append(rechargeResultConfirmRequest.getPayStatus());
			stringBuffer.append("&orderTime=");
			stringBuffer.append(rechargeResultConfirmRequest.getOrderTime());
			stringBuffer.append("&orderAmount=");
			stringBuffer.append(rechargeResultConfirmRequest.getOrderAmount());
			stringBuffer.append("&bankCode=");
			stringBuffer.append(rechargeResultConfirmRequest.getBankCode());
			stringBuffer.append("&orderPayTime=");
			stringBuffer.append(rechargeResultConfirmRequest.getOrderPayTime());
			stringBuffer.append("&key=");
			SysConfig config = titanFinancialUtilService.querySysConfig();
			stringBuffer.append(config.getRsCheckKey());
		}
		return stringBuffer.toString();
	}

}
