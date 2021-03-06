package com.fangcang.titanjr.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fangcang.titanjr.common.enums.CoopTypeEnum;
import com.fangcang.titanjr.common.util.Tools;
import com.fangcang.titanjr.dto.bean.CheckStatus;
import com.fangcang.titanjr.dto.request.FinancialOrganQueryRequest;
import com.fangcang.titanjr.dto.response.FinancialOrganResponse;
import com.fangcang.titanjr.service.TitanFinancialOrganService;
import com.fangcang.titanjr.service.TitanFinancialUserService;
import com.fangcang.titanjr.web.util.WebConstant;
import com.fangcang.util.StringUtil;

/**
 * Created by zhaoshan on 2016/5/21.
 */
@Controller
public class FinancialMainController extends BaseController {

    private static final Log log = LogFactory.getLog(FinancialMainController.class);
    @Resource
    private TitanFinancialOrganService titanFinancialOrganService;
    @Resource
    private TitanFinancialUserService titanFinancialUserService;
    
    @RequestMapping(value = "/common/main", method = RequestMethod.GET)
    public String toIndex(HttpServletRequest request, Model model) {
        return "main";
    }
    /***
     * 主页
     * @param request
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String home(HttpServletRequest request, Model model)  {
    	try {
    		queryOrgInfo(model);
		} catch (Exception e) {
			//errormsg
			model.addAttribute("errormsg", "系统错误，请重试或者联系SaaS管理员");
			log.error("金融首页错误", e);
			return "error";
		}
    	
        return "tfs-main/home";
    }
    
    /***
     * 查询机构信息
     * @param model
     * @throws Exception
     */
    private void queryOrgInfo(Model model) throws Exception{
    	Integer isAdmin = (Integer)getSession().getAttribute(WebConstant.SESSION_KEY_LOGIN_IS_ADMIN);
    	String merchantCode = (String)getSession().getAttribute(WebConstant.SESSION_KEY_CURRENT_MERCHANT_CODE);		
    	String jrUserLoginName = (String)getSession().getAttribute(WebConstant.SESSION_KEY_JR_LOGIN_UESRNAME);
    	
    	//暂时统一从session中取
    	String orgBindStatus = (String)getSession().getAttribute(WebConstant.SESSION_KEY_JR_BIND_STATUS);
    	String saasLoginName =   (String)getSession().getAttribute(WebConstant.SESSION_KEY_LOGIN_USER_LOGINNAME);
    	
    	String orgCheckResultKey = "";
    	String orgCheckResultMsg = "";
    	try {
    		if(StringUtil.isValidString(merchantCode)&&StringUtil.isValidString(jrUserLoginName)){//查询机构审核状态,只有添加了的员工才可以看到
        		FinancialOrganQueryRequest organQueryRequest = new FinancialOrganQueryRequest();
            	organQueryRequest.setMerchantcode(merchantCode);
            	organQueryRequest.setCoopType(CoopTypeEnum.SAAS.getKey());
            	FinancialOrganResponse organOrganResponse = titanFinancialOrganService.queryBaseFinancialOrgan(organQueryRequest);
				if (organOrganResponse.isResult()) {
					CheckStatus checkStatus = organOrganResponse.getFinancialOrganDTO().getCheckStatus();
					if (checkStatus != null) {
						orgCheckResultKey = checkStatus.getCheckResultKey();
						orgCheckResultMsg = checkStatus.getCheckResultMsg();
						model.addAttribute("userType", organOrganResponse.getFinancialOrganDTO().getUserType());
						model.addAttribute("orgId", organOrganResponse.getFinancialOrganDTO().getOrgId());
					}else{
						log.error("金融首页错误，机构无审核状态[organOrganResponse]:"+Tools.gsonToString(organOrganResponse)+",saasLoginName:"+saasLoginName);
					}
					
				}else{
	        		log.error("金融首页错误，查询金融机构失败[organOrganResponse]:"+Tools.gsonToString(organOrganResponse)+",saasLoginName:"+saasLoginName);
	        	}
        	}else{
        		log.info("该员工未添加为金融员工，SAAS商家编码："+merchantCode+",SAAS登录用户名："+saasLoginName);
        	}
    		
		} catch (Exception e) {
			log.error("金融首页错误，saasLoginName为:"+saasLoginName+",商家编码merchantCode："+merchantCode, e);
			throw new Exception(e);
		}
    	model.addAttribute("orgCheckResultKey", orgCheckResultKey);
    	model.addAttribute("orgCheckResultMsg", Tools.replaceEnterKeyHTML(orgCheckResultMsg));
    	model.addAttribute("resultMsg_title", orgCheckResultMsg);
    	model.addAttribute("isAdmin", isAdmin==null?0:isAdmin);
    	model.addAttribute("orgBindStatus", orgBindStatus==null?0:orgBindStatus);
    }
    
    /**
     * 信贷
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/loan", method = RequestMethod.GET)
    public String loan(Model model) throws Exception{
    	try {
    		queryOrgInfo(model);
		} catch (Exception e) {
			model.addAttribute("errormsg", "系统错误，请重试或者联系SaaS管理员");
			log.error("贷款页面请求错误/loan", e);
			return "error";
		}
        return "tfs-main/loan";
    }
    
    /**
     * 收款介绍
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/receipt", method = RequestMethod.GET)
    public String receipt(Model model) throws Exception{
    	try {
    		queryOrgInfo(model);
		} catch (Exception e) {
			model.addAttribute("errormsg", "系统错误，请重试或者联系SaaS管理员");
			log.error("收款介绍页面请求错误/receipt", e);
			return "error";
		}
        return "tfs-main/receipt";
    }
    
    /**
     * 付款介绍
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/pay", method = RequestMethod.GET)
    public String pay(Model model) throws Exception{
    	try {
    		queryOrgInfo(model);
		} catch (Exception e) {
			model.addAttribute("errormsg", "系统错误，请重试或者联系SaaS管理员");
			log.error("付款介绍页面请求错误/pay", e);
		}
        return "tfs-main/pay";
    }
    
    
    
    
    @RequestMapping(value = "/common/clickToPay", method = RequestMethod.GET)
    public String testPay(HttpServletRequest request, Model model) {
        return "checkstand-pay/clickToPayPage";
    }
    
    /**
     * 心跳
     */
    @ResponseBody
    @RequestMapping(value = "/beat")
    public String beat(){
    	return toJson(putSuccess("心跳"));
    }

//    @RequestMapping(value = "/testValExcp", method = RequestMethod.GET)
//    public String testValidateException(HttpServletRequest request, Model model) throws RSValidateException {
//        throw new RSValidateException("errorCode", "errorMsg");
//    }
//
//    @RequestMapping(value = "/testRunExcp", method = RequestMethod.GET)
//    public String testRuntimeException(HttpServletRequest request, Model model) throws RuntimeException {
//        throw new RuntimeException("errorCode", new IllegalAccessException("test...."));
//    }
//
//    @RequestMapping(value = "/testExcp", method = RequestMethod.GET)
//    public String testException(HttpServletRequest request, Model model) throws Exception {
//        throw new Exception("errorCode", new StackOverflowError("test..."));
//    }
}
