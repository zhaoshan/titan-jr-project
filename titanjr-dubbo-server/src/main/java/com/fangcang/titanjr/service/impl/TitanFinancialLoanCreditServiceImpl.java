package com.fangcang.titanjr.service.impl;

import java.io.File;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.fangcang.corenut.dao.PaginationSupport;
import com.fangcang.merchant.api.MerchantFacade;
import com.fangcang.merchant.query.dto.MerchantDetailQueryDTO;
import com.fangcang.merchant.response.dto.MerchantResponseDTO;
import com.fangcang.titanjr.common.enums.FileTypeEnum;
import com.fangcang.titanjr.common.enums.LoanApplyOrderEnum;
import com.fangcang.titanjr.common.enums.LoanCreditStatusEnum;
import com.fangcang.titanjr.common.enums.entity.LoanCreditCompanyEnum;
import com.fangcang.titanjr.common.enums.entity.LoanCreditCompanyEnum.CompanyType;
import com.fangcang.titanjr.common.enums.entity.LoanCreditOrderEnum;
import com.fangcang.titanjr.common.enums.entity.LoanPersonEnsureEnum;
import com.fangcang.titanjr.common.enums.entity.TitanOrgEnum;
import com.fangcang.titanjr.common.exception.GlobalServiceException;
import com.fangcang.titanjr.common.factory.HessianProxyBeanFactory;
import com.fangcang.titanjr.common.factory.ProxyFactoryConstants;
import com.fangcang.titanjr.common.util.CommonConstant;
import com.fangcang.titanjr.common.util.FileHelp;
import com.fangcang.titanjr.common.util.FtpUtil;
import com.fangcang.titanjr.common.util.JsonConversionTool;
import com.fangcang.titanjr.common.util.SMSTemplate;
import com.fangcang.titanjr.common.util.ThreadPoolUtil;
import com.fangcang.titanjr.common.util.Tools;
import com.fangcang.titanjr.dao.LoanCompanyEnsureDao;
import com.fangcang.titanjr.dao.LoanCreditCompanyDao;
import com.fangcang.titanjr.dao.LoanCreditOpinionDao;
import com.fangcang.titanjr.dao.LoanCreditOrderDao;
import com.fangcang.titanjr.dao.LoanCreditOrderDiscardDao;
import com.fangcang.titanjr.dao.LoanPersonEnsureDao;
import com.fangcang.titanjr.dao.PlatformOrderOprDao;
import com.fangcang.titanjr.dto.BaseResponseDTO;
import com.fangcang.titanjr.dto.bean.CreditCompanyInfoDTO;
import com.fangcang.titanjr.dto.bean.LoanCompanyAppendInfo;
import com.fangcang.titanjr.dto.bean.LoanCompanyEnsureBean;
import com.fangcang.titanjr.dto.bean.LoanCreditApplicationJsonDataBean;
import com.fangcang.titanjr.dto.bean.LoanCreditCompanyBean;
import com.fangcang.titanjr.dto.bean.LoanCreditOpinionBean;
import com.fangcang.titanjr.dto.bean.LoanCreditOrderBean;
import com.fangcang.titanjr.dto.bean.LoanPersonEnsureBean;
import com.fangcang.titanjr.dto.bean.OrgBindInfo;
import com.fangcang.titanjr.dto.bean.OrgDTO;
import com.fangcang.titanjr.dto.request.AgreementConfirmRequest;
import com.fangcang.titanjr.dto.request.ApplyLoanCreditRequest;
import com.fangcang.titanjr.dto.request.AuditCreditOrderRequest;
import com.fangcang.titanjr.dto.request.FinancialOrganQueryRequest;
import com.fangcang.titanjr.dto.request.GetAuditEvaluationRequest;
import com.fangcang.titanjr.dto.request.GetCreditInfoRequest;
import com.fangcang.titanjr.dto.request.GetCreditOrderCountRequest;
import com.fangcang.titanjr.dto.request.LoanAmountEvaluationRequest;
import com.fangcang.titanjr.dto.request.LoanCreditOrderDiscardRequest;
import com.fangcang.titanjr.dto.request.LoanCreditSaveRequest;
import com.fangcang.titanjr.dto.request.NotifyRequest;
import com.fangcang.titanjr.dto.request.OrgUpdateRequest;
import com.fangcang.titanjr.dto.request.QueryPageCreditCompanyInfoRequest;
import com.fangcang.titanjr.dto.request.SendMessageRequest;
import com.fangcang.titanjr.dto.request.SynLoanCreditOrderRequest;
import com.fangcang.titanjr.dto.request.UserInfoQueryRequest;
import com.fangcang.titanjr.dto.response.AgreementConfirmResponse;
import com.fangcang.titanjr.dto.response.ApplyLoanCreditResponse;
import com.fangcang.titanjr.dto.response.AuditCreidtOrderResponse;
import com.fangcang.titanjr.dto.response.FTPConfigResponse;
import com.fangcang.titanjr.dto.response.FinancialOrganResponse;
import com.fangcang.titanjr.dto.response.GetAuditEvaluationResponse;
import com.fangcang.titanjr.dto.response.GetCreditInfoResponse;
import com.fangcang.titanjr.dto.response.GetCreditOrderCountResponse;
import com.fangcang.titanjr.dto.response.LoanAmountEvaluationResponse;
import com.fangcang.titanjr.dto.response.LoanCreditSaveResponse;
import com.fangcang.titanjr.dto.response.NotifyResponse;
import com.fangcang.titanjr.dto.response.PageCreditCompanyInfoResponse;
import com.fangcang.titanjr.dto.response.SendMessageResponse;
import com.fangcang.titanjr.dto.response.SynLoanCreditOrderResponse;
import com.fangcang.titanjr.dto.response.UserInfoPageResponse;
import com.fangcang.titanjr.entity.LoanCompanyEnsure;
import com.fangcang.titanjr.entity.LoanCreditCompany;
import com.fangcang.titanjr.entity.LoanCreditOpinion;
import com.fangcang.titanjr.entity.LoanCreditOrder;
import com.fangcang.titanjr.entity.LoanCreditOrderDiscard;
import com.fangcang.titanjr.entity.LoanPersonEnsure;
import com.fangcang.titanjr.entity.PlatformOrderStat;
import com.fangcang.titanjr.entity.TitanUser;
import com.fangcang.titanjr.entity.parameter.LoanCreditOrderParam;
import com.fangcang.titanjr.entity.parameter.PlatformOrderStatConditons;
import com.fangcang.titanjr.rs.manager.RSCreditManager;
import com.fangcang.titanjr.rs.manager.RSFileManager;
import com.fangcang.titanjr.rs.request.OprsystemCreditCompanyRequest;
import com.fangcang.titanjr.rs.request.OrderMixserviceCreditapplicationRequest;
import com.fangcang.titanjr.rs.request.OrderServiceAgreementConfirmRequest;
import com.fangcang.titanjr.rs.request.QueryCreditMerchantInfoRequest;
import com.fangcang.titanjr.rs.request.RSFsFileUploadRequest;
import com.fangcang.titanjr.rs.response.OprsystemCreditCompanyResponse;
import com.fangcang.titanjr.rs.response.OrderMixserviceCreditapplicationResponse;
import com.fangcang.titanjr.rs.response.OrderServiceAgreementConfirmResponse;
import com.fangcang.titanjr.rs.response.QueryCreditMerchantInfoResponse;
import com.fangcang.titanjr.rs.response.RSFsFileUploadResponse;
import com.fangcang.titanjr.service.TitanCodeCenterService;
import com.fangcang.titanjr.service.TitanFinancialLoanCreditService;
import com.fangcang.titanjr.service.TitanFinancialOrganService;
import com.fangcang.titanjr.service.TitanFinancialSendSMSService;
import com.fangcang.titanjr.service.TitanFinancialUserService;
import com.fangcang.titanjr.service.TitanSysconfigService;
import com.fangcang.titanjr.task.CreditLoanRunnable;
import com.fangcang.titanjr.util.LoanTypeConvertUtil;
import com.fangcang.util.DateUtil;
import com.fangcang.util.StringUtil;

/**
 * Created by zhaoshan on 2016/11/18.
 */
@Service("titanFinancialLoanCreditService")
public class TitanFinancialLoanCreditServiceImpl implements
		TitanFinancialLoanCreditService {

	private static final Log log = LogFactory
			.getLog(TitanFinancialLoanCreditServiceImpl.class);

	@Resource
	private LoanCreditOrderDao loanCreditOrderDao;

	@Resource
	private LoanCreditCompanyDao loanCreditCompanyDao;

	@Resource
	private LoanCompanyEnsureDao loanCompanyEnsureDao;

	@Resource
	private LoanPersonEnsureDao loanPersonEnsureDao;

	@Resource
	private TitanCodeCenterService titanCodeCenterService;

	@Resource
	private TitanFinancialOrganService titanFinancialOrganService;

	private MerchantFacade merchantFacade;

	@Resource
	private HessianProxyBeanFactory hessianProxyBeanFactory;

	@Resource
	private LoanCreditOpinionDao loanCreditOpinionDao;
	
	@Resource
	private LoanCreditOrderDiscardDao loanCreditOrderDiscardDao;

	@Resource
	private RSCreditManager rsCreditManager;

	@Resource
	private RSFileManager rsFileManager;

	@Resource
	private TitanSysconfigService titanSysconfigService;
	
	@Resource
    private TitanFinancialSendSMSService sendSMSService;
	
	@Resource
	private TitanFinancialUserService userService;
	
	@Resource
	private PlatformOrderOprDao platformOrderOprDao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
	public AuditCreidtOrderResponse auditCreditOrder(AuditCreditOrderRequest req) {
		AuditCreidtOrderResponse response = new AuditCreidtOrderResponse();
		if (!StringUtil.isValidString(req.getOrderNo())) {
			response.putErrorResult("[授信申请单号(orderNo)]不能为空");
			return response;
		}
		if (req.getCheckState() == null) {
			response.putErrorResult("审核状态不能为空");
			return response;
		}
		// 校验申请单号是否存在
		LoanCreditOrder loanCreditOrderParam = new LoanCreditOrder();
		loanCreditOrderParam.setOrderNo(req.getOrderNo());
		List<LoanCreditOrder> loanCreditOrderList = loanCreditOrderDao
				.queryLoanCreditOrder(loanCreditOrderParam);

		if (CollectionUtils.isEmpty(loanCreditOrderList)) {
			response.putErrorResult("[" + req.getOrderNo()
					+ "]授信申请记录不存在,请检查数据是否正确");
			return response;
		}
		LoanCreditOrder loanCreditOrder = loanCreditOrderList.get(0);
		if (loanCreditOrder.getStatus() == LoanCreditStatusEnum.PASS
				.getStatus()) {
			response.putErrorResult("该申请已经审核通过，不能重复审核");
			return response;
		}
		Date now = new Date();
		LoanCreditOrder updateLoanCreditOrderParam = new LoanCreditOrder();
		updateLoanCreditOrderParam.setFirstAuditor(req.getOperator());
		boolean auditResult = false;
		int status = 0;
		if (req.getCheckState() == 0) {
			auditResult = false;
			status = LoanCreditStatusEnum.NO_PASS.getStatus();
			// 添加批注记录
			LoanCreditOpinion loanCreditOpinion = new LoanCreditOpinion();
			loanCreditOpinion.setOrderNo(req.getOrderNo());
			loanCreditOpinion.setResult(req.getCheckState());
			loanCreditOpinion.setContent(req.getContent());
			loanCreditOpinion.setStatus(1);// 1：新添加，没有修改过，2：修改过
			loanCreditOpinion.setCreater(req.getOperator());
			loanCreditOpinion.setCreateTime(now);
			loanCreditOpinionDao.saveLoanCreditOpinion(loanCreditOpinion);
		} else if (req.getCheckState() == 1) {
			updateLoanCreditOrderParam.setFirstAuditTime(now);
			status = LoanCreditStatusEnum.INFO_COMMIT_ING.getStatus();
			auditResult = true;
		}
		// 通过后,调用融数处理
		if (auditResult) {
			asynPushCreditInfo(req.getOrderNo());
		}
		// 改申请单状态
		updateLoanCreditOrderParam.setOrderNo(req.getOrderNo());
		updateLoanCreditOrderParam.setStatus(status);
		loanCreditOrderDao.updateLoanCreditOrder(updateLoanCreditOrderParam);
		response.putSuccess("审核成功");
		return response;
	}
	/***
	 * 异步推送授信申请资料到融数
	 * @param creditOrderNo
	 */
	public void asynPushCreditInfo(String creditOrderNo){
		CreditLoanRunnable creditLoanRunnable = new CreditLoanRunnable(this, creditOrderNo);
		ThreadPoolUtil.excute(creditLoanRunnable);
	}
	
	/***
	 * 提交申请资料到融数
	 * @param creditOrderNo
	 */
	public BaseResponseDTO pushCreditInfoToRs(String creditOrderNo){
		BaseResponseDTO response = new BaseResponseDTO();
		int time = 1;
		//重试2次
		while (time<=3) {
			try {
				response = this.pushCreditInfoToRsSub(creditOrderNo);
				if(response.isResult()){
					log.info("授信申请单["+creditOrderNo+"]------------第["+time+"]次提交授信申请资料到融数----------，结果:成功");
					break;
				}else{
					log.info("授信申请单["+creditOrderNo+"]------------第["+time+"]次提交授信申请资料到融数----------，结果:失败。原因："+response.getReturnMessage());
				}
			} catch (Exception e) {
				log.error("授信申请单["+creditOrderNo+"]提交失败", e);
			}
			time ++;
		}
		if(response.isResult()==false){
			LoanCreditOrder loanCreditOrderParam = new LoanCreditOrder();
			loanCreditOrderParam.setOrderNo(creditOrderNo);
			List<LoanCreditOrder> loanCreditOrderList = loanCreditOrderDao
					.queryLoanCreditOrder(loanCreditOrderParam);
			OrgDTO orgDTO = new OrgDTO();
			orgDTO.setOrgcode(loanCreditOrderList.get(0).getOrgCode());
			orgDTO = titanFinancialOrganService.queryOrg(orgDTO);
			//发送短信通知运营
			SendMessageRequest sendMessageRequest = new SendMessageRequest();
			sendMessageRequest.setMerchantCode(CommonConstant.FANGCANG_MERCHANTCODE);
			sendMessageRequest.setContent("<"+orgDTO.getOrgname()+">提交的授信申请，提交复审时失败，请查看服务器日志或者重新提交复审");
			sendMessageRequest.setReceiveAddress("13543309695");//峰哥手机
			sendSMSService.asynSendMessage(sendMessageRequest);
		}
		return response;
	}
	
	private BaseResponseDTO pushCreditInfoToRsSub(String creditOrderNo){
		BaseResponseDTO response = new BaseResponseDTO();
		LoanCreditOrder loanCreditOrderParam = new LoanCreditOrder();
		loanCreditOrderParam.setOrderNo(creditOrderNo);
		List<LoanCreditOrder> loanCreditOrderList = loanCreditOrderDao
				.queryLoanCreditOrder(loanCreditOrderParam);

		if (CollectionUtils.isEmpty(loanCreditOrderList)) {
			response.putErrorResult("申请授信单不存在");
			log.error("授信申请记录不存在,请检查授信订单号["+creditOrderNo+"]是否正确");
			return response;
		}
		LoanCreditOrder loanCreditOrder = loanCreditOrderList.get(0);
		// 1-上传申请附件
		LoanCreditCompany loanCreditCompanyParam = new LoanCreditCompany();
		loanCreditCompanyParam.setCreditOrderNo(loanCreditOrder.getOrderNo());
		List<LoanCreditCompany> loanCreditCompanyList = loanCreditCompanyDao
				.queryLoanCreditCompany(loanCreditCompanyParam);

		if (CollectionUtils.isEmpty(loanCreditCompanyList)) {
			response.putErrorResult("申请授信单没有找到对应的企业资料");
			updateLoanCreditOrderStatus(loanCreditOrder.getOrderNo(),LoanCreditStatusEnum.INFO_COMMIT_FAIL.getStatus());
			return response;
		}
		// 授信申请公司文件
		List<String> companyFilesList = new ArrayList<String>();
		LoanCreditCompany loanCreditCompany = loanCreditCompanyList.get(0);

		companyFilesList.add(loanCreditCompany.getLicenseUrl());
		companyFilesList.add(loanCreditCompany.getLegalNoUrl());
		companyFilesList.add(loanCreditCompany.getOfficeNoUrl());
		companyFilesList.add(loanCreditCompany.getAccountUrl());
		companyFilesList.add(loanCreditCompany.getCreditUrl());
		companyFilesList.add(loanCreditCompany.getOfficeUrl());
		companyFilesList.add(loanCreditCompany.getWaterUrl());
		companyFilesList.add(loanCreditCompany.getTaxRegUrl());
		companyFilesList.add(loanCreditCompany.getOrgCodeUrl());

		List<String> ensureFilesList = new ArrayList<String>();
		// 个人担保文件
		LoanPersonEnsure loanPersonEnsure = null;
		if (loanCreditOrder.getAssureType() == LoanCreditOrderEnum.AssureType.PERSON
				.getType()) {
			LoanPersonEnsure loanPersonEnsureParam = new LoanPersonEnsure();
			loanPersonEnsureParam.setOrderNo(loanCreditOrder.getOrderNo());
			List<LoanPersonEnsure> loanPersonEnsureList = loanPersonEnsureDao
					.queryLoanPersonEnsure(loanPersonEnsureParam);
			if (CollectionUtils.isEmpty(loanPersonEnsureList)) {
				response.putErrorResult("个人担保资料不存在，请补充担保资料");
				updateLoanCreditOrderStatus(loanCreditOrder.getOrderNo(),LoanCreditStatusEnum.INFO_COMMIT_FAIL.getStatus());
				return response;
			}
			loanPersonEnsure = loanPersonEnsureList.get(0);
			ensureFilesList.add(loanPersonEnsure.getIdCardUrl());
			ensureFilesList.add(loanPersonEnsure.getRegisteredUrl());
			ensureFilesList.add(loanPersonEnsure.getSpouseRegisteredUrl());
			ensureFilesList.add(loanPersonEnsure.getSpouseIdCardUrl());
			ensureFilesList.add(loanPersonEnsure.getMarriageUrl());
		}

		// 企业担保文件
		LoanCompanyEnsure loanCompanyEnsure = null;
		if (loanCreditOrder.getAssureType() == LoanCreditOrderEnum.AssureType.ENTERPRISE
				.getType()) {
			LoanCompanyEnsure loanCompanyEnsureParam = new LoanCompanyEnsure();
			loanCompanyEnsureParam.setOrderNo(loanCreditOrder.getOrderNo());
			List<LoanCompanyEnsure> loanCompanyEnsureList = loanCompanyEnsureDao
					.queryLoanCompanyEnsure(loanCompanyEnsureParam);
			if (CollectionUtils.isEmpty(loanCompanyEnsureList)) {
				response.putErrorResult("企业担保资料不存在，请补充担保资料");
				updateLoanCreditOrderStatus(loanCreditOrder.getOrderNo(),LoanCreditStatusEnum.INFO_COMMIT_FAIL.getStatus());
				return response;
			}
			loanCompanyEnsure = loanCompanyEnsureList.get(0);
			ensureFilesList.add(loanCompanyEnsure.getBusinessLicenseUrl());
			ensureFilesList.add(loanCompanyEnsure
					.getOrgCodeCertificateUrl());
			ensureFilesList.add(loanCompanyEnsure.getTaxRegisterCodeUrl());
			ensureFilesList.add(loanCompanyEnsure.getLicenseUrl());
			ensureFilesList.add(loanCompanyEnsure.getLegalPersonUrl());
		}

		// 本地加密上传的文件
		String encryptRSFilePath = encryptRSFile(companyFilesList,
				ensureFilesList, loanCreditOrder.getOrgCode());
		if (!StringUtil.isValidString(encryptRSFilePath)) {
			response.putErrorResult("授信文件上传融数时失败");
			updateLoanCreditOrderStatus(loanCreditOrder.getOrderNo(),LoanCreditStatusEnum.INFO_COMMIT_FAIL.getStatus());
			return response;
		}

		// 上传到融数
		RSFsFileUploadRequest rsFsFileUploadRequest = new RSFsFileUploadRequest();
		rsFsFileUploadRequest.setUserid(loanCreditOrder.getOrgCode());
		rsFsFileUploadRequest
				.setConstid(CommonConstant.RS_FANGCANG_CONST_ID);
		rsFsFileUploadRequest
				.setProductid(CommonConstant.RS_FANGCANG_PRODUCT_ID);
		rsFsFileUploadRequest.setType(FileTypeEnum.UPLOAD_FILE_73
				.getFileType());
		rsFsFileUploadRequest.setInvoiceDate(DateUtil.getCurrentDate());
		rsFsFileUploadRequest.setPath(encryptRSFilePath);
		rsFsFileUploadRequest.setBacth(loanCreditOrder.getOrgCode()
				+ DateUtil.getCurrentDate().getTime());

		RSFsFileUploadResponse rsFsFileUploadResponse = rsFileManager
				.fsFileUpload(rsFsFileUploadRequest);
		//TODO  删除文件
		//FileHelp.deleteFile(encryptRSFilePath.substring(0,
		//		encryptRSFilePath.lastIndexOf("/")));
		if ((!StringUtil.isValidString(rsFsFileUploadResponse.getUrlKey()))
				|| rsFsFileUploadResponse.isSuccess() == false) {
			response.putErrorResult(rsFsFileUploadResponse.getReturnMsg());
			log.error("上传授信文件压缩包到融数失败,请求参数为rsFsFileUploadRequest:"
					+ Tools.gsonToString(rsFsFileUploadRequest));
			updateLoanCreditOrderStatus(loanCreditOrder.getOrderNo(),LoanCreditStatusEnum.INFO_COMMIT_FAIL.getStatus());
			return response;
		}

		// 2-提交企业资料
		OprsystemCreditCompanyRequest creditCompanyRequest = new OprsystemCreditCompanyRequest();
		creditCompanyRequest
				.setConstid(CommonConstant.RS_FANGCANG_CONST_ID);
		creditCompanyRequest.setUserid(loanCreditOrder.getOrgCode());

		creditCompanyRequest.setCompanyname(loanCreditCompany.getName());
		creditCompanyRequest.setBusinesslicense(loanCreditCompany
				.getLicense());

		creditCompanyRequest.setCertificatestartdate(loanCreditCompany
				.getCertificateStartDate());
		creditCompanyRequest.setCertificateexpiredate(loanCreditCompany
				.getCertificateExpireDate());
		creditCompanyRequest.setCompanytype(loanCreditCompany
				.getCompanyType());
		creditCompanyRequest.setRegistfinance(loanCreditCompany
				.getRegistFinance());
		creditCompanyRequest.setAddress(loanCreditCompany.getRegAddress());
		creditCompanyRequest.setTaxregcard(loanCreditCompany.getTaxRegNo());
		
		creditCompanyRequest.setOrgancertificate(loanCreditCompany.getOrgCode());

		creditCompanyRequest.setCorporatename(loanCreditCompany
				.getLegalName());
		creditCompanyRequest.setCertificatetype(loanCreditCompany
				.getLegalceType());
		creditCompanyRequest.setCertificatenumber(loanCreditCompany
				.getLegalNo());
		creditCompanyRequest.setAcuntopenlince(loanCreditCompany.getOpenAccount());
		OprsystemCreditCompanyResponse oprsystemCreditCompanyResponse = rsCreditManager
				.oprsystemCreditCompany(creditCompanyRequest);
		if (oprsystemCreditCompanyResponse.isSuccess() == false) {
			response.putErrorResult(oprsystemCreditCompanyResponse
						.getReturnMsg());
			log.error("授信申请时上报企业资料信息给融数失败,接口：oprsystemCreditCompany，企业信息为creditCompanyRequest:"
					+ Tools.gsonToString(creditCompanyRequest));
			updateLoanCreditOrderStatus(loanCreditOrder.getOrderNo(),LoanCreditStatusEnum.INFO_COMMIT_FAIL.getStatus());
			return response;
		}
		FinancialOrganQueryRequest organQueryRequest = new FinancialOrganQueryRequest();
		organQueryRequest.setUserId(loanCreditOrder.getOrgCode());
		//获取当前用户可以授信单的额度
		long maxLoanAmount = getLoanCreditAmount(loanCreditOrder.getOrgCode());
		
		// 3-授信申请接口
		LoanCompanyAppendInfo companyAppendInfo = LoanTypeConvertUtil
				.getCompanyAppendInfo(loanCreditCompany.getAppendInfo());
		String creditApplicationJsonData = creditApplicationJson(loanCreditOrder,
				loanCreditCompany, companyAppendInfo, loanPersonEnsure,
				loanCompanyEnsure);

		OrderMixserviceCreditapplicationRequest orderMixserviceCreditapplicationRequest = new OrderMixserviceCreditapplicationRequest();
		orderMixserviceCreditapplicationRequest
				.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
		orderMixserviceCreditapplicationRequest
				.setProductid(CommonConstant.RS_FANGCANG_PRODUCT_ID);
		orderMixserviceCreditapplicationRequest.setUserid(loanCreditOrder
				.getOrgCode());
		orderMixserviceCreditapplicationRequest.setUserorderid(loanCreditOrder
				.getOrderNo());
		orderMixserviceCreditapplicationRequest.setAmount(""+maxLoanAmount);
		orderMixserviceCreditapplicationRequest
				.setReqesttime(CommonConstant.RS_LOAN_CREDIT_REQUEST_TIME);
		orderMixserviceCreditapplicationRequest
				.setOrderplatformname(loanCreditCompany.getName());
		orderMixserviceCreditapplicationRequest.setRequestdate(DateUtil
				.dateToString(new Date(), "yyyy-MM-DD HH:mm:ss"));
		orderMixserviceCreditapplicationRequest
				.setRatetemplrate(CommonConstant.RS_LOAN_CREDIT_RATETEMPL_RATE);
		orderMixserviceCreditapplicationRequest
				.setJsondata(creditApplicationJsonData);
		orderMixserviceCreditapplicationRequest
				.setUrlkey(rsFsFileUploadResponse.getUrlKey());
		orderMixserviceCreditapplicationRequest.setCreditype("2");// 2：零售商授信申请（房仓）
		OrderMixserviceCreditapplicationResponse orderMixserviceCreditapplicationResponse = rsCreditManager
				.orderMixserviceCreditapplication(orderMixserviceCreditapplicationRequest);
		if (orderMixserviceCreditapplicationResponse.isSuccess() == false) {
			response.putErrorResult(orderMixserviceCreditapplicationResponse.getReturnMsg());
			log.error("授信申请时融数接口(orderMixserviceCreditapplication)失败,OrgCode:" + loanCreditOrder.getOrgCode());
			updateLoanCreditOrderStatus(loanCreditOrder.getOrderNo(),LoanCreditStatusEnum.INFO_COMMIT_FAIL.getStatus());
			return response;
		}
		log.info("金融后台初审通过后，授信申请的资料提交成功，金融机构OrgCode:"+loanCreditOrder.getOrgCode());
		//把urlkey 保存到数据库。
		LoanCreditOrder updateLoanCreditOrderParam = new LoanCreditOrder();
		updateLoanCreditOrderParam.setOrderNo(creditOrderNo);
		updateLoanCreditOrderParam.setUrlKey(rsFsFileUploadResponse.getUrlKey());
		updateLoanCreditOrderParam.setStatus(LoanCreditStatusEnum.PASS.getStatus());
		loanCreditOrderDao.updateLoanCreditOrder(updateLoanCreditOrderParam);
		response.putSuccess("授信资料提交成功");
		return response;
	}
	
	private long getLoanCreditAmount(String orgCode) {
		LoanAmountEvaluationRequest request = new LoanAmountEvaluationRequest();
		request.setOrgCode(orgCode);
		LoanAmountEvaluationResponse response = this
				.loanAmountEvaluation(request);
		// 默认50W授信额度
		long amount = 500000;

		try {
			amount = Long.parseLong(response.getMaxCreditAmount());
		} catch (NumberFormatException ex) {
			log.error("get loan credit amount fail!");
		}

		if (amount < 500000) {
			amount = 500000;
		}

		return amount;
	}
	
	/**
	 * 授信资料提交失败，则保存失败状态
	 * @param creditOrderNo
	 * @param status
	 */
	private void updateLoanCreditOrderStatus(String creditOrderNo,Integer status){
		LoanCreditOrder updateLoanCreditOrderParam = new LoanCreditOrder();
		updateLoanCreditOrderParam.setOrderNo(creditOrderNo);
		updateLoanCreditOrderParam.setStatus(status);
		loanCreditOrderDao.updateLoanCreditOrder(updateLoanCreditOrderParam);
	}
	/***
	 * * 打包加密授信申请文件
	 * 
	 * @param companyFilesList
	 *            企业证明文件名ftp路径 /org/123.jpg
	 * @param ensureFilesList
	 * @param orgCode
	 * @return 本地加密文件的地址
	 */
	private String encryptRSFile(List<String> companyFilesList,
			List<String> ensureFilesList, String orgCode) {
		// 下载文件，加密，删除文件，
		// 企业证件资料本地路径
		String orgCreditFileRootDir = TitanFinancialLoanCreditServiceImpl.class
				.getClassLoader().getResource("").getPath()
				+ "tmp" + FtpUtil.UPLOAD_PATH_CREDIT_APPLY + "/" + orgCode;
		String orgCreditDir = "EnterpriseCreditPackage";
		// 法人担保
		String localEnterpriseDocumentInfoPath = orgCreditFileRootDir + "/"
				+ orgCreditDir + "/" + "EnterpriseDocumentInfo";
		// 个人担保
		String localGuarantorDocumentsInfoPath = orgCreditFileRootDir + "/"
				+ orgCreditDir + "/" + "GuarantorDocumentsInfo";
		// 先删除本地旧的临时文件
		FileHelp.deleteFile(orgCreditFileRootDir);

		FtpUtil.makeLocalDirectory(localEnterpriseDocumentInfoPath);
		FtpUtil.makeLocalDirectory(localGuarantorDocumentsInfoPath);
		try {
			// 下载文件
			FTPConfigResponse ftpConfigResponse = titanSysconfigService
					.getFTPConfig();
			FtpUtil ftpUtil = new FtpUtil(ftpConfigResponse.getFtpServerIp(),
					ftpConfigResponse.getFtpServerPort(),
					ftpConfigResponse.getFtpServerUser(),
					ftpConfigResponse.getFtpServerPassword());
			ftpUtil.ftpLogin();
			// 公司证件资料
			for (String file : companyFilesList) {
				if (StringUtil.isValidString(file)) {
					ftpUtil.downloadFile(file, localEnterpriseDocumentInfoPath,
							FtpUtil.baseLocation
									+ FtpUtil.UPLOAD_PATH_CREDIT_APPLY + "/"
									+ orgCode);
					 
				}
			}
			// 担保人证件资料
			for (String file : ensureFilesList) {
				if (StringUtil.isValidString(file)) {
					ftpUtil.downloadFile(file, localGuarantorDocumentsInfoPath,
							FtpUtil.baseLocation
									+ FtpUtil.UPLOAD_PATH_CREDIT_APPLY + "/"
									+ orgCode);
				}
			}

			ftpUtil.ftpLogOut();
		} catch (Exception e) {
			log.error("下载ftp文件失败，路径：" + FtpUtil.baseLocation
					+ FtpUtil.UPLOAD_PATH_CREDIT_APPLY + "/" + orgCode
					+ "，文件为：" + Tools.gsonToString(companyFilesList) + ","
					+ Tools.gsonToString(ensureFilesList), e);
			return "";
		}
		
		// 压缩打包文件
		File srcZipFile = FileHelp.zipFile(orgCreditFileRootDir + "/"
				+ orgCreditDir, orgCreditDir + "_src.zip");
		log.info("授信申请文件(" + srcZipFile.getName() + ")压缩后大小："
				+ srcZipFile.length() / 1024 + " KB,orgCode:" + orgCode);
		// 加密
		String encryptFilePath = orgCreditFileRootDir + "/" + orgCreditDir
				+ ".zip";
		try {
			FileHelp.encryptRSFile(srcZipFile, encryptFilePath);
		} catch (Exception e) {
			log.error("encryptRSFile，融数授信申请资料文件加密失败，原文件路径srcZipFile："
					+ srcZipFile.getAbsolutePath(), e);
			return "";
		}

		return encryptFilePath;
	}

	// private boolean sendEmail(){

	// }

	/**
	 * 组装授信申请jsondata
	 * 
	 * @param loanCreditCompany
	 * @param companyAppendInfo
	 * @param loanPersonEnsure
	 * @param loanCompanyEnsure
	 * @return
	 */
	private String creditApplicationJson(LoanCreditOrder loanCreditOrder ,LoanCreditCompany loanCreditCompany,
			LoanCompanyAppendInfo companyAppendInfo,
			LoanPersonEnsure loanPersonEnsure,
			LoanCompanyEnsure loanCompanyEnsure) {

		LoanCreditApplicationJsonDataBean creditJsonData = new LoanCreditApplicationJsonDataBean();
		
		creditJsonData.setContacts(loanCreditCompany.getContactName());
		creditJsonData.setContactsInfo(loanCreditCompany.getContactPhone());
		creditJsonData.setRootInstCd(CommonConstant.RS_FANGCANG_CONST_ID);
		creditJsonData.setPassportNumber("");// 无法提供，暂时不传
		creditJsonData.setWorkPhone(loanCreditCompany.getContactPhone());

		creditJsonData.setPlatformRegistTime(DateUtil
				.dateToString(loanCreditCompany.getRegDate()));
		creditJsonData.setPlatformOperaNo(loanCreditCompany.getOrgCode());
		creditJsonData.setOperatNumberEmployees(LoanCreditCompanyEnum.OrgSize
				.getEnumBySizeType(loanCreditCompany.getOrgSize()).getDes());
		creditJsonData.setLeaseTerm(companyAppendInfo.getCompanyLease()
				.getBeginLeaseDate()
				+ " 到 "
				+ companyAppendInfo.getCompanyLease().getEndLeaseDate());
		creditJsonData.setLeaseAddress(companyAppendInfo.getCompanyLease()
				.getLeaseAddress());

		creditJsonData.setHousingArea(companyAppendInfo.getCompanyLease()
				.getHousingArea()+" 平方米");
		creditJsonData.setRental(companyAppendInfo.getCompanyLease()
				.getRental() + "元/年");
		creditJsonData.setPaymentMethod(companyAppendInfo.getCompanyLease()
				.getPaymentMethod());
		creditJsonData.setRemark(companyAppendInfo.getCompanyLease()
				.getRemark());

		creditJsonData.setMainBusinessData(companyAppendInfo
				.getMainBusinessDatas());
		creditJsonData.setCooperationCompanyInfo(companyAppendInfo
				.getCooperationCompanyInfos());
		creditJsonData.setControllData(companyAppendInfo.getControllDatas());
		// 个人担保   担保 1 个人 2 企业
		if (loanPersonEnsure != null && "1".equals(String.valueOf(loanCreditOrder.getAssureType()))) {
			creditJsonData.setApplicateName_p(loanPersonEnsure.getPersonName());
			creditJsonData.setApplicateCardId_p(loanPersonEnsure
					.getNationalIdentityNumber());
			creditJsonData.setWorkUnit_p(loanPersonEnsure.getWorkCompany());
			creditJsonData.setWorkStatus_p(loanPersonEnsure.getIndustry());
			creditJsonData.setOccupation_p(loanPersonEnsure.getOccupation());
			creditJsonData
					.setCourseOpenTime_p(LoanPersonEnsureEnum.YearsWorking
							.getEnumByType(loanPersonEnsure.getYearsWorking())
							.getDes());
			creditJsonData.setAnnualIncome_p("");// 暂无提供
			creditJsonData.setPhoneNumber_p(loanPersonEnsure.getMobileNumber());
			creditJsonData
					.setFamilyFixed_p(loanPersonEnsure.getWorkTelephone());
			creditJsonData
					.setPlaceOfOrigin_p(loanPersonEnsure.getNativePlace());
			creditJsonData.setEmail_p("");// 暂无提供
			creditJsonData.setAcademy_p(loanPersonEnsure.getGraduateSchool());
			creditJsonData.setEducation_p(LoanPersonEnsureEnum.HighestEducation
					.getEnumByType(loanPersonEnsure.getHighestEducation())
					.getDes());
			creditJsonData.setAddress_p(loanPersonEnsure
					.getCurrentLiveAddress());
			creditJsonData
					.setHouseNature_p(LoanPersonEnsureEnum.HousePropertyType
							.getEnumByType(
									loanPersonEnsure.getHousePropertyType())
							.getDes());
			creditJsonData
					.setMarriageStatus_p(LoanPersonEnsureEnum.MarriageStatus
							.getEnumByStatus(
									loanPersonEnsure.getMarriageStatus())
							.getDes());
			creditJsonData
					.setChildrenStatus_p(LoanPersonEnsureEnum.HaveChildren
							.getEnumByType(loanPersonEnsure.getHaveChildren())
							.getDes());
			creditJsonData.setFirstContactName_p(loanPersonEnsure
					.getFirstContactName());
			creditJsonData.setFirstContactPhone_p(loanPersonEnsure
					.getFirstContactPhone());
			creditJsonData
					.setFirstContactRelations_p(LoanPersonEnsureEnum.RelationToGuarantee
							.getEnumByType(
									loanPersonEnsure.getRelationToGuarantee1())
							.getDes());
			creditJsonData.setSecondContactName_p(loanPersonEnsure
					.getSecondContactName());
			creditJsonData.setSecondContactPhone_p(loanPersonEnsure
					.getSecondContactPhone());
			creditJsonData
					.setSecondContactRelations_p(LoanPersonEnsureEnum.RelationToGuarantee
							.getEnumByType(
									loanPersonEnsure.getRelationToGuarantee2())
							.getDes());
			creditJsonData
					.setRoomSituation_p(LoanPersonEnsureEnum.CarPropertyType
							.getEnumByType(
									loanPersonEnsure.getCarPropertyType())
							.getDes());
			creditJsonData.setCarBrandModel_p(loanPersonEnsure.getCarBrand());// 界面暂无提供
			creditJsonData.setCarValue_p(loanPersonEnsure.getCarWorth());// 暂无提供
			creditJsonData.setBuyCarYear_p(loanPersonEnsure
					.getCarPurchaseDate());
			creditJsonData.setOtherAssets(loanPersonEnsure.getOtherProperty());
			creditJsonData.setRelatedNote(loanPersonEnsure.getPropertyRemark());
			
			creditJsonData.setEmail_p(loanPersonEnsure.getEmail());
			creditJsonData.setAnnualIncome_p(loanPersonEnsure.getYearIncome());
//			private String email;//邮箱
//			
//			private String yearIncome;//年收入

		}
		//TODO 根据字段属性担保判断
		// 企业担保
		if (loanCompanyEnsure != null  && "2".equals(String.valueOf(loanCreditOrder.getAssureType()))) {
			creditJsonData.setCompanyName_c(loanCompanyEnsure.getCompanyName());
			creditJsonData.setBusinessExpire_c(loanCompanyEnsure.getCertificateStartDate()+"到"+loanCompanyEnsure.getCertificateExpireDate());
			creditJsonData.setCompanyType_c(CompanyType.getEnumByType(loanCompanyEnsure.getCompanyType()).getDes());// 暂无提供
			creditJsonData.setRegisteredCapital_c(loanCompanyEnsure.getRegistFinance()+"元");
			creditJsonData.setEmployeesNumber_c(LoanCreditCompanyEnum.OrgSize
					.getEnumBySizeType(loanCompanyEnsure.getEnterpriseScale())
					.getDes());
			creditJsonData.setRegisteredAddress_c(loanCompanyEnsure
					.getRegAddress());
			creditJsonData.setOfficeAddress_c(loanCompanyEnsure
					.getOfficeAddress());
			creditJsonData.setSetUpDate_c(DateUtil
					.dateToString(loanCompanyEnsure.getFoundDate()));
			creditJsonData.setBusinessLicenseNumber_c(loanCompanyEnsure
					.getBusinessLicense());
			creditJsonData.setTaxregCard_c(loanCompanyEnsure
					.getTaxRegisterCode());
			creditJsonData.setOrganizaCertificateNo_c(loanCompanyEnsure
					.getOrgCodeCertificate());
			creditJsonData.setPlatformOperaNo_c(loanCompanyEnsure
					.getRegisterAccount());
			creditJsonData.setPlatformRegistTime_c(DateUtil
					.dateToString(loanCompanyEnsure.getRegisterDate()));
			creditJsonData.setLegalPersonName_c(loanCompanyEnsure
					.getLegalPersonName());
			creditJsonData
					.setPersonCertificate_c(LoanCreditCompanyEnum.CertificateType
							.getEnumByKey(
									loanCompanyEnsure
											.getLegalPersonCertificateType())
							.getDes());
			creditJsonData.setApplicateCardId_c(loanCompanyEnsure
					.getLegalPersonCertificateNumber());
			creditJsonData.setPrimaryContactName_c(loanCompanyEnsure
					.getContactName());
			creditJsonData.setPrimaryContactPhoneNumber(loanCompanyEnsure
					.getContactPhone());
			creditJsonData.setPlatformOperaNo_c(loanCompanyEnsure.getRegisterAccount());
			creditJsonData.setPlatformRegistTime_c(""+loanCompanyEnsure.getRegisterDate());

		}
		JSON result = JSONSerializer.toJSON(creditJsonData);
		return result.toString();
	}

	public SynLoanCreditOrderResponse synLoanCreditOrder(
			SynLoanCreditOrderRequest creditOrderRequest) {

		SynLoanCreditOrderResponse creditOrderResponse = new SynLoanCreditOrderResponse();

		String orgCode = creditOrderRequest.getOrgCode();
		String orderNo = creditOrderRequest.getOrderNo();

		LoanCreditOrder loanCreditOrder = new LoanCreditOrder();
		loanCreditOrder.setOrgCode(orgCode);
		loanCreditOrder.setOrderNo(orderNo);

		List<LoanCreditOrder> creditOrders = loanCreditOrderDao
				.queryLoanCreditOrder(loanCreditOrder);

		LoanCreditOrder qCrditOrderInfo = null;
		if (CollectionUtils.isNotEmpty(creditOrders)) {
			qCrditOrderInfo = creditOrders.get(0);
		}

		log.info("query credit for database info = "
				+ JsonConversionTool.toJson(qCrditOrderInfo));

		QueryCreditMerchantInfoRequest request = new QueryCreditMerchantInfoRequest();
		request.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
		request.setProductid(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
		request.setUserid(orgCode);
		request.setUserorderid(orderNo);

		log.info("query rs credit merchant req = "
				+ JsonConversionTool.toJson(request));

		QueryCreditMerchantInfoResponse response = rsCreditManager
				.queryCreditMerchantInfo(request);

		log.info("query rs credit merchant info  result = "
				+ JsonConversionTool.toJson(response));

		if (response != null && response.isSuccess()) {
			int dayLimit = Integer.valueOf(response.getCreditdeadline());
			Date expireTime = DateUtils.addMonths(
					qCrditOrderInfo.getFirstAuditTime(), dayLimit);
			qCrditOrderInfo.setActualAmount(Long.valueOf(response
					.getCreditavailability()));
			qCrditOrderInfo.setAmount(Long.valueOf(response.getCreditlimit()));
			qCrditOrderInfo.setDayLimit(dayLimit);
			qCrditOrderInfo.setExpireTime(expireTime);

			if (qCrditOrderInfo.getExpireTime() != null) {
				if (System.currentTimeMillis() > qCrditOrderInfo
						.getExpireTime().getTime()) {
					log.warn("credit order expire time  [orderNo="
							+ qCrditOrderInfo.getOrderNo() + " orgCode="
							+ qCrditOrderInfo.getOrgCode() + "]");

					qCrditOrderInfo.setStatus(LoanCreditStatusEnum.EXPIRE
							.getStatus());
				}
			}

			log.info("update credit order info = "
					+ JsonConversionTool.toJson(qCrditOrderInfo));

			loanCreditOrderDao.updateLoanCreditOrder(qCrditOrderInfo);

			creditOrderResponse.putSuccess();

			return creditOrderResponse;
		}
		creditOrderResponse.putSysError();
		return creditOrderResponse;
	}

	/**
	 * 获取单个授信信息
	 */
	@Override
	public GetCreditInfoResponse getCreditOrderInfo(GetCreditInfoRequest req) {

		GetCreditInfoResponse creditInfoResponse = new GetCreditInfoResponse();

		LoanCreditOrder loanCreditOrder = new LoanCreditOrder();
		loanCreditOrder.setOrgCode(req.getOrgCode());
		loanCreditOrder.setOrderNo(req.getOrderNo());

		List<LoanCreditOrder> creditOrders = loanCreditOrderDao
				.queryLoanCreditOrder(loanCreditOrder);

		LoanCreditOrder qCrditOrderInfo = null;
		if (CollectionUtils.isNotEmpty(creditOrders)) {
			qCrditOrderInfo = creditOrders.get(0);
		}

		if (qCrditOrderInfo == null) {

			// // 以下针对首次进入授信审核带出所有参数列表值
			FinancialOrganQueryRequest organQueryRequest = new FinancialOrganQueryRequest();
			organQueryRequest.setUserId(req.getOrgCode());
			FinancialOrganResponse financialOrganResponse = titanFinancialOrganService
					.queryFinancialOrgan(organQueryRequest);

			MerchantDetailQueryDTO queryDTO = new MerchantDetailQueryDTO();
			queryDTO.setMerchantCode(financialOrganResponse
					.getFinancialOrganDTO().getOrgBindInfo().getMerchantCode());
			MerchantResponseDTO merchantResponseDTO = this.getMerchantFacade()
					.queryMerchantDetail(queryDTO);

			creditInfoResponse.setCreditCompany(new LoanCreditCompanyBean());
			creditInfoResponse.getCreditCompany().setRegAccount(
					merchantResponseDTO.getMerchantCode());
			creditInfoResponse.getCreditCompany().setRegDate(
					DateUtil.dateToString(merchantResponseDTO.getCreatedate()));
			creditInfoResponse.getCreditCompany().setName(
					financialOrganResponse.getFinancialOrganDTO().getOrgName());
			creditInfoResponse.getCreditCompany()
					.setLicense(
							financialOrganResponse.getFinancialOrganDTO()
									.getBuslince());

			return creditInfoResponse;
		}

		LoanCreditOrderBean creditOrderBean = LoanTypeConvertUtil
				.getLoanCreditOrderBean(qCrditOrderInfo);
		creditInfoResponse.setCreditOrder(creditOrderBean);

		LoanCreditCompany loanCreditCompany = new LoanCreditCompany();
		loanCreditCompany.setCreditOrderNo(qCrditOrderInfo.getOrderNo());
		List<LoanCreditCompany> creditCompanies = loanCreditCompanyDao
				.queryLoanCreditCompany(loanCreditCompany);

		if (CollectionUtils.isNotEmpty(creditCompanies)) {
			LoanCreditCompany creditCompany = creditCompanies.get(0);

			LoanCreditCompanyBean companyBean = LoanTypeConvertUtil
					.getLoanCreditCompanyBean(creditCompany);
			LoanCompanyAppendInfo companyAppendInfo = LoanTypeConvertUtil
					.getCompanyAppendInfo(creditCompany.getAppendInfo());

			creditInfoResponse.setCompanyAppendInfo(companyAppendInfo);
			creditInfoResponse.setCreditCompany(companyBean);

		}

		if (qCrditOrderInfo.getAssureType() != null
				&& qCrditOrderInfo.getAssureType() == 2) {

			LoanCompanyEnsure loanCompanyEnsure = new LoanCompanyEnsure();
			loanCompanyEnsure.setOrgCode(qCrditOrderInfo.getOrgCode());
			loanCompanyEnsure.setOrderNo(qCrditOrderInfo.getOrderNo());
			List<LoanCompanyEnsure> companyEnsures = loanCompanyEnsureDao
					.queryLoanCompanyEnsure(loanCompanyEnsure);
			if (CollectionUtils.isNotEmpty(companyEnsures)) {
				LoanCompanyEnsure companyEnsure = companyEnsures.get(0);

				LoanCompanyEnsureBean companyEnsureBean = LoanTypeConvertUtil
						.getLoanCompanyEnsureBean(companyEnsure);
				// 基本信息
				companyEnsureBean.setOrderNo(qCrditOrderInfo.getOrderNo());
				companyEnsureBean.setOrgCode(qCrditOrderInfo.getOrgCode());

				creditInfoResponse.setCompanyEnsure(companyEnsureBean);
			}

		} else if (qCrditOrderInfo.getAssureType() != null
				&& qCrditOrderInfo.getAssureType() == 1) {

			LoanPersonEnsure loanPersonEnsure = new LoanPersonEnsure();
			loanPersonEnsure.setOrgCode(qCrditOrderInfo.getOrgCode());
			loanPersonEnsure.setOrderNo(qCrditOrderInfo.getOrderNo());
			List<LoanPersonEnsure> personEnsures = loanPersonEnsureDao
					.queryLoanPersonEnsure(loanPersonEnsure);

			if (CollectionUtils.isNotEmpty(personEnsures)) {
				LoanPersonEnsure personEnsure = personEnsures.get(0);
				LoanPersonEnsureBean ensureBean = LoanTypeConvertUtil
						.getLoanPersonEnsureBean(personEnsure);
				ensureBean.setOrgCode(qCrditOrderInfo.getOrgCode());
				ensureBean.setOrderNo(qCrditOrderInfo.getOrderNo());
				creditInfoResponse.setLoanPersonEnsure(ensureBean);
			}
		}
		creditInfoResponse.setOrderNo(qCrditOrderInfo.getOrderNo());
		creditInfoResponse.putSuccess();
		return creditInfoResponse;
	}

	@Override
	public LoanCreditSaveResponse saveCreditOrder(LoanCreditSaveRequest req) {

		LoanCreditSaveResponse creditSaveResponse = new LoanCreditSaveResponse();

		if (req == null || (!StringUtil.isValidString(req.getOrgCode()))) {
			log.error("saveCreditOrder orgCode Can't be empty");
			// 参数有误，一定需要存在creditOrder
			creditSaveResponse.putParamError();
			return creditSaveResponse;
		}

		LoanCreditOrder loanCreditOrder = new LoanCreditOrder();
		loanCreditOrder.setOrgCode(req.getOrgCode());
		List<LoanCreditOrder> creditOrders = loanCreditOrderDao
				.queryLoanCreditOrder(loanCreditOrder);

		LoanCreditOrder qCrditOrderInfo = null;
		if (CollectionUtils.isNotEmpty(creditOrders)) {
			qCrditOrderInfo = creditOrders.get(0);
		}

		LoanCreditOrder creditOrder = LoanTypeConvertUtil
				.getLoanCreditOrder(req.getCreditOrder());

		String orderNo = null;
		// 如果外部没指定，而且该用户也未创建授信单，那么给他一个默认的授信单哦 亲
		if (creditOrder == null && qCrditOrderInfo == null) {

			orderNo = titanCodeCenterService.createLoanCreditOrderNo();
			creditOrder = LoanTypeConvertUtil.createDefaultCreditOrder(orderNo,
					req.getOrgCode());
			loanCreditOrderDao.saveLoanCreditOrder(creditOrder);

		} else if (creditOrder != null && qCrditOrderInfo == null) {

			orderNo = titanCodeCenterService.createLoanCreditOrderNo();
			creditOrder.setOrderNo(orderNo);
			loanCreditOrderDao.saveLoanCreditOrder(creditOrder);

		} else if (creditOrder != null && qCrditOrderInfo != null) {
			orderNo = qCrditOrderInfo.getOrderNo();
			creditOrder.setOrderNo(qCrditOrderInfo.getOrderNo());
			loanCreditOrderDao.updateLoanCreditOrder(creditOrder);

			// 更新批注状态为已修改
			if (creditOrder.getStatus() != null && creditOrder.getStatus() == 2) {
				LoanCreditOpinion loanCreditOpinion = new LoanCreditOpinion();
				loanCreditOpinion.setOrderNo(orderNo);
				loanCreditOpinion.setStatus(2);
				loanCreditOpinionDao.updateLoanCreditOpinion(loanCreditOpinion);
			}
		} else if (qCrditOrderInfo != null && orderNo == null) {
			if (creditOrder != null) {
				creditOrder.setOrderNo(orderNo);
			}
			orderNo = qCrditOrderInfo.getOrderNo();
		}

		// 以下三个针对非授信申请单相关信息，数据库存在并传过来的数据不为空，则更新，否则新增
		LoanCreditCompany loanCreditCompany = LoanTypeConvertUtil
				.getLoanCreditCompany(req.getCreditCompany(),
						req.getCompanyAppendInfo());

		if (loanCreditCompany == null && req.getCompanyAppendInfo() != null) {
			LoanCreditCompany creditCompanyQuery = new LoanCreditCompany();
			creditCompanyQuery.setCreditOrderNo(orderNo);
			List<LoanCreditCompany> creditCompanies = loanCreditCompanyDao
					.queryLoanCreditCompany(creditCompanyQuery);

			if (CollectionUtils.isNotEmpty(creditCompanies)) {
				creditCompanies.get(0).setAppendInfo(
						JsonConversionTool.toJson(req.getCompanyAppendInfo()));

				loanCreditCompanyDao
						.updateCreditCompany(creditCompanies.get(0));
			}
		}

		if (null != loanCreditCompany) {
			LoanCreditCompany creditCompanyQuery = new LoanCreditCompany();
			creditCompanyQuery.setCreditOrderNo(orderNo);
			List<LoanCreditCompany> creditCompanies = loanCreditCompanyDao
					.queryLoanCreditCompany(creditCompanyQuery);
			loanCreditCompany.setCreditOrderNo(orderNo);
			if (CollectionUtils.isNotEmpty(creditCompanies)) {
				loanCreditCompanyDao.updateCreditCompany(loanCreditCompany);
			} else {
				loanCreditCompanyDao.saveCreditCompany(loanCreditCompany);
			}
		}

		LoanPersonEnsure personEnsure = LoanTypeConvertUtil
				.getLoanPersonEnsure(req.getLoanPersonEnsure());
		if (null != personEnsure) {// 此时才处理
			LoanPersonEnsure personEnsureQuery = new LoanPersonEnsure();
			personEnsureQuery.setOrderNo(orderNo);
			personEnsureQuery.setOrgCode(req.getOrgCode());
			List<LoanPersonEnsure> loanPersonEnsures = loanPersonEnsureDao
					.queryLoanPersonEnsure(personEnsureQuery);
			// 通过orderNo关联
			personEnsure.setOrderNo(orderNo);
			personEnsure.setOrgCode(req.getOrgCode());

			LoanCreditOrder creditOrder2 = new LoanCreditOrder();
			creditOrder2.setOrderNo(orderNo);
			creditOrder2.setAssureType(1);
			loanCreditOrderDao.updateLoanCreditOrder(creditOrder2);

			if (CollectionUtils.isNotEmpty(loanPersonEnsures)) {
				loanPersonEnsureDao.updateLoanPersonEnsure(personEnsure);
			} else {
				loanPersonEnsureDao.saveLoanPersonEnsure(personEnsure);
			}
		}

		LoanCompanyEnsure companyEnsure = LoanTypeConvertUtil
				.getLoanCompanyEnsure(req.getCompanyEnsure());
		if (null != companyEnsure) {// 此时才处理
			LoanCompanyEnsure companyEnsureQuery = new LoanCompanyEnsure();
			companyEnsureQuery.setOrderNo(orderNo);
			companyEnsureQuery.setOrgCode(req.getOrgCode());
			List<LoanCompanyEnsure> loanCompanyEnsures = loanCompanyEnsureDao
					.queryLoanCompanyEnsure(companyEnsureQuery);
			// 通过userid关联
			companyEnsure.setOrderNo(orderNo);
			companyEnsure.setOrgCode(req.getOrgCode());

			LoanCreditOrder creditOrder2 = new LoanCreditOrder();
			creditOrder2.setOrderNo(orderNo);
			creditOrder2.setAssureType(2);
			loanCreditOrderDao.updateLoanCreditOrder(creditOrder2);

			if (CollectionUtils.isNotEmpty(loanCompanyEnsures)) {
				loanCompanyEnsureDao.updateCompanyEnsure(companyEnsure);
			} else {
				loanCompanyEnsureDao.saveCompanyEnsure(companyEnsure);
			}
		}

		creditSaveResponse.putSuccess();
		return creditSaveResponse;
	}

	@Override
	public ApplyLoanCreditResponse applyLoanCredit(ApplyLoanCreditRequest req) {

		return null;
	}

	@Override
	public PageCreditCompanyInfoResponse queryPageCreditCompanyInfo(
			QueryPageCreditCompanyInfoRequest req) {
		PageCreditCompanyInfoResponse response = new PageCreditCompanyInfoResponse();
		LoanCreditOrderParam condition = new LoanCreditOrderParam();
		condition.setName(req.getName());
		condition.setContactName(req.getContactName());
		condition.setStatus(req.getStatus());
		PaginationSupport<CreditCompanyInfoDTO> paginationSupport = new PaginationSupport<CreditCompanyInfoDTO>();
		paginationSupport.setCurrentPage(req.getCurrentPage());
		paginationSupport.setOrderBy(" createTime desc ");
		paginationSupport = loanCreditOrderDao.selectForPage(condition,
				paginationSupport);
		response.setPageCreditCompanyInfoDTO(paginationSupport);
		response.setResult(true);
		return response;

	}

	@Override
	public GetCreditOrderCountResponse getCreditOrderCount(
			GetCreditOrderCountRequest request) {
		GetCreditOrderCountResponse response = new GetCreditOrderCountResponse();
		LoanCreditOrderParam condition = new LoanCreditOrderParam();
		condition.setStatus(request.getStatus());
		int count = loanCreditOrderDao.getCreditOrderCount(condition);
		response.setCount(count);
		return response;
	}

	private MerchantFacade getMerchantFacade() {
		if (null == merchantFacade) {
			merchantFacade = hessianProxyBeanFactory.getHessianProxyBean(
					MerchantFacade.class,
					ProxyFactoryConstants.merchantServerUrl + "merchantFacade");
		}
		return merchantFacade;
	}

	/**
	 * 根据授信单号获取最新的评价信息
	 */
	@Override
	public GetAuditEvaluationResponse getAuditEvaluationInfo(
			GetAuditEvaluationRequest req) {

		GetAuditEvaluationResponse auditEvaluationResponse = new GetAuditEvaluationResponse();

		LoanCreditOpinion loanCreditOpinion = new LoanCreditOpinion();
		loanCreditOpinion.setOrderNo(req.getOrderNo());
		loanCreditOpinion.setStatus(1);
		List<LoanCreditOpinion> creditOpinions = loanCreditOpinionDao
				.queryLoanCreditOpinion(loanCreditOpinion);

		if (CollectionUtils.isNotEmpty(creditOpinions)) {
			loanCreditOpinion = creditOpinions.get(0);
			LoanCreditOpinionBean creditOpinionBean = new LoanCreditOpinionBean();
			creditOpinionBean.setContent(loanCreditOpinion.getContent());
			creditOpinionBean.setCreater(loanCreditOpinion.getCreater());
			creditOpinionBean.setCreateTime(loanCreditOpinion.getCreateTime());
			creditOpinionBean.setOrderNo(loanCreditOpinion.getOrderNo());
			creditOpinionBean.setResult(loanCreditOpinion.getResult());
			creditOpinionBean.setStatus(loanCreditOpinion.getStatus());
			auditEvaluationResponse.setCreditOpinionBean(creditOpinionBean);
		}
		return auditEvaluationResponse;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
	public NotifyResponse loanCreditNotify(NotifyRequest notifyRequest)
			throws GlobalServiceException {
		NotifyResponse response = new NotifyResponse();
		if (!StringUtil.isValidString(notifyRequest.getBuessNo())) {
			response.putErrorResult("BuessNo 不能为空");
			return response;
		}
		if (notifyRequest.getStatus() == null) {
			response.putErrorResult("Status 不能为空");
			return response;
		}
		Date now = new Date();
		String orgCode = "";
		String subject = "";
		String content = "";
		LoanCreditOrder updateLoanCreditOrderParam = new LoanCreditOrder();
		updateLoanCreditOrderParam.setOrderNo(notifyRequest.getBuessNo());
		updateLoanCreditOrderParam.setStatus(notifyRequest.getStatus());
		try {
			LoanCreditOrder loanCreditOrderParam = new LoanCreditOrder();
			loanCreditOrderParam.setOrderNo(notifyRequest.getBuessNo());

			List<LoanCreditOrder> loanCreditOrderList = loanCreditOrderDao
					.queryLoanCreditOrder(loanCreditOrderParam);
			if (loanCreditOrderList == null
					|| loanCreditOrderList.size() == 0) {
				log.info("通知传过来的授信申请单不存在,notifyRequest:"+Tools.gsonToString(notifyRequest));
				response.putErrorResult("授信申请单不存在");
				return response;
			}
			LoanCreditOrder loanCreditOrder = loanCreditOrderList.get(0);
			orgCode = loanCreditOrder.getOrgCode();
			if (notifyRequest.getStatus() == LoanCreditStatusEnum.REVIEW_PASS
					.getStatus()) {
				// 通过
				// 授信结构信息
				QueryCreditMerchantInfoRequest request = new QueryCreditMerchantInfoRequest();
				request.setProductid(CommonConstant.RS_FANGCANG_PRODUCT_ID);
				request.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
				request.setUserorderid(notifyRequest.getBuessNo());
				request.setUserid(loanCreditOrder.getOrgCode());
				QueryCreditMerchantInfoResponse queryCreditMerchantInfoResponse = rsCreditManager
						.queryCreditMerchantInfo(request);

				int dayLimit = Integer.valueOf(queryCreditMerchantInfoResponse
						.getCreditdeadline());
				long amount = Long.valueOf(queryCreditMerchantInfoResponse
						.getCreditlimit());
				long actualAmount = Long
						.valueOf(queryCreditMerchantInfoResponse
								.getCreditavailability());
				Date expireTime = DateUtils.addMonths(loanCreditOrderList
						.get(0).getFirstAuditTime(), dayLimit);

				updateLoanCreditOrderParam.setDayLimit(dayLimit);
				updateLoanCreditOrderParam.setAuditPass(now);
				updateLoanCreditOrderParam.setExpireTime(expireTime);
				updateLoanCreditOrderParam.setAmount(amount);// 授信总额度
				updateLoanCreditOrderParam.setActualAmount(actualAmount);// 剩余可用额度
				subject = SMSTemplate.CREDIT_REVIEW_PASS.getSubject();
				content = MessageFormat.format(SMSTemplate.CREDIT_REVIEW_PASS.getContent(), com.fangcang.titanjr.common.util.NumberUtil.covertToYuan(actualAmount+""));
			} else if(notifyRequest.getStatus() == LoanCreditStatusEnum.NO_PASS.getStatus()) {
				// 融数审核不通过
				//更新授信申请订单号
				LoanCreditOrder updateNoLoanCreditOrderParam = new LoanCreditOrder();
				String newOrderNo = titanCodeCenterService.createLoanCreditOrderNo();
				updateNoLoanCreditOrderParam.setNewOrderNo(newOrderNo);
				updateNoLoanCreditOrderParam.setOrderNo(notifyRequest.getBuessNo());
				loanCreditOrderDao.updateLoanCreditOrder(updateNoLoanCreditOrderParam);
				
				updateLoanCreditOrderParam.setOrderNo(newOrderNo);
				
				LoanCompanyEnsure loanCompanyEnsureParam = new LoanCompanyEnsure();
				loanCompanyEnsureParam.setOrgCode(loanCreditOrder.getOrgCode());
				loanCompanyEnsureParam.setOrderNo(notifyRequest.getBuessNo());
				loanCompanyEnsureParam.setNewOrderNo(newOrderNo);
				loanCompanyEnsureDao.updateCompanyEnsure(loanCompanyEnsureParam);
				
				LoanCreditCompany loanCreditCompanyParam = new LoanCreditCompany();
				loanCreditCompanyParam.setCreditOrderNo(notifyRequest.getBuessNo());
				loanCreditCompanyParam.setNewOrderNo(newOrderNo);
				loanCreditCompanyDao.updateCreditCompany(loanCreditCompanyParam);
				
				LoanPersonEnsure loanPersonEnsureParam = new LoanPersonEnsure();
				loanPersonEnsureParam.setOrderNo(notifyRequest.getBuessNo());
				loanPersonEnsureParam.setNewOrderNo(newOrderNo);
				loanPersonEnsureDao.updateLoanPersonEnsure(loanPersonEnsureParam);
				
				// 添加批注记录
				LoanCreditOpinion loanCreditOpinion = new LoanCreditOpinion();
				loanCreditOpinion.setOrderNo(newOrderNo);
				loanCreditOpinion.setResult(notifyRequest.getStatus());
				loanCreditOpinion.setContent(notifyRequest.getMsg());
				loanCreditOpinion.setStatus(1);// 1：新添加，没有修改过，2：修改过
				loanCreditOpinion.setCreater(CommonConstant.CHECK_ADMIN_RS);
				loanCreditOpinion.setCreateTime(now);
				loanCreditOpinionDao.saveLoanCreditOpinion(loanCreditOpinion);
				
				//记录授信申请单废弃历史
				LoanCreditOrderDiscard entity = new LoanCreditOrderDiscard();
				entity.setNewOrderNo(newOrderNo);
				entity.setOldOrderNo(notifyRequest.getBuessNo());
				entity.setRsorderNo(notifyRequest.getOrderNo());
				entity.setOrgCode(loanCreditOrder.getOrgCode());
				entity.setDiscardMsg(notifyRequest.getMsg());
				entity.setDiscardTime(now);
				loanCreditOrderDiscardDao.insert(entity);
				subject = SMSTemplate.CREDIT_NO_PASS.getSubject();
				content = MessageFormat.format(SMSTemplate.CREDIT_NO_PASS.getContent(), notifyRequest.getMsg());
			
			}
			loanCreditOrderDao
					.updateLoanCreditOrder(updateLoanCreditOrderParam);
			response.putSuccess("数据保存成功");
		} catch (Exception e) {
			log.error("loanCreditNotify()系统处理融数的授信申请通知失败！,参数notifyRequest:"
					+ Tools.gsonToString(notifyRequest));
			throw new GlobalServiceException("授信申请的通知处理失败", e);
		}
		//发送通过或者不通过短信通知
		if(StringUtil.isValidString(content)){
			try {
				sendCreditSms(orgCode,subject,content);
			} catch (Exception e) {
				log.error("授信申请短信通知发送失败，授信申请单号："+notifyRequest.getBuessNo(),e);
			}
		}
		return response;
	}
	/**
	 * 给管理员发送授信申请结果通知(短信或者邮件)
	 */
	private void sendCreditSms(String orgCode,String subject,String content){
		
		OrgDTO orgDTO = new OrgDTO();
		orgDTO.setUserid(orgCode);
		orgDTO = titanFinancialOrganService.queryOrg(orgDTO);
		String receiveAddress;
		if(orgDTO.getUsertype().equals(TitanOrgEnum.UserType.ENTERPRISE.getKey())){//企业用户
			//给联系人发
			receiveAddress = orgDTO.getMobiletel();
		}else{
			//给管理员发
			UserInfoQueryRequest userInfoQueryRequest = new UserInfoQueryRequest();
			userInfoQueryRequest.setIsadmin(1);
			userInfoQueryRequest.setOrgCode(orgDTO.getOrgcode());
			UserInfoPageResponse userInfoPageResponse = userService.queryUserInfoPage(userInfoQueryRequest);
			receiveAddress = userInfoPageResponse.getTitanUserPaginationSupport().getItemList().get(0).getUserloginname();
		}
		
		SendMessageRequest sendMessageRequest = new SendMessageRequest();
		sendMessageRequest.setReceiveAddress(receiveAddress);
		sendMessageRequest.setMerchantCode(CommonConstant.FANGCANG_MERCHANTCODE);
		sendMessageRequest.setSubject(subject);
		sendMessageRequest.setContent(content);
    	
    	try {
    		sendSMSService.asynSendMessage(sendMessageRequest);
		} catch (Exception e) {
			log.error("授信申请通知短信或者邮件发送失败,内容content："+content+",接收者receiveAddress:"+receiveAddress, e);
		}
	}

	@Override
	public AgreementConfirmResponse agreementConfirm(
			AgreementConfirmRequest agreementConfirmRequest) {
		AgreementConfirmResponse agreementConfirmResponse = new AgreementConfirmResponse();
		if (!StringUtil.isValidString(agreementConfirmRequest.getBuessNo())) {
			agreementConfirmResponse.putErrorResult("授信申请单号[buessNo]不能为空");
			return agreementConfirmResponse;
		}
		LoanCreditOrder loanCreditOrder = new LoanCreditOrder();
		loanCreditOrder.setOrderNo(agreementConfirmRequest.getBuessNo());

		List<LoanCreditOrder> loanCreditOrderList = loanCreditOrderDao
				.queryLoanCreditOrder(loanCreditOrder);
		if (loanCreditOrderList == null || loanCreditOrderList.size() == 0) {
			agreementConfirmResponse.putErrorResult("授信申请单不存在");
			return agreementConfirmResponse;
		}
		LoanCreditOrder updateLoanCreditOrderParam = new LoanCreditOrder();
		updateLoanCreditOrderParam.setOrderNo(agreementConfirmRequest
				.getBuessNo());
		updateLoanCreditOrderParam.setLastAuditTime(new Date());
		loanCreditOrderDao.updateLoanCreditOrder(updateLoanCreditOrderParam);
		// 协议确认
		OrderServiceAgreementConfirmRequest orderServiceAgreementConfirmRequest = new OrderServiceAgreementConfirmRequest();
		orderServiceAgreementConfirmRequest
				.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
		orderServiceAgreementConfirmRequest.setUserid(loanCreditOrderList
				.get(0).getOrgCode());
		orderServiceAgreementConfirmRequest
				.setUserorderid(agreementConfirmRequest.getBuessNo());
		orderServiceAgreementConfirmRequest.setUserflag("2");
		orderServiceAgreementConfirmRequest.setMerchanturlkey("xxxxxx");// 暂时不用传
		OrderServiceAgreementConfirmResponse orderServiceAgreementConfirmResponse = rsCreditManager
				.agreementConfirm(orderServiceAgreementConfirmRequest);
		if (!orderServiceAgreementConfirmResponse.isSuccess()) {
			log.error("授信申请单确认失败(AgreementConfirm)，申请单号BuessNo："
					+ agreementConfirmRequest.getBuessNo());
			agreementConfirmResponse
					.putErrorResult(orderServiceAgreementConfirmResponse
							.getReturnMsg());
			return agreementConfirmResponse;
		}
		agreementConfirmResponse.putSuccess("协议确认成功");
		return agreementConfirmResponse;
	}

	/**
	 * 可贷款金额评测
	 */
	public LoanAmountEvaluationResponse loanAmountEvaluation(
			LoanAmountEvaluationRequest request) {

		LoanAmountEvaluationResponse evaluationResponse = new LoanAmountEvaluationResponse();

		Calendar caled = Calendar.getInstance();
		caled.setTime(new Date());
		caled.add(Calendar.MONTH, -1);

		PlatformOrderStatConditons conditons = new PlatformOrderStatConditons();
		conditons.setBeginDate(com.fangcang.titanjr.common.util.DateUtil.sdf
				.format(caled.getTime()));
		conditons.setEndDate(com.fangcang.titanjr.common.util.DateUtil.sdf
				.format(new Date()));

		OrgBindInfo orgBindInfo = new OrgBindInfo();
		orgBindInfo.setUserid(request.getOrgCode());
		orgBindInfo = titanFinancialOrganService
				.queryOrgBindInfoByUserid(orgBindInfo);

		if (orgBindInfo != null
				&& StringUtil.isValidString(orgBindInfo.getMerchantCode())) {
			conditons.setMerchantCode(orgBindInfo.getMerchantCode());
			PlatformOrderStat platformOrderStat = platformOrderOprDao
					.queryPlatformStatInfo(conditons);

			if (platformOrderStat != null) {
				if (StringUtil.isValidString(platformOrderStat.getReceivable())) {

					BigDecimal reAmount = new BigDecimal(
							platformOrderStat.getReceivable());

					if (reAmount.longValue() < 500000) {
						reAmount = new BigDecimal(500000);
					}
					//
					// long amount = reAmount
					// .divide(new BigDecimal(12), 2,
					// BigDecimal.ROUND_HALF_UP)
					// .multiply(new BigDecimal(0.7)).longValue();

					OrgUpdateRequest orgUpdateRequest = new OrgUpdateRequest();
					orgUpdateRequest.setMaxLoanAmount(reAmount.longValue());
					orgUpdateRequest.setOrgCode(request.getOrgCode());
					try {
						titanFinancialOrganService.updateOrg(orgUpdateRequest);
						evaluationResponse.setMaxCreditAmount(""
								+ reAmount.longValue());
					} catch (GlobalServiceException e) {
						log.error("", e);
					}
				}
			}
		}
		evaluationResponse.putSuccess();
		return evaluationResponse;
	}

	@Override
	public BaseResponseDTO insertLoanCreditOrderDiscard(
			LoanCreditOrderDiscardRequest request) {
		BaseResponseDTO responseDTO = new BaseResponseDTO();
		LoanCreditOrderDiscard entity = new LoanCreditOrderDiscard();
		entity.setNewOrderNo(request.getNewOrderNo());
		entity.setOldOrderNo(request.getOldOrderNo());
		entity.setOrgCode(request.getOrgCode());
		entity.setRsorderNo(request.getRsorderNo());
		entity.setDiscardMsg(request.getDiscardMsg());
		entity.setRemark(request.getRemark());
		
		int i =  loanCreditOrderDiscardDao.insert(entity);
		if(i==1){
			responseDTO.putSuccess();
		}else{
			responseDTO.putErrorResult("数据保存失败");
		}
		
		return responseDTO;
	}
	
	
}
