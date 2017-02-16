package com.fangcang.titanjr.common.enums;

/**
 * 用户注册或登录来源
 * @author luoqinglong
 * @2016年6月3日
 */
public enum UserSourceEnum {
	TFS(1,"TFS"),SAAS(2,"SAAS"),AUTO(3,"后台自动"),TTM(4,"TTM"),TWS(5,"泰坦钱包");
	
	/**
	 * 注册渠道
	 */
	private int key;
	
	private String des; 
	/**
	 * 注册渠道混淆码
	 */
	private String mixCode;
	
	private UserSourceEnum(int key,String des){
		this.key = key;
		this.des = des;
	}
	public static UserSourceEnum  getEnumByKey(int key){
		UserSourceEnum entity = null;
		for(UserSourceEnum item : UserSourceEnum.values()) {
			if(item.key == key) {
				entity = item;
				break;
			}
		}
		return entity;
	}
	public int getKey() {
		return key;
	}
	public String getDes() {
		return des;
	}
	
}
