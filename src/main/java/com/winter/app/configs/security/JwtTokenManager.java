package com.winter.app.configs.security;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.winter.app.members.MemberDAO;
import com.winter.app.members.MemberVO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenManager {
	
	//Token을 생성 하거나, Token을 검증
	
	
	//노출금지, 모든 서버가 같은 값
	@Value("${jwt.secretKey}")
	private String secretKey;
	
	@Value("${jwt.tokenValidTime}")
	private Long tokenValidTime;
	
	@Value("${jwt.issuer}")
	private String issuer;
	
	private SecretKey key;
	
	@Autowired
	private MemberDAO memberDAO;
	
	//생성자에서 코드 작성 가능
	@PostConstruct
	public void init() {
		String k = Base64.getEncoder().encodeToString(this.secretKey.getBytes());
		key = Keys.hmacShaKeyFor(k.getBytes());
	}
	
	//Token 발급
	public String createToken(Authentication authentication) {
		return Jwts
				.builder()
				.subject(authentication.getName()) //subject : 사용자의 ID(username)
				.claim("roles", authentication.getAuthorities().toString())
				.issuedAt(new Date()) //Token을 생성한 시간
				.expiration(new Date(System.currentTimeMillis()+tokenValidTime))
				.issuer(issuer)
				.signWith(key)
				.compact()
			;
		}
	
	//Token 검증
	public Authentication getAuthenticationByToken(String token)throws Exception{
		Claims claims = Jwts
			.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			;
		
		//검증 통과
		MemberVO memberVO = new MemberVO();
		memberVO.setUsername(claims.getSubject());
		UserDetails userDetails = memberDAO.login(memberVO);
		
		//MemberVO(UserDetail)를 Authentication으로 변경
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		
		return authentication;
		//검증실패는 Exception 발생
	}

}
