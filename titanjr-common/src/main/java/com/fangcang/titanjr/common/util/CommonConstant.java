package com.fangcang.titanjr.common.util;

public class CommonConstant {
    //‘全国’的编码
    public static final String COUNTRY_CODE = "00";
    //一次批量插入的条数
    public static final Integer INSERT_BATCH_NUM = 100;

    //执行成功
    public static final String OPERATE_SUCCESS = "true";
    
    public static final String OPERATE_FAIL = "false";
    
    //待处理
    public static final String WAIT_MOMENT = "wait";


    public static final String RS_FANGCANG_CONST_ID = "M000016";//机构号
    public static final String RS_FANGCANG_PRODUCT_ID = "P000070";//产品id
    
    public static final String RS_FANGCANG_PRODUCT_ID_230 = "P000230";//贷款产品id
    public static final String RS_FANGCANG_PRODUCT_ID_229 = "P000229";//收益子账户产品id
    
    
    public static final String RS_LOAN_CREDIT_RATETEMPL_RATE = "RA201611011800001";//贷款费率模板
    public static final String RS_LOAN_CREDIT_REQUEST_TIME = "36";//授信申请有效期，时限,单位月
    public static final int RS_LOAN_REPAYMENT_TIME = 90;//贷款还款到期时长,单位：天
    
    
    public static final String ORG_CODE_PREFIX = "TJM";//虚拟证件机构编码前缀
    public static final String ORG_SUB_CODE_PREFIX = "TJMS";//真实证件机构编码前缀
    public static final String ACCOUNT_CODE_PREFIX = "TJA";//accountCode编码前缀
    public static final String LOAN_CREDIT_NO_PREFIX = "CR";//accountCode编码前缀
    public static final String RS_FANGCANG_USER_ID = "141223100000056";//房仓平台userid

    //基础业务类型
    public static final String ORDEROTYPE = "B1";

	  
	//返回成功
	public static final String RETURN_SUCCESS="success";
	
	//融数回调时的支付成功的状态
    public static final String PAY_SUCCESS="3";
	/***
	 * 免密码支付额度(单位分)
	 */
	public static final Double NO_PWD_PAY_LIMIT = 100000d;
	//融数充值单默认的失效天数
	public static final Integer ORDER_EXPIRE_TIME = 5;
	/******* 审核人*******/
	public static final String CHECK_ADMIN_JR = "titanjr-admin";//泰坦金融系统管理员
	public static final String CHECK_ADMIN_RS = "rs-admin";//融数系统管理员
	/*******冻结资金账户*******/
	//冻结授权码
	public static final String FUNCCODE = "40171";
	//冻结账户调用的ip
	public static final String USER_IPADDRESS = "218.17.13.199";
	//冻结状态
	public static final Integer FREEZE_STATUS = 1;
	
	public static final int PASSWORD_SALT_LENGTH = 12; //密码salt长度
	
	//线上支付使用路径
	//public static final String REQUSET_URL ="http://www.fangcang.com:8080/TFS/trade/payment.action";
	//测试使用路径
	public static final String REQUSET_URL = "/trade/showCashierDesk.action";
	
	public static final String QUICK_PAYMENT_URL = "/quickPayment/showQuickPayment.action";
	
	public static final String QUICK_PAYMENT = "6";
	
	//商户
	public static final String ENTERPRISE = "1";
	
	//个人用户
	public static final String PERSONAL = "2";
	
	//所有绑定卡
	public static final String ALLCARD = "2";
	
	//结算卡
	public static final String DEBIT_CARD = "1";
	
	//提现卡
	public static final String WITHDRAW_CARD="3";
	
	//绑卡正常
	public static final String BIND_SUCCESS = "1";
	
	//审核失败
	public static final String BIND_FAIL = "4";
	
	
	//短信发送选用服务号
	public static final String DEFAULT_SMS_PROVIDER_KEY = "3369";
	
	/************************************** begin 权限   ****************************/
	//是否忘记付款密码
    public static final String IS_FORGET_PAYPASSWORD="1";
	// 付款权限
    public static final String ROLECODE_PAY_38 = "PAY";
    //查看权限
    public static final String ROLECODE_VIEW_39 = "VIEW";
    // 充值和提现权限
    public static final String ROLECODE_RECHARGE_40 = "RECHARGE";
    //理财权限
    public static final String ROLECODE_FINANCING_41 = "FINANCING";
    //贷款权限
    public static final String ROLECODE_LOAN_42 = "LOAN";
    //消息提醒权限
    public static final String ROLECODE_MESSAGE_43 = "MESSAGE";
    //系统运营员权限
    public static final String ROLECODE_OPERATION_44 = "OPERATION";
    
    //系统管理员
    public static final String ROLECODE_ADMIN = "ADMIN";
    
    //不需要权限
    public static final String ROLECODE_NO_LIMIT = "NO_LIMIT";
    
    /************************************** end 权限   ****************************/
    
    //验证码有效时长，单位：小时
    public static final Integer CODE_TIME_OUT_HOUR = 1 ;
    /**
     * 商家房仓的编码
     */
    public static final String FANGCANG_MERCHANTCODE = "M10000001";
    
    //冻结订单
    public static final String FREEZE_ORDER = "0";
    
    public static final String RESULT = "result";
    
    public static final String RETURN_MSG = "msg";
	
    public static final String LOCAL_ORDERNO="L";
    
	public static final String ACCOUNT_TYPE_ID = "00";
    //支付地址
    public static final String QRCODE="qrCode";
    
    public static final String GATE_WAY_PAYGE="checkstand-pay/genRechargePayment";
    
    public static final String PAY_WX="checkstand-pay/wx";
    
    //中国民生银行简写
    public static final String CMBC = "cmbc";
    
    //企业银行
    public static final int BUS_BANK = 1;
    
    //微信支付的bankName
    public static final String WXPAY = "wx";
    
    public static final String ALIPAY = "alipay";
    
    public static final String CURRENT_TYPE="1";
    
    //融数订单状态。1为正常
    public static final String RS_ORDER_STATUS="1";
    
    public static final String ORDER_DELAY = "001_001";
	
	    //退款成功
    public static final String REFUND_SUCCESS="2";
    
    //退款审理中
    public static final String REFUND_IN_PROCESS="0";
	
	public static final String IS_BIND_CARD = "0";
	
	//商户有效
	public static final Integer IS_ACTIVE = 1;
	
	//30天所对应的毫秒数
	public static final Long MS = (long)30*24*60*60*1000;
	
	public static final String ISRECIEVEDESK = "1";
	
	//是否是商家编码
	public static final String ISMERCHCODE = "1";
	
//	public static final Integer REAL_TIME = 0;
//	
//	public static final Integer NOT_REAL_TIME = 1;
	
    public static final String RATE_TEMPLETE = "RA201611011800001";
    
    public static final String BEIJING_CODE ="1000";
    public static final String TIANJING_CODE ="1100";
    public static final String CHONGQING_CODE ="6530";
    public static final String SHNAGHAI_CODE ="2900";
    
    public static final Integer DEFALUT_GOODCNT = 1;
    
    public static final String DEFAULT_PRODUCT_NUM="1";
    
    public static final Integer DEFAULT_EXPIRE_TIME=45;
    
    public static final Integer RQ_WIDTH = 170;
    
    public static final Integer RQ_HEIGH = 170;
    //钱包支付成功后通知SAAS服务，签名key
    public static final String PAY_NOTIFY_SIGN_MD5_KEY = "tLrLggmLgpfU0lZG";
    
    public static final String ORDER_EXCEPTION_MAX_FAIL_STATE = "99";
    
    /***
	 * 金融缓存key前缀
	 */
    public static final String REDIS_KEY_PREFIX_TITANJR = "TITANJR";
    
    /**
     * 冻结在付款方
     */
    public static final String FREEZE_PAYER = "1";
    /**
     * 冻结在收款方
     */
    public static final String FREEZE_PAYEE = "2";
    
    /**
     * 费率类型：百分比
     */
    public static final int RATETYPE_PERCENT = 1;
    /**
     * 费率类型：每笔固定值
     */
    public static final int RATETYPE_FIXATION = 2;
}
