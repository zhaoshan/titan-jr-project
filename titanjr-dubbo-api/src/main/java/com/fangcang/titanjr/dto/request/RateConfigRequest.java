package com.fangcang.titanjr.dto.request;

import com.fangcang.titanjr.common.enums.CashierDeskTypeEnum;
import com.fangcang.titanjr.common.enums.CashierItemTypeEnum;
import com.fangcang.titanjr.dto.BaseRequestDTO;

/**
 * @ClassName: RateConfigRequest
 * @Description: 查询费率配置请求类
 * @author: wengxitao
 * @date: 2016年8月15日 上午10:09:24
 */
public class RateConfigRequest extends BaseRequestDTO 
{
	/**
	 * @fieldName: serialVersionUID
	 * @fieldType: long
	 */
	private static final long serialVersionUID = 1L;
	

	/** 金服的用户ID */
	private String userId;

	/** 付款源类型枚举 */
	private CashierDeskTypeEnum parSourceType;

	/** 付款类型 **/
	private CashierItemTypeEnum payType;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public CashierDeskTypeEnum getParSourceType() {
		return parSourceType;
	}

	public void setParSourceType(CashierDeskTypeEnum parSourceType) {
		this.parSourceType = parSourceType;
	}

	public CashierItemTypeEnum getPayType() {
		return payType;
	}

	public void setPayType(CashierItemTypeEnum payType) {
		this.payType = payType;
	}

}