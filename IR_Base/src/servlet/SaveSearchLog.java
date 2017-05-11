package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mains.ScoreCalculator;

/**
 * Servlet implementation class SaveSearchLog
 */
@WebServlet("/SaveSearchLog")
public class SaveSearchLog extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SaveSearchLog() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String userId = request.getParameter("userId");
		String displayed_docs = request.getParameter("displayed_docs");
		String search_keyword = request.getParameter("search_keyword");
		String time = request.getParameter("time");
		String luceneIndex = request.getParameter("luceneIndex");
		String sessionId="";
		try {
			sessionId = ScoreCalculator.getSessionId(userId);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean saved = insertSearchLog(userId,sessionId,time,search_keyword,displayed_docs,luceneIndex);
		if(saved){
			response.getWriter().append("Search data saved under session Id: "+ sessionId).append(request.getContextPath());
			
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	boolean insertSearchLog(String userId,String sessionId,String time,String search_keyword,String displayed_docs,String luceneIndex){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection myConn = DriverManager.getConnection("jdbc:mysql://hcdm.cs.virginia.edu","logger","lOGGing");			
			Statement myStmt = myConn.createStatement();
			String sqlQuery = "INSERT INTO LiteratureSearchEngine.search_logs values(null,'"+userId+"','"+sessionId+"',now(),'"+displayed_docs+"','"+search_keyword+"','"+luceneIndex+"',0);";//2nd NULL for IP
			int success = myStmt.executeUpdate(sqlQuery);
			if(success==1){
				System.out.println("Search log added under SessionId = "+sessionId);				
			}	
			
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

}
