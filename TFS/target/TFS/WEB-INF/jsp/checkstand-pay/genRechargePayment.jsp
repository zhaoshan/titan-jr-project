<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/comm/taglib.jsp"%>
<!DOCTYPE html>
<html>
	<head>
	 <meta charset="UTF-8">
    <title>融数支付</title>
    <jsp:include page="/comm/static-resource.jsp"></jsp:include>
	<jsp:include page="/comm/tfs-static-resource.jsp"></jsp:include>
	</head>
<body>
<form action="http://devrsjf.rongcapital.com.cn:8486/checkstand/payment" name="pay_form" id="pay_form" method="post" >
  <c:if test="${rechargeDataDTO !=null}">
    <input name="merchantNo" type="hidden" value="${rechargeDataDTO.merchantNo}"/>
	<input name="orderNo" type="hidden" value="${rechargeDataDTO.orderNo}"/>
	<input name="orderAmount" type="hidden" value="${rechargeDataDTO.orderAmount}"/>
	<input name="amtType"  type="hidden" value="${rechargeDataDTO.amtType}"/>
	<input name="payType" type="hidden" value="${rechargeDataDTO.payType}"/>
	<input name="bankInfo" type="hidden" value="${rechargeDataDTO.bankInfo}"/>
    <input name="pageUrl" type="hidden" value="${rechargeDataDTO.pageUrl}"/>
	<input name="notifyUrl" type="hidden" value="${rechargeDataDTO.notifyUrl}"/>
	<input name="orderTime" type="hidden" value="${rechargeDataDTO.orderTime}"/>
	<input name="orderExpireTime" type="hidden" value="${rechargeDataDTO.orderExpireTime}"/>
	<input name="orderMark" type="hidden" value="${rechargeDataDTO.orderMark}"/>
	<input name="signType" type="hidden" value="${rechargeDataDTO.signType}"/>
	<input name="busiCode" type="hidden" value="${rechargeDataDTO.busiCode}"/>
	<input name="version" type="hidden" value="${rechargeDataDTO.version}"/>
	<input name="charset" type="hidden" value="${rechargeDataDTO.charset}"/>
	<input name="signMsg" type="hidden" value="${rechargeDataDTO.signMsg}"/>
  </c:if>

</form>
<jsp:include page="/comm/static-js.jsp"></jsp:include>
</body>
<script type="text/javascript">
   if('${result}'=="false"){
	   new top.Tip({msg: '${msg}', type: 1, time: 20000});
   }else{
	   function submitform(){
		   $("form").submit();
		}
		 window.onload = submitform;
   }

</script>

</html>