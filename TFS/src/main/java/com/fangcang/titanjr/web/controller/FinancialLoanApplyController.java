package com.fangcang.titanjr.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fangcang.titanjr.common.enums.LoanProductEnum;
import com.fangcang.titanjr.common.util.CommonConstant;
import com.fangcang.titanjr.common.util.DateUtil;
import com.fangcang.titanjr.common.util.FtpUtil;
import com.fangcang.titanjr.common.util.NumberUtil;
import com.fangcang.titanjr.common.util.OrderGenerateService;
import com.fangcang.titanjr.dto.bean.LoanApplyInfo;
import com.fangcang.titanjr.dto.bean.LoanRoomPackSpecBean;
import com.fangcang.titanjr.dto.bean.LoanSpecBean;
import com.fangcang.titanjr.dto.request.ApplyLoanRequest;
import com.fangcang.titanjr.dto.request.GetLoanOrderInfoRequest;
import com.fangcang.titanjr.dto.response.ApplyLoanResponse;
import com.fangcang.titanjr.dto.response.FTPConfigResponse;
import com.fangcang.titanjr.dto.response.GetLoanOrderInfoResponse;
import com.fangcang.titanjr.service.TitanFinancialLoanService;
import com.fangcang.titanjr.service.TitanSysconfigService;
import com.fangcang.titanjr.web.annotation.AccessPermission;
import com.fangcang.util.StringUtil;
/**
 * 贷款申请
 * @author luoqinglong
 * @date   2016年12月8日
 */
@Controller
@RequestMapping("loan_apply")
public class FinancialLoanApplyController extends BaseController{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Log log = LogFactory
			.getLog(FinancialLoanApplyController.class);
	
	@Resource
	private TitanSysconfigService sysconfigService;
	
	@Resource
	private TitanFinancialLoanService titanFinancialLoanService;
	
	
	@RequestMapping(value="/main", method = RequestMethod.GET)
	public String applyLoanMain()
	{
		return "/loan/loan-apply/loan-apply";
	}
	
	/**
	 * 申请包房贷
	 * @param info
	 * @return
	 */
	@RequestMapping("apply")
	@AccessPermission(allowRoleCode={CommonConstant.ROLECODE_LOAN_42})
	public String loanApply(LoanApplyInfo info){
		if(info ==null || !StringUtil.isValidString(info.getLoanOrderNo()) 
					   || !StringUtil.isValidString(info.getAccountName())
					   || !StringUtil.isValidString(info.getAccount())
					   || !StringUtil.isValidString(info.getAmount())
					   || !StringUtil.isValidString(info.getContactNames())){
			this.putSysError("参数不合法");
			return this.toJson();
		}
		
		try{
			
			if(!checkLoanApplyInfo(info)){
				return this.toJson();
			}
			
			//申请贷款
			LoanRoomPackSpecBean loanSpecBean = new LoanRoomPackSpecBean();
			loanSpecBean.setAmount(NumberUtil.covertToCents(info.getAmount()));
			loanSpecBean.setLoanOrderNo(info.getLoanOrderNo());
			loanSpecBean.setAccount(info.getAccount());
			loanSpecBean.setAccountName(info.getAccountName());
			loanSpecBean.setBank(info.getBank());
			loanSpecBean.setBeginDate(DateUtil.sdf.parse(info.getBeginDate()));
			loanSpecBean.setContractUrl(info.getContactNames());
			loanSpecBean.setEndDate(DateUtil.sdf.parse(info.getEndDate()));
			loanSpecBean.setHotleName(info.getHotelName());
			if(StringUtil.isValidString(info.getRoomNights())){
				loanSpecBean.setRoomNights(Integer.parseInt(info.getRoomNights()));
			}
			
			ApplyLoanRequest request = new ApplyLoanRequest();
			request.setProductType(LoanProductEnum.ROOM_PACK);
			request.setLcanSpec(loanSpecBean);
			request.setOrgCode(this.getUserId());
			//request.setOrgCode("TJM10000087");
			ApplyLoanResponse response = titanFinancialLoanService.applyLoan(request);
			if(!response.isResult()){
				this.putSysError(response.getReturnMessage());
				return toJson();
			}
			this.putSuccess();
		}catch(Exception e){
			this.putSysError();
			log.error("包房贷申请失败",e);
		}
		return toJson();
	} 
	
	private boolean checkLoanApplyInfo(LoanApplyInfo info){
		String neg = "(^[1-9]{1}\\d{0,20}(\\.\\d{1,2})?$)";
		String neg2 = "(^[0]{1}(\\.\\d{1,2})?$)";
		if(!(Pattern.matches(neg, info.getAmount())||Pattern.matches(neg2, info.getAmount()))){
			log.error("传入金额格式错误");
			this.putSysError("传入金额格式错误");
			return false;
		}
		
		//验证getLoanOrderNo
		GetLoanOrderInfoRequest req = new GetLoanOrderInfoRequest();
		req.setOrderNo(info.getLoanOrderNo());
		GetLoanOrderInfoResponse response =titanFinancialLoanService.getLoanOrderInfo(req);
		if(null !=response.getApplyOrderInfo()){
			log.error("该贷款单已被申请,请重新申请");
			this.putSysError("该贷款单已被申请,请重新申请");
			return false;
		}
		
		
		
		if(!StringUtil.isValidString(info.getBeginDate())){
			log.error("开始时间为空");
			this.putSysError("开始时间为空");
			return false;
		}
		
		if(!StringUtil.isValidString(info.getEndDate())){
			log.error("结束时间为空");
			this.putSysError("结束时间为空");
			return false;
		}
		
		try {
				String regex = "([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))";
				boolean beginFlg = Pattern.matches(regex, info.getBeginDate()); 
				boolean endFlag = Pattern.matches(regex, info.getEndDate()); 
				if(!beginFlg || !endFlag){
					log.error("时间格式不正确");
					this.putSysError("时间格式不正确");
				    return false;
				}
				Long times = DateUtil.sdf.parse(info.getEndDate()).getTime()-DateUtil.sdf.parse(info.getBeginDate()).getTime();
		        if(times <=0){
		        	log.error("结束时间必须大于开始时间");
		        	this.putSysError("结束时间必须大于开始时间");
				    return false;
		        }	
		
		} catch (Exception e) {
				log.error("开始时间格式不正确");
				this.putSysError("开始时间格式不正确");
				return false;
		}
		
		if(!StringUtil.isValidString(info.getRoomNights())){
			log.error("间夜数不能为空");
			this.putSysError("间夜数不能为空");
			return false;
		}
		
		String rex="^\\+?[1-9][0-9]*$";
		boolean vRoomNight =Pattern.matches(rex, info.getRoomNights());
		if(!vRoomNight){
			log.error("间夜数必须为正整数");
			this.putSysError("间夜数必须为正整数");
			return false;
		}
		
		if(info.getRoomNights().length()>11){
			log.error("间夜数太大");
			this.putSysError("间夜数太大");
			return false;
		}
		
		if(!StringUtil.isValidString(info.getHotelName())){
			log.error("酒店名称不能为空");
			this.putSysError("酒店名称不能为空");
			return false;
		}
		
		if(!StringUtil.isValidString(info.getBank())){
			log.error("银行不能为空");
			this.putSysError("银行不能为空");
			return false;
		}
		
		return true;
	}
	
	/**
	 * 生成贷款单号
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/orderNo")
	@AccessPermission(allowRoleCode={CommonConstant.ROLECODE_LOAN_42})
	public Map<String,Object> getLoanApplyOrderNo(){
		return this.putSuccess("orderNo", OrderGenerateService.genLoanApplyOrderNo());
	}
	
	
	@RequestMapping(value="/upload")
	public void upload(@RequestParam(value = "compartment_contract", required = false) MultipartFile file,
			String loanApplyOrderNo,String fileName) throws IOException
	{
	
		getResponse().setCharacterEncoding("utf-8"); // 这里不设置编码会有乱码
		getResponse().setContentType("text/html;charset=utf-8");
		getResponse().setHeader("Cache-Control", "no-cache");

		PrintWriter out = getResponse().getWriter();
		
		boolean check = checkFileType(file.getOriginalFilename());
		if(!check){
			this.putSysError("文件格式不正确");
			out.print(toJson());
			out.flush();
			out.close();
			return;
		}
		

		String newName = this
				.getNewFileName(file.getOriginalFilename(), fileName);
		FtpUtil util = null;
		try {

			FTPConfigResponse configResponse = sysconfigService.getFTPConfig();

			util = new FtpUtil(configResponse.getFtpServerIp(),
					configResponse.getFtpServerPort(),
					configResponse.getFtpServerUser(),
					configResponse.getFtpServerPassword());

			util.ftpLogin();

			log.info("login ftp success fileName=" + newName);

			List<String> fileList = util
					.listFiles(FtpUtil.UPLOAD_PATH_LOAN_APPLY + "/"
							+ this.getUserId() + "/"+loanApplyOrderNo+"/");

			log.info("list ftp files fileList=" + fileList);

			// 检查文件是否已经上传过，如果上传过则需要把旧的文件先干掉，在上传新的哦 亲
			if (fileList != null) {
				for (int i = 0; i < fileList.size(); i++) {
					if (fileList.get(i).indexOf(fileName + ".") != -1) {
						util.deleteFile(FtpUtil.UPLOAD_PATH_LOAN_APPLY + "/"
								+ this.getUserId() + "/"+loanApplyOrderNo+"/" + fileList.get(i));
					}
				}
			}

			util.uploadStream(newName, file.getInputStream(),
					FtpUtil.UPLOAD_PATH_LOAN_APPLY + "/" + this.getUserId()
							+ "/"+loanApplyOrderNo+"/");

			log.info("upload to ftp success fileName=" + newName);

			util.ftpLogOut();

			this.putSuccess("fileName", newName);

		} catch (Exception e) {
			this.putSysError();
			log.error("", e);
		} finally {
			if (util != null) {
				try {
					util.ftpLogOut();
				} catch (Exception e) {
					this.putSysError();
					log.error("", e);
				}
			}
		}

		out.print(toJson());
		out.flush();
		out.close();
	}
	
	private String getNewFileName(String filename, String newName) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length() - 1))) {
				return newName + "." + filename.substring(dot + 1);
			}
		}
		return filename;
	}
	
	private boolean checkFileType(String filename){
		if(filename ==null || !StringUtil.isValidString(filename)){
			return false;
		}
		int dot = filename.lastIndexOf('.');
		if ((dot > -1) && (dot < (filename.length() - 1))) {
			String suffix = filename.substring(dot + 1).toLowerCase();
			
			if(suffix.equals("pdf") || suffix.equals("jpg") || suffix.equals("jpeg") || suffix.equals("png") || suffix.equals("zip") ||suffix.equals("rar")){
				return true;
			}
		}
		return false;
	}

}
