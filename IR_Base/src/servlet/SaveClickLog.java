package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.sql.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mains.ScoreCalculator;

/**
 * Added by sak2km 4/27/17
 * Servlet implementation class SaveUserLog
 */
@WebServlet("/SaveClickLog")
public class SaveClickLog extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SaveClickLog() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String userId = request.getParameter("userId");
		String clickeded_WOS = request.getParameter("clicked_WOS");
		String list_positon = request.getParameter("position");
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
		
		boolean saved = insertClickLog(userId,sessionId,time,search_keyword,clickeded_WOS,list_positon,luceneIndex);
		if(saved){
			response.getWriter().append("Click data saved under session Id: "+ sessionId).append(request.getContextPath());
			
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	boolean insertClickLog(String userId,String sessionId,String time,String search_keyword,String clickeded_WOS, String list_positon, String luceneIndex){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection myConn = DriverManager.getConnection("jdbc:mysql://hcdm.cs.virginia.edu","logger","lOGGing");			
			Statement myStmt = myConn.createStatement();
			String sqlQuery = "INSERT INTO LiteratureSearchEngine.click_logs values(null,'"+userId+"','"+sessionId+"',now(),'"+search_keyword+"','"+clickeded_WOS+"','"+list_positon+"','"+luceneIndex+"',0);";//2nd NULL for IP
			int success = myStmt.executeUpdate(sqlQuery);
			if(success==1){
				System.out.println("Click log added under sessionId = "+sessionId);				
			}	
			
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
/*	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String displayed_docs = request.getParameter("displayed_docs");
		String clicked_docs = request.getParameter("clicked_docs");
		String search_keyword = request.getParameter("search_keyword");
		
		String[] disp_StringArray = displayed_docs.split(",");
		String[] clicked_docArray = clicked_docs.split(",");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection myConn = DriverManager.getConnection("jdbc:mysql://hcdm.cs.virginia.edu","logger","lOGGing");			
			Statement myStmt = myConn.createStatement();
			String sqlQuery = "INSERT INTO LiteratureSearchEngine.displayed_logs values(null,null,NOW(),'"+displayed_docs+"','"+clicked_docs+"','"+search_keyword+"');";//2nd NULL for IP
			int success = myStmt.executeUpdate(sqlQuery);
			if(success==1){
				System.out.println("User log added successfully");				
			}	
			
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String [][] rankedPair = ScoreCalculator.generatePairs(disp_StringArray,clicked_docArray);
		String toString="";
		for(int i=0;i<rankedPair.length;i++){
			for(int j=0;j<2;j++){
				toString=toString+rankedPair[i][j].toString()+" ";
			}
			toString=toString+"<br> ";
		}
		response.getWriter().append("Pairs: <br>").append(toString);

	}*/

}
