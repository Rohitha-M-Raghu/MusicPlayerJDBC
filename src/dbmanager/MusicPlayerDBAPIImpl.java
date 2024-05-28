//$Id$
package dbmanager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import cache.Cache;
import exception.PlaylistOrSongNotFoundException;
import playlist.Playlist;
import playsongs.PlaySongsAPIImpl;
import queue.SongQueue;
import song.Song;
import ui.DisplayColor;
import ui.MusicPlayerMainMenu;

public class MusicPlayerDBAPIImpl implements UserAuthenticationDBAPI, PlaylistDBAPI, QueueDBAPI, SongsDBAPI, UserPreferenceDBAPI, ArtistDBAPI {
	private static final String URL = "jdbc:mysql://localhost:3306/music_player";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "";
	private String query = "";
	private PreparedStatement pstmt = null;
	private CallableStatement cstmt = null;
	
	private Connection conn = null;
	private Statement stmt = null;
	private ResultSet res = null;
	
	public static MusicPlayerDBAPIImpl getInstance() {
		return MusicPlayerDBInstance.INSTANCE;
	}
	
	private static class MusicPlayerDBInstance {
		private static final MusicPlayerDBAPIImpl INSTANCE = new MusicPlayerDBAPIImpl();
	}
	
	public Connection getConn() {
		return conn;
	}

	private MusicPlayerDBAPIImpl(){
		try {
			//Registering JDBC 
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			//Creating Connection to Database
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			
		}catch (SQLException se) {
			se.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public void closeResources() {
		System.out.println("Closing all Resources...");
		try {
			if(res != null) {
				res.close();
			}
			if(pstmt != null) {
				pstmt.close();
			}
			if(stmt != null) {
				stmt.close();
			}
			if(cstmt != null) {
				cstmt.close();
			}
			if(conn != null) {
				conn.close();
			}
			System.out.println("Database Disconnected...");
		}catch (SQLException se) {
			se.printStackTrace();
		}
	}

	@Override
	public boolean manageSignInUsingUserName(String userName, String password) throws SQLException {
		query = "SELECT USER_ID FROM User_Details WHERE USER_NAME = ? AND PASSWORD = ?";
		pstmt = conn.prepareStatement(query);
		pstmt.setString(1, userName);
		pstmt.setString(2, password);
		res = pstmt.executeQuery();
		if(res.next()) {
			String userId = res.getString("USER_ID");
			if(!userId.isEmpty()) {
				Cache.getInstance().cacheData("USER_ID", userId);
				return true;
			} 
		}
		return false;
	}
	
	@Override
	public boolean passwordValidation() throws SQLException {
		Scanner inputReader = new Scanner(System.in);
		System.out.println("Enter password: ");
		String password = inputReader.nextLine();
		
		query = "SELECT COUNT(*) AS count FROM User_Details WHERE USER_ID = ? AND PASSWORD = ?";
		
		pstmt = conn.prepareStatement(query);
		pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
		pstmt.setString(2, password);
		res = pstmt.executeQuery();
		if(res.next()) {
			return res.getInt("count") == 1;
		}
		
		return false;
	}
	
	@Override
	public boolean changeUserName(String newUserName) throws SQLException {
		try {
			query = "UPDATE User_Details SET USER_NAME = ? WHERE USER_ID = ?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, newUserName);
			pstmt.setString(2, Cache.getInstance().getDataFromCache("USER_ID"));
			return pstmt.executeUpdate() > 0;	
		} catch (SQLException e) {
			if (e.getErrorCode() == 1062) {
	            System.out.println("Username already exists...");
	        } else {
	            throw e;
	        }
		}
		return false;
	}
	
	@Override
	public boolean changePassword(String newPassword) throws SQLException {
		query = "UPDATE User_Details SET Password = ? WHERE USER_ID = ?";
		pstmt = conn.prepareStatement(query);
		pstmt.setString(1, newPassword);
		pstmt.setString(2, Cache.getInstance().getDataFromCache("USER_ID"));
		return pstmt.executeUpdate() > 0;	
	}
	
	private String getUserPassword() throws SQLException {
		query = "SELECT * FROM User_Details WHERE USER_ID = ?";
		pstmt = conn.prepareStatement(query);
		pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
		res = pstmt.executeQuery();
		if(res.next()) {
			return res.getString("Password");
		}
		return null;
	}
	
	@Override
	public String manageSignInUsingEmail(String emailId, String password) throws SQLException {
		query = "SELECT USER_NAME, USER_ID FROM User_Details WHERE EMAIL_ID = ? AND PASSWORD = ?";
		pstmt = conn.prepareStatement(query);
		pstmt.setString(1, emailId);
		pstmt.setString(2, password);
		res = pstmt.executeQuery();
		if(res.next()) {
			Cache.getInstance().cacheData("USER_ID", res.getString("USER_ID"));
			return res.getString("USER_NAME");
		}
		return null;
	}
	
	@Override
	public void manageSignUp(String userName, String emailId, String password) throws SQLException {
		query = "{CALL InsertUserDetailsAndReturnId(?, ?, ?, ?)}";
		cstmt = conn.prepareCall(query);
        
        cstmt.setString(1, userName);
        cstmt.setString(2, emailId);
        cstmt.setString(3, password);
        cstmt.registerOutParameter(4, Types.INTEGER);
        
        cstmt.execute();
        String userId = Integer.toString(cstmt.getInt(4));
        Cache.getInstance().cacheData("USER_ID", userId);
        System.out.println(userName.toUpperCase() + " signed up... ");
	}
	
	@Override
	public int searchSongOrPlaylist(String searchContent) throws SQLException {
		query = "SELECT * FROM Song_Details "
				+ "JOIN Artist_Details ON Song_Details.Artist_ID = Artist_Details.ARTIST_ID "
				+ " WHERE SONG_TITLE LIKE ? " ;
		pstmt = conn.prepareStatement(query);
		pstmt.setString(1, searchContent + "%");
		res = pstmt.executeQuery();
		int index = 0;
		HashMap<Integer, Song> songResultMap = new HashMap<>();
		HashMap<Integer, Playlist> playlistResultMap = new HashMap<>();
		
		if(res.next()) {
			do {
				String songId = res.getString("Song_Details.SONG_ID");
                String songTitle = res.getString("Song_Details.SONG_TITLE");
                String artistName = res.getString("Artist_Details.ARTIST_NAME");
                if (songId != null) {
                    System.out.print(++index +". Song Title: " + songTitle);
                    System.out.println("\t Artist: " + artistName);
                    Song songResult = new Song(Integer.parseInt(songId), songTitle);
                    songResult.setArtistName(artistName);
                    songResultMap.put(index, songResult);
                } 
			} while(res.next());
		}
		query = "SELECT * FROM PlayList_Details WHERE PLAYLIST_NAME LIKE ? AND IS_PRESENCE = 1";
		pstmt = conn.prepareStatement(query);
		pstmt.setString(1, searchContent + "%");
		res = pstmt.executeQuery();
		if(res.next()) {
			do {
                String playlistId = res.getString("PLAYLIST_ID");
                String playlistName = res.getString("PLAYLIST_NAME");
                if (playlistId != null) {
                    System.out.print(++index +". Playlist: ");
                    System.out.println("\t Playlist Name: " + playlistName);
                    playlistResultMap.put(index, new Playlist(Integer.parseInt(playlistId), playlistName));
                }    
			} while(res.next());
		}
		if(index == 0) {
			System.out.println("No result found...");
			return -1;
		} else {
			Scanner inputReader = new Scanner(System.in);
			System.out.println(++index + ". Exit");
			System.out.println("Enter your choice: ");
			int choice = inputReader.nextInt();
			if(choice == index) {
				System.out.println("Returning to main menu");
			} else if(songResultMap.keySet().contains(choice)) {
				// play song
				return PlaySongsAPIImpl.getInstance().playSong(songResultMap.get(choice).getSongId());
			} else if(playlistResultMap.keySet().contains(choice)) {
				// play playlist 
				return PlaySongsAPIImpl.getInstance().playPlaylist(playlistResultMap.get(choice).getPlayListId());
			} else {
				System.out.println("No Song to play... ");
			}
		}
		return -1;
	}
	
	@Override
	public boolean addNewPlayList(String playListName) throws SQLException {
		query = "INSERT INTO PlayList_Details (USER_ID, PLAYLIST_NAME, TYPE) VALUES (?, ?, ?)";
		
		try {
	        pstmt = conn.prepareStatement(query);

	        pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
	        pstmt.setString(2, playListName);
	        pstmt.setString(3, "CUSTOM");

	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        if (e.getErrorCode() == 1062) { // MySQL error code for duplicate entry
	            System.out.println("Playlist already exists.");
	            return false; 
	        } else {
	            throw e; 
	        }
	    }
	}
	
	@Override
	public boolean addSongToPlayList(String songTitle, String playListName, String artistName) throws SQLException {
	    int playlistId = getPlaylistId(playListName);
	    try {
	    	if(playlistId == -1) {
		    	throw new PlaylistOrSongNotFoundException("Playlist not found");
		    }
		    Song song = getSongDetails(songTitle, artistName);
		    if(song == null) {
		    	throw new PlaylistOrSongNotFoundException("Song not found");
		    }
		    int songId = getSongDetails(songTitle, artistName).getSongId();
		    if(checkIfSongIsPresentInPlaylist(playlistId, songId)) {
	    		System.out.println("Song Already exists in playlist...");
	    		return false;
	    	}
		    int order = getLastOrderOfPlayListSongs(playlistId) + 1;
		    query = "INSERT INTO PlayListSongMapping (PLAYLISTID, SONGID, `ORDER`) VALUES (?, ?, ?)";
	        pstmt = conn.prepareStatement(query);
	        pstmt.setInt(1, playlistId);
	        pstmt.setInt(2, songId);
	        pstmt.setInt(3, order);
	        pstmt.executeUpdate();
	        return true;
		} catch (PlaylistOrSongNotFoundException e) {
			System.out.println(e.getMessage());
		}
	    return false;
	}
	
	private boolean checkIfSongIsPresentInPlaylist(int playlistId, int songId) {
	    try {
	        query = "SELECT COUNT(*) FROM PlayListSongMapping WHERE PLAYLISTID = ? AND SONGID = ?";
	        pstmt = conn.prepareStatement(query);
	        pstmt.setInt(1, playlistId);
	        pstmt.setInt(2, songId);
	        
	        res = pstmt.executeQuery();
	        if (res.next()) {
	            int count = res.getInt(1);
	            return count > 0; 
	        }
	    } catch (SQLException e) {
	        e.printStackTrace(); 
	    }
	    return false; 
	}

	@Override	
	public boolean removeSongFromPlaylist(String songTitle, String artistName,String playListName) throws SQLException, PlaylistOrSongNotFoundException {
		int playlistId = getPlaylistId(playListName);
		if(playlistId == -1) {
			throw new PlaylistOrSongNotFoundException("Playlist not found");
		}
		Song song = getSongDetails(songTitle, artistName);
		if(song == null) {
	    	throw new PlaylistOrSongNotFoundException("Song not found");
	    }
    	int songId = song.getSongId();
    
        query = "DELETE FROM PlayListSongMapping WHERE PLAYLISTID = ? AND SONGID = ?";
        pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, playlistId);
        pstmt.setInt(2, songId);
        return pstmt.executeUpdate() > 0;
	    
	}
	
	@Override
	public boolean removeSongFromQueue(Double order) {
		if(order < 0) {
			return false;
		}
		query = "DELETE FROM Queued_Songs WHERE USER_ID = ? AND `ORDER` = ?";
        try {
        	pstmt = conn.prepareStatement(query);
            pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
            pstmt.setDouble(2, order);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public int getLastOrderOfPlayListSongs(int playListId) throws SQLException {
		query = "SELECT MAX(`ORDER`) AS highest_order FROM PlayListSongMapping WHERE PLAYLISTID = ?";
		pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, playListId);
        res = pstmt.executeQuery();
        if (res.next()) {
            return res.getInt("highest_order");
        }
        return 0;
	}
	
	@Override
	public int getMaxOrderOfQueuedSongs() throws SQLException {
		query = "SELECT MAX(`ORDER`) AS highest_order FROM Queued_Songs WHERE USER_ID = ?";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
		res = pstmt.executeQuery();
        if (res.next()) {
            return res.getInt("highest_order");
        }
        return 0;
	}
	
	@Override
	public void displayAllSongs() {
		query = "SELECT * FROM Song_Details Song_Details JOIN Artist_Details "
				+ "ON Song_Details.ARTIST_ID = Artist_Details.ARTIST_ID LIMIT 20";
		try {
			pstmt = conn.prepareStatement(query);
			res = pstmt.executeQuery();
			if(!res.isBeforeFirst()) { // checks if res is empty
				System.out.println("No songs in music player... ");
				return;
			}
			MusicPlayerMainMenu.getInstance().printHighlightMessage("Songs", DisplayColor.BLUE);
			MusicPlayerMainMenu.getInstance().printHighlightMessage("---------------------", DisplayColor.BLUE);
			while(res.next()) {
				System.out.println(String.format("%-30s - %-20s %s", res.getString("Song_Details.SONG_TITLE"), res.getString("Artist_Details.ARTIST_NAME"), res.getTime("Song_Details.DURATION")));

			}
			System.out.println();
		} catch (SQLException e) {
			System.out.println("Error while getting song data... " + e.getMessage());
		}
 	}
	
	@Override
	public boolean displayUserPlaylists() {
		query = "SELECT * FROM PlayList_Details WHERE USER_ID = ? AND IS_PRESENCE = 1 LIMIT 10";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
			res = pstmt.executeQuery();
			if(!res.isBeforeFirst()) {
				System.out.println("No playlist found... ");
				return false;
			}
			MusicPlayerMainMenu.getInstance().printHighlightMessage(Cache.getInstance().getDataFromCache("userName").toUpperCase() + "'s Playlist", DisplayColor.BLUE);
			MusicPlayerMainMenu.getInstance().printHighlightMessage("---------------------", DisplayColor.BLUE);
			while(res.next()) {
				System.out.println(res.getString("PLAYLIST_NAME"));
			}
			System.out.println();
		} catch (SQLException e) {
			System.out.println("Error while getting playlist data...");
		}
		return true;
 	}
	
	@Override
	public void displayPlaylistSongs(String playListName) throws SQLException, PlaylistOrSongNotFoundException {
		int playlistId = getPlaylistId(playListName);
		if (playlistId != -1) {
			query = "SELECT Song_Details.*, Artist_Details.ARTIST_NAME "
					+ "FROM Song_Details "
					+ "JOIN PlayListSongMapping ON Song_Details.SONG_ID = PlayListSongMapping.SONGID "
					+ "JOIN Artist_Details ON Song_Details.Artist_ID = Artist_Details.ARTIST_ID "
					+ "WHERE PlayListSongMapping.PLAYLISTID = ? "
					+ "ORDER BY PlayListSongMapping.ORDER;";
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, playlistId);
			res = pstmt.executeQuery();
			if(res == null) {
				System.out.println("No songs in playlist " + playListName);
				return;
			}
			System.out.println("PlayList - " + playListName);
			System.out.println("---------------------");
			while(res.next()) {
				System.out.println(res.getString("Song_Details.SONG_TITLE") + " - " + res.getString("Artist_Details.ARTIST_NAME") + " " + res.getTime("Song_Details.DURATION"));
			}
			return;
		}
		throw new PlaylistOrSongNotFoundException("Playlist not found...");
 	}
	
	@Override
	public List<Double> displayQueuedSongs() {
		try {
			query = "SELECT Song_Details.SONG_TITLE, Queued_Songs.`ORDER` " +
                    "FROM Queued_Songs " +
                    "JOIN Song_Details ON Queued_Songs.SONG_ID = Song_Details.SONG_ID " +
                    "WHERE Queued_Songs.USER_ID = ? " +
                    "ORDER BY Queued_Songs.ORDER";
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
			res = pstmt.executeQuery();
			if(res == null) {
				System.out.println("No Songs in queue...");
				return new ArrayList<>();
			}
			List<Double> orderList = new ArrayList<>();
			System.out.println("SongQueue");
			System.out.println("---------------------");
			int songCount = 0;
			while(res.next()) {
				System.out.println(++songCount + ". " + res.getString("SONG_TITLE"));
				orderList.add(res.getDouble("ORDER"));
			}
			return orderList;
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}
	
	@Override
	public List<Integer> displayLikedSongs() throws SQLException {
		List<Integer> likedSongs = new ArrayList<>();
		query = "SELECT * FROM Song_Details "
				+ "JOIN Artist_Details ON Song_Details.Artist_ID = Artist_Details.ARTIST_ID"
				+ " WHERE SONG_ID IN (SELECT SONG_ID FROM User_Liked_Songs WHERE USER_ID = ?)";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
		res = pstmt.executeQuery();
		if(!res.next()) {
			System.out.println("No Liked Songs...");
			return likedSongs;
		}
		System.out.println("Liked Songs");
		System.out.println("---------------------");
		do {
			likedSongs.add(res.getInt("Song_Details.SONG_ID"));
			System.out.println(res.getString("Song_Details.SONG_TITLE") + " - " + res.getString("Artist_Details.ARTIST_NAME") + " " + res.getTime("Song_Details.DURATION"));
		} while(res.next());
		return likedSongs;
	}
	
	@Override
	public Song getSongOfTheDay() {
		query = "SELECT Song_Details.SONG_ID, Song_Details.SONG_TITLE, Artist_Details.ARTIST_NAME, COUNT(*) AS play_count, "
				+ "SEC_TO_TIME(SUM(TIME_TO_SEC(Song_Details.DURATION))) AS total_duration_streamed "
				+ "FROM Played_Song_History JOIN Song_Details ON Played_Song_History.SONG_ID = Song_Details.SONG_ID " 
				+ "JOIN Artist_Details ON Song_Details.Artist_ID = Artist_Details.ARTIST_ID "
				+ "WHERE DATE(FROM_UNIXTIME(PLAYED_TIME)) = CURDATE() "
				+ "GROUP BY SONG_ID "
				+ "ORDER BY total_duration_streamed DESC "
				+ "LIMIT 1;";
		
		try {
			pstmt = conn.prepareStatement(query);
			res = pstmt.executeQuery();
			if(res.next()) {
				Song songOfTheDay = new Song();
				songOfTheDay.setSongId(res.getInt("Song_Details.SONG_ID"));
				songOfTheDay.setSongTitle(res.getString("Song_Details.SONG_TITLE"));
				songOfTheDay.setArtistName(res.getString("Artist_Details.ARTIST_NAME"));
				return songOfTheDay;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Song mostPlayedSongInApp() {
		query = "SELECT Song_Details.SONG_ID, Song_Details.SONG_TITLE, Artist_Details.ARTIST_NAME,"
				+ "COUNT(Played_Song_History.SONG_ID) AS play_count, "
				+ "SEC_TO_TIME(SUM(TIME_TO_SEC(Song_Details.DURATION))) AS total_duration_streamed "
				+ "FROM Played_Song_History "
				+ "JOIN Song_Details ON Played_Song_History.SONG_ID = Song_Details.SONG_ID "
				+ "JOIN Artist_Details ON Song_Details.ARTIST_ID = Artist_Details.ARTIST_ID "
				+ "GROUP BY Played_Song_History.SONG_ID "
				+ "ORDER BY play_count DESC";
		
		try {
			pstmt = conn.prepareStatement(query);
			res = pstmt.executeQuery();
			if(res.next()) {
				Song mostPlayedSong = new Song();
				mostPlayedSong.setSongId(res.getInt("Song_Details.SONG_ID"));
				mostPlayedSong.setSongTitle(res.getString("Song_Details.SONG_TITLE"));
				mostPlayedSong.setArtistName(res.getString("Artist_Details.ARTIST_NAME"));
				mostPlayedSong.setCount(res.getLong("play_count"));
				mostPlayedSong.setDuration(res.getString("total_duration_streamed"));
				return mostPlayedSong;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Song mostStreamedArtistInApp() {
		query = "SELECT Artist_Details.ARTIST_NAME, COUNT(Played_Song_History.SONG_ID) AS play_count, "
				+ "SEC_TO_TIME(SUM(TIME_TO_SEC(Song_Details.DURATION))) AS total_duration_streamed "
				+ "FROM Played_Song_History "
				+ "JOIN Song_Details ON Played_Song_History.SONG_ID = Song_Details.SONG_ID "
				+ "JOIN Artist_Details ON Song_Details.ARTIST_ID = Artist_Details.ARTIST_ID "
				+ "GROUP BY Song_Details.ARTIST_ID "
				+ "ORDER BY total_duration_streamed DESC "
				+ "LIMIT 1;";		
		try {
			pstmt = conn.prepareStatement(query);
			res = pstmt.executeQuery();
			if(res.next()) {
				Song mostStreamedArtist = new Song();
				mostStreamedArtist.setArtistName(res.getString("Artist_Details.ARTIST_NAME"));
				mostStreamedArtist.setCount(res.getLong("play_count"));
				mostStreamedArtist.setDuration(res.getString("total_duration_streamed"));
				return mostStreamedArtist;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void displayFrequentlyPlayedGenre() throws SQLException {
		query = "SELECT sd.GENRE , COUNT(*) AS ListenCount "
				+ "FROM Played_Song_History his "
				+ "LEFT JOIN Song_Details sd ON his.SONG_ID = sd.SONG_ID "
				+ "WHERE his.USER_ID = ? "
				+ "GROUP BY sd.GENRE "
				+ "ORDER BY ListenCount DESC "
				+ "LIMIT 3;";
		
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
		res = pstmt.executeQuery();
		
		if(!res.next()) {
			System.out.println("No Songs Played by " + Cache.getInstance().getDataFromCache("userName").toUpperCase() +"...");
			return;
		}
		System.out.println("Frequently Played Genres");
		System.out.println("---------------------");
		do {
	        System.out.println(res.getString("GENRE") + "\t\t\t" + res.getString("ListenCount"));
	    } while(res.next());
	}
	
	@Override
	public HashSet<Integer> displayFrequentlyPlayedSongs(boolean isTitleNeeded, boolean isCountNeeded) throws SQLException {
		HashSet<Integer> recommendedSongs = new HashSet<>();

		query = "SELECT Song_Details.SONG_ID, Song_Details.SONG_TITLE, COUNT(Played_Song_History.SONG_ID) AS play_count "
				+ "FROM Played_Song_History "
				+ "JOIN Song_Details ON Played_Song_History.SONG_ID = Song_Details.SONG_ID "
				+ "WHERE Played_Song_History.USER_ID = ? "
				+ "GROUP BY Song_Details.SONG_ID "
				+ "ORDER BY play_count DESC "
				+ "LIMIT 5";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
		res = pstmt.executeQuery();
		
		if(!res.next()) {
			System.out.println("No Songs Played...");
			return recommendedSongs;
		}
		if(isTitleNeeded) {
			System.out.println("Frequently Accessed Songs");
			System.out.println("---------------------");
		}
		
		do {
			recommendedSongs.add(res.getInt("Song_Details.SONG_ID"));
			if(isCountNeeded) {	
				System.out.println(String.format("%-30s %-10s", res.getString("Song_Details.SONG_TITLE"), res.getString("play_count")));
			} else {
				System.out.println(res.getString("Song_Details.SONG_TITLE"));
			}
	    } while(res.next());
		return recommendedSongs;
	}
	
	@Override
	public void displayMusicRecommendation() {
		System.out.println("Recommended Songs");
		System.out.println("---------------------");
		
		HashSet<Integer> recommendedSongs;
		try {
			recommendedSongs = displayFrequentlyPlayedSongs(false, false);
		} catch (SQLException e) {
			recommendedSongs = new HashSet<>();
		}
		int limit = 10 - recommendedSongs.size();
		
		// already recommended songs
		StringBuilder alreadyRecommendedSongs = new StringBuilder();
	    for (Integer songId : recommendedSongs) {
	        if (alreadyRecommendedSongs.length() > 0) {
	            alreadyRecommendedSongs.append(",");
	        }
	        alreadyRecommendedSongs.append(songId);
	    }
		
		query = "SELECT Song_Details.SONG_TITLE, COUNT(Played_Song_History.SONG_ID) AS play_count "
				+ "FROM Played_Song_History "
				+ "JOIN Song_Details ON Played_Song_History.SONG_ID = Song_Details.SONG_ID "
				+ (alreadyRecommendedSongs.length() > 0 ? "WHERE Song_Details.SONG_ID NOT IN (" + alreadyRecommendedSongs.toString() + ") " : "")
				+ "GROUP BY Song_Details.SONG_ID "
				+ "ORDER BY play_count DESC "
				+ "LIMIT ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, limit);
			res = pstmt.executeQuery();
			
			// didn't test
			if(recommendedSongs.isEmpty() && !res.isBeforeFirst()) {
				displayInitialSongRecommendation();
				return;
			}
			while(res.next()) {
		        System.out.println(res.getString("Song_Details.SONG_TITLE"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Something went wrong... Try Again...");
		}
	}
	
	@Override 
	public void displayInitialSongRecommendation() throws SQLException {
		query = "SELECT * FROM Song_Details LIMIT 10";
		pstmt = conn.prepareStatement(query);
		res = pstmt.executeQuery();
		while(res.next()) {
	        System.out.println(res.getString("Song_Details.SONG_TITLE"));
		}
	}
	
	@Override
	public List<Integer> displayLikedPlayLists() throws SQLException {
		List<Integer> likedPlaylists = new ArrayList<>();
		query = "SELECT * FROM PlayList_Details WHERE USER_ID = ? AND IS_LIKED = 1 AND IS_PRESENCE = 1";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
		res = pstmt.executeQuery();
		if(!res.next()) {
			System.out.println("No Liked Playlists...");
			return likedPlaylists;
		}
		System.out.println("Liked Playlists");
		System.out.println("---------------------");
		do {
			likedPlaylists.add(res.getInt("PLAYLIST_ID"));
	        System.out.println(res.getString("PLAYLIST_NAME"));
	    } while(res.next());
		return likedPlaylists;
	}
	
	@Override
	public boolean likeASong(int songId) throws NumberFormatException, SQLException, PlaylistOrSongNotFoundException {
		if(songId != -1) {
			query = "INSERT INTO User_Liked_Songs (USER_ID, SONG_ID) VALUES (?, ?)";
	        pstmt = conn.prepareStatement(query);
	        pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
	        pstmt.setInt(2, songId);
	        pstmt.executeUpdate();
	        return true;
		}
		return false;
	}
	
	@Override
	public List<Integer> getLikedSongs() throws NumberFormatException, SQLException {
		List<Integer> likedSongIds = new ArrayList<>();
		query = "SELECT * FROM User_Liked_Songs WHERE USER_ID = ?";
		pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
        res = pstmt.executeQuery();
        while(res.next()) {
        	likedSongIds.add(res.getInt("SONG_ID"));
        }
        return likedSongIds;
	}
	
	@Override
	public List<Integer> getLikedPlaylistSongs() throws SQLException {
		List<Integer> likedPlaylistIds = new ArrayList<>();
		query = "SELECT * FROM PlayList_Details WHERE USER_ID = ? AND IS_LIKED = 1 AND IS_PRESENCE = 1";
		pstmt = conn.prepareStatement(query);
		pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
		res = pstmt.executeQuery();
		while(res.next()) {
			likedPlaylistIds.add(res.getInt("PLAYLIST_ID"));
		}
		return likedPlaylistIds;
	}
	
	@Override
	public boolean unlikeASong(int songId) throws SQLException {
		if(songId != -1) {
			query = "DELETE FROM User_Liked_Songs WHERE USER_ID = ? AND SONG_ID = ?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
			pstmt.setInt(2, songId);
			return pstmt.executeUpdate() > 0;
		}
		return false;
	}
	
	@Override
	public boolean unlikeAPlaylist(int playlistId) throws SQLException {
		if(playlistId != -1) {
			query = "UPDATE PlayList_Details SET IS_LIKED = 0 WHERE USER_ID = ? AND PLAYLIST_ID = ? AND IS_PRESENCE = 1";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
			pstmt.setInt(2, playlistId);
			return pstmt.executeUpdate() > 0;
		}
		return false;
	}
	
	@Override
	public boolean likeAPlayList(String playListName) throws SQLException {
		int playlistId = getPlaylistId(playListName);
		if (playlistId != -1) {
		     query = "UPDATE PlayList_Details SET IS_LIKED = ? WHERE PLAYLIST_ID = ?";
		     pstmt = conn.prepareStatement(query);
		     pstmt.setInt(1, 1);
		     pstmt.setInt(2, playlistId);
		     return pstmt.executeUpdate() > 0;
		}
		return false;
	}
	
	@Override
	public int getCurrentUserSettings() throws SQLException {
		query = "SELECT * FROM User_Preference WHERE USER_ID = ?";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
		ResultSet rs = pstmt.executeQuery();
	    if (rs.next()) {
	        return rs.getInt("MODE");
	    }
	    return -1;
	}
	
	@Override
	public boolean updateSettings(int valueToUpdate) throws SQLException {
		query = "UPDATE User_Preference SET MODE = ? WHERE USER_ID = ?";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, valueToUpdate);
	    pstmt.setInt(2, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
	    return pstmt.executeUpdate() > 0;
	}
	
	@Override
	public String getUserTheme() {
		try {
			query = "SELECT THEME FROM User_Preference WHERE USER_ID = ?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
			res = pstmt.executeQuery();
			if(res.next()) {
				return res.getString("THEME");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "WHITE";
	}
	
	@Override
	public boolean setUserTheme(String theme) {
		try {
			query = "UPDATE User_Preference SET THEME = ? WHERE USER_ID = ?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, theme);
			pstmt.setString(2, Cache.getInstance().getDataFromCache("USER_ID"));
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public Song getFirstSongInQueue(boolean isSongDetailsNeeded) throws NumberFormatException, SQLException {
		query = "SELECT * FROM Queued_Songs WHERE USER_ID = ? ORDER BY `ORDER` LIMIT 1";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
		res = pstmt.executeQuery();
		if(res.next()) {
			Song nextSongDetails = new Song();
			nextSongDetails.setSongId(res.getInt("SONG_ID"));
			nextSongDetails.setOrder(res.getDouble("ORDER"));
			if(!isSongDetailsNeeded) {
				return nextSongDetails;
			}
			return getSongDetails(res.getInt("SONG_ID"));
		}
		return null;
	}
	
	@Override
	public Song getLastSongInQueue(boolean isSongDetailsNeeded) throws SQLException {
		query = "SELECT * FROM Queued_Songs WHERE USER_ID = ? ORDER BY `ORDER` DESC LIMIT 1";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
		res = pstmt.executeQuery();
		if(res.next()) {
			Song nextSongDetails = new Song();
			nextSongDetails.setSongId(res.getInt("SONG_ID"));
			nextSongDetails.setOrder(res.getDouble("ORDER"));
			if(!isSongDetailsNeeded) {
				return nextSongDetails;
			}
			return getSongDetails(res.getInt("SONG_ID"));
		}
		return null;
	} 
	
	@Override
	public Song getNextSongInQueue(boolean isSongDetailsNeeded) throws NumberFormatException, SQLException {
		double currentPlayingSongOrder = SongQueue.getInstance().getCurrentPlayingSongOrder();
		query = "SELECT * FROM Queued_Songs WHERE USER_ID = ? AND `ORDER` > ? ORDER BY `ORDER` LIMIT 1";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
		pstmt.setDouble(2, currentPlayingSongOrder);
		res = pstmt.executeQuery();
		if(res.next()) {
			Song nextSongDetails = new Song();
			nextSongDetails.setSongId(res.getInt("SONG_ID"));
			nextSongDetails.setOrder(res.getDouble("ORDER"));
			if(!isSongDetailsNeeded) {
				return nextSongDetails;
			}
			return getSongDetails(res.getInt("SONG_ID"));
		}
		return null;
	}
	
	@Override
	public Song getPrevSongInQueue(boolean isSongDetailsNeeded) throws SQLException {
		double currentPlayingSongOrder = SongQueue.getInstance().getCurrentPlayingSongOrder();
		query = "SELECT * FROM Queued_Songs WHERE USER_ID = ? AND `ORDER` < ? ORDER BY `ORDER` DESC LIMIT 1";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
		pstmt.setDouble(2, currentPlayingSongOrder);
		res = pstmt.executeQuery();
		if(res.next()) {
			Song nextSongDetails = new Song();
			nextSongDetails.setSongId(res.getInt("SONG_ID"));
			nextSongDetails.setOrder(res.getDouble("ORDER"));
			if(!isSongDetailsNeeded) {
				return nextSongDetails;
			}
			return getSongDetails(res.getInt("SONG_ID"));
		}
		return null;
	}
	
	@Override
	public boolean resetCurrentPlayingSong() {
		try {
			query = "UPDATE Queued_Songs SET IS_CURRENTPLAYING = 0 WHERE IS_CURRENTPLAYING = 1 AND USER_ID = ?";
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
			pstmt.executeUpdate();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean resetCurrentPlayingSong(int currentSongId, int nextSongId, double order) throws SQLException {
		boolean isResetCurrentPlayingSong = false;
		
		isResetCurrentPlayingSong = resetCurrentPlayingSong();	
		
		
		if(isResetCurrentPlayingSong) {
			query = "UPDATE Queued_Songs SET IS_CURRENTPLAYING = 1 WHERE SONG_ID = ? AND `ORDER` = ?";
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, nextSongId);
			pstmt.setDouble(2, order);
			if(pstmt.executeUpdate() > 0) {
				SongQueue.getInstance().setCurrentPlayingSongId(nextSongId);
				SongQueue.getInstance().setCurrentPlayingSongOrder(order);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean clearSongQueue() {
		query = "DELETE FROM Queued_Songs WHERE USER_ID = ?";
        try {
        	pstmt = conn.prepareStatement(query);
            pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
            pstmt.executeUpdate();
            SongQueue.getInstance().setQueueEmptied(true);
            return true;
        } catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public Song addAllPlaylistSongsToQueue(boolean isQueueCleared, int playListId) throws SQLException {
		double order;
		if(isQueueCleared) {
			order = 1;
		} else {
			order = getHighestOrder() + 1;
		}
		
		List<Integer> songList = getPlayListSongs(playListId);
		for (Integer songId : songList) {
	        addSongToQueue(songId, order, playListId);
	        order++; // Increment the order for the next song
	    }
		if(isQueueCleared) {
			return getFirstSongInQueue();
		} else {
			return null;
		}
	}
	
	@Override
	public Song addAllSongsInQueue(boolean isQueueCleared) throws SQLException {
		double order;
		double nextSongOrder = 1;
		List<Integer> songList;
		if(isQueueCleared) {
			order = SongQueue.getInstance().getCurrentPlayingSongOrder() + 1;
		} else {
			order = getHighestOrder() + 1;
			nextSongOrder = order;
		}
		songList = getAllSongs();
		for (Integer songId : songList) {
	        addSongToQueue(songId, order, -1);
	        order++; 
	    }
		if(songList == null || songList.isEmpty()) {
			System.out.println("No Songs to add...");
			return null;
		}
		if(isQueueCleared) {
			return getFirstSongInQueue();
		} else {
			Song nextSongToPlay = getSongDetails(songList.get(0));
			nextSongToPlay.setOrder(nextSongOrder);
			return nextSongToPlay;
		}
	}
	
	@Override
	public List<Integer> getAllSongs() {
		List<Integer> songList = new ArrayList<>();
		try {
			query = "SELECT * FROM Song_Details";
			pstmt = conn.prepareStatement(query);
			res = pstmt.executeQuery();
			if(!res.next()) {
				return songList;
			} 
			do {
				songList.add(res.getInt("SONG_ID"));
			}while(res.next());
		} catch (Exception e) {
			return songList;
		}
		return songList;
	}
	
	@Override
	public void setCurrentPlayingSong(int songId) throws NumberFormatException, SQLException{
		Song song = getSongDetails(songId);
		if(song == null) {
			System.out.println("Unable to find song...");
			return;
		}
		song.setOrder(getSongOrderFromQueue(songId));
		query = "UPDATE Queued_Songs SET IS_CURRENTPLAYING = 1 WHERE `ORDER` = ? AND USER_ID = ?";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, (int) song.getOrder());
		pstmt.setInt(2, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
		boolean isSuccess = pstmt.executeUpdate() > 0;
		if(isSuccess) {
			SongQueue.getInstance().setCurrentPlayingSongId(song.getSongId());
			SongQueue.getInstance().setCurrentPlayingSongOrder(song.getOrder());
			SongQueue.getInstance().setPlaying(true);
		}
	}
	
	private double getSongOrderFromQueue(int songId) {
		query = "SELECT * FROM Queued_Songs WHERE SONG_ID = ? AND USER_ID = ?";
		try {
			pstmt = conn.prepareStatement(query);
		    pstmt.setInt(1, songId);
		    pstmt.setString(2, Cache.getInstance().getDataFromCache("USER_ID"));		    
		    res = pstmt.executeQuery();
		    if (res.next()) {
		    	return res.getDouble("ORDER");
		    }
		} catch (Exception e) {
			return -1;
		}
	    return -1;
	}
	
	private Song getFirstSongInQueue() throws NumberFormatException, SQLException {
		query = "SELECT * FROM Queued_Songs WHERE USER_ID = ? ORDER BY `ORDER`";
		pstmt = conn.prepareStatement(query);
	    pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
	    res = pstmt.executeQuery();
		if(res.next()) {
			Song firstSong = new Song();
			firstSong.setSongId(res.getInt("SONG_ID"));
			firstSong.setOrder(res.getDouble("ORDER"));
			return firstSong;
		}
		return null;
	}
	
	@Override
	public void addSongToQueue(int songId, double order, int playListId) throws SQLException {
	    query = "INSERT INTO Queued_Songs (USER_ID, SONG_ID, CREATED_TIME, `ORDER`, PLAYLIST_ID) VALUES (?, ?, ?, ?, ?)";
	    if(playListId == -1) {
	    	query = "INSERT INTO Queued_Songs (USER_ID, SONG_ID, CREATED_TIME, `ORDER`) VALUES (?, ?, ?, ?)";
	    }
	    pstmt = conn.prepareStatement(query);
	    pstmt.setInt(1, Integer.parseInt(Cache.getInstance().getDataFromCache("USER_ID")));
	    pstmt.setInt(2, songId);
	    pstmt.setLong(3, System.currentTimeMillis());
	    pstmt.setDouble(4, order);
	    if(playListId != -1) {
	    	pstmt.setInt(5, playListId);
	    }
	    pstmt.executeUpdate();
	    pstmt.close();
	}

	private List<Integer> getPlayListSongs(int playListId) throws SQLException {
		List<Integer> songList = new ArrayList<>();
		query = "SELECT * FROM PlayListSongMapping WHERE PLAYLISTID = ? ORDER BY `ORDER`";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, playListId);
		res = pstmt.executeQuery();
		if(!res.next()) {
			return songList;
		} 
		do {
			songList.add(res.getInt("SONGID"));
		}while(res.next());
		return songList;
	}
	
	@Override
	public double getHighestOrder() throws SQLException {
		query = "SELECT MAX(`ORDER`) AS highest_order FROM Queued_Songs WHERE USER_ID = ?";
		pstmt = conn.prepareStatement(query);
        pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
        res = pstmt.executeQuery();
        if (res.next()) {
            return res.getDouble("highest_order");
        }
        return 0;
	}
	
	@Override
	public HashMap<Integer, String> getArtistList() {
		HashMap<Integer, String> artistDetails = new HashMap<>();
		query = "SELECT * FROM Artist_Details";
		try {
			pstmt = conn.prepareStatement(query);
			res = pstmt.executeQuery();
			while(res.next()) {
				artistDetails.put(res.getInt("ARTIST_ID"), res.getString("ARTIST_NAME"));
			}
		} catch (SQLException e) {
			System.out.println("Something went wrong while fetching Artist Data...");
		}
		return artistDetails;
	}
	
	@Override
	public boolean displayArtistDetails(int artistId) {
		query = "SELECT * FROM Artist_Details WHERE ARTIST_ID = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, artistId);
			res = pstmt.executeQuery();
			if(res.next()) {
				System.out.println("ArtistName: " + res.getString("ARTIST_NAME"));
				System.out.println("Description: " + res.getString("DESCRIPTION"));
				// check again
				System.out.println("Country: " + res.getString("COUNTRY"));
				System.out.println("Genre: " + res.getString("GENRE"));
				int ranking =  getArtistRanking(artistId);
				if(ranking != -1) {
					System.out.println("Ranking: " + ranking);
				}
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private int getArtistRanking(int artistId) {
		query = "SELECT sd.ARTIST_ID, COUNT(psh.SONG_ID) AS play_count "
				+ "FROM Played_Song_History psh "
				+ "JOIN Song_Details sd ON psh.SONG_ID = sd.SONG_ID "
				+ "GROUP BY sd.ARTIST_ID "
				+ "ORDER BY play_count DESC";
		try {
			pstmt = conn.prepareStatement(query);
			res = pstmt.executeQuery();
			int rank = 0;
			while(res.next()) {
				if(res.getInt("sd.ARTIST_ID") == artistId) {
					return rank + 1;
				}
				++rank;
			}
		} catch (SQLException e) {
			e.printStackTrace(); // comment out
			return -1;
		}
		return -1;
	}
	
	@Override
	public int getPlaylistId(String playListName) throws SQLException {
	    query = "SELECT PLAYLIST_ID FROM PlayList_Details WHERE PLAYLIST_NAME = ? AND USER_ID = ? AND IS_PRESENCE = 1";
	    pstmt = conn.prepareStatement(query);
	    pstmt.setString(1, playListName);
	    pstmt.setString(2, Cache.getInstance().getDataFromCache("USER_ID"));
	    ResultSet rs = pstmt.executeQuery();
	    if (rs.next()) {
	        return rs.getInt("PLAYLIST_ID");
	    }
	    return -1;
	}
	
	@Override
	public boolean renamePlaylist(int playlistId, String newPlaylistName) throws SQLException {
		query = "UPDATE PlayList_Details SET PLAYLIST_NAME = ? WHERE PLAYLIST_ID = ? AND USER_ID = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, newPlaylistName);
			pstmt.setInt(2, playlistId);
			pstmt.setString(3, Cache.getInstance().getDataFromCache("USER_ID"));
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			if (e.getErrorCode() == 1062) {
	            System.out.println("PlaylistName already exists...");
	        } else {
	        	throw e;
	        }
		}
		return false;
	}
	
	@Override
	public boolean deletePlaylist(int playlistId) throws SQLException {
		query = "UPDATE PlayList_Details SET IS_PRESENCE = 0 WHERE PLAYLIST_ID = ? AND USER_ID = ? ";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, playlistId);
		pstmt.setString(2, Cache.getInstance().getDataFromCache("USER_ID"));
		return pstmt.executeUpdate() > 0;
	}
	
	private boolean validateSong(String songTitle) {
		query = "SELECT SONG_ID FROM Song_Details WHERE SONG_TITLE = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, songTitle);
			res = pstmt.executeQuery();
			if(res.next()) {
				return res.getInt("SONG_ID") > 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Song getSongDetails(String songTitle, String artistName) throws SQLException {
		try {
			if(!validateSong(songTitle)) {
				throw new PlaylistOrSongNotFoundException("Song not found");
			} else if(getArtistId(artistName) == -1) {
				throw new PlaylistOrSongNotFoundException("Artist not found");
			}
		} catch (PlaylistOrSongNotFoundException e) {
			System.out.println(e.getMessage());
			return null;
		}
		
		query = "SELECT * "
				+ "FROM Song_Details "
				+ "JOIN Artist_Details ON Song_Details.Artist_ID = Artist_Details.ARTIST_ID "
				+ "WHERE Song_Details.SONG_TITLE = ? AND Artist_Details.ARTIST_NAME = ? ;";
		pstmt = conn.prepareStatement(query);
		pstmt.setString(1, songTitle);
		pstmt.setString(2, artistName);
		res = pstmt.executeQuery();
		if (res.next()) {
			Song songDetails = new Song(res.getInt("Song_Details.SONG_ID"), res.getString("Song_Details.SONG_TITLE"));
			songDetails.setArtistId(res.getInt("Artist_Details.ARTIST_ID"));
			return songDetails;
		}
	    return null;
	}
	
	@Override
	public int getArtistId(String artistName) throws SQLException {
		query = "SELECT ARTIST_ID FROM Artist_Details WHERE ARTIST_NAME = ?";
		pstmt = conn.prepareStatement(query);
		pstmt.setString(1, artistName);
		res = pstmt.executeQuery();
		if(res.next()) {
			return res.getInt("ARTIST_ID");
		}
		return -1;
	}
	
//	@Override
//	public boolean validateSongAndArtist(int songId, int artistId) throws SQLException {
//		query = "SELECT COUNT(*) AS count FROM Song_Details WHERE SONG_ID = ? AND Artist_ID = ?";
//		pstmt = conn.prepareStatement(query);
//		pstmt.setInt(1, songId);
//		pstmt.setInt(2, artistId);
//		res = pstmt.executeQuery();
//		if(res.next()) {
//			return res.getInt("count") == 1;
//		}
//		return false;
//	}
	
	@Override
	public Song getCurrentPlayingSongDetails() {
		query = "SELECT * FROM Song_Details "
				+ "JOIN Artist_Details ON Song_Details.Artist_ID = Artist_Details.ARTIST_ID "
				+ "WHERE Song_Details.SONG_ID IN (SELECT SONG_ID FROM Queued_Songs WHERE USER_ID = ? AND IS_CURRENTPLAYING = 1)";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
		    res = pstmt.executeQuery();
		    if (res.next()) {
		    	Song songDetails = new Song();
		    	songDetails.setSongId(res.getInt("Song_Details.SONG_ID"));
		    	songDetails.setSongTitle(res.getString("Song_Details.SONG_TITLE"));
		    	songDetails.setArtistName(res.getString("Artist_Details.ARTIST_NAME"));
		    	songDetails.setDuration(res.getTime("Song_Details.DURATION").toString());
		    	songDetails.setGenre(res.getString("Song_Details.GENRE"));
		        return songDetails;
		    }
		} catch (Exception e) {
			return null;
		}
	    return null;
	}
	
	@Override
	public double getCurrentPlayingSongOrder() {
		query = "SELECT * FROM Queued_Songs WHERE USER_ID = ? AND IS_CURRENTPLAYING = 1";
		try {
			pstmt = conn.prepareStatement(query);
		    pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
		    res = pstmt.executeQuery();
		    if (res.next()) {
		        return res.getDouble("ORDER");
		    }
		} catch (Exception e) {
			return -1;
		}
		return -1;
	}
	
	@Override
	public Song getSongDetails(int songId) {
		Song songDetails = null;
		query = "SELECT * FROM Song_Details "
				+ "JOIN Artist_Details ON Song_Details.Artist_ID = Artist_Details.ARTIST_ID "
				+ "WHERE SONG_ID = ?";
		try {
			pstmt = conn.prepareStatement(query);
		    pstmt.setInt(1, songId);
		    res = pstmt.executeQuery();
		    if (res.next()) {
		    	songDetails = new Song();
		    	songDetails.setSongId(res.getInt("Song_Details.SONG_ID"));
		    	songDetails.setSongTitle(res.getString("Song_Details.SONG_TITLE"));
		    	songDetails.setArtistName(res.getString("Artist_Details.ARTIST_NAME"));
		        return songDetails;
		    }
		} catch (Exception e) {
			return null;
		}
	    return null;
	}
	
	@Override
	public List<Integer> getAllQueueSongs() throws SQLException {
		query = "SELECT SONG_ID FROM Queued_Songs WHERE USER_ID = ?";
		List<Integer> songIds = new ArrayList<>();
		pstmt = conn.prepareStatement(query);
		pstmt.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
		res = pstmt.executeQuery();
		while(res.next()) {
			songIds.add(res.getInt("SONG_ID"));
		}
		return songIds;
	}
	
	@Override
	public boolean updateQueuedSongOrders(List<Integer> songIds, double order) throws SQLException {
		query = "UPDATE Queued_Songs SET `ORDER` = ?, ORIGINAL_ORDER = ? WHERE SONG_ID = ? AND USER_ID = ?";
        String querySelect = "SELECT `ORDER` FROM Queued_Songs WHERE SONG_ID = ? AND USER_ID = ?";
        try(PreparedStatement pstmtSelect = conn.prepareStatement(querySelect)) {
        	pstmt = conn.prepareStatement(query);
    		int currentPlayingSongId = SongQueue.getInstance().getCurrentPlayingSongId();
    		
    		for(int songId: songIds) {
    			pstmtSelect.setInt(1, songId);
                pstmtSelect.setString(2, Cache.getInstance().getDataFromCache("USER_ID"));
                res = pstmtSelect.executeQuery();
                if (res.next()) {
                    double originalOrder = res.getDouble("ORDER");
                    if(songId == currentPlayingSongId) {
        				pstmt.setDouble(1, originalOrder);     
        			} else {
        				 pstmt.setDouble(1, order++);
        			}
                    pstmt.setDouble(2, originalOrder);
        			pstmt.setInt(3, songId);
        			pstmt.setString(4, Cache.getInstance().getDataFromCache("USER_ID"));
        			pstmt.addBatch();
                }
    		}
    		int[] updateCounts = pstmt.executeBatch();
            for (int count : updateCounts) {
                if (count == Statement.EXECUTE_FAILED) {
                    return false;
                }
            }
        }
		
        return true;
    }	
	
	@Override
	public boolean retainOriginalSongOrders() throws SQLException {
	    String updateQuery = "UPDATE Queued_Songs SET `ORDER` = ? WHERE SONG_ID = ? AND USER_ID = ?";
	    try (PreparedStatement updateStatement = conn.prepareStatement(updateQuery)) {
	        String selectQuery = "SELECT SONG_ID, ORIGINAL_ORDER FROM Queued_Songs WHERE USER_ID = ? AND ORIGINAL_ORDER IS NOT NULL";
	        try (PreparedStatement selectStatement = conn.prepareStatement(selectQuery)) {
	            selectStatement.setString(1, Cache.getInstance().getDataFromCache("USER_ID"));
	            try (ResultSet rs = selectStatement.executeQuery()) {
	                while (rs.next()) {
	                    double order = rs.getDouble("ORIGINAL_ORDER");
	                    int songId = rs.getInt("SONG_ID");
	                    updateStatement.setDouble(1, order);
	                    updateStatement.setInt(2, songId);
	                    updateStatement.setString(3, Cache.getInstance().getDataFromCache("USER_ID"));
	                    updateStatement.addBatch();
	                }
	            }
	        }

	        int[] updateCounts = updateStatement.executeBatch();
	        for (int count : updateCounts) {
	            if (count == Statement.EXECUTE_FAILED) {
	                return false;
	            }
	        }
	        return true;
	    }
	}

	@Override
	public Song getFirstSongDetails() {
		Song songDetails = null;
		query = "SELECT * FROM Song_Details "
				+ "JOIN Artist_Details ON Song_Details.Artist_ID = Artist_Details.ARTIST_ID "
				+ "LIMIT 1;";
		try {
			pstmt = conn.prepareStatement(query);
		    res = pstmt.executeQuery();
		    if (res.next()) {
		    	songDetails = new Song();
		    	songDetails.setSongId(res.getInt("Song_Details.SONG_ID"));
		    	songDetails.setSongTitle(res.getString("Song_Details.SONG_TITLE"));
		    	songDetails.setArtistName(res.getString("Artist_Details.ARTIST_NAME"));
		        return songDetails;
		    }
		} catch (Exception e) {
			return null;
		}
	    return null;
	}
}
