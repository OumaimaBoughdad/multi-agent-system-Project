import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.sql.*;

public class RessourceInterneAgent extends Agent {
    private Connection conn;

    protected void setup() {
        System.out.println(getLocalName() + " started.");
        setupDatabase();

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String query = msg.getContent();
                    String result = searchData(query);
                    System.out.println(getLocalName() + ": DB result:\n" + result);
                } else {
                    block();
                }
            }
        });
    }

    private void setupDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE data (id INT PRIMARY KEY, info VARCHAR(255))");
            stmt.execute("INSERT INTO data VALUES (1, 'Internal info about AI'), (2, 'Internal data about databases')");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String searchData(String keyword) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT info FROM data WHERE LOWER(info) LIKE ?");
            stmt.setString(1, "%" + keyword.toLowerCase() + "%");
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(rs.getString("info")).append("\n");
            }
            return sb.length() > 0 ? sb.toString() : "No internal data found.";
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }
}
