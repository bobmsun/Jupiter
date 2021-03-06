package external;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.monkeylearn.ExtraParam;
import com.monkeylearn.MonkeyLearn;
import com.monkeylearn.MonkeyLearnException;
import com.monkeylearn.MonkeyLearnResponse;

public class MonkeyLearnClient {

	private static final String API_KEY = "5727b1df596b42f17e83f30a5c90b27baf5a1c29";

	
	public static void main(String[] args) {

		String[] textList = {
				"Elon Musk has shared a photo of the spacesuit designed by SpaceX. This is the second image "
				+ "shared of the new design and the first to feature the spacesuit’s full-body look.", };
		List<List<String>> words = extractKeywords(textList);
		for (List<String> ws : words) {
			for (String w : ws) {
				System.out.println(w);
			}
			System.out.println();
		}
	}

	
	public static List<List<String>> extractKeywords(String[] text) {
		if (text == null || text.length == 0) {
			return new ArrayList<>();
		}
		
		MonkeyLearn ml = new MonkeyLearn(API_KEY);

		ExtraParam[] extraParams = { new ExtraParam("max_keywords", "3") };            // return top 3 keywords
		MonkeyLearnResponse response;

		try {
			response = ml.extractors.extract("ex_YCya9nrn", text, extraParams);        // first parameter is model ID
			JSONArray resultArray = response.arrayResult;
			// System.out.println(resultArray);                                        // print out to see how this JSONArray looks like
			return getKeywords(resultArray);
		} catch (MonkeyLearnException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}


	private static List<List<String>> getKeywords(JSONArray mlResultArray) {
		List<List<String>> topKeywords = new ArrayList<>();

		for (int i = 0; i < mlResultArray.size(); i++) {
			List<String> keywords = new ArrayList<>();
			JSONArray keywordsArray = (JSONArray) mlResultArray.get(i);

			for (int j = 0; j < keywordsArray.size(); j++) {
				JSONObject keywordObject = (JSONObject) keywordsArray.get(j);
				String keyword = (String) keywordObject.get("keyword");

				keywords.add(keyword);
			}
			topKeywords.add(keywords);
		}
		return topKeywords;
	}

}
