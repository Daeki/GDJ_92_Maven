package com.winter.app.board.notice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.winter.app.board.BoardFileVO;
import com.winter.app.board.BoardVO;
import com.winter.app.commons.Pager;
import com.winter.app.members.MemberVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping(value="/notice/*")
@Slf4j
public class NoticeController {

	@Autowired
	private NoticeService noticeService;
	
	@Value("${board.notice}")
	private String name;
	
	@ModelAttribute("board")
	public String getBoard() {
		return name;
	}
	
	
	@GetMapping("list")
	public String list(@ModelAttribute Pager pager, Model model)throws Exception{
		//
		
		if(pager != null) {
			throw new NullPointerException();
		}
		
		List<BoardVO> list = noticeService.list(pager);
		
		
		model.addAttribute("list", list);
		
		return "board/list";
	}
	
	@GetMapping("detail")
	public String detail(NoticeVO noticeVO, Model model)throws Exception{
//		String n=request.getParameter("boardNum");
//		int num = Integer.parseInt(n);
//		NoticeVO noticeVO = new NoticeVO();
//		noticeVO.setBoardNum(num);
		
		BoardVO boardVO = noticeService.detail(noticeVO);
		
		model.addAttribute("vo", boardVO);
		
		return "board/detail";
	}
	
	@GetMapping("add")
	public String insert(@ModelAttribute("boardVO") BoardVO noticeVO, Model model)throws Exception{
		//model.addAttribute("boardVO", new NoticeVO());
		return "board/add";
	}
	
	@PostMapping("add")
	public String insert(@Valid BoardVO noticeVO, BindingResult bindingResult,MultipartFile [] attaches, HttpSession session)throws Exception{
		
		if(bindingResult.hasErrors()) {
			return "board/add";
		}
		
		MemberVO memberVO = (MemberVO)session.getAttribute("member");
		noticeVO.setBoardWriter(memberVO.getUsername());
		int result = noticeService.insert(noticeVO, attaches);
		return "redirect:./list";
	}
	
	@GetMapping("update")
	public String update(BoardVO noticeVO, Model model)throws Exception{
		BoardVO boardVO = noticeService.detail(noticeVO);
		model.addAttribute("vo", boardVO);
		
		return "board/add";
	}
	
	@PostMapping("update")
	public String update(NoticeVO noticeVO,MultipartFile [] attaches, Model model)throws Exception{
		int result = noticeService.update(noticeVO, attaches);
		
		String msg = "수정 실패";
		
		if(result>0) {
			msg="수정 성공";
		}
		
		String url="./detail?boardNum="+noticeVO.getBoardNum();
		
		model.addAttribute("msg", msg);
		model.addAttribute("url", url);
		
		return "commons/result";//"redirect:./detail?boardNum="+noticeVO.getBoardNum();
	}
	
	@PostMapping("delete")
	public String delete(NoticeVO noticeVO, Model model)throws Exception{
		int result = noticeService.delete(noticeVO);
		String msg = "삭제 실패";
		
		if(result>0) {
			msg="삭제 성공";
		}
		
		String url="./list";
		
		model.addAttribute("msg", msg);
		model.addAttribute("url", url);
		
		return "commons/result";
		
	}
	
	@PostMapping("fileDelete")
	@ResponseBody
	public int fileDelete(BoardFileVO boardFileVO, Model model)throws Exception{
		int result = noticeService.fileDelete(boardFileVO);
		
		return result;
	}
	
	@GetMapping("fileDown")
	public String fileDown(BoardFileVO boardFileVO, Model model)throws Exception{
		boardFileVO = noticeService.fileDetail(boardFileVO);
		model.addAttribute("vo", boardFileVO);
		return "fileDownView";
	}
	
	@PostMapping("boardFile")
	@ResponseBody
	public String boardFile(MultipartFile bf)throws Exception{
		log.info(bf.getOriginalFilename());
		
		return noticeService.boardFile(bf);
	}
	
	@PostMapping("boardFileDelete")
	@ResponseBody
	public boolean boardFileDelete(String fileName)throws Exception{
		return noticeService.boardFileDelete(fileName);
	}
	
//	@ExceptionHandler(exception = Exception.class)
//	public String error() {
//		
//		return "errors/error";
//	}
	

	
	
}
