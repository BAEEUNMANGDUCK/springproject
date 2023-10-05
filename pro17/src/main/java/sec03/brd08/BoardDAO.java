package sec03.brd08;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class BoardDAO {

	private DataSource dataFactory;
	Connection conn;
	PreparedStatement pstmt;

	public BoardDAO() {
		try {
			Context ctx = new InitialContext();
			Context envContext = (Context) ctx.lookup("java:/comp/env");
			dataFactory = (DataSource) envContext.lookup("jdbc/oracle");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<ArticleVO> selectAllArticles(Map<String, Integer> pagingMap) {
//		7-8) 7-7) 과정을 통해 호출된 selectAllArticles() 메서드와 section과 pageNum 값을 가진 pagingMap이 매개변수를 통해 전달됨. (게시판 페이징 기능 구현)
		List<ArticleVO> articlesList = new ArrayList();
//		7-9) pagingMap에서 section과 pageNum 값을 가져와 각각의 변수에 저장함. (게시판 페이징 기능 구현)
		int section = pagingMap.get("section");
		int pageNum = pagingMap.get("pageNum");
		try {
			conn = dataFactory.getConnection();
//		7-10) 페이징 기능 구현을 위한 쿼리문 작성. 이때 서브 쿼리문과 Oracle에서 제공하는 가상 칼럼 ROWNUM 사용. ROWNUM은 select문으로 조회한 레코드 목록에 오라클 자체에서 
//			  순서를 부여하여 레코드 번호를 순서대로 할당함. 쿼리문은 먼저 기존의 계층형 구조로 글 목록을 조회한 후, 그 결과에 대하여 다시 select문을 실행해 recNum(ROWNUM)을
//			  표시되도록 함. 그리고 각각 저장됨 section과 pageNum을 where문에 넣고 해당하는 레코드만 최종적으로 조회함. (게시판 페이징 기능 구현)
			String query = "SELECT * FROM ( " + "select ROWNUM as recNum,"
			+ "LVL," +"articleNO,"
					+ "parentNO," + "title," + "id," + "writeDate"
			+ " from (select LEVEL as LVL, "
			+ "articleNO," + "parentNO," + "title," + "id,"
			+ "writeDate" + " from t_board"
			+ " START WITH parentNO=0" + " CONNECT BY PRIOR articleNO = parentNO"
			+ " ORDER SIBLINGS BY articleNO DESC)" + ") "
					+ " where recNum between(?-1)*100+(?-1)*10+1 and (?-1)*100+?*10";
			System.out.println(query);

			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, section);
			pstmt.setInt(2, pageNum);
			pstmt.setInt(3, section);
			pstmt.setInt(4, pageNum);
//		7-11) 해당 쿼리문을 실행하여 섹션과 페이지번호에 따라 반환된 글 정보를 ResultSet의 객체 rs에 저장. (게시판 페이징 기능 구현)
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
//		7-12) rs 객체에 담긴 글 정보를 가져와 각각의 변수에 대입하고, ArticleVO 클래스의 article 객체의 setter에 재차 대입. (게시판 페이징 기능 구현)
				int level = rs.getInt("lvl");
				int articleNO = rs.getInt("articleNO");
				int parentNO = rs.getInt("parentNO");
				String title = rs.getString("title");
				String id = rs.getString("id");
				Date writeDate = rs.getDate("writeDate");
				ArticleVO article = new ArticleVO();
				article.setLevel(level);
				article.setArticleNO(articleNO);
				article.setParentNO(parentNO);
				article.setTitle(title);
				article.setId(id);
				article.setWriteDate(writeDate);
//		7-13) 해당 섹션과 페이지 넘버의 글 정보가 담긴 article 객체를 인자로하여 articlesList에 넣고 해당 과정을 while문을 이용하여 반복함. (게시판 페이징 기능 구현)
				articlesList.add(article);
			}
			rs.close();
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		7-14) 해당 섹션과 페이지 넘버의 글 정보가 담긴 articlesList를 메서드가 호출된 BoardService 클래스의 boardDAO.selectAllArticles(pagingMap)로 반환함. (BoardService 클래스로 이동) (게시판 페이징 기능 구현)
		return articlesList;
	}

	public List selectAllArticles() {
//		1-7) 1-6) BoardService에서 selectAllArticles() 메서드 호출로 현재의 위치로 이동 (글 목록보기 구현 과정) 
		List articlesList = new ArrayList();
		try {
			conn = dataFactory.getConnection();
//			1-8) 오라클의 계층형 SQL문 실행하는 쿼리를 아래에 작성  (글 목록보기 구현 과정) 
			String query = "SELECT LEVEL,articleNO, parentNO, title, content, id, writeDate" + " from t_board"
					+ " START WITH parentNO=0" + " CONNECT BY PRIOR articleNO=parentNO"
					+ " ORDER SIBLINGS BY articleNO DESC";
			System.out.println(query);
//			1-9) 위의 작성된 query를 prepareStatement() 메서드의 인자로 넣음 (글 목록보기 구현 과정)
			pstmt = conn.prepareStatement(query);
//			1-10) executeQuery() 메서드 호출을 통해 데이터베이스에 query가 실행되고, 여기서 반환되는 값이 ResultSet의 클래스의 객체인 rs에 저장됨. (글 목록보기 구현 과정) 
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
//				1-11) rs에 저장된 값이 getInt() 또는 getString() 메서드를 통해 추출되고, ArticleVO 클래스의 article 객체의 속성으로 저장되고 , 이 값이 articlesList에 들어가는 과정이 while문에 의해 반복됨(글 목록보기 구현 과정) 
				int level = rs.getInt("level");
				int articleNO = rs.getInt("articleNO");
				int parentNO = rs.getInt("parentNO");
				String title = rs.getString("title");
				String content = rs.getString(parentNO);
				String id = rs.getString("id");
				Date writeDate = rs.getDate("writeDate");
				ArticleVO article = new ArticleVO();
				article.setLevel(level);
				article.setArticleNO(articleNO);
				article.setParentNO(parentNO);
				article.setTitle(title);
				article.setContent(content);
				article.setId(id);
				article.setWriteDate(writeDate);
				articlesList.add(article);
			}
			rs.close();
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		1-12) 메서드가 호출된 BoardService의 boardDAO.selectAllArticles()로 글목록을 가진 articlesList 값을 반환함. (BoardService 클래스로 다시 이동) (글 목록보기 구현 과정) 
		return articlesList;
	}

	public ArticleVO selectArticle(int articleNO) {
//		3-8) 3-7)을 통해 호출된 selectArticle() 메서드와 함께 전달된 해당하는 글 번호인 articleNO. (글 상세 기능 구현)
//		3-9) 해당하는 글 정보를 담을 ArticleVO 클래스의 article 객체를 생성. (글 상세 기능 구현)
		ArticleVO article = new ArticleVO();
		try {
			conn = dataFactory.getConnection();
//		3-10) 데이터베이스에 전달할 쿼리문 작성 여기서 where문을 통해 해당하는 글 번호의 정보만을 선택해서 가져올 수 있게 됨. (글 상세 기능 구현)
			String query = "select articleNO, parentNO, title, content,  NVL(imageFileName, 'null') as imageFileName, id, writeDate"
					+ " from t_board"
					+ " where articleNO=?";
			System.out.println(query);
			pstmt = conn.prepareStatement(query);
//		3-11) 매개변수로 받은 articleNO의 값을 쿼리문에 넣고 쿼리를 실행한 후, 해당하는 글 정보를 ResultSet 클래스의 객체인 rs에 저장. (글 상세 기능 구현)
			pstmt.setInt(1, articleNO);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
//		3-12) 글정보가 있는 rs객체에서 getter 함수를 사용하여 각각의 정보를 변수에 저장 (글 상세 기능 구현)
			int _articleNO = rs.getInt("articleNO");
			int parentNO = rs.getInt("parentNO");
			String title = rs.getString("title");
			String content = rs.getString("content");
			String imageFileName = URLEncoder.encode(rs.getString("imageFileName"), "UTF-8");
			if (imageFileName.equals("null")) {
				imageFileName = null;
			}
				
			String id = rs.getString("id");
			Date writeDate = rs.getDate("writeDate");
//		3-13) 위의 저장된 변수들을 article 객체의 setter함수를 이용해 저장 (글 상세 기능 구현)
			article.setArticleNO(_articleNO);
			article.setParentNO(parentNO);
			article.setTitle(title);
			article.setContent(content);
			article.setImageFileName(imageFileName);
			article.setId(id);
			article.setWriteDate(writeDate);
			rs.close();
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		3-14) article객체에 저장된 상세 글 정보를 BoardService 클래스의 viewArticle() 메서드의 boardDAO.selectArticle(articleNO)로 반환 (BoardServie 클래스로 이동) (글 상세 기능 구현) 
		return article;
	}
	
	


	private int getNewArticleNO() {
//		2-18) insertNewArticle()에서 호출됨. (게시판 글쓰기 과정 구현)

//		6-20) 6-19)의 insertNewArticle()에서 호출된 getNewArticleNO() 메서드 (답글 쓰기 기능 구현)
		try {
			conn = dataFactory.getConnection();
//		2-19) 데이터베이스에 전달할 쿼리를 작성, 글 목록에서 가장 큰 글번호를 가져오기 위한 SQL문. 쿼리를 실행하면 ResultSet의 객체인 rs에 해당 정보가 저장됨 (게시판 글쓰기 과정 구현)

//		6-21) 게시판 글쓰기 과정과 동일하게 데이터베이스에 전달할 쿼리를 작성. 글 목록에서 가장 큰 글 번호를 가져오기 위한 SQL문으로 쿼리를 실행 후, ResultSet의 객체인 rs에 답글의 정보가 저장됨. (답글 쓰기 기능 구현)  	
			String query = "SELECT max(articleNO) from t_board";
			System.out.println(query);
			pstmt = conn.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
//		2-20) 가장 큰 글번호에 1을 더하여 반환 (호출된 insertNewArticle()로 이동) (게시판 글쓰기 과정 구현)

//		6-22) 가장 큰 글번호에서 1을 더하여 반환 (호출된 insertNewArticle()로 이동) (답글 쓰기 기능 구현) 
			if (rs.next()) {
				return (rs.getInt(1) + 1);
			}
			rs.close();
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int insertNewArticle(ArticleVO article) {
//		2-16) 2-15)를 통해 호출된 insertNewArticle() 메서드와 글정보가 담긴 article이 전달됨 (게시판 글쓰기 과정 구현)
//		2-17) 새 글에 대한 글번호를 지정하기 위해 BoardDAO의 메서드인 getNewArticleNO()를 호출함 (게시판 글쓰기 과정 구현)
//		2-21) 2-20)을 통해 반환된 글번호를 정수형 변수 articleNO에 저장 (게시판 글쓰기 과정 구현)

//		6-18) 6-17) 과정을 통해 호출된 insertNewArticle() 메서드와 답글 정보가 담긴 article이 전달됨. (답글 쓰기 기능 구현) 
//		6-19) BoardDAO의 메서드인 getNewArticleNO()를 호출. (답글 쓰기 기능 구현) 
//		6-23) 6-22)에서 반환된 글 번호를 articleNO에 저장. (답글 쓰기 기능 구현) 
		int articleNO = getNewArticleNO();
		try {
			conn = dataFactory.getConnection();
//		2-22) article에 저장된 글 정보를 getter 함수를 이용해서 가져오고 각각의 자료형에 맞는 변수에 저장 (게시판 글쓰기 과정 구현)

//		6-24) 답글 정보를 담은 ArticleVO 클래스의 articleVO객체에서 getter()를 이용하여 해당하는 값을 가져온 후, 각각의 변수에 저장.
			int parentNO = article.getParentNO();
			String title = article.getTitle();
			String content = article.getContent();
			String id = article.getId();
			String imageFileName = article.getImageFileName();
//		2-23) SQL의 insert문을 통해 데이터베이스에 정보를 넣을 준비를 함, 그리고 위의 변수에 저장된 값을 이용하여 Values(?,?,?,?,?,?)로 정보가 전달하여 쿼리를 실행 (게시판 글쓰기 과정 구현)

//		6-25) SQL의 insert문을 통해 데이터베이스에 정보를 넣을 준비를 함. 그리고 위의 변수에 저장된 값을 이용하여 Values(?,?,?,?,?,?)로 정보를 전달하여 쿼리를 실행 (답글 쓰기 기능 구현) 
			String query = "INSERT INTO t_board (articleNO, parentNO, title, content, imageFileName, id)"
					+ " Values (?, ?, ?, ?, ?, ?)";
			System.out.println(query);
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, articleNO);
			pstmt.setInt(2, parentNO);
			pstmt.setString(3, title);
			pstmt.setString(4, content);
			pstmt.setString(5, imageFileName);
			pstmt.setString(6, id);
			pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		2-24) 위에 저장된 새 글의 글번호를 반환하고 메서드가 호출된 BoardService의 addArticle() 메서드로 이동 (BoardService 클래스로 이동) (게시판 글쓰기 과정 구현)

//		6-26) articleNO를 반환하며 insertNewArticle()가 호출된 BoardService의 addReply() 메서드로 이동 (BoardService 클래스로 이동) (답글 쓰기 기능 구현) 
		return articleNO;
	}

	public void updateArticle(ArticleVO article) {
//		4-10) 4-9)의 과정에서 호출된 updateArticle() 메서드와 함께 수정할 글 정보가 포함된 article 객체가 전달됨 (글 수정 기능 구현)
//		4-11) article 객체에 담긴 정보가 각각의 변수에 저장됨. (글 수정 기능 구현)
		int articleNO = article.getArticleNO();
		String title = article.getTitle();
		String content = article.getContent();
		String imageFileName = article.getImageFileName();
		try {
			conn = dataFactory.getConnection();
//		4-12) 데이터베이스에서 해당하는 글 번호의 글 정보를 수정하기 위한 쿼리 작성. (글 수정 기능 구현)
			String query = "update t_board set title=?,content=?";
			if (imageFileName != null && imageFileName.length() != 0) {
				query += ",imageFileName=?";
			}
			query += " where articleNO = ?";
//		4-13) 각각의 변수에 저장된 글 정보를 쿼리에 대입하고 쿼리를 실행함. 그리고 updadteArticle() 메서드가 호출된 BoardService 클래스를 거쳐, modArticle() 메서드가 호출된 BoardController 클래스로 이동(BoardController로 이동) (글 수정 기능 구현)
			System.out.println(query);
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, title);
			pstmt.setString(2, content);
			if (imageFileName != null && imageFileName.length() != 0) {
				pstmt.setString(3, imageFileName);
				pstmt.setInt(4, articleNO);
			} else {
				pstmt.setInt(3, articleNO);
			}
			pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteArticle(int articleNO) {
//		5-15) 5-14)를 통해 호출된 deleteArticle() 메서드와 인자로 전달된 삭제할 글 번호를 담은 articleNO (글 삭제 기능 구현)
		try {
			conn = dataFactory.getConnection();
//		5-16) 오라클의 계층형 SQL문을 이용하여 삭제 글과 관련된 자식 글을 모두 삭제하기 위한 쿼리문. articleNO는 쿼리문에 전달되고 쿼리문이 실행되어 삭제됨. 그리고 다시 메서드가 호출된 곳으로 돌아감 (BoardService로 이동)(글 삭제 기능 구현)
			String query = "DELETE FROM t_board ";
			query += " WHERE articleNO in (";
			query += "  SELECT articleNO FROM t_board ";
			query += " START WITH articleNO = ?";
			query += " CONNECT BY PRIOR articleNO = parentNO )";
			System.out.println(query);
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, articleNO);
			pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<Integer> selectRemovedArticles(int articleNO) {
//		5-8) 5-7)을 통해 호출된 selectRemovedArticles() 메서드와 매개변수를 통해 전달된 삭제할 글 번호인 articleNO (글 삭제 기능 구현) 
//		5-9) 삭제할 글과 그 자식글의 리스트를 담을 articleNOList 객체 생성 (글 삭제 기능 구현) 
		List<Integer> articleNOList = new ArrayList<Integer>();
		try {
			conn = dataFactory.getConnection();
//		5-10) 삭제할 글들을 조회하기 위한 쿼리문 작성 (글 삭제 기능 구현). 그리고 이 쿼리 문을 실행하여 ResultSet의 객체인 rs에 해당하는 정보가 저장됨. (글 삭제 기능 구현) 
			String query = "SELECT articleNO FROM t_board ";
			query += " START WITH articleNO = ?";
			query += " CONNECT BY PRIOR articleNO = parentNO";
			System.out.println(query);
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, articleNO);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
//		5-11) 삭제할 글 번호와 그 자식글의 글 번호가 articleNO에 저장되고, 그 번호가 다시 articleNOList에 저장됨 (글 삭제 기능 구현) 
				articleNO = rs.getInt("articleNO");
				articleNOList.add(articleNO);
			}
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		5-12) 삭제할 글 번호를 담긴 리스트가 호출된 BoardService 클래스의 boardDAO.selectRemovedArticles(articleNO)로 반환됨. (BoardService로 이동)(글 삭제 기능 구현) 
		return articleNOList;
	}

	public int selectTotArticles() {
//		7-17) 7-16)의 과정을 통해 호출된 selectTotArticles() 메서드. (게시판 페이징 기능 구현)
		try {
			conn = dataFactory.getConnection();
//		7-18) t_board에 저장된 전체 글의 개수를 세는 쿼리문을 작성하고 실행하여 ResultSet 클래스의 객체 rs에 저장. (게시판 페이징 기능 구현) 
			String query = "select count(articleNO) from t_board ";
			System.out.println(query);
			pstmt = conn.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
//		7-19) 그중 가장 큰 글 번호(그것이 글의 개수이므로)를 호출된 BoardService의 boardDAO.selectTotArticles()로 반환함.(BoardService 클래스로 이동)(게시판 페이징 기능 구현)
				return (rs.getInt(1));
			rs.close();
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}


}
