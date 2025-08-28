package com.winter.app.members;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import com.winter.app.board.notice.NoticeService;
import com.winter.app.commons.FileManager;
import com.winter.app.products.ProductVO;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class MemberService extends DefaultOAuth2UserService implements UserDetailsService {

    private final NoticeService noticeService;
	@Autowired
	private MemberDAO memberDAO;
	
	@Autowired
	private FileManager fileManager;
	
	@Value("${app.upload}")
	private String upload;
	
	@Value("${board.member}")
	private String board;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

    MemberService(NoticeService noticeService) {
        this.noticeService = noticeService;
    }
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		// TODO Auto-generated method stub
		log.info("{}", userRequest.getAccessToken());
		log.info("{}", userRequest.getAdditionalParameters());
		log.info("{}", userRequest.getClientRegistration());
		
		String sns = userRequest.getClientRegistration().getRegistrationId();
		OAuth2User user=null;
		if(sns.toUpperCase().equals("KAKAO")) {
			user = this.useKakao(userRequest);
			
		}
		
		return user;
		
		
	}
	
	private OAuth2User useKakao(OAuth2UserRequest userRequest) {
		OAuth2User user = super.loadUser(userRequest);
		log.info("{}", user.getName());
		log.info("{}", user.getAttributes());
		log.info("{}", user.getAuthorities());
		Map<String, Object> map = user.getAttributes();
		LinkedHashMap<String, Object> m = (LinkedHashMap<String, Object>)map.get("properties");
		
		MemberVO memberVO = new MemberVO();
		
		memberVO.setName(user.getName());
		
		
		memberVO.setAccessToken(userRequest.getAccessToken().getTokenValue());
		
		
		memberVO.setUsername(m.get("nickname").toString());
		
		ProfileVO profileVO = new ProfileVO();
		profileVO.setSaveName(m.get("profile_image").toString());
		memberVO.setProfileVO(profileVO);
		
		List<RoleVO> lists = new ArrayList<>();
		RoleVO roleVO = new RoleVO();
		roleVO.setRoleName("ROLE_MEMBER");
		lists.add(roleVO);
		
		memberVO.setRoleVOs(lists);
		
		memberVO.setAttributes(map);
		
		memberVO.setSns("kakao");
		return memberVO;
		
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		MemberVO memberVO = new MemberVO();
		memberVO.setUsername(username);
		System.out.println("로그인 서비스");
		try {
			memberVO = memberDAO.login(memberVO);
			return memberVO;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	//검증 메서드
	public boolean hasMemberError(MemberVO memberVO, BindingResult bindingResult)throws Exception{
		
		boolean check=false;
		//check: true  => 검증실패
		//check: false => 검증통과
		
		//1. Annotation 검증
		check= bindingResult.hasErrors();
		
		//2. 사용자 정의로 패스워드가 일치하는지 검증
		if(!memberVO.getPassword().equals(memberVO.getPasswordCheck())) {
			check = true;
			bindingResult.rejectValue("passwordCheck", "member.password.notEqual");
		}
		
		//3. ID 중복 검사
		MemberVO result = memberDAO.login(memberVO);
		
		if(result != null) {
			check=true;
			bindingResult.rejectValue("username", "member.id.equal");
		}
		
		
		return check;
	}
	
	
	
	public int join(MemberVO memberVO, MultipartFile profile) throws Exception{
		
		memberVO.setPassword(passwordEncoder.encode(memberVO.getPassword()));
		int result = memberDAO.join(memberVO);
		
		ProfileVO profileVO = new ProfileVO();
		profileVO.setUsername(memberVO.getUsername());
		profileVO.setSaveName("default.jpg");
		profileVO.setOriName("default.jpg");
		if(profile != null && !profile.isEmpty()) {
			profileVO.setSaveName(fileManager.fileSave(upload+board, profile));
			profileVO.setOriName(profile.getOriginalFilename());
		}
		
		result = memberDAO.profileInsert(profileVO);
		
		Map<String, Object> map = new HashMap<>();
		map.put("username", memberVO.getUsername());
		map.put("roleNum", 3);
		
		result = memberDAO.addRole(map);
				
		
		return result;
	}
	
	public int update(MemberVO memberVO)throws Exception{
		//passwordEncoder.matches(memberVO.getPassword(), passwordEncoder.encode("user1"));
		
		return memberDAO.update(memberVO);
		
	}
	
	
	public int cartAdd(Map<String, Object> map)throws Exception{
		return memberDAO.cartAdd(map);
	}
	
	public List<ProductVO> cartList(MemberVO memberVO) throws Exception{
		//페이징 처리 해야 함
		return memberDAO.cartList(memberVO);
	}
	
	public int cartDelete(Long [] productNum, MemberVO memberVO)throws Exception{
			Map<String, Object> map = new HashMap<>();
			map.put("username", memberVO.getUsername());
			map.put("list", Arrays.asList(productNum));
		
		return memberDAO.cartDelete(map);
		
	}


}
