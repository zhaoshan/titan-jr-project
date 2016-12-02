package com.fangcang.titanjr.dto.request;



import com.fangcang.titanjr.dto.BaseRequestDTO;

/**
 * 机构注册前校验 是否已经注册
 * @author luoqinglong
 * @2016年5月17日
 */
public class OrgRegisterValidateRequest extends BaseRequestDTO{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5925152704898953015L;
	private Integer usertype;
	/**
	 * 营业执照号码
	 */
	private String buslince;
	
	private String certificatetype;
	/**
	 * 其他证件号码
	 */
	private String certificateNumber;
	
	
	public Integer getUsertype() {
		return usertype;
	}
	public void setUsertype(Integer usertype) {
		this.usertype = usertype;
	}
	
	public String getCertificatetype() {
		return certificatetype;
	}
	public void setCertificatetype(String certificatetype) {
		this.certificatetype = certificatetype;
	}
	public String getBuslince() {
		return buslince;
	}
	public void setBuslince(String buslince) {
		this.buslince = buslince;
	}
	public String getCertificateNumber() {
		return certificateNumber;
	}
	public void setCertificateNumber(String certificateNumber) {
		this.certificateNumber = certificateNumber;
	}
	
}
