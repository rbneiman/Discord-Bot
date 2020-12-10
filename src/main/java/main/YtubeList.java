package main;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class YtubeList {

    private static final long NUMBER_OF_VIDEOS_RETURNED = 1;

    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private static YouTube youtube;
    private static String apiKey;
    
    /**
     * Initialize a YouTube object to search for videos on YouTube. Then
     * display the name and thumbnail image of each video in the result set.
     */
    public static void setup() {
        // Read the developer key from the properties file.

        try {
            // This object is used to make YouTube Data API requests. The last
            // argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override
            // the interface and provide a no-op function.
        	final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        	JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
            youtube = new YouTube.Builder(httpTransport, JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("search test").build();
            
            apiKey = ConfigStorage.googleApiKey;

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static String doSearch(String word) {
    	// Define the API request for retrieving search results.
        YouTube.Search.List search;
		try {
			search = youtube.search().list("id,snippet");
		} catch (IOException e) {
			System.err.println("Youtube io error");
			return null;
		}

        // Set your developer key from the {{ Google Cloud Console }} for
        // non-authenticated requests. See:
        // {{ https://cloud.google.com/console }}
        search.setKey(apiKey);
        search.setQ(word);

        // Restrict the search results to only include videos. See:
        // https://developers.google.com/youtube/v3/docs/search/list#type
        search.setType("video");

        // To increase efficiency, only retrieve the fields that the
        // application uses.
        search.setFields("items(id/videoId)");
        search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
        SearchListResponse searchResponse;
		try {
			searchResponse = search.execute();
		} catch (IOException e) {
			System.err.println("Youtube io error 2");
			return null;
		}
        
        List<SearchResult> searchResultList = searchResponse.getItems();
        
        return "https://www.youtube.com/watch?v=" + searchResultList.get(0).getId().getVideoId();
        
    }
    
    
    public static String[] getPlayList(String link) {

    	String[] linkSplit = link.split("&");
    	String playlistId = null;
    	int startIndex = 0;
    	for(String param : link.split("&")) {
    		if(param.contains("list"))
    			playlistId = param.substring(5);
    		if(param.contains("index")) {
    			startIndex = Integer.parseInt(param.substring(6));
    		}
    	}
    	
    	if(playlistId == null) return new String[] {link};
    	
    	
    	YouTube.PlaylistItems.List playlistItemSearch;
    	
    	try {
    		playlistItemSearch = youtube.playlistItems().list("contentDetails");
		} catch (IOException e) {
			System.err.println("Youtube io error");
			return null;
		}

    	
    	playlistItemSearch.setKey(apiKey);
    	playlistItemSearch.setFields("items(contentDetails/videoId)");
    	playlistItemSearch.setPlaylistId(playlistId);
    	playlistItemSearch.setMaxResults(100L);
    	
    	PlaylistItemListResponse playlistResponse;
		try {
			playlistResponse = playlistItemSearch.execute();
		} catch (IOException e) {
			System.err.println("Youtube io error 2");
			return null;
		}
    	
		if(playlistResponse.getItems().size() == 0)
			return new String[] {link};
		
		
		List<PlaylistItem> playlistItemList = playlistResponse.getItems();
    	
		
		int i = 1;
		ArrayList<String> out = new ArrayList<>();
		for(PlaylistItem item : playlistItemList) {
			if(i>=startIndex) {
				String videoId = item.getContentDetails().getVideoId();
				out.add("https://www.youtube.com/watch?v=" + videoId);
			}
			i++;
		}
		System.out.print(out);
		
    	return out.toArray(new String[] {});
    }
}








