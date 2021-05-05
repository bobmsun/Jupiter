package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class Item {
	
	private String itemId;
	private String name;
	private String address;
	private Set<String> keywords;
	private String imageUrl;
	private String url;
	
	// This class uses Builder pattern
	private Item(ItemBuilder builder) {
		this.itemId = builder.itemId;
		this.name = builder.name;
		this.address = builder.address;
		this.imageUrl = builder.imageUrl;
		this.url = builder.url;
		this.keywords = builder.keywords;
	}
	
	// We don't have public setter in this Item class, because we want immutability.
	
	public String getItemId() {
		return itemId;
	}


	public String getName() {
		return name;
	}


	public String getAddress() {
		return address;
	}


	public Set<String> getKeywords() {
		return keywords;
	}


	public String getImageUrl() {
		return imageUrl;
	}


	public String getUrl() {
		return url;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("item_id", itemId);
		obj.put("name", name);
		obj.put("address", address);
		obj.put("keywords", new JSONArray(keywords));
		obj.put("image_url", imageUrl);
		obj.put("url", url);
		return obj;		
	}
	
	// Builder class must be inner class because
	public static class ItemBuilder {
		private String itemId;
		private String name;
		private String address;
		private Set<String> keywords;
		private String imageUrl;
		private String url;
		
		public void setItemId(String itemId) {
			this.itemId = itemId;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public void setKeywords(Set<String> keywords) {
			this.keywords = keywords;
		}
		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		
		public Item build() {
			return new Item(this);
		}
	}
	
}

// ItemBuilder builder = new ItemBuilder();
// builder.setItemId("abcd");
// builder.setName("vincent");
// Item item = builder.build();
