<!-- common css-->
<%@ include file="/comm/path-param.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${empty CURRENT_THEME  }"><c:set var="CURRENT_THEME" value="DeepSkyBlue" scope="session"></c:set></c:if>
<link rel="stylesheet" href="<%=webWalletPath%>/font/iconfont.css"/>
<link rel="stylesheet" href="<%=cssSaasPath%>/css/fangcang.min.css">
<link rel="stylesheet" href="<%=cssSaasPath%>/css/jquery-ui-1.9.2.custom.css" >
<link rel="stylesheet" href="<%=cssSaasPath%>/css/style.css">
<link rel="stylesheet" href="<%=cssSaasPath%>/css/withdrawals.css"/>
<link href="<%=cssSaasPath%>/themes/${CURRENT_THEME }/css/style.css" rel="stylesheet" type="text/css">
<script type="text/javascript">
var js_base_path = "<%=basePath%>";
</script>


