package com.winter.app.members;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.winter.app.members.validation.AddGroup;
import com.winter.app.members.validation.UpdateGroup;
import com.winter.app.products.ProductVO;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/member/*")
@Slf4j
public class MemberController {
	
	@Autowired
	private MemberService memberService;
	
	@GetMapping("detail")
	public void detail()throws Exception{}
	

	
	@GetMapping("update")
	public String update(HttpSession session, Model model) {
		MemberVO memberVO = (MemberVO)session.getAttribute("member");
		
		model.addAttribute("memberVO", memberVO);
		return "member/memberUpdate";
	}
	
	@PostMapping("update")
	public String update(HttpSession session, @Validated(UpdateGroup.class) MemberVO memberVO,BindingResult bindingResult, MultipartFile profile)throws Exception{
		
		if(bindingResult.hasErrors()) {
			return "member/memberUpdate";
		}
		
		MemberVO member = (MemberVO)session.getAttribute("member");
		
		memberVO.setUsername(member.getUsername());
		
		int result = memberService.update(memberVO);
		
		if(result>0) {
			memberVO.setPassword(member.getPassword());
			memberVO = memberService.login(memberVO);
			session.setAttribute("member", memberVO);
		}
		
		return "redirect:./detail";
	}
	
	@GetMapping("login")
	public String login(Principal principal) throws Exception{
		if(principal != null) {
			return "redirect:/";
		}
		
		return "member/login";
	}
	


	@GetMapping("join")
	public void join(MemberVO memberVO) throws Exception{}
	
	@PostMapping("join")
	public String join(@Validated({AddGroup.class, UpdateGroup.class}) MemberVO memberVO, BindingResult bindingResult,MultipartFile profile) throws Exception{
		
		boolean check = memberService.hasMemberError(memberVO, bindingResult);
		
		if(check) {
			return "member/join";
		}
		
		//int result = memberService.join(memberVO, profile);
		
		return "redirect:/";
	}
	
	@PostMapping("cartAdd")
	@ResponseBody
	public int cartAdd(Long productNum, HttpSession session)throws Exception{
		MemberVO memberVO =(MemberVO)session.getAttribute("member");
		Map<String, Object> map = new HashMap<>();
		map.put("username", memberVO.getUsername());
		map.put("productNum", productNum);
		
		return memberService.cartAdd(map);
		
	}
	
	@GetMapping("cartList")
	public void cartList(HttpSession session, Model model)throws Exception{
		MemberVO memberVO = (MemberVO)session.getAttribute("member");
		List<ProductVO> list = memberService.cartList(memberVO);
		model.addAttribute("list", list);
	}
	
	@PostMapping("cartDelete")
	public String cartDelete(Long [] productNum, HttpSession session)throws Exception{
		MemberVO memberVO = (MemberVO)session.getAttribute("member");
		int result = memberService.cartDelete(productNum, memberVO);
		
		return "redirect:./cartList";
	}
	
	@GetMapping("delete")
	public String delete(@AuthenticationPrincipal MemberVO memberVO)throws Exception{
		log.info("{}", memberVO);
		
		if(memberVO.getSns() == null) {
			//service에서 삭제
		}else if(memberVO.getSns().toUpperCase().equals("KAKAO")) {
			//연결해제
			WebClient webClient = WebClient.create();
			
			Mono<String> result = webClient
				.post()
				.uri("https://kapi.kakao.com/v1/user/unlink")
				.header("Authorization", "Bearer "+memberVO.getAccessToken())
				.retrieve()
				.bodyToMono(String.class)
				;
			log.info("{}", result.block());
			
			
		}
		
		return "redirect:./logout";
	}
	
	
	
}
