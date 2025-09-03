package com.winter.app.configs.security;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

	private JwtTokenManager jwtTokenManager;
	
	public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenManager jwtTokenManager) {
		// TODO Auto-generated constructor stub
		super(authenticationManager);
		
		this.jwtTokenManager = jwtTokenManager;
		
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		// Token을 검증
		
		//1. 토큰을 꺼내기
		Cookie [] cookies =request.getCookies();
		String token="";
		
		if(cookies !=null) {
			for(Cookie c: cookies) {
				if(c.getName().equals("accessToken")) {
					token = c.getValue();
					 
					break;
				}
				
			}
		}
		System.out.println("Token : "+token);
		//2. 토큰을 검증
		if(token != null && token.length() !=0) {
			try {
				Authentication authentication = jwtTokenManager.getAuthenticationByToken(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				System.out.println(authentication.getName());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				//SecurityException || MalformedException || SignatureException : 유효하지 않는 JWT 서명
				//ExpiredJwtException     : 기간이 만료된 Token
				//UnsupportedJwtException : 지원되지 않는 Token
				//IllegalArgumentException : 잘못된 토큰
				
				if(e instanceof ExpiredJwtException) {
					for(Cookie cookie: cookies) {
						if(cookie.getName().equals("refreshToken")) {
							String newtoken = cookie.getValue();
							try {
								Authentication authentication = jwtTokenManager.getAuthenticationByToken(newtoken);
								SecurityContextHolder.getContext().setAuthentication(authentication);
								newtoken = jwtTokenManager.makeAccessToken(authentication);
								Cookie c = new Cookie("accessToken", newtoken);
								c.setPath("/");
								c.setMaxAge(180);
								c.setHttpOnly(true);
								
								response.addCookie(c);
								
							}catch (Exception ex) {
								// TODO: handle exception
								ex.printStackTrace();
							}
						}
					}
				}
			}
		}
		
		chain.doFilter(request, response);
	}
	
}
