package mains;
import java.sql.*;


public class ScoreCalculator {
	final int WOS_number;
	static int weight;
	static int score;
    static double[][] feature;
	
	public ScoreCalculator(int WOS_no) {
		WOS_number = WOS_no;		

    }
	
	double calculateScore(double[] weight, double[] feature){
		
		return 0;
	}
	double updateWeight(double[] weight, double[][] documentSet){
		
		return 0;
	}
	
	double[] loadWeight(){
		double[] weight = new double[10];
		return weight;
	}
	
	double[] saveWeight(){
		double[] weight = new double[10];
		return weight;
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {//
	//		Class.forName("com.mysql.jdbc.Driver");  
			Connection myConn = DriverManager.getConnection("jdbc:mysql://hcdm.cs.virginia.edu","logger","lOGGing");
			
			Statement myStmt = myConn.createStatement();
			
			ResultSet myRS = myStmt.executeQuery("SELECT * FROM LiteratureSearchEngine.WeightVectors");
			
			while(myRS.next()){
				System.out.println("WOS Number: "+myRS.getString("WOS") + "Weight vector values: "+myRS.getString("Weight"));
				
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
