/** 
 * CopyRright ©2017 深圳市天下房仓科技有限公司 All Right Reserved.
 * 
 * @fileName TitanPayConfirmDTO.java
 * @author Jerry
 * @date 2018年1月5日 上午11:27:40  
 */
package com.titanjr.checkstand.dto;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

import com.titanjr.checkstand.constants.BusiCodeEnum;

/**
 * @author Jerry
 * @date 2018年1月5日 上午11:27:40  
 */
public class TitanPayConfirmDTO implements Serializable {

	/** 
	 * 
	 */
	private static final long serialVersionUID = -153785468371148319L;
	
	/**
	 * 业务号  {@link BusiCodeEnum}
	 */
	@NotBlank
	private String busiCode;
	/**
	 * 商户号
	 */
	@NotBlank
	private String merchantNo;
	/**
	 * 业务订单号
	 */
	@NotBlank
	private String orderNo;
	/**
	 * 支付方式   41：新快捷支付
	 */
	@NotBlank
	private String payType;
	/**
	 * 手机验证码
	 */
	@NotBlank
	private String checkCode;
	/**
	 * 版本号   新版本：v1.1（含快捷支付）
	 */
	@NotBlank
	private String version;
	/**
	 * 签名的类型，默认1为MD5加签
	 */
	@NotBlank
	private String signType;
	/**
	 * 签名字符串
	 */
	@NotBlank
	private String signMsg;
	
	public String getBusiCode() {
		return busiCode;
	}
	public void setBusiCode(String busiCode) {
		this.busiCode = busiCode;
	}
	public String getMerchantNo() {
		return merchantNo;
	}
	public void setMerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public String getCheckCode() {
		return checkCode;
	}
	public void setCheckCode(String checkCode) {
		this.checkCode = checkCode;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getSignType() {
		return signType;
	}
	public void setSignType(String signType) {
		this.signType = signType;
	}
	public String getSignMsg() {
		return signMsg;
	}
	public void setSignMsg(String signMsg) {
		this.signMsg = signMsg;
	}

}