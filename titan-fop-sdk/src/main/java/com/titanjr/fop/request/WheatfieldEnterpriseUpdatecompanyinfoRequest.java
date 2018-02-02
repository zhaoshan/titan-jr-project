package com.titanjr.fop.request;

import java.util.Map;

import com.titanjr.fop.domain.FopHashMap;
import com.titanjr.fop.exceptions.ApiRuleException;
import com.titanjr.fop.response.WheatfieldEnterpriseUpdatecompanyinfoResponse;

/***
 * 修改机构信息
 * @author luoqinglong
 * @date 2018年1月25日
 */
public class WheatfieldEnterpriseUpdatecompanyinfoRequest
		implements FopRequest<WheatfieldEnterpriseUpdatecompanyinfoResponse> {

	private FopHashMap udfParams;
	
	private String constid;
	private String userid;
	private String productid;
	private String remark;
	// 电话
	private String connect;
	// 公司法人
	private String corporatename;
	private String buslince;
	private String username;
	//	用户类型(1：商户 )
	private String usertype;
	//更新类型（1.更新）
	private String opertype;
	

	public FopHashMap getUdfParams() {
		return udfParams;
	}

	public void setUdfParams(FopHashMap udfParams) {
		this.udfParams = udfParams;
	}

	public String getConstid() {
		return constid;
	}

	public void setConstid(String constid) {
		this.constid = constid;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getProductid() {
		return productid;
	}

	public void setProductid(String productid) {
		this.productid = productid;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getConnect() {
		return connect;
	}

	public void setConnect(String connect) {
		this.connect = connect;
	}

	public String getCorporatename() {
		return corporatename;
	}

	public void setCorporatename(String corporatename) {
		this.corporatename = corporatename;
	}

	public String getBuslince() {
		return buslince;
	}

	public void setBuslince(String buslince) {
		this.buslince = buslince;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsertype() {
		return usertype;
	}

	public void setUsertype(String usertype) {
		this.usertype = usertype;
	}

	public String getOpertype() {
		return opertype;
	}

	public void setOpertype(String opertype) {
		this.opertype = opertype;
	}

	@Override
	public String getApiMethodName() {
		return "ruixue.wheatfield.enterprise.updatecompanyinfo";
	}

	@Override
	public Map<String, String> getTextParams() {
		
		FopHashMap localRopHashMap = new FopHashMap();
	    localRopHashMap.put("username", this.username);
	    localRopHashMap.put("userid", this.userid);
	    localRopHashMap.put("productid", this.productid);
	    localRopHashMap.put("constid", this.constid);
	    localRopHashMap.put("buslince", this.buslince);
	    localRopHashMap.put("connect", this.connect);
	    localRopHashMap.put("corporatename", this.corporatename);
	    localRopHashMap.put("opertype", this.opertype);
	    localRopHashMap.put("remark", this.remark);
	    localRopHashMap.put("usertype", this.usertype);
	    if (this.udfParams != null) {
	      localRopHashMap.putAll(this.udfParams);
	    }
	    return localRopHashMap;
	}

	@Override
	public Class<WheatfieldEnterpriseUpdatecompanyinfoResponse> getResponseClass() {
		return WheatfieldEnterpriseUpdatecompanyinfoResponse.class;
	}

	@Override
	public void check() throws ApiRuleException {

	}

}