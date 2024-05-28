//$Id$
package dbmanager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import exception.PlaylistOrSongNotFoundException;
import song.Song;

public interface SongsDBAPI {
	public void displayAllSongs();
	
	public List<Integer> displayLikedSongs() throws SQLException;
	
	public Song getSongOfTheDay();
	
	public Song mostPlayedSongInApp();
	
	public Song mostStreamedArtistInApp();
	
	public void displayFrequentlyPlayedGenre() throws SQLException;
	
	public Set<Integer> displayFrequentlyPlayedSongs(boolean isTitleNeeded, boolean isCountNeeded) throws SQLException;
	
	public boolean likeASong(int songId) throws NumberFormatException, SQLException, PlaylistOrSongNotFoundException;
	
	public List<Integer> getAllSongs();
	
	public Song getSongDetails(String songTitle, String artistName) throws SQLException;
	
	public Song getSongDetails(int songId);
	
	public Song getFirstSongDetails();

	void displayMusicRecommendation();

	boolean unlikeASong(int songId) throws SQLException;

	List<Integer> getLikedSongs() throws NumberFormatException, SQLException;
	
	public int searchSongOrPlaylist(String searchContent) throws SQLException;

	void displayInitialSongRecommendation() throws SQLException;

}
