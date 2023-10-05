package sec03.brd08;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;


//1.게시판 글 목록보기 
//1-1) /board/listArticles.do로 요청을 하면 /board/* 로 맵핑된 BoardController로 이동.(글목록보기 구현 과정) 

//2. 게시판 글쓰기
//2-1) 글목록창(listArticles.jsp)에서 글쓰기창을 요청하면 /board/* 로 맵핑된 BoardController로 이동 (게시판 글쓰기 과정 구현)

//3. 글 상세 기능
//3-1) /board/listArticles.do 위치에서 시작함. (글 상세 기능 구현)

//4. 글 수정 기능 
//4-1) 글 상세창 /board/viewArticle.do?articleNO="글번호"에서 시작함 (글 수정 기능 구현) 

//5. 글 삭제 기능
//5-1) 글 상세창 /board/viewArticle.do?articleNO="글번호"에서 시작함 (글 삭제 기능 구현) 

//6. 답글 쓰기 기능
//6-1) 글 상세창 /board/viewArticle.do?articleNo="글번호"에서 시작함 (답글 쓰기 기능 구현)

//7. 게시판 페이징 기능
//7-1) /board/listArticles.do 요청을 하면 /board/* 로 맵핑된 BoardController로 이동. (게시판 페이징 기능 구현)
@WebServlet("/board/*")
public class BoardController extends HttpServlet {
	private static final long serialVersionUID = 1L;
//	2-2) 글에 첨부한 이미 저장 위치를 ARTICLE_IMAGE_REPO 상수로 선언 후 대입 (게시판 글쓰기 과정 구현) 
	private static String ARTICLE_IMAGE_REPO = "C:\\board\\article_image";
	BoardService boardService;
	ArticleVO articleVO;

    public BoardController() {
        super();
        // TODO Auto-generated constructor stub
    }


	@Override
	public void init(ServletConfig config) throws ServletException {
		boardService = new BoardService();
		articleVO = new ArticleVO();
	}


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}



	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}

	private void doHandle(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String nextPage = "";
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
//		6-2) 답글에 대한 부모글 번호를 저장하기 위해 session객체를 만든다. (답글 쓰기 기능 구현)
		HttpSession session;
		String action = request.getPathInfo();
//		1-2) getPathInfo() 메서드를 통해 /listArticles.do가 추출되고 변수 action에 저장된다. (글목록보기 구현 과정) 

//		2-3) getPathInfo()를 통해 추출된 /addArticle.do 값이 변수 action에 저장됨. (게시판 글쓰기 과정 구현)

//		3-2) 글 목록의 제목을 누르면 getPathInfo()를 통해 /viewArticle.do 값이 변수 action에 저장됨. (글 상세 기능 구현)

//		4-2) viewArticle.jsp의 수정 반영하기를 누르면 getPathInfo()를 통해 /modArticle.do 값이 변수 action에 저장됨. (글 수정 기능 구현)

//		5-2) viewArticle.jsp의 삭제하기를 누르면 getPathInfo()를 통해 /removeArticle.do 값이 변수 action에 저장됨. (글 삭제 기능 구현) 

//		6-3) viewArticle.jsp에서 답글쓰기를 누르면 getPathInfo()를 통해 /replyForm.do 값이 변수 action에 저장됨 (답글 쓰기 기능 구현)

//		6-9) replyForm.jsp에서 답글 반영하기를 누르면 getPathInfo()를 통해 /addReply.do 값이 변수 action에 저장됨 (답글 쓰기 기능 구현) 
		System.out.println("action: " + action);
		try {
			List<ArticleVO> articlesList = new ArrayList<ArticleVO>();
			if (action == null) {
//		7-2) 최초 요청시 또는 /listArticles.do로 요청 시 request객체의 getParameter() 메서드로 section값과 pageNum 값을 가져와 각각의 변수에 저장. (게시판 페이징 기능 구현)
				String _section = request.getParameter("section");
				String _pageNum = request.getParameter("pageNum");
//		7-3) 3항 연산자를 사용하여 Integer 래퍼 클래스의 parseInt() 의 인자로 만약 최초 요청을 해서 section과 pageNum 값이 없다면 1로 초기화하고 아니면 기존의 _section과 _pageNum에 저장된 값을 가각의 변수에 대입.(게시판 페이징 기능 구현)
				int section = Integer.parseInt(((_section == null) ? "1" : _section));
				int pageNum = Integer.parseInt(((_pageNum == null) ? "1" : _pageNum));
//		7-4) 위의 과정을 통해 구해진 section과 pageNum 값을 pagingMap에 (키,값)으로 저장함. (게시판 페이징 기능 구현)
				Map<String, Integer> pagingMap = new HashMap<String, Integer>();
				pagingMap.put("section", section);
				pagingMap.put("pageNum", pageNum);
//		7-5) BoardService 클래스의 boardService 객체의 메서드 listArticles()를 호출하며 인자로 pagingMap을 전달함. (BoardService 클래스로 이동) (게시판 페이징 기능 구현)
//		7-22) 총 글의 개수와 섹션과 페이지 넘버에 해당하는 글의 정보를 담은 articlesMap의 값이 articlesMap에 저장됨. (게시판 페이징 기능 구현)
				Map articlesMap = boardService.listArticles(pagingMap);
//		7-23) articlesMap에 section과 pageNum 값을 넣고, articlesMap에 바인딩함. (게시판 페이징 기능 구현)
				articlesMap.put("section", section);
				articlesMap.put("pageNum", pageNum);
				request.setAttribute("articlesMap", articlesMap);
//		7-24) 글 목록이 표시되는 /listArticles.jsp로 포워딩하기 위해, nextPage에 listArticles.jsp를 저장함. 
				nextPage = "/board07/listArticles.jsp";
			} else if (action.equals("/listArticles.do")) {
//				1-3) action값이 /listArticles.do이기 때문에 해당 조건에 충족하여 아래의 명령문이 실행된다. (글목록보기 구현 과정) 
				String _section = request.getParameter("section");
				String _pageNum = request.getParameter("pageNum");
				int section = Integer.parseInt(((_section == null) ? "1" : _section));
				int pageNum = Integer.parseInt(((_pageNum == null) ? "1" : _pageNum));
				Map pagingMap = new HashMap();
				pagingMap.put("section", section);
				pagingMap.put("pageNum", pageNum);
//				1-4) init 메소드에서 만들어진 boardService 객체가 만들어지고 listArticles() 메서드에 인자를 넣고, 호출함(BoardService 클래스로 이동) (글목록보기 구현 과정) 
//				1-15) 1-14) 과정을 통해 반환된 articlesList의 값이 articlesMap 변수에 저장됨. (글목록보기 구현 과정) 
				Map articlesMap = boardService.listArticles(pagingMap);
				articlesMap.put("section", section);
				articlesMap.put("pageNum", pageNum);
//				1-16) articlesMap을 통해 조회된 글 목록을 바인딩 함. (글목록보기 구현 과정) 
				request.setAttribute("articlesMap", articlesMap);
//				1-17) listArticles.jsp로 포워딩하기 위해 nextPage에 값이 저장됨 (글목록보기 구현 과정) 
				nextPage = "/board07/listArticles.jsp";
			} else if (action.equals("/articleForm.do")) {
				nextPage = "/board07/articleForm.jsp";
			} else if (action.equals("/addArticle.do")) {
//				2-4) action값이 /addArticle.do이기 때문에 해당 조건에 충족하여 아래의 명령문이 실행됨. (게시판 글쓰기 과정 구현)
				int articleNO = 0;
//				2-5) 파일 업로드 기능을 사용하기 위해 upload() 메서드를 호출 (upload() 메서드로 이동)
//				2-13) 2-12를 통해 반환된 articleMap의 값이 articleMap 변수에 저장됨, 그리고 Map의 get함수를 통해 title, content, imageFilename 등이 반환됨. (게시판 글쓰기 과정 구현)
				Map<String, String> articleMap = upload(request, response);
				String title = articleMap.get("title");
				String content = articleMap.get("content");
				String imageFileName = articleMap.get("imageFileName");
//				2-14) articleVO 객체에 setter를 이용해서 parentNO, id, title, content, imageFileName의 정보를 담아, boardService 객체의 addArticle() 메서드의 인자로 전달하여 호출 (BoardService클래스로 이동) (게시판 글쓰기 과정 구현)
				articleVO.setParentNO(0);
				articleVO.setId("hong");
				articleVO.setTitle(title);
				articleVO.setContent(content);
				articleVO.setImageFileName(imageFileName);
//				2-26) 2-25)를 통해 반환된 새글의 글번호가  articleNO에 저장됨 (게시판 글쓰기 과정 구현)
				articleNO = boardService.addArticle(articleVO);
//				2-27) 파일을 선택한 경우에 아래의 조건문이 실행됨 (게시판 글쓰기 과정 구현)
				if (imageFileName != null && imageFileName.length() != 0) {
//				2-28) temp 폴더에 임시로 업로드된 파일 객체를 생성함. (게시판 글쓰기 과정 구현)
					File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + imageFileName);
//				2-29) 업로드된 파일의 최종위치인 새 글번호로 된 폴더 이름을 생성함 (게시판 글쓰기 과정 구현)
					File destDir = new File(ARTICLE_IMAGE_REPO + "\\" + articleNO);
					destDir.mkdirs();
//				2-30) 임시로 업로드된 파일을 destDir에 저장된 폴더로 이동 (게시판 글쓰기 과정 구현) 
					FileUtils.moveFileToDirectory(srcFile, destDir, true);
//				2-31) 임시로 저장되어 있는 temp 폴더의 파일을 삭제 (게시판 글쓰기 과정 구현)
					srcFile.delete();
				}
//				2-32) 새 글 등록을 알리는 메시지와 글목록을 보여주는 location 객체의 href속성을 이용해 글목록 페이지로 이동하는 것을 자바스크립트로 요청 <끝> (게시판 글쓰기 과정 구현)
				PrintWriter pw = response.getWriter();
				pw.print("<script>" + " alert('새글을 추가했습니다.');" + " location.href='" + request.getContextPath()
						+ "/board/listArticles.do';" + "</script>");
				return;
			} else if (action.equals("/viewArticle.do")) {
//				3-3) action에 저장된 /viewArtlcie.do값으로 인해 해당 조건에 만족하여 아래의 명령문들이 실행됨 (글 상세 기능 구현)
//				3-4) 글 상세창을 요청할 경우 request객체를 통해 가져온 articleNO의 값이 articleNO 변수에 저장됨. (글 상세 기능 구현)
				String articleNO = request.getParameter("articleNO");
//				3-5) 저장된 articleNO의 값을 BoardService 클래스의 객체인 boardService의 메서드 viewArticle()의 인자로 넣어서 호출 (BoardService 클래스로 이동)(글 상세 기능 구현)
//				3-17) 3-16)을 통해 반환된 상세 글 정보를 articleVO 객체에 저장 (글 상세 기능 구현)
				articleVO = boardService.viewArticle(Integer.parseInt(articleNO));
//				3-18) article에 상세 글 정보가 담긴 articleVO가 바인딩 됨 (글 상세 기능 구현)
				request.setAttribute("article", articleVO);
//				3-19) viewArticle.jsp로 포워딩하기 위해 nextPage 변수에 아래의 값을 대입 (글 상세 기능 구현) 
				nextPage = "/board07/viewArticle.jsp";
			} else if (action.equals("/modArticle.do")) {
//				4-3) action에 저장된 /modArticle.do로 해당 조건문에 만족하여 아래의 명령문이 실행됨. (글 수정 기능 구현)
//				4-4) BoardController 클래스의 upload()메서드를 통해 해당하는 파일 또는 텍스트의 정보를 (키, 값)으로 담은 articleMap이 반환되어 articleMap 객체에 저장됨. (글 수정 기능 구현)
				Map<String, String> articleMap = upload(request, response);
//				4-5) 수정할 글 정보가 담긴 articleMap에 get() 메서드를 이용해 각각의 정보를 변수에 담음 (글 수정 기능 구현)
				int articleNO = Integer.parseInt(articleMap.get("articleNO"));
				String title = articleMap.get("title");
				String content = articleMap.get("content");
				String imageFileName = articleMap.get("imageFileName");
//				4-6) 변수에 담긴 수정한 글 정보의 값을 ArticleVo의 객체인 articleVO의 setter 함수를 이용해 담음 (글 수정 기능 구현)
				articleVO.setArticleNO(articleNO);
				articleVO.setParentNO(0);
				articleVO.setId("hong");
				articleVO.setTitle(title);
				articleVO.setContent(content);
				articleVO.setImageFileName(imageFileName);
//				4-7) 수정할 글 정보를 담은 articleVO를 BoardService 클래스의 boardService 객체의 메서드인 modArticle()에 인자로 넣고 호출 (BoardService 클래스로 이동) (글 수정 기능 구현)
				boardService.modArticle(articleVO);
				if (imageFileName != null && imageFileName.length() != 0) {
//				4-14) viewArticle에 hidden으로 저장되어 있던 원래 이미지 파일의 이름인 originalFileName을 Map의 get() 메서드로 가져옴. 그리고 아래는 원래의 업로드 과정 반복 (글 수정 기능 구현)
					String originalFileName = articleMap.get("originalFileName");
					File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + imageFileName);
					File destDir = new File(ARTICLE_IMAGE_REPO + "\\" + articleNO);
					destDir.mkdirs();
					FileUtils.moveFileToDirectory(srcFile, destDir, true);
					;
//				4-15) 수정되기 전의 기존의 파일은 삭제 (글 수정 기능 구현)
					File oldFile = new File(ARTICLE_IMAGE_REPO + "\\" + articleNO + "\\" + originalFileName);
					oldFile.delete();
				}
//				4-16) 글 수정 후에 location 객체의 href 속성을 이용해 수정된 글의 상세화면을 나타냄. <끝>(글 수정 기능 구현)
				PrintWriter pw = response.getWriter();
				pw.print("<script>" + " alert('글을 수정했습니다.');" + " location.href='" + request.getContextPath()
						+ "/board/viewArticle.do?articleNO=" + articleNO + "';" + "</script>");
				return;
			} else if (action.equals("/removeArticle.do")) {
//				5-3) action에 저장된 /removeArticle.do로 해당 조건에 만족하여 아래의 명령문이 실행됨. (글 삭제 기능 구현) 
//				5-4) request객체를 통해 해당하는 글번호를 Integer 래퍼 클래스의 메서드 parseInt()를 통해 int 자료형으로 변환 후, articleNO 변수에 저장 (글 삭제 기능 구현)  
				int articleNO = Integer.parseInt(request.getParameter("articleNO"));
//				5-5) BoardService 클래스의 객체인 boardService의 메서드 removeArticle()를 호출하며 삭제하려는 글 번호인 articleNO를 인자로 함께 전달. (BoardService 클래스로 이동) (글 삭제 기능 구현)
//				5-18) 5-17)을 통해 반환된 삭제된 글 번호들이 articleNOList 객체에 저장됨. (글 삭제 기능 구현)
				List<Integer> articleNOList = boardService.removeArticle(articleNO);
//				5-19) for문을 통해 List에 담긴 삭제된 글 번호가 하나씩 추출되어 _articleNO에 저장. (글 삭제 기능 구현)
				for (int _articleNO : articleNOList) {
//				5-20) _articleNO에 저장된 값으로 imgDir이라는 파일 객체를 생성하고, 해당하는 폴더가 있다면 삭제하는 과정을 반복함. (글 삭제 기능 구현)
					File imgDir = new File(ARTICLE_IMAGE_REPO + "\\" + _articleNO);
					if (imgDir.exists()) {
						FileUtils.deleteDirectory(imgDir);
					}
				}
//				5-21) 자바스크립트의 location 객체의 href 속성으로 글의 삭제가 반영된 글목록으로 이동. <끝> (글 삭제 기능 구현)
				PrintWriter pw = response.getWriter();
				pw.print("<script>" + " alert('글을 삭제했습니다.');" + " location.href='" + request.getContextPath()
						+ "/board/listArticles.do';" + "</script>");
				return;
			} else if (action.equals("/replyForm.do")) {
//				6-4) action에 저장된 /replyForm.do가 해당 조건을 만족하여 아래의 명령문이 실행됨. (답글 쓰기 기능 구현)
//				6-5) viewArticle.jsp의 fn_reply_form()함수를 통해 parentNO가 request객체에 담겨 전송되고, 아래의 request.getParameter('parentNO') 메서드로 반환된다. 
//				그리고 반환된 값을 Integer 래퍼클래스의 parseInt()메서드로 정수 자료형으로 변환시키고 parentNO 변수에 저장한다. (답글 쓰기 기능 구현) 
				int parentNO = Integer.parseInt(request.getParameter("parentNO"));
//				6-6) 세션을 반환받고 parentNO값을 바인딩함 (답글 쓰기 기능 구현).
				session = request.getSession();
				session.setAttribute("parentNO", parentNO);
//				6-7) /replyForm.jsp로 포워딩하기 위해 nextPage 변수에 저장. (답글 쓰기 기능 구현)
				nextPage = "/board07/replyForm.jsp";
			} else if (action.equals("/addReply.do")) {
//				6-10) 답글 전송 시 session에 저장된 parentNO 값을 getAttribute("parentNO") 메서드로 가져와서 parentNO변수에 저장 후, 세션에서 삭제 (답글 쓰기 기능 구현) 
				session = request.getSession();
				int parentNO = (Integer) session.getAttribute("parentNO");
				session.removeAttribute("parentNO");
//				6-11) 글 작성과 비슷한 과정으로 upload() 메서드를 호출.
//				6-13) upload()에 반환된 articleMap의 값이 반환되며 articleMap 객체에 저장되고, get() 메서드를 통해 해당하는 정보가 변수에 저장됨. (답글 쓰기 기능 구현) 
				Map<String, String> articleMap = upload(request, response);
				String title = articleMap.get("title");
				String content = articleMap.get("content");
				String imageFileName = articleMap.get("imageFileName");
//				6-14) 변수에 저장됨 각각의 정보, 특히 session을 통해 가져온 parentNO 등의 변수를 articleVO객체의 setter에 담음. (답글 쓰기 기능 구현) 
				articleVO.setParentNO(parentNO);
				articleVO.setId("lee");
				articleVO.setTitle(title);
				articleVO.setContent(content);
				articleVO.setImageFileName(imageFileName);
//				6-15) BoardService 클래스의 boardService 객체의 addReply()메서드가 답글의 정보가 담긴 articleVO를 인자를 담고 호출됨.(BoardService 클래스로 이동) (답글 쓰기 기능 구현)  
//				6-27) 6-26)의 과정을 통해 articleNO 값이 반환되어 articleNO 변수에 저장됨. (답글 쓰기 기능 구현)
				int articleNO = boardService.addReply(articleVO);
				if (imageFileName != null && imageFileName.length() != 0) {
//				6-28) 만약 답글에 이미지 파일을 첨부했다면 해당 조건문에 만족하여 아래의 명령문이 실행됨. 
//				File 객체들을 생성하여, 임시폴더에 저장된 imageFile 정보를 가진 객체 srcFile과 글번호를 이름으로 가진 폴더를 만들기 위한 객체 destDir을 생성. (답글 쓰기 기능 구현)
					File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + imageFileName);
					File destDir = new File(ARTICLE_IMAGE_REPO + "\\" + articleNO);
//				6-29) 글번호를 이름으로 가진 폴더를 만들기 위해 mkdirs() 메서드를 호출, 그리고 임시폴더에 저장된 이미지 파일을 이동시킴.  (답글 쓰기 기능 구현)
					destDir.mkdirs();
					FileUtils.moveFileToDirectory(srcFile, destDir, true);
				}
//				6-30) 자바 스크립트의 location 객체의 href 속성을 이용하여 답글의 부모 글 페이지로 이동.<끝> (답글 쓰기 기능 구현) 
				PrintWriter pw = response.getWriter();
				pw.print("<script>" + " alert('답글을 추가했습니다.');" + " location.href='" + request.getContextPath()
						+ "/board/viewArticle.do?articleNO=" + articleNO + "';" + "</script>");
				return;
			} else {
				nextPage = "/board06/listArticles.jsp";
			}
//			1-18) 위에 nextPage를 통해 listArticles.jsp로 포워딩됨 <끝> (글목록보기 구현 과정) 

//			3-20) 위의 nextPage를 통해 viewArticle.sjp로 포워딩됨 <끝> (글 상세 기능 구현)

//			6-8) 위의 nextPage를 통해 replyForm.jsp로 포워딩됨. 그리고 포워딩된 replyForm.jsp 페이지에서 답글을 작성 (답글 쓰기 기능 구현)

//			7-25) 위의 nextPage를 통해 listArticles.jsp로 포워딩됨. <끝> (게시판 페이징 기능 구현)
			RequestDispatcher dispatch = request.getRequestDispatcher(nextPage);
			dispatch.forward(request, response);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String, String> upload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
//		2-6) 2-5) 과정을 통해 upload() 메서드로 이동됨. (게시판 글쓰기 과정 구현)

//		6-12) 6-11) 과정을 통해 upload() 메서드로 이동. 이후의 과정은 게시판 글쓰기 과정과 유사(답글 쓰기 기능 구현) 
		Map<String, String> articleMap = new HashMap<String, String>();
		String encoding = "utf-8";
//		2-7) 파일 객체 생성하고 글 이미지 저장 폴더위치를 인자로 전달 (게시판 글쓰기 과정 구현)
		File currentDirPath = new File(ARTICLE_IMAGE_REPO);
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setRepository(currentDirPath);
		factory.setSizeThreshold(1024 * 1024);
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List items = upload.parseRequest(request);
			for (int i = 0 ;i<items.size();i++) {
//				공통: 파일 업로드창에서 업로드된 항목을 하나씩 가져온다.
				FileItem fileItem = (FileItem) items.get(i);
//				공통: fileItem.isFormField() 의 리턴값이 true면 일반 텍스트 파일, false면 파일데이터이다. 
				if (fileItem.isFormField()) 
				{
					System.out.println(fileItem.getFieldName() + "=" + fileItem.getString(encoding));
//					2-8) 파일업로드로 같이 전송된 새 글 관련 매개변수를 Map에 (key, value) 값으로 저장한 후 반환하고, 새 글과 관련된 title, content(articleForm에서 일반 텍스트인)를 Map에 저장한다. (게시판 글쓰기 과정 구현)
					articleMap.put(fileItem.getFieldName(), fileItem.getString(encoding));
				} else {
//					2-9) 일반 텍스트가 아닌 파일 데이터의 경우 else 문 안으로 들어온다. getFieldName() input 태그의 name변수의 값이 반환됨. getSize()는 파일의 크기 반환  (게시판 글쓰기 과정 구현)
					System.out.println("파라미터 이름: " + fileItem.getFieldName());
//					System.out.println("파일 이름: " + fileItem.getName());
					System.out.println("파일 크기: " + fileItem.getSize() + "bytes");
					if (fileItem.getSize() > 0) {
//						2-10) 데이터가 첨부파일일 경우 파일명을 getName()메서드를 통해 반환됨. (게시판 글쓰기 과정 구현)
						int idx = fileItem.getName().lastIndexOf("\\");
						if (idx == -1) {
							idx = fileItem.getName().lastIndexOf("/");
						}
						String fileName = fileItem.getName().substring(idx + 1);
						System.out.println("파일명: " + fileName);
//						2-11) 업로드된 파일의 파일이름을 Map에 ("imageFileName", "업로드파일이름")으로 저장된다. 즉, 업로드한 파일이 존재하는 경우 업로드한 파일의 파일이름으로 저장소에 업로드 됨. (게시판 글쓰기 과정 구현)
						articleMap.put(fileItem.getFieldName(), fileName);
						File uploadFile = new File(currentDirPath + "\\temp\\" + fileName);
						fileItem.write(uploadFile);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//		2-12) 일반텍스트 및 업르드된 파일 정보를 가진 articleMap이 반환되어 2-5)의 upload()로 전달됨. (게시판 글쓰기 과정 구현)
//		6-13) 답글에 대한 일반 텍스트 및 파일 정보를 가진 articleMap이 반환되며 호출된 6-11)의 upload()로 전달됨. (답글 쓰기 기능 구현) 
		return articleMap;
	}

}
