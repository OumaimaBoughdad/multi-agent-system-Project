package utils;

import java.net.*;
import java.io.*;
import org.json.*;

public class HttpHelper {

    public static String searchExternalSource(String source, String query) {
        try {
            switch (source.toLowerCase()) {
                case "wikipedia":
                    return searchWikipedia(query);
                case "duckduckgo":
                    return searchDuckDuckGo(query);
                default:
                    return "Unknown source: " + source;
            }
        } catch (Exception e) {
            return "Error fetching from " + source + ": " + e.getMessage();
        }
    }

    public static String searchWikipedia(String query) throws Exception {
        // Wikipedia expects underscores instead of spaces
        String formattedQuery = query.trim().replace(" ", "_");
        String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + URLEncoder.encode(formattedQuery, "UTF-8");

        String response = sendGet(url);

        JSONObject json = new JSONObject(response);
        return json.optString("extract", "No summary found.");
    }


    public static String searchDuckDuckGo(String query) throws Exception {
        String url = "https://api.duckduckgo.com/?q=" + URLEncoder.encode(query, "UTF-8") + "&format=json";
        String response = sendGet(url);
        JSONObject json = new JSONObject(response);

        String result = json.optString("AbstractText");
        if (result == null || result.isEmpty()) {
            JSONArray relatedTopics = json.optJSONArray("RelatedTopics");
            if (relatedTopics != null && relatedTopics.length() > 0) {
                JSONObject topic = relatedTopics.getJSONObject(0);
                result = topic.optString("Text", "No result found.");
            }
        }
        return result != null && !result.isEmpty() ? result : "No result found.";
    }


    public static String sendGet(String url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("GET");

        // 👇 THIS LINE IS CRUCIAL
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        int status = con.getResponseCode();

        BufferedReader in;
        if (status >= 200 && status < 300) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            throw new IOException("HTTP error code: " + status);
        }

        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            content.append(line);
        }

        in.close();
        con.disconnect();

        return content.toString();
    }

}

