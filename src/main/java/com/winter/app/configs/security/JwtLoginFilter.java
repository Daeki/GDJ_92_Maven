package com.winter.app.configs.security;

import java.io.IOException;
import java.net.URLEncoder;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//로그인 요청시 실행하는 필터
//username, password를 꺼내서 UserDetailService의 loadUserByUsername 호출

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;
	
	private JwtTokenManager jwtTokenManager;
	
	public JwtLoginFilter(AuthenticationManager authenticationManager, JwtTokenManager jwtTokenManager) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenManager = jwtTokenManager;
		
		this.setFilterProcessesUrl("/member/loginProcess");
		
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		
		
		
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
		//UsernamePasswordAuthenticationToken에서 UserDetailService의 loadUserByUsername을 호출하고
		//패스워드가 일치한ㄴ지 판별하고 해당 Authentication객체를 SecurityContextHolder에 담아줌
		
		return authenticationManager.authenticate(authenticationToken);
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		//사용자의 정보로 Token을 생성
		String accesstoken = jwtTokenManager.makeAccessToken(authResult);
		String refreshtoken = jwtTokenManager.makeRefreshToken(authResult);
		Cookie cookie = new Cookie("accessToken", accesstoken);
		cookie.setPath("/");
		cookie.setMaxAge(180);
		cookie.setHttpOnly(true);
		
		
		response.addCookie(cookie);
		
		cookie = new Cookie("refreshToken", refreshtoken);
		cookie.setPath("/");
		cookie.setMaxAge(600);
		cookie.setHttpOnly(true);	
		
		response.addCookie(cookie);
		
		response.sendRedirect("/");
		
	}
	
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		// TODO Auto-generated method stub
		String message="관리자에게 문의";
		if(failed instanceof BadCredentialsException) {
			message="비밀번호 틀림";
		}
		
		if(failed instanceof DisabledException) {
			message="유효하지 않은 사용자";
		}
		
		if(failed instanceof AccountExpiredException) {
			message="사용자 계정의 유효 기간이 만료";
		}

		if(failed instanceof LockedException) {
			message="사용자 계정이 잠겨 있습니다";
		}
	
		if(failed instanceof CredentialsExpiredException) {
			message="자격 증명 유효 기간이 만료";
		}
		
		if(failed instanceof InternalAuthenticationServiceException) {
			message="ID 틀림";
		}			
		
		if(failed instanceof AuthenticationCredentialsNotFoundException) {
			message="관리자에게 문의";
		}	
		
		message = URLEncoder.encode(message, "UTF-8");
		
		response.sendRedirect("./login?failMessage="+message);
	}
	
}
