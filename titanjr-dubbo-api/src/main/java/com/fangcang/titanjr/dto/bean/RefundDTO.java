package com.fangcang.titanjr.dto.bean;

import java.io.Serializable;
import java.util.Date;


public class RefundDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String refundId ;
	
	private String merchantNo;
	
	private String busiCode;
	
	private String orderNo;
	
	private String refundAmount;
	
	private String refundOrderno;
	
	private String orderTime;
	
	private String version;
	
	private String signType;
	
	private String creator;
	
	private Integer status;

	private Date createtime;
	
	private String transferAmount;
	
	private String fee;
	
	private String payOrderNo;
	
	private String notifyUrl;
	
	private String userOrderId;
	
	private String businessInfo;

	//退款单创建的开始时间，用作参数
	private Date startTime;
	//退款单创建的结束时间，用作参数
	private Date endTime;
	//交易单状态，对账使用，查询列表不返回
	private Integer transStatus;
	//退款单对应的原始付款方，对账使用，查询列表不返回
	private String payerMerchant;
	//退款单对应的原始收款方，对账使用，查询列表不返回
	private String payeeMerchant;
	
	public String getBusinessInfo() {
		return businessInfo;
	}

	public void setBusinessInfo(String businessInfo) {
		this.businessInfo = businessInfo;
	}

	public String getMerchantNo() {
		return merchantNo;
	}

	public void setMerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}

	public String getBusiCode() {
		return busiCode;
	}

	public void setBusiCode(String busiCode) {
		this.busiCode = busiCode;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getRefundOrderno() {
		return refundOrderno;
	}

	public void setRefundOrderno(String refundOrderno) {
		this.refundOrderno = refundOrderno;
	}

	public String getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
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

	public String getRefundId() {
		return refundId;
	}

	public void setRefundId(String refundId) {
		this.refundId = refundId;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public String getTransferAmount() {
		return transferAmount;
	}

	public void setTransferAmount(String transferAmount) {
		this.transferAmount = transferAmount;
	}

	public String getFee() {
		return fee;
	}

	public void setFee(String fee) {
		this.fee = fee;
	}

	public String getPayOrderNo() {
		return payOrderNo;
	}

	public void setPayOrderNo(String payOrderNo) {
		this.payOrderNo = payOrderNo;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getUserOrderId() {
		return userOrderId;
	}

	public void setUserOrderId(String userOrderId) {
		this.userOrderId = userOrderId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Integer getTransStatus() {
		return transStatus;
	}

	public void setTransStatus(Integer transStatus) {
		this.transStatus = transStatus;
	}

	public String getPayerMerchant() {
		return payerMerchant;
	}

	public void setPayerMerchant(String payerMerchant) {
		this.payerMerchant = payerMerchant;
	}

	public String getPayeeMerchant() {
		return payeeMerchant;
	}

	public void setPayeeMerchant(String payeeMerchant) {
		this.payeeMerchant = payeeMerchant;
	}
}
