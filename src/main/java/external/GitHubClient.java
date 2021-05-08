package external;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;


public class GitHubClient {
	
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	private static final String DEFAULT_KEYWORD = "developer";
	
	
	// Search method is the method that our servlet calls
	public List<Item> search(double lat, double lon, String keyword) {
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
		
		ResponseHandler<List<Item>> responseHandler = new ResponseHandler<List<Item>>() {
			
			@Override
			public List<Item> handleResponse(final HttpResponse response) throws IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status != 200) {
					return new ArrayList<>();
				}
				
				// Get response body
				HttpEntity entity = response.getEntity();
				if (entity == null) {
					return new ArrayList<>();
				}
				String responseBody = EntityUtils.toString(entity);
				JSONArray array = new JSONArray(responseBody);
				return getItemList(array);
			}
		};
		
		try {
			List<Item> responseBody = httpclient.execute(httpget, responseHandler);
			return responseBody;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return new ArrayList<>();
	}
	
	
	// Convert JSONArray to a list of Item object
	private List<Item> getItemList(JSONArray array) {
		List<Item> itemList = new ArrayList<>();
		List<String> descriptionList = new ArrayList<>();
		
		for (int i = 0; i < array.length(); i++) {
			// We need to extract keywords from description since GitHub API doesn't return keywords.
			String description = getStringFieldOrEmpty(array.getJSONObject(i), "description");
			
			if (description.contentEquals("") || description.contentEquals("\n")) {
				descriptionList.add(getStringFieldOrEmpty(array.getJSONObject(i), "titile"));
			} else {
				descriptionList.add(description);
			}
		}
		
		// We need to get keywords from multiple text in one request since MonkeyLearn API has limitations on request per minute.
		String[] descriptionArray = descriptionList.toArray(new String[descriptionList.size()]);
		List<List<String>> keywords = MonkeyLearnClient.extractKeywords(descriptionArray);
		
		
		for (int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);
			
			// object -> item
			ItemBuilder builder = new ItemBuilder();
			
			builder.setItemId(getStringFieldOrEmpty(object, "id"));
			builder.setName(getStringFieldOrEmpty(object, "title"));
			builder.setAddress(getStringFieldOrEmpty(object, "location"));
			builder.setUrl(getStringFieldOrEmpty(object, "url"));
			builder.setImageUrl(getStringFieldOrEmpty(object, "company_logo"));
			
			builder.setKeywords(new HashSet<String>(keywords.get(i)));
			
			Item item = builder.build();
			
			// add item to item list
			itemList.add(item);
			
		}
		return itemList;
	}
	
	
	// If the key in JSONObject does not exist, the get will throw exception
	private String getStringFieldOrEmpty(JSONObject obj, String field) {
		// If the key does not exist in the JSONObject, the isNull will return false; otherwise return true.
		return obj.isNull(field) ? "" : obj.getString(field);
	}

}
