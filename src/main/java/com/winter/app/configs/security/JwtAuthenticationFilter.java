package com.winter.app.configs.security;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

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
		for(Cookie c: cookies) {
			if(c.getName().equals("accessToken")) {
				token = c.getValue();
				break;
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
			}
		}
		
		chain.doFilter(request, response);
	}
	
}
