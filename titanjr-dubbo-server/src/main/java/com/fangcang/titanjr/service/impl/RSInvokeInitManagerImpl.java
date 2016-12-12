package com.fangcang.titanjr.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.Rop.api.DefaultRopClient;
import com.Rop.api.request.ExternalSessionGetRequest;
import com.Rop.api.response.ExternalSessionGetResponse;
import com.fangcang.titanjr.common.util.Tools;
import com.fangcang.titanjr.dao.TitanSysconfigDao;
import com.fangcang.titanjr.dto.bean.RSInvokeConfig;
import com.fangcang.titanjr.dto.bean.TitanCallBackConfig;
import com.fangcang.titanjr.dto.bean.TitanPayMethod;
import com.fangcang.titanjr.entity.TitanSysconfig;
import com.fangcang.titanjr.entity.parameter.TitanSysconfigParam;
import com.fangcang.titanjr.rs.util.RSInvokeConstant;

/**o
 * Created by zhaoshan on 2016/4/8.
 */
public class RSInvokeInitManagerImpl {

    private static final Log log = LogFactory.getLog(RSInvokeInitManagerImpl.class);
    private final static String OBJ_KEY_RSINVOKECONFIG = "RSInvokeConfig";
	private final static String OBJ_KEY_TITANPAYMETHOD = "TitanPayMethod";
	private final static String OBJ_KEY_TITANCALLBACKCONFIG = "TitanCallBackConfig";
	private final static String OBJ_KEY_DEFAULTPAYERCONFIG = "DefaultPayerConfig";
	
	@Resource(name="titanSysconfigDao")
	private TitanSysconfigDao titanSysconfigDao;
	
    public void initRopClient() {
        //测试时使用
//        String ropUrl = "https://testapi.open.ruixuesoft.com:30005/ropapi";
//        String appKey = "F1A95B5E-3485-4CEB-8036-F2B9EC53EF65";
//        String appSecret = "8B6E8EEF-48CC-4CCF-94C6-55C4AA2FE9F2";
//        RSInvokeConstant.ropClient = new DefaultRopClient(ropUrl, appKey, appSecret, "xml");
//        RSInvokeConstant.sessionKey = "1460355562856409835";
//        RSInvokeConstant.defaultMerchant = "M10020809";
//        RSInvokeConstant.defaultRoleId = 1301L;
    	System.setProperty("sun.net.client.defaultConnectTimeout", "5000");  
    	System.setProperty("sun.net.client.defaultReadTimeout", "5000");
//        正式上线之后使用
        RSInvokeConfig rSInvokeConfig = getRSInvokeConfig();
        if (rSInvokeConfig!=null) {
            String ropUrl = rSInvokeConfig.getInvokeURL();
            String appKey = rSInvokeConfig.getAppKey();
            String appSecret = rSInvokeConfig.getAppSecret();
            RSInvokeConstant.ropClient = new DefaultRopClient(ropUrl, appKey, appSecret, "xml");
            RSInvokeConstant.defaultMerchant = rSInvokeConfig.getDefaultMerchant();
            RSInvokeConstant.defaultRoleId = rSInvokeConfig.getDefaultRoleId();
            log.info("------sdk init success, rSInvokeConfig:"+Tools.gsonToString(rSInvokeConfig));
        }else{
        	throw new RuntimeException("rong shu sdk init failed, param[rSInvokeConfig] is null");
        }
        initSessionKey(RSInvokeConstant.ropClient);
        //用新的方式查询
        TitanPayMethod titanPayMethod = getTitanPayMethod();
        if (titanPayMethod!=null) {
            RSInvokeConstant.gateWayURL = titanPayMethod.getGatewayURL();
            RSInvokeConstant.rsCheckKey = titanPayMethod.getCheckKey();
            RSInvokeConstant.titanjrCheckKey = titanPayMethod.getTitanjrCheckKey();
            log.info("----------rong shu notify url init success, titanPayMethod:"+Tools.gsonToString(titanPayMethod));
        }else{
        	throw new RuntimeException("rong shu notify url init failed, param[titanPayMethod] is null");
        }
        log.info("当前构建的sessionkey是：" + RSInvokeConstant.sessionKey);
        List<TitanCallBackConfig> callBackConfigs = getTitanCallBackConfig();
        for (TitanCallBackConfig callBackConfig : callBackConfigs) {
            RSInvokeConstant.callBackConfigMap.put(callBackConfig.getPaySource(),callBackConfig.getCallBackURL());
        }
        log.info("callBackConfig:"+Tools.gsonToString(RSInvokeConstant.callBackConfigMap));
        getDefaultPayerConfig();
    }
	/***
	 * 融数sdk初始化参数
	 * @return
	 */
	private RSInvokeConfig getRSInvokeConfig() {
		TitanSysconfigParam param = new TitanSysconfigParam();
		param.setObjKey(OBJ_KEY_RSINVOKECONFIG);
		List<TitanSysconfig> list = titanSysconfigDao.query(param);
		if(CollectionUtils.isNotEmpty(list)){
			RSInvokeConfig invokeConfig = new RSInvokeConfig();
			for(TitanSysconfig item : list){
				//TODO 改为连接uat的贷款环境，上线后改成从数据库取。
				if(item.getCfgKey().equals("RSInvokeConfig_appKey")){
					invokeConfig.setAppKey("762DF53A-4DFD-427A-88F8-C4EEF26195A3");
					continue;
				}
				if(item.getCfgKey().equals("RSInvokeConfig_appSecret")){
					invokeConfig.setAppSecret("6461B23C-3ABE-4BE2-8E2C-D3FF4B2F5415");
					continue;
				}
				if(item.getCfgKey().equals("RSInvokeConfig_invokeURL")){
					invokeConfig.setInvokeURL("https://api.open.ruixuesoft.com:30005/ropapi");
					continue;
				}
				if(item.getCfgKey().equals("RSInvokeConfig_defaultMerchant")){
					invokeConfig.setDefaultMerchant(item.getCfgValue());
					continue;
				}
				if(item.getCfgKey().equals("RSInvokeConfig_defaultRoleId")){
					invokeConfig.setDefaultRoleId(NumberUtils.toLong(item.getCfgValue()));
					continue;
				}
				break;
			}
			return invokeConfig;
		}
		return null;
	}
	/**
	 * 读取融数中间账户
	 */
	private void getDefaultPayerConfig(){
		TitanSysconfigParam param = new TitanSysconfigParam();
		param.setObjKey(OBJ_KEY_DEFAULTPAYERCONFIG);
		List<TitanSysconfig> list = titanSysconfigDao.query(param);
		if(CollectionUtils.isNotEmpty(list)){
			for(TitanSysconfig item : list){
				if(item.getCfgKey().equals("DefaultPayerConfig_USERID")){
					RSInvokeConstant.DEFAULTPAYERCONFIG_USERID = item.getCfgValue();
					continue;
				}
				if(item.getCfgKey().equals("DefaultPayerConfig_PRODUCTID")){
					RSInvokeConstant.DEFAULTPAYERCONFIG_PRODUCTID = item.getCfgValue();
					continue;
				}
				break;
			}
		}else{
			log.error("OBJ_KEY_DEFAULTPAYERCONFIG 融数中间账户没有配置,请检查t_tfs_sysconfig表 ");
		}
	}
		
	/**
	 * 支付成功后，融数的通知泰坦云的地址
	 * @return
	 */
	private TitanPayMethod getTitanPayMethod() {
		TitanSysconfigParam param = new TitanSysconfigParam();
		param.setObjKey(OBJ_KEY_TITANPAYMETHOD);
		List<TitanSysconfig> list = titanSysconfigDao.query(param);
		if(CollectionUtils.isNotEmpty(list)){
			TitanPayMethod payMethod = new TitanPayMethod();
			for(TitanSysconfig item : list){
				if(item.getCfgKey().equals("TitanPayMethod_gatewayURL")){
					payMethod.setGatewayURL(item.getCfgValue());
					continue;
				}
				if(item.getCfgKey().equals("TitanPayMethod_checkKey")){
					payMethod.setCheckKey(item.getCfgValue());
					continue;
				}
				if(item.getCfgKey().equals("TitanPayMethod_titanjrCheckKey")){
					payMethod.setTitanjrCheckKey(item.getCfgValue());
					continue;
				}
				break;
			}
			return payMethod;
		}
		return null;
	}
	/**
	 * 金融系统处理成功后通知第三方地址
	 * @return
	 */
	public List<TitanCallBackConfig> getTitanCallBackConfig() {
		TitanSysconfigParam param = new TitanSysconfigParam();
		param.setObjKey(OBJ_KEY_TITANCALLBACKCONFIG);
		List<TitanSysconfig> list = titanSysconfigDao.query(param);
		List<TitanCallBackConfig> cList = new ArrayList<TitanCallBackConfig>();
		if(CollectionUtils.isNotEmpty(list)){
			for(TitanSysconfig item : list){
				TitanCallBackConfig callBackConfig = new TitanCallBackConfig();
				callBackConfig.setPaySource(item.getCfgKey().split("_")[1]);
				callBackConfig.setCallBackURL(item.getCfgValue());
				cList.add(callBackConfig);
			}
		}
		return cList;
	}
    
    public void initSessionKey(DefaultRopClient ropClient) {
        try {
            ExternalSessionGetRequest sessionGetReq = new ExternalSessionGetRequest();
            ExternalSessionGetResponse sessionGetRsp = ropClient.execute(sessionGetReq);
            RSInvokeConstant.sessionKey = sessionGetRsp.getSession();
        } catch (Exception e) {
            log.error("初始化加载sessionkey失败", e);
        }
    }
}
