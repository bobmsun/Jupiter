package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import db.MySQLConnection;

/**
 * Servlet implementation class Login
 */
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// The method will only check the existence of the session id and verify whether the corresponding session is still alive
		
		HttpSession session = request.getSession(false);     // false: make sure there is no session created; true: if no session, will create one
		JSONObject  obj = new JSONObject();
		
		if (session != null) {
			MySQLConnection connection = new MySQLConnection();
			String userId = session.getAttribute("user_id").toString();
			obj.put("status", "OK").put("user_id", userId).put("name", connection.getFullname(userId));
			connection.close();
		} else {
			obj.put("status", "Invalid Session");
			response.setStatus(403);
		}
		RpcHelper.writeJsonObject(response, obj);
	}

	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// This method will check the input credentials and create a session for a valid user.

		JSONObject input = new JSONObject(IOUtils.toString(request.getReader()));
		
		String userId = input.getString("user_id");
		String password = input.getString("password");
		
		MySQLConnection connection = new MySQLConnection();
		JSONObject obj = new JSONObject();
		if (connection.verifyLogin(userId, password)) {
			HttpSession session = request.getSession();
			session.setAttribute("user_id", userId);
			session.setMaxInactiveInterval(600);
			obj.put("status", "OK").put("user_id", userId).put("name", connection.getFullname(userId));
		} else {
			obj.put("status", "User Doesn't Exist");
			response.setStatus(401);
		}
		connection.close();
		RpcHelper.writeJsonObject(response, obj);
	}

}
