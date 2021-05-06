package db;

public class MySQLDBUtil {
	
	// Parameters we need to connect to database
	
	private static final String INSTANCE = "laiproject-bob.cgndlrnz0qzd.us-east-2.rds.amazonaws.com";
	private static final String PORT_NUM = "3306";
	public static final String DB_NAME = "laiproject";
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "laiproject-bob";
	public static final String URL = "jdbc:mysql://"
			+ INSTANCE + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";

	
}
