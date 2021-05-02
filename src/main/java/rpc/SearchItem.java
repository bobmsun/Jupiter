package rpc;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet implementation class SearchItem
 */
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// response.getWriter().append("Served at: xxx").append(request.getContextPath());
		
//		 Example 1: return json (json oject)
//		// Set response header (metadata)
//		response.setContentType("application/json");
		
//		PrintWriter writer = response.getWriter();
//		JSONObject obj = new JSONObject();
//		obj.put("username", "abcd");
//		writer.print(obj);                        // write to response body
		
		// Example 2: add a parameter in the url
//		response.setContentType("application/json");
//		PrintWriter writer = response.getWriter();
//		if (request.getParameter("username") != null) {
//			JSONObject obj = new JSONObject();
//			String username = request.getParameter("username");
//			obj.put("username", username);
//			writer.print(obj);
//		}                                         // testing url: http://localhost:8080/jupiter/search?username=vincent
//		                                          //              protocol://hostname:port/resource_path?query
		
		// Example 3: json array
//		response.setContentType("application/json");
//		PrintWriter writer = response.getWriter();
//		
//		JSONArray array = new JSONArray();
//		array.put(new JSONObject().put("username", "abcd"));
//		array.put(new JSONObject().put("username", "1234"));
//		writer.print(array);
		
		// Example 4: json array 2
//		JSONArray array = new JSONArray();
//		array.put(new JSONObject().put("name", "abce").put("address", "san francisoco").put("time", "01/01/2017"));
//		array.put(new JSONObject().put("name", "1234").put("address", "san jose").put("time", "01/02/2017"));
//		RpcHelper.writeJsonArray(response, array);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
