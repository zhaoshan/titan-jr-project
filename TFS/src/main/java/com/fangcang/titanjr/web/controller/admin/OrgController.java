package com.fangcang.titanjr.web.controller.admin;

import javax.annotation.Resource;

import net.sf.json.JSONSerializer;

//import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fangcang.titanjr.common.enums.OrgCheckResultEnum;
import com.fangcang.titanjr.common.enums.entity.TitanOrgEnum;
import com.fangcang.titanjr.common.exception.GlobalServiceException;
import com.fangcang.titanjr.common.exception.MessageServiceException;
import com.fangcang.titanjr.dto.bean.CheckStatus;
import com.fangcang.titanjr.dto.bean.FinancialOrganDTO;
import com.fangcang.titanjr.dto.request.FinancialOrganQueryRequest;
import com.fangcang.titanjr.dto.request.OrganCheckRequest;
import com.fangcang.titanjr.dto.response.FinancialOrganResponse;
import com.fangcang.titanjr.dto.response.OrganCheckResponse;
import com.fangcang.titanjr.dto.response.OrganQueryCheckResponse;
import com.fangcang.titanjr.service.TitanFinancialOrganService;
import com.fangcang.titanjr.service.TitanFinancialUserService;
import com.fangcang.titanjr.web.controller.BaseController;
import com.fangcang.titanjr.web.pojo.OrgPojo;
import com.fangcang.titanjr.web.util.CommonConstant;

/**
 * 金融机构审核
 * @author luoqinglong
 * @2016年6月12日
 */
@Controller
@RequestMapping("/admin")
public class OrgController extends BaseController{
//	private static Logger logger = Logger.getLogger(OrgController.class);
	
	@Resource
	private TitanFinancialOrganService organService;
	@Resource
	private TitanFinancialUserService userService;
	/**
	 * 企业机构页面
	 * @return
	 */
	@RequestMapping(value = "/org")
	public String org(Integer userType,Model model){
		//count(model);
		if(userType==null||userType==1){
			//企业
			return "admin/org/en-verify-org-list";
		}
		if(userType==2){
			//个人
			return "admin/org/per-verify-org-list";
		}
		//默认
		return "admin/org/en-verify-org-list";
	}
	
	/***
	 * 机构查询
	 * @param orgPojo
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/orgTable")
	public String orgTable(OrgPojo orgPojo, Model model) throws Exception{
		
		int userType = orgPojo.getUserType()==null?1:orgPojo.getUserType();
		
		FinancialOrganQueryRequest organQueryRequest = new FinancialOrganQueryRequest();
		organQueryRequest.setIsadmin(1);
		organQueryRequest.setUserloginname(orgPojo.getUserLoginName());
		organQueryRequest.setUserType(userType);
		organQueryRequest.setOrgName(orgPojo.getOrgName());
		organQueryRequest.setResultKey("".equals(orgPojo.getResultKey())?null:orgPojo.getResultKey());
		organQueryRequest.setCurrentPage(orgPojo.getPageNo());
		organQueryRequest.setPageSize(orgPojo.getPageSize());
		
		OrganQueryCheckResponse organCheckResponse = organService.queryFinancialOrganForPage(organQueryRequest);
		if(organCheckResponse.isResult()){
			model.addAttribute("orgCheckDTOPage", organCheckResponse.getPaginationSupport());
		}else{
//			logger.error("机构查询失败,参数:"+JSONSerializer.toJSON(orgPojo).toString());
			throw new Exception("机构查询失败");
		}
		count(model);
		if(userType==1){
			return "admin/org/en-verify-org-table";
		}else{
			return "admin/org/per-verify-org-table";
		}
		
	}
	
	/**
	 * 审核和查看机构（企业）
	 * @param orgId
	 * @return
	 */
	@RequestMapping(value = "/viewOrg")
	public String viewOrg(Integer orgId,String opt,String tfsLoginUsername, Model model){
		if(orgId==null||opt==null){
			model.addAttribute("errormsg", "参数错误");
			return "error";
		}
		model.addAttribute("opt", opt);
		
		FinancialOrganQueryRequest organQueryRequest = new FinancialOrganQueryRequest();
		organQueryRequest.setOrgId(orgId);
		FinancialOrganResponse financialOrganResponse = organService.queryFinancialOrgan(organQueryRequest);
		if(financialOrganResponse.isResult()){
			FinancialOrganDTO financialOrganDTO = financialOrganResponse.getFinancialOrganDTO();
			model.addAttribute("financialOrganDTO", financialOrganDTO);
			model.addAttribute("orgImgUrl", financialOrganDTO.getOrgImageInfoList().get(0).getImageURL());
			model.addAttribute("operatorName", getSAASLoginName());
			model.addAttribute("tfsLoginUsername", tfsLoginUsername);
			
			if(financialOrganDTO.getUserType()==1){
				return "admin/org/en-verify-org-info";
			}else {
				return "admin/org/per-verify-org-info";
			}
		}else{
			model.addAttribute("errormsg", "请求错误");
			return "error";
		}
	}
	
	
	
	/**
	 * 提交审核
	 * @param checkStatus 审核结果
	 * @param reason 原因
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/checkOrg")
	public String checkOrg(Integer checkStatus,String reason,Integer orgId){
		if(orgId==null||orgId<=0||checkStatus==null){
			putSysError("参数错误");
			return toJson();
		}
		OrganCheckRequest organCheckRequest = new OrganCheckRequest();
		organCheckRequest.setCheckstatus(checkStatus);
		organCheckRequest.setOrgId(orgId);
		organCheckRequest.setResultMsg(reason);
		organCheckRequest.setOperator(getSAASLoginName());
		
		try {
			OrganCheckResponse organCheckResponse = organService.checkFinancialOrgan(organCheckRequest);
			if(organCheckResponse.isResult()){
				putSuccess("审核成功");
				
				return toJson();
			}else{
				putSysError(organCheckResponse.getReturnMessage());
				return toJson();
			}
		} catch (GlobalServiceException e) {
//			logger.error("机构审核失败,参数:orgId["+orgId+"],checkStatus["+checkStatus+"],reason["+reason+"]",e);
			putSysError(CommonConstant.CONTROLLER_ERROR_MSG);
			return toJson();
			
		} catch (MessageServiceException e) {
//			logger.error("机构审核失败,参数:orgId["+orgId+"],checkStatus["+checkStatus+"],reason["+reason+"]",e);
			putSysError(e.getMessage());
			return toJson();
		}
		
	}
	/***
	 * 待审核数
	 */
	private void count(Model model){
		FinancialOrganQueryRequest enterpriseRequest = new FinancialOrganQueryRequest();
		enterpriseRequest.setResultKey(OrgCheckResultEnum.FT.getResultkey());
		enterpriseRequest.setUserType(TitanOrgEnum.UserType.ENTERPRISE.getKey());
		int enCount = organService.countOrg(enterpriseRequest);
		model.addAttribute("enCount", enCount);
		
		FinancialOrganQueryRequest personalRequest = new FinancialOrganQueryRequest();
		personalRequest.setResultKey(OrgCheckResultEnum.FT.getResultkey());
		personalRequest.setUserType(TitanOrgEnum.UserType.PERSONAL.getKey());
		int perCount = organService.countOrg(personalRequest);
		model.addAttribute("perCount", perCount);
	}
}
