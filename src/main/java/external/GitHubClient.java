package external;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;


public class GitHubClient {
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	private static final String DEFAULT_KEYWORD = "developer";
	
	public JSONArray search(double lat, double lon, String keyword) {
		// This method returns the JSON array in the github Job API response body
		
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		
		// Encode keyword for url
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");     // This line needs to be surrounded with try-catch
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String url = String.format(URL_TEMPLATE, keyword, lat, lon);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(url);
		
		ResponseHandler<JSONArray> responseHandler = new ResponseHandler<JSONArray>() {
			
			@Override
			public JSONArray handleResponse(final HttpResponse response) throws IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status != 200) {
					return new JSONArray();
				}
				
				// Get response body
				HttpEntity entity = response.getEntity();
				if (entity == null) {
					return new JSONArray();
				}
				String responseBody = EntityUtils.toString(entity);
				JSONArray array = new JSONArray(responseBody);
				return array;
			}
		};
		
		try {
			JSONArray responseBody = httpclient.execute(httpget, responseHandler);
			return responseBody;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return new JSONArray();
	}

}
