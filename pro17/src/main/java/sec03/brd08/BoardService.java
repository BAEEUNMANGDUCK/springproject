package sec03.brd08;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardService {
	BoardDAO boardDAO;

	public BoardService() {
		boardDAO = new BoardDAO();
	}

	public int addArticle(ArticleVO article) {
//		2-15) 2-14)를 통해 호출된 addArticle() 메서드에서 BoardDAO 클래스의 메서드인 insertNewArticle()에 ArticleVO에 담긴 article 정보를 담아 호출 (BoardDAO로 이동) (게시판 글쓰기 과정 구현) 
//		2-25) 2-24)를 통해 반환된 새 글 번호 값이 재차 return을 통해 addArticle() 메서드가 호출된 BoardController클래스의 boardService.addArticle(articleVO); 부분으로 반환됨. (BoardController로 이동)(게시판 글쓰기 과정 구현) 
		return boardDAO.insertNewArticle(article);
	}

	public Map listArticles(Map pagingMap) {
//		7-6) 7-5) 과정을 통해 listArticles() 메서드가 호출되고, 매개변수를 통해 pagingMap이 전달됨. (게시판 페이징 기능 구현)
		Map articlesMap = new HashMap();
//		7-7) BoardDAO 클래스의 boardDAO 객체의 메서드 selectAllArticles()를 호출하며 pagingMap을 인자로 전달 (BoardDAO 클래스로 이동) (게시판 페이징 기능 구현)
//		7-15) 7-14)를 통해 반환된 해당 섹션과 페이지 넘버의 글 정보를 articlesList 객체에 저장. (게시판 페이징 기능 구현)
		List<ArticleVO> articlesList = boardDAO.selectAllArticles(pagingMap);
//		7-16) 전체 글 수를 가져오기 위해 BoardDAO 클래스의 boardDAO 객체의 메서드 selectTotArticles()를 호출함. (BoardDAO 클래스로 이동) (게시판 페이징 기능 구현) 
//		7-20) 7-19)를 통해 반환된 총 글의 개수를 변수 totArticles에 저장함. (게시판 페이징 기능 구현) 
		int totArticles = boardDAO.selectTotArticles();
//		7-21) 각 섹션과 페이지 넘버에 해당하는 글의 정보를 담은 articlesList와, 총 글의 개수를 가진 totArticles를 각각의 키 값으로 articlesMap에 넣고 이 값을
//		호출된 BoardController 클래스의 boardService.listArticles(pagingMap)로 반환함. (게시판 페이징 기능 구현) 
		articlesMap.put("articlesList", articlesList);
		articlesMap.put("totArticles", totArticles);
//		articlesMap.put("totArticles", 170);
		return articlesMap;
	}

	public List<ArticleVO> listArticles() {
//		1-5) 1-4) 과정 BoardController 파일에서의 listArticles() 메서드의 호출. (글목록보기 구현 과정) 
//		1-6) boardDAO의 객체의 selectAllArticles() 메서드 호출 (BoardDAO 클래스로 이동) (글목록보기 구현 과정) 
//		1-13) 1-12)과정을 통해 반환된 articlesList의 값이 List의 articlesList 변수에 저장됨  (글목록보기 구현 과정) 
		List<ArticleVO> articlesList = boardDAO.selectAllArticles();
//		1-14) articlesList의 값이 호출된 boardService.listArticles()로 반환됨 (BoardController로 이동) (글목록보기 구현 과정) 
		return articlesList;
	}

	public ArticleVO viewArticle(int articleNO) {
//		3-6) 3-5)의 과정을 통해 호출된 viewArticle 메서드에 해당하는 글번호인 articleNO가 전달됨. (글 상세 기능 구현)
		ArticleVO article = null;
//		3-7) articleNO에 저장된 글번호를 BoardDAO클래스의 객체인 boardDAO의 메서드 selectArticle()의 인자로 넣어 호출 (BoardDAO 클래스로 이동) (글 상세 기능 구현)
//		3-15) 3-14)를 통해 반환된 상세 글 정보를 article 객체에 저장 (글 상세 기능 구현)
		article = boardDAO.selectArticle(articleNO);
//		3-16) 상세 글 정보가 담긴 article이 viewArticle() 메서드를 호출한 BoardController의 boardService.viewArticle(Integer.parseInt(articleNO)) 로 반환됨 (BoardController 클래스로 이동) (글 상세 기능 구현) 
		return article;
	}

	public void modArticle(ArticleVO article) {
//		4-8) 4-7) 과정을 통해 호출된 modArticle()에 수정할 글 정보가 담긴 article 객체. (글 수정 기능 구현)
//		4-9) BoardDAO 클래스의 객체인 boardDAO의 메서드인 updateArticle()에 수정할 글 정보가 담긴 article 객체를 인자로 전달 (BoardDAO 클래스로 이동) (글 수정 기능 구현)
		boardDAO.updateArticle(article);
	}

	public List<Integer> removeArticle(int articleNO) {
//		5-6) 5-5)를 통해 호출된 removeArticle() 메서드와 함께 전달된 삭제할 글 번호를 담은 articleNO. (글 삭제 기능 구현) 
//		5-7) BoardDAO 클래스의 객체인 boardDAO의 메서드 selectRemovedArticles()를 호출하며 articleNO를 인자로 함께 전달 (BoardDAO 클래스로 이동) (글 삭제 기능 구현) 
//		5-13) 5-12)를 통해 반환된 삭제할 글들의 번호가 담긴 articleNOList가 articleNOList 객체에 저장됨. 
		List<Integer> articleNOList = boardDAO.selectRemovedArticles(articleNO);
//		5-14) BoardDAO 클래스의 객체인 boardDAO의 메서드 deleteArticle()을 호출하며 삭제할 글 번호인 articleNO를 인자로 전달. (BoardDAO 클래스로 이동) (글 삭제 기능 구현)
		boardDAO.deleteArticle(articleNO);
//		5-17) 삭제된 글 번호가 담긴 articleNOList를 메서드가 호출된 BoardController 클래스의 boardService.removeArticle(articleNO)로 반환 (BoardController 클래스로 이동) (글 삭제 기능 구현) 
		return articleNOList;
	}

	public int addReply(ArticleVO article) {
//		6-16) 6-15를 통해 호출된 addReply() 메서드와 함께 전달된 답글정보가 담긴 article 객체 (답글 쓰기 기능 구현) 
//		6-17) BoardDAO 클래스의 boardDAO 객체에서 insertNewArticle 메서드를 호출하고 인자로 article을 전달 (BoardDAO 클래스로 이동) (답글 쓰기 기능 구현) 
//		6-26) 6-25)에서 반환된 articleNO의 값이 addReply()가 호출된 BoardController 클래스의 boardService.addReply(articleVO)와 함께 전달됨. (BoardController 클래스로 이동) (답글 쓰기 기능 구현) 
		return boardDAO.insertNewArticle(article);
	}
}
