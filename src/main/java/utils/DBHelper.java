package utils;

import java.sql.*;

public class DBHelper {

    private static final String DB_URL = "jdbc:sqlite:sample.db";

    public static String queryDatabase(String keyword) {
        String result = "No result found for: " + keyword;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT info FROM knowledge WHERE keyword = ?")) {

            stmt.setString(1, keyword);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                result = rs.getString("info");
            }

        } catch (SQLException e) {
            result = "Database error: " + e.getMessage();
        }

        return result;
    }
}
