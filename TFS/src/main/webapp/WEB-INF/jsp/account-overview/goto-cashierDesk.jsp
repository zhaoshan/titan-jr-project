<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<%
  	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/titanjr-pay-app";
%>

<script type="text/javascript" src="<%=basePath%>/js/titanpay.js"></script>
</head>
<body>

	.....跳转中，请稍后
	
	
<script>
	
titanPayObj.initTitanPay();
window.onload = function() {
	
 	 

		var orderInfo = {
			name : "${userName}", //打开收银台人员姓名  N
			escrowedDate : "2016-08-31",//保证期时间  N
			goodsId : "${payOrderNo}",//商品编号，可以是对方的订单号
			goodsDetail : "${payDesc}",//商品描述  N
			goodsName : "${payName}",//商品名称 N
			userId : "${userId}",//付款方身份标示   如果是财务，则建议是FCUSERID，  如果是GDP，则是用户ID
			ruserId : "${userId}",//收款方身份标示 N  ,GDP可以指定接受方的   商家联盟可以指定其FCUSERID
			amount : "0",//订单金额
			payerType : "${payType}",//付款人类型   财务 GDP 等
			// 			currencyType : "1",//币种
			// 			checkOrderUrl : 'http://www.baidu.com',//可选
			notify : '',
			version : "${version}"//金融版本 v1.0  v1.1
		};

		var businessInfo = {
			fcUserId:"${fcUserId}"
		};
	
	//window.location 就是指当前文档的路径，就是document 
		window.location.href = titanPayObj.getTitanPayUrl(orderInfo, businessInfo);
		//titanPayObj.titanPay(orderInfo, businessInfo);
		

}
</script>
</body>
</html>