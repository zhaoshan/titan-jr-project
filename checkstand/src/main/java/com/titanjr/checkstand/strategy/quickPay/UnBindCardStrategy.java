/** 
 * CopyRright ©2017 深圳市天下房仓科技有限公司 All Right Reserved.
 * 
 * @fileName DepositPayStrategy.java
 * @author Jerry
 * @date 2018年1月4日 上午9:32:12  
 */
package com.titanjr.checkstand.strategy.quickPay;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

/**
 * 解绑卡
 * @author Jerry
 * @date 2018年1月9日 上午11:52:47
 */
@Service("unBindCardStrategy")
public class UnBindCardStrategy implements QuickPayStrategy {

	@Override
	public String redirectResult(HttpServletRequest request) {
		
		return "/quick/unBindCard.shtml";
		
	}

}
