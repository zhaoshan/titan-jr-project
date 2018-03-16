/** 
 * CopyRright ©2017 深圳市天下房仓科技有限公司 All Right Reserved.
 * 
 * @fileName RequestTypeEnum.java
 * @author Jerry
 * @date 2017年11月30日 下午4:36:44  
 */
package com.titanjr.checkstand.constants;

/**
 * 上送支付渠道时的请求类型
 * @author Jerry
 * @date 2017年11月30日 下午4:36:44  
 */
public enum RequestTypeEnum {
	
	GATEWAY_PAY_QUERY_REFUND("101","网关支付,单笔订单查询,网关退款"),
	GATEWAY_REFUNDQUERY("102","网关退款查询"),
    PUBLIC_PAY("201","扫码/公账号/服务窗支付"),
    PUBLIC_CANCEL("202","交易撤销"),
    PUBLIC_REFUND("203","交易退款"),
	PUBLIC_QUERY("204","交易查询"),
	QUICK_CARDBIN_QUERY("301","卡BIN查询"),
	QUICK_PAY_DEPOSIT("302","储蓄卡签约"),
	QUICK_PAY_CREDIT("303","信用卡签约"),
	QUICK_BIND_QUERY("304","查询绑卡信息"),
	QUICK_BIND_PAY("305","绑卡支付请求"),
	QUICK_CARD_AUTH("306","卡密鉴权"),
	QUICK_PAY_CONFIRM("307","确认支付"),
	QUICK_PAY_QUERY("308","快捷支付结果查询"),
	QUICK_UNBIND("309","解绑卡"),
	QUICK_MSG_SEND("310","发送(重发)短信"),
	QUICK_CHANGE_PHONE("311","更换手机号"),
	QUICK_REFUND("312","退款请求"),
	QUICK_CLOSE_ORDER("313","手动关闭订单"),
	QUICK_REFUND_QUERY("314","退款查询"),
	AGENT_TRADE("401","通联-账户交易"),
	AGENT_PAY("402","融宝-代付"),
	AGENT_PAY_QUERY("403","融宝-代付查询"),
	AGENT_DOWNLOAD("404","融宝-对账文件下载"),
	GATEWAY_DOWNLOAD("405","通联-网关支付对账文件下载"),
	QRCODE_DOWNLOAD("406","通联-扫码/公众号对账文件下载");
	
	private String key;
	private String value;
	private RequestTypeEnum(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
