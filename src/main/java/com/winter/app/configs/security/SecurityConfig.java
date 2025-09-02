package com.winter.app.configs.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.AntPathMatcher;

import com.winter.app.members.MemberService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
	private LoginSuccessHandler loginSuccessHandler;
	
	@Autowired
	private LoginFailHandler loginFailHandler;
	
	@Autowired
	private AddLogoutHandler addLogoutHandler;
	
	@Autowired
	private AddLogoutSuccessHandler addLogoutSuccessHandler;
	
	@Autowired
	private MemberService memberService;
	
	@Autowired
	private JwtTokenManager jwtTokenManager;
	
	@Autowired
	private AuthenticationConfiguration authenticationConfiguration;
	
	//정적자원들을 Security에서 제외
	@Bean
	WebSecurityCustomizer customizer() {
		
		//web => WebSecurity
		return web -> {
			web
				.ignoring()
					.requestMatchers("/css/**")
					.requestMatchers("/js/**", "/vendor/**")
					.requestMatchers("/files/**")
					;
		};
	}
	
	//인증과 권한의 설정
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)throws Exception{
		httpSecurity
			.cors(cors-> cors.disable())
			.csrf(csrf -> csrf.disable())
			
			//권한에 관련된 설정
			.authorizeHttpRequests(auth->{
				auth
					.requestMatchers("/notice/add", "/notice/update", "/notice/delete").hasRole("ADMIN") //ROLE_ADMIN
					.requestMatchers("/products/add", "/products/update", "/products/delete").hasAnyRole("MANAGER", "ADMIN")
					//.requestMatchers("/member/detail", "/member/logout", "/member/update", "/member/delete").access("hasRole('ROLE_MEMBER') or hasRole('ROLE_MANAGER')")
					.requestMatchers("/member/detail", "/member/logout", "/member/update", "/member/delete").authenticated()
					.anyRequest().permitAll()
					;
			})
			//form 관련 설정
			//개발자가 로그인 검증을 하지 않는다, Security Filter에서 검증
			.formLogin(form->{
				form.disable()
			 ;
			})
			//logout 설정
			//개발자가 아닌 Security Filter 처리
			.logout(logout->{
				logout
				.logoutUrl("/member/logout")
				.invalidateHttpSession(true)
				.deleteCookies("accessToken")
				.logoutSuccessUrl("/");
			})
			

			
			//Session 인증방식이 아닌
			//Token 인증방식이기 때문에 Session을 사용하지 않음
			.sessionManagement((s)->{
				s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				;
			})
			
			
			.httpBasic((httpBasic)->{
				httpBasic.disable();
			})
			//
			.addFilter(new JwtAuthenticationFilter(authenticationConfiguration.getAuthenticationManager(), jwtTokenManager))
			.addFilter(new JwtLoginFilter(authenticationConfiguration.getAuthenticationManager(), jwtTokenManager))
			
//			.oauth2Login((o)->{
//				o.userInfoEndpoint((user)->{
//					user.userService(memberService);
//				});
//			})
			
			;
		
		return httpSecurity.build();	
	}
	

}
