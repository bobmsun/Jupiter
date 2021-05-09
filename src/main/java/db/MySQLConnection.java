package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;


public class MySQLConnection {
	
	private Connection conn;
	
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void setFavoriteItems(String userId, Item item) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		
//		String sql = String.format("INSERT INTO history (user_id, item_id) VALUES (%s, %s)", userId, item.getItemId());
//		try {
//			Statement statement = conn.createStatement();
//			statement.executeUpdate(sql);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
		
		// Maybe insert item to items table
		// Need to insert item before inserting into history table, because of FOREIGN KEY constraints
		saveItem(item);
		
		String sql = "INSERT INTO history (user_id, item_id) VALUES (?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, item.getItemId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public void unSetFavoriteItems(String userId, String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
		
		PreparedStatement statement;
		try {
			statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, itemId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// When unSetFavoriteItems, we choose not to delete item in item table and keywords in keywords table, because of performance.
		// Can run a db clean up script periodically to delete item and keywords. No need to delete item and keywords when unfavoriting
	}
	
	
	// Save an item in items table
	public void saveItem(Item item) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?)";   // IGNORE: will ignore (no opt) if primary key already exists.
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getAddress());
			statement.setString(4, item.getImageUrl());
			statement.setString(5, item.getUrl());
			statement.executeUpdate();
			
			// Also need to insert keywords into keywords table
			sql = "INSERT IGNORE INTO keywords VALUES (?, ?)";
			statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			for (String keyword : item.getKeywords()) {
				statement.setString(2, keyword);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	// Get favorite item (give a user, want to return all the jobs that this user favorited)
	
	// Return a set of userID
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			return new HashSet<>();
		}
		
		Set<String> favoriteItemIds = new HashSet<>();
		String sql = "SELECT item_id FROM history WHERE user_id = ?";
		
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();          // SELECT --> executeQuery(); CREATE / INSERT --> executeUpdate()
			
			// Traverse ResultSet using iterator
			while (rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItemIds.add(itemId);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return favoriteItemIds;
	}
	
	
	// Return a set of items
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		
		Set<Item> favoriteItems = new HashSet<>();
		
		// Get favorite item ids
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);
		
		// Get item based on item id
		for (String itemId : favoriteItemIds) {
			String sql = "SELECT * FROM items WHERE item_id = ?";
			try {
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, itemId);
				ResultSet rs = statement.executeQuery();
				
				while (rs.next()) {         // Tg=his line can also be:   if (rs.next())   since we only have one record
					ItemBuilder builder = new Item.ItemBuilder();
					
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					
					// Get keywords based on item id
					builder.setKeywords(getKeywords(itemId));
					
					favoriteItems.add(builder.build());
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return favoriteItems;
	}
	
	
	public Set<String> getKeywords(String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		Set<String> keywords = new HashSet<>();
		String sql = "SELECT keyword FROM keywords WHERE item_id = ?";
		
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String keyword = rs.getString("keyword");
				keywords.add(keyword);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keywords;
	}
	
	
	// Below are 3 APIs related to log in, log out, sigh up
	
	public String getFullname(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return "";
		}
		
		String name = "";
		String sql = "SELECT first_name, last_name FROM users WHERE user_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				name = rs.getString("first_name") + " " + rs.getString("last_name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return name;
	}
	
	
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		
		String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			
			// Below 3 lines can be :   return rs.next();
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {	
			e.printStackTrace();
		}
		return false;
	}
	
	
	public boolean addUser(String userId, String password, String firstname, String lastname) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		
		String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			statement.setString(3, firstname);
			statement.setString(4, lastname);
			
			return statement.executeUpdate() == 1;      // we expect one row is affected
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
