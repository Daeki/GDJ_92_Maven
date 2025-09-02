<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<%@ include file="/WEB-INF/views/include/head_css.jsp"%>
<link
	href="https://cdn.jsdelivr.net/npm/summernote@0.9.0/dist/summernote-lite.min.css"
	rel="stylesheet">

</head>
<body id="page-top">
	<div id="wrapper">
		<c:import url="/WEB-INF/views/include/sidebar.jsp"></c:import>

		<!-- Start  -->
		<div id="content-wrapper" class="d-flex flex-column">
			<div id="content">
				<c:import url="/WEB-INF/views/include/topbar.jsp"></c:import>
				<div class="container-fluid">
					<!-- page contents 내용 -->
					<div class="row justify-content-center ">
						<div class="col-md-8">
							<h3>${param.failMessage}</h3>
							<form method="post" action="loginProcess">
								<div class="mb-3">
									<label for="username" class="form-label">ID</label> 
									<input type="text" value="${cookie.rememberId.value}" class="form-control" name="username" id="username" aria-describedby="usernameHelp">
									<div id="emailHelp" class="form-text">We'll never share
										your email with anyone else.</div>
								</div>
								<div class="mb-3">
									<label for="password" class="form-label">Password</label>
									<input type="password" class="form-control"
										id="password" name="password">
								</div>
								<div class="mb-3 form-check">
									<input type="checkbox" class="form-check-input"
										id="exampleCheck1" value="1" name="rememberId"> <label class="form-check-label"
										for="exampleCheck1">ID 기억하기</label>
								</div>
								<div class="mb-3 form-check">
									<input type="checkbox" class="form-check-input"
										id="exampleCheck1" value="1" name="remember-me"> <label class="form-check-label"
										for="exampleCheck1">자동로그인</label>
								</div>								
								<button type="submit" class="btn btn-primary">Submit</button>
							</form>
							
							<div>
								<a class="btn btn-primary" href="/oauth2/authorization/kakao">카카오로그인</a>
							</div>
						</div>
					</div>

				</div>
			</div>
			<!--  End Content  -->
			<c:import url="/WEB-INF/views/include/footer.jsp"></c:import>
		</div>


	</div>
	<c:import url="/WEB-INF/views/include/tail.jsp"></c:import>
	<script type="text/javascript" src="/js/board/board_add.js"></script>
	<script
		src="https://cdn.jsdelivr.net/npm/summernote@0.9.0/dist/summernote-lite.min.js"></script>
	<script type="text/javascript">
	$("#contents").summernote({
		callbacks:{
			onImageUpload: function (files) {
				console.log("files: ", files[0]);
				let f = new FormData();
				f.append("bf", files[0])
				
				fetch("./boardFile",{
					method:"POST",
					body:f
				})
				.then(r=>r.text())
				.then(r=>{
					$("#contents").summernote('editor.insertImage', r);
				})
				.catch(e => console.log(e))
				
				;
			},
			onMediaDelete: function(files){
				let f = $(files[0]).attr("src"); // /files/notice/****.jpg
				
				let params = new URLSearchParams();
				params.append("fileName", f);
				fetch("./boardFileDelete", {
					method:"POST",
					body:params
				})
				.then(r=>r.json())
				.then(r=>{
					console.log(r)
				})
				
			}
		}
		
	})
	</script>
</body>
</html>