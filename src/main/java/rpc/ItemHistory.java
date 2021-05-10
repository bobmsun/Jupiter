package rpc;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import db.MySQLConnection;
import entity.Item;

/**
 * Servlet implementation class ItemHistory
 */
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Add session validation to protect my service
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		
		String userId = request.getParameter("user_id");      // get parameter from url --> getParameter() function
		
		MySQLConnection connection = new MySQLConnection();     // Create a MySQL database connection
		Set<Item> favoriteItems = connection.getFavoriteItems(userId);
		connection.close();
		
		JSONArray array = new JSONArray();
		for (Item item : favoriteItems) {
			JSONObject object = item.toJSONObject();
			object.put("favorite", true);             // This is for front end display
			array.put(object);
		}
		RpcHelper.writeJsonArray(response, array);
	}

	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		
		JSONObject input = new JSONObject(IOUtils.toString(request.getReader()));    // Get body of the request
		String userId = input.getString("user_id");
		JSONObject itemJsonObject = input.getJSONObject("favorite");
		Item item = RpcHelper.parseFavoriteItem(itemJsonObject);
		
		// Connect to DB
		MySQLConnection connection = new MySQLConnection();
		connection.setFavoriteItems(userId, item);
		connection.close();
		
		RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));    // This line is for debug purposes
	}

	
	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		
		JSONObject input = new JSONObject(IOUtils.toString(request.getReader()));    // Get body of the request
		String userId = input.getString("user_id");
		JSONObject itemJsonObject = input.getJSONObject("favorite");
		Item item = RpcHelper.parseFavoriteItem(itemJsonObject);
		
		// Connect to DB
		MySQLConnection connection = new MySQLConnection();
		connection.unSetFavoriteItems(userId, item.getItemId());
		connection.close();
		
		RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));       // This line is for debug purposes
	}

}
