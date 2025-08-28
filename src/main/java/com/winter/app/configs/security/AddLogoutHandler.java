package com.winter.app.configs.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.winter.app.members.MemberVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
@Component
@Slf4j
public class AddLogoutHandler implements LogoutHandler {

	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	private String restKey;
	@Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
	private String uri;
	@Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
	private String adminKey;
	
	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		// TODO Auto-generated method stub
		try {
		if(authentication instanceof OAuth2AuthenticationToken) {
			MemberVO memberVO=(MemberVO)authentication.getPrincipal();
			if(memberVO.getSns().toUpperCase().equals("KAKAO")) {
				this.useKakao(memberVO);
			}
		}
		}catch (Exception e) {
			log.error(e.getMessage());
		}
		
		
		
	}
	
	
	private void useKakao(MemberVO memberVO) {
		
		//parameter
		Map<String, Object> param = new HashMap<>();
		param.put("target_id_type", "user_id");
		param.put("target_id", memberVO.getName());
		log.info("logout");
		WebClient webClient = WebClient.create();
		
		Mono<String> result = webClient.post()
				 .uri("https://kapi.kakao.com/v1/user/logout")
				 .header("Authorization", "KakaoAK "+adminKey)
				 .bodyValue(param)
				 .retrieve()
				 .bodyToMono(String.class);
				 
		log.info("Logout : {}", result.block());
	}

}









