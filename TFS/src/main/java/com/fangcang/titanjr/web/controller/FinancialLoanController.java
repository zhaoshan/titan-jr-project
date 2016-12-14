package com.fangcang.titanjr.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fangcang.titanjr.common.enums.LoanOrderStatusEnum;
import com.fangcang.titanjr.common.enums.LoanProductEnum;
import com.fangcang.titanjr.common.util.CommonConstant;
import com.fangcang.titanjr.common.util.JsonConversionTool;
import com.fangcang.titanjr.dto.request.CancelLoanRequest;
import com.fangcang.titanjr.dto.request.GetHistoryRepaymentListRequest;
import com.fangcang.titanjr.dto.request.GetLoanOrderInfoListRequest;
import com.fangcang.titanjr.dto.request.GetLoanOrderInfoRequest;
import com.fangcang.titanjr.dto.request.GetOrgLoanStatInfoRequest;
import com.fangcang.titanjr.dto.response.CancelLoanResponse;
import com.fangcang.titanjr.dto.response.GetHistoryRepaymentListResponse;
import com.fangcang.titanjr.dto.response.GetLoanOrderInfoListResponse;
import com.fangcang.titanjr.dto.response.GetLoanOrderInfoResponse;
import com.fangcang.titanjr.dto.response.GetOrgLoanStatInfoResponse;
import com.fangcang.titanjr.service.TitanFinancialLoanService;
import com.fangcang.titanjr.service.TitanSysconfigService;
import com.fangcang.titanjr.web.annotation.AccessPermission;
import com.fangcang.titanjr.web.pojo.LoanQueryConditions;
import com.fangcang.util.StringUtil;

/**
 * 贷款请求控制器
 * 
 * @author wengxitao
 */
@Controller
@RequestMapping("loan")
public class FinancialLoanController extends BaseController {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory
			.getLog(FinancialLoanCreditController.class);

	@Resource
	private TitanFinancialLoanService financialLoanService;

	@Resource
	private TitanSysconfigService sysconfigService;

	private final static Map<String, Object> initDataMap = new HashMap<String, Object>();

	static {
		initDataMap.put("loan-all-status", LoanOrderStatusEnum.values());
		initDataMap.put("loan-all-orderby", "createTime,status");

		initDataMap.put("loan-audit-status", new LoanOrderStatusEnum[] {
				LoanOrderStatusEnum.LOAN_REQ_ING,
				LoanOrderStatusEnum.LENDING_ING });
		initDataMap.put("loan-audit-orderby", "createTime");

		initDataMap.put("loan-over-status",
				new LoanOrderStatusEnum[] { LoanOrderStatusEnum.LOAN_FINISH });
		initDataMap.put("loan-over-orderby", "createTime");

		initDataMap.put("loan-payment-status",
				new LoanOrderStatusEnum[] { LoanOrderStatusEnum.HAVE_LOAN,
						LoanOrderStatusEnum.LOAN_EXPIRY });
		initDataMap.put("loan-payment-orderby", "createTime");

		initDataMap.put("" + LoanProductEnum.ROOM_PACK.getCode(),
				"loan-roompack");
		initDataMap.put("" + LoanProductEnum.OPERACTION.getCode(), "");

	}

	@RequestMapping(value = "/getLoanDetailsInfo", method = RequestMethod.GET)
	@AccessPermission(allowRoleCode = { CommonConstant.ROLECODE_LOAN_42 })
	public String getLoanDetailsInfo(String orderNo, Model model) {
		if (!StringUtil.isValidString(orderNo)) {
			log.error("loan detail orderNo is null");
			model.addAttribute("errormsg", "查询贷款单号不能为空!");
			return "error";
		}

		GetLoanOrderInfoRequest req = new GetLoanOrderInfoRequest();
		req.setOrgCode(this.getUserId());
		req.setOrderNo(orderNo);

		GetLoanOrderInfoResponse infoResponse = financialLoanService
				.getLoanOrderInfo(req);

		if (infoResponse == null || infoResponse.getApplyOrderInfo() == null) {
			log.error(" get loan info response is null");
			model.addAttribute("errormsg", "查询贷款信息失败，请稍后再试!");
			return "error";
		}

		Object pageKey = initDataMap.get(String.valueOf(infoResponse
				.getApplyOrderInfo().getProductType()));
		if (pageKey == null || "".equals(pageKey)) {
			log.error("product type pageKey is null");
			model.addAttribute("errormsg", "产品类型不支持查看详情，请确认！");
			return "error";
		}
		model.addAttribute("loanOrderInfo", infoResponse.getApplyOrderInfo());
		if (infoResponse.getApplyOrderInfo() != null) {
			model.addAttribute("loanSpecInfo", infoResponse.getApplyOrderInfo()
					.getLoanSpec());
		}

		return "/loan/product-info/" + pageKey;
	}

	@ResponseBody
	@RequestMapping(value = "/getRepaymentList", method = RequestMethod.GET)
	@AccessPermission(allowRoleCode = { CommonConstant.ROLECODE_LOAN_42 })
	public String getRepaymentList(String orderNo) {
		log.info("get repayment List info ");

		GetHistoryRepaymentListRequest listRequest = new GetHistoryRepaymentListRequest();
		listRequest.setOrderNo(orderNo);
		listRequest.setOrgCode(this.getUserId());

		GetHistoryRepaymentListResponse listResponse = financialLoanService
				.getHistoryRepaymentList(listRequest);

		log.info("repayment List = "
				+ JsonConversionTool.toJson(listResponse
						.getLoanRepaymentInfos()));

		return toJson(listResponse.getLoanRepaymentInfos());
	}

	@ResponseBody
	@RequestMapping(value = "/loanStatInfo", method = RequestMethod.GET)
	@AccessPermission(allowRoleCode = { CommonConstant.ROLECODE_LOAN_42 })
	public String getLoanStatInfo() {
		log.info("get loan stat info ");
		GetOrgLoanStatInfoRequest req = new GetOrgLoanStatInfoRequest();

		req.setOrgCode(this.getUserId());

		GetOrgLoanStatInfoResponse loanStatInfoResponse = financialLoanService
				.getOrgLoanStatInfo(req);

		log.info("loanStatInfo = "
				+ JsonConversionTool.toJson(loanStatInfoResponse));

		return toJson(loanStatInfoResponse.getOrgLoanStatInfo());
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/stopLoan", method = RequestMethod.GET)
	@AccessPermission(allowRoleCode = { CommonConstant.ROLECODE_LOAN_42 })
	public String stopLoan(String orderNo) 
	{
		log.info("stop loan orderNo=" + orderNo);
		
		CancelLoanRequest req = new CancelLoanRequest();
		req.setOrderNo(orderNo);
		req.setOrgCode(this.getUserId());
		CancelLoanResponse loanResponse = financialLoanService.cancelLoan(req);
		
		log.info("stopLoan = "
				+ JsonConversionTool.toJson(loanResponse));
		
		if(loanResponse != null && loanResponse.isResult())
		{
			putSuccess();
		}else{
			putSysError("取消贷款单失败，请稍后再试！");
		}

		return toJson();
	}

	
	@RequestMapping(value = "/getLoanInfoList", method = RequestMethod.GET)
	@AccessPermission(allowRoleCode = { CommonConstant.ROLECODE_LOAN_42 })
	public String getLoanInfoList(LoanQueryConditions loanQueryConditions,
			Model model) {

		if (loanQueryConditions == null
				|| !StringUtil.isValidString(loanQueryConditions.getPageKey())) {
			log.error("page key is null");
			model.addAttribute("errormsg", "错误的查询方式，请确认!");
			return "error";
		}

		GetLoanOrderInfoListRequest req = new GetLoanOrderInfoListRequest();

		// 设置查询条件
		req.setOrgCode(this.getUserId());

		if (StringUtil.isValidString(loanQueryConditions.getCurrPage())) {
			req.setCurrentPage(Integer.parseInt(loanQueryConditions
					.getCurrPage()));
		}

		req.setBeginActualRepaymentDate(loanQueryConditions
				.getBeginActualRepaymentDate());
		req.setBeginCreateTime(loanQueryConditions.getBeginCreateTime());
		req.setBeginLastRepaymentDate(loanQueryConditions
				.getBeginLastRepaymentDate());
		req.setBeginRelMoneyTime(loanQueryConditions.getBeginRelMoneyTime());

		req.setEndActualRepaymentDate(loanQueryConditions
				.getEndActualRepaymentDate());
		req.setEndCreateTime(loanQueryConditions.getEndCreateTime());
		req.setEndLastRepaymentDate(loanQueryConditions
				.getEndLastRepaymentDate());
		req.setEndRelMoneyTime(loanQueryConditions.getEndRelMoneyTime());

		if (StringUtil.isValidString(loanQueryConditions.getProductType())) {
			req.setProductEnum(LoanProductEnum.getEnumByKey(Integer
					.parseInt(loanQueryConditions.getProductType())));
		}
		// 按照套路给查询分配过滤的状态
		List<LoanOrderStatusEnum> statusList = new ArrayList<LoanOrderStatusEnum>(
				Arrays.asList((LoanOrderStatusEnum[]) initDataMap
						.get(loanQueryConditions.getPageKey() + "-status")));
		// 如果页面指定了要查询的状态，那么就需要按照页面的要求来
		if (StringUtil.isValidString(loanQueryConditions.getLoanStatus())) {
			LoanOrderStatusEnum tempEnum = LoanOrderStatusEnum
					.getEnumByStatus(Integer.parseInt(loanQueryConditions
							.getLoanStatus()));
			if (tempEnum != null) {
				statusList.clear();
				statusList.add(tempEnum);
			}
		}
		// 设置要过滤的状态
		req.setOrderStatusEnum(statusList.toArray(new LoanOrderStatusEnum[0]));
		// 设置排序条件
		Object orderBy = initDataMap.get(loanQueryConditions.getPageKey()
				+ "-orderby");
		if (orderBy != null) {
			req.setOrderBy(orderBy.toString());
		}
		GetLoanOrderInfoListResponse infoListResponse = financialLoanService
				.getLoanOrderInfoList(req);

		model.addAttribute("totalCount", infoListResponse.getTotalCount());
		model.addAttribute("pageSize", infoListResponse.getPageSize());
		model.addAttribute("currentPage", infoListResponse.getCurrentPage());
		model.addAttribute("loanInfoList", infoListResponse.getApplyOrderInfo());

		return "/loan/loan-list/" + loanQueryConditions.getPageKey();
	}

}
