package com.fangcang.titanjr.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fangcang.titanjr.common.enums.LoanApplyOrderEnum;
import com.fangcang.titanjr.common.enums.LoanProductEnum;
import com.fangcang.titanjr.common.util.CommonConstant;
import com.fangcang.titanjr.common.util.DateUtil;
import com.fangcang.titanjr.common.util.OrderGenerateService;
import com.fangcang.titanjr.dao.LoanCreditOrderDao;
import com.fangcang.titanjr.dao.LoanOrderDao;
import com.fangcang.titanjr.dao.LoanRoomPackSpecDao;
import com.fangcang.titanjr.dto.bean.LoanOrderBean;
import com.fangcang.titanjr.dto.bean.LoanRoomPackSpecBean;
import com.fangcang.titanjr.dto.bean.LoanSpecBean;
import com.fangcang.titanjr.dto.request.ApplyLoanRequest;
import com.fangcang.titanjr.dto.request.CancelLoanRequest;
import com.fangcang.titanjr.dto.request.GetHistoryRepaymentListRequest;
import com.fangcang.titanjr.dto.request.GetLoanOrderInfoListRequest;
import com.fangcang.titanjr.dto.request.GetLoanOrderInfoRequest;
import com.fangcang.titanjr.dto.request.GetOrgLoanStatInfoRequest;
import com.fangcang.titanjr.dto.request.RepaymentLoanRequest;
import com.fangcang.titanjr.dto.request.SaveLoanOrderInfoRequest;
import com.fangcang.titanjr.dto.response.ApplyLoanResponse;
import com.fangcang.titanjr.dto.response.CancelLoanResponse;
import com.fangcang.titanjr.dto.response.GetHistoryRepaymentListResponse;
import com.fangcang.titanjr.dto.response.GetLoanOrderInfoListResponse;
import com.fangcang.titanjr.dto.response.GetLoanOrderInfoResponse;
import com.fangcang.titanjr.dto.response.GetOrgLoanStatInfoResponse;
import com.fangcang.titanjr.dto.response.RepaymentLoanResponse;
import com.fangcang.titanjr.dto.response.SaveLoanOrderInfoResponse;
import com.fangcang.titanjr.entity.LoanApplyOrder;
import com.fangcang.titanjr.entity.LoanCreditOrder;
import com.fangcang.titanjr.entity.LoanRoomPackSpec;
import com.fangcang.titanjr.rs.dto.NewLoanApplyJsonData;
import com.fangcang.titanjr.rs.manager.RSCreditManager;
import com.fangcang.titanjr.rs.request.NewLoanApplyRequest;
import com.fangcang.titanjr.rs.response.NewLoanApplyResponse;
import com.fangcang.titanjr.service.TitanFinancialLoanService;
import com.fangcang.util.StringUtil;

@Service("titanFinancialLoanService")
public class TitanFinancialLoanServiceImpl implements TitanFinancialLoanService{
	
	private static final Log log = LogFactory
			.getLog(TitanFinancialLoanServiceImpl.class);
	
	@Resource 
	private LoanOrderDao loanOrderDao;
	
	@Resource
	private LoanRoomPackSpecDao loanRoomPackSpecDao;
	
	@Resource
	private LoanCreditOrderDao loanCreditOrderDao;
	
	@Resource 
	private RSCreditManager rsCreditManager;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
	public ApplyLoanResponse applyLoan(ApplyLoanRequest req) throws Exception {
		ApplyLoanResponse response = new ApplyLoanResponse();
		try{
			if(req ==null || req.getLcanSpec()==null ||req.getProductType()==null ){
				response.putErrorResult("参数错误");
				return response;
			}
			LoanProductEnum productType = req.getProductType();
			if(LoanProductEnum.ROOM_PACK.getCode()==productType.getCode()){
				LoanRoomPackSpecBean LoanSpecBean  = (LoanRoomPackSpecBean)req.getLcanSpec();
				//保存相关数据
				boolean flag = this.saveLoanRoomPackSpecBean(LoanSpecBean);
				if(!flag){
					log.error("保存包房贷订单失败");
					throw new Exception("保存包房贷订单失败");
				}
				
			}else {//运营贷
				
			}
			
			//保存贷款申请单
			boolean flag = saveLoanOrderBean(req.getLcanSpec(), productType.getCode(), req.getOrgCode());
			if(!flag){
				throw new Exception("保存订单失败");
			}
			
			//上传文件到融数，并且获取相应的urlKey
			String urlKey = "150a15fa-b2fa-4c72-95b5-20f173c47814";
			
			//申请贷款
			NewLoanApplyRequest request = this.convertToNewLoanApplyRequest(req.getLcanSpec(), productType.getCode(), req.getOrgCode());
			request.setUrlkey(urlKey);
			this.packLoanJSonData(request, req.getLcanSpec(), productType.getCode());
			NewLoanApplyResponse loanResponse = rsCreditManager.newLoanApply(request);
			if(!loanResponse.isSuccess()){
				log.error(loanResponse.getReturnCode()+":"+loanResponse.getReturnMsg());
				throw new Exception(loanResponse.getReturnCode()+":"+loanResponse.getReturnMsg());
			}
			response.putSuccess();
			return response;
			
		}catch(Exception e){
			log.error("贷款申请异常",e);
			throw e;
		}
	}
	
	private boolean saveLoanRoomPackSpecBean(LoanRoomPackSpecBean loanSpecBean) throws Exception{
		LoanRoomPackSpec loanRoomPackSpec = new LoanRoomPackSpec();
		try{
			loanRoomPackSpec.setAccount(loanSpecBean.getAccount());
			loanRoomPackSpec.setAccountName(loanSpecBean.getAccountName());
			loanRoomPackSpec.setBank(loanSpecBean.getBank());
			loanRoomPackSpec.setBeginDate(loanSpecBean.getBeginDate());
			loanRoomPackSpec.setEndDate(loanSpecBean.getEndDate());
			loanRoomPackSpec.setContractUrl(loanSpecBean.getContractUrl());
			loanRoomPackSpec.setHotelName(loanSpecBean.getHotleName());
			loanRoomPackSpec.setRoomNights(loanSpecBean.getRoomNights());
			loanRoomPackSpec.setOrderNo(loanSpecBean.getLoanOrderNo());
			int row = loanRoomPackSpecDao.saveLoanRoomPackSpec(loanRoomPackSpec);
			if(row>0){
				return true;
			}
			throw new Exception("保存包房单失效");
		}catch(Exception e){
			log.error("保存包房贷信息失败",e);
			throw e;
		}
	}
	
	private boolean saveLoanOrderBean(LoanSpecBean loanSpecBean,Integer type,String orgCode) throws Exception{
		LoanApplyOrder loanApplyOrder = new LoanApplyOrder();
		try{
			
			LoanRoomPackSpecBean loanRoomPackSpecBean  = null;
			
			LoanCreditOrder loanCreditOrder = new LoanCreditOrder();
			loanCreditOrder.setOrgCode(orgCode);
			List<LoanCreditOrder> loanCreditOrderList = loanCreditOrderDao.queryLoanCreditOrder(loanCreditOrder);
			if(loanCreditOrderList.size()!=1){
				throw new Exception("查询授信申请单失败");
			}
			loanCreditOrder =  loanCreditOrderList.get(0);
			if(LoanProductEnum.ROOM_PACK.getCode()==type.intValue()){
				loanRoomPackSpecBean = (LoanRoomPackSpecBean)loanSpecBean;
				loanApplyOrder.setCreditOrderNo(loanCreditOrder.getOrderNo());
				loanApplyOrder.setOrderNo(loanRoomPackSpecBean.getLoanOrderNo());
				if(StringUtil.isValidString(loanRoomPackSpecBean.getAmount())){
					loanApplyOrder.setAmount(Long.parseLong(loanRoomPackSpecBean.getAmount()));
				}
			}else if(LoanProductEnum.OPERACTION.getCode()==type.intValue()){//运营贷
				
			}
			
			loanApplyOrder.setCreateTime(new Date());
			loanApplyOrder.setOrgCode(orgCode);
			loanApplyOrder.setProductId(LoanApplyOrderEnum.ProductId.LOAN_PRODUCTID.productId);
			loanApplyOrder.setRateTmp(CommonConstant.RATE_TEMPLETE);
			loanApplyOrder.setProductType(type);
			loanApplyOrder.setRsorgId(CommonConstant.RS_FANGCANG_CONST_ID);
			loanApplyOrder.setRspId(LoanApplyOrderEnum.ProductId.LOAN_PRODUCTID.productId);
			loanApplyOrder.setStatus(LoanApplyOrderEnum.LoanOrderStatus.LOAN_APPLYING.status);
			
			int row = loanOrderDao.saveLoanApplyOrder(loanApplyOrder);
			if(row >0){
				return true;
			}
			throw new Exception("查询授信申请单失败");
		}catch(Exception e){
			log.error("保存包房贷订单失效");
			throw e;
		}
	}
	
	
	private NewLoanApplyRequest convertToNewLoanApplyRequest(LoanSpecBean loanSpecBean,Integer type,String orgCode) throws Exception{
		try{
			NewLoanApplyRequest request = new NewLoanApplyRequest();
			LoanRoomPackSpecBean loanRoomPackSpecBean  = null;
			if(LoanProductEnum.ROOM_PACK.getCode()==type.intValue()){
				loanRoomPackSpecBean = (LoanRoomPackSpecBean)loanSpecBean;
				request.setAmount(loanRoomPackSpecBean.getAmount());
				request.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
				request.setProductid(LoanApplyOrderEnum.ProductId.LOAN_PRODUCTID.productId);
				request.setRatetemplate(CommonConstant.RATE_TEMPLETE);
				request.setReqesttime(DateUtil.sdf4.format(new Date()));
				request.setRequestdate(DateUtil.sdf4.format(new Date()));
				request.setUserid(orgCode);
				request.setUserorderid(OrderGenerateService.genSyncUserOrderId());
				//暂时还未确定TODO
				request.setUserrelateid("TJM10000110");
				request.setUsername("罗庆龙");
			}else if(LoanProductEnum.OPERACTION.getCode()==type.intValue()){
				
			}
			return request;
		}catch(Exception e){
			log.error("申请贷款失败");
			throw e;
		}
	}

	private void packLoanJSonData(NewLoanApplyRequest request,LoanSpecBean loanSpecBean,Integer type){
		NewLoanApplyJsonData moneyCreditJsonData = new NewLoanApplyJsonData();
		LoanRoomPackSpecBean loanRoomPackSpecBean  = null;
		if(LoanProductEnum.ROOM_PACK.getCode()==type.intValue()){
			loanRoomPackSpecBean = (LoanRoomPackSpecBean)loanSpecBean;
		}
    	moneyCreditJsonData.setLoanApplicateName(loanRoomPackSpecBean.getAccountName());
    	moneyCreditJsonData.setInParty(request.getUsername());
    	moneyCreditJsonData.setUserOrderId(request.getUserorderid());
    	moneyCreditJsonData.setOrderTime(request.getReqesttime());
    	moneyCreditJsonData.setProductName(loanRoomPackSpecBean.getHotleName());
    	moneyCreditJsonData.setInBankAccount(loanRoomPackSpecBean.getBank());
    	moneyCreditJsonData.setInBankAccountNo(loanRoomPackSpecBean.getAccount());
    	moneyCreditJsonData.setDeliveryStatus("");
    	moneyCreditJsonData.setReceivingState("");
    	moneyCreditJsonData.setReceiptAddress("深圳市宝安区");
    	moneyCreditJsonData.setCode("10000000000009");
    	moneyCreditJsonData.setUnitPrice("");
    	moneyCreditJsonData.setNumber(loanRoomPackSpecBean.getRoomNights()+"");
    	moneyCreditJsonData.setOrderAmount(loanRoomPackSpecBean.getAmount());
    	moneyCreditJsonData.setOrderType("");
    	moneyCreditJsonData.setRootInstCd(request.getRootinstcd());
    	moneyCreditJsonData.setLoanTerm("60");
    	JSON result = JSONSerializer.toJSON(moneyCreditJsonData);
    	request.setJsondata(result.toString());
	}
	
	@Override
	public CancelLoanResponse cancelLoan(CancelLoanRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RepaymentLoanResponse repaymentLoan(RepaymentLoanRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetLoanOrderInfoResponse getLoanOrderInfo(GetLoanOrderInfoRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetLoanOrderInfoListResponse getLoanOrderInfoList(
			GetLoanOrderInfoListRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetOrgLoanStatInfoResponse getOrgLoanStatInfo(
			GetOrgLoanStatInfoRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetHistoryRepaymentListResponse getHistoryRepaymentList(
			GetHistoryRepaymentListRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SaveLoanOrderInfoResponse saveLoanOrderInfo(
			SaveLoanOrderInfoRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

}
