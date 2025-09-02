<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<%@ include file="/WEB-INF/views/include/head_css.jsp" %>
</head>
<body id="page-top">
	<div id="wrapper">
		<c:import url="/WEB-INF/views/include/sidebar.jsp"></c:import>
		
		<!-- Start  -->
		<div id="content-wrapper" class="d-flex flex-column">
			<div id="content">
				<c:import url="/WEB-INF/views/include/topbar.jsp"></c:import>
				<sec:authentication property="principal" var="member"/>
				<div class="container-fluid">
					<div>
					<div class="alert alert-primary" role="alert">
					  ${member.email}
					</div>
					<div class="alert alert-primary" role="alert">
					  ${member.email}
					</div>
					<div class="alert alert-primary" role="alert">
					  <img src="/files/member/${member.profileVO.saveName}">
					</div>
					
					<div>
						<a href="./delete">탈퇴</a>
					</div>
					</div>
				</div>
			</div>
			<!--  End Content  -->
			<c:import url="/WEB-INF/views/include/footer.jsp"></c:import>
		</div>
		
		
	</div>
	<c:import url="/WEB-INF/views/include/tail.jsp"></c:import>
	
</body>
</html>