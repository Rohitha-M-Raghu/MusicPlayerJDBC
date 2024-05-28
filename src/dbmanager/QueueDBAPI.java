//$Id$
package dbmanager;

import java.sql.SQLException;
import java.util.List;

import song.Song;

public interface QueueDBAPI {
	public boolean removeSongFromQueue(Double order);
	
	public int getMaxOrderOfQueuedSongs() throws SQLException;
	
	public List<Double> displayQueuedSongs();
	
	public Song getFirstSongInQueue(boolean isSongDetailsNeeded) throws NumberFormatException, SQLException;
	
	public Song getLastSongInQueue(boolean isSongDetailsNeeded) throws SQLException;
	
	public Song getNextSongInQueue(boolean isSongDetailsNeeded) throws NumberFormatException, SQLException;
	
	public Song getPrevSongInQueue(boolean isSongDetailsNeeded) throws SQLException;
	
	public boolean resetCurrentPlayingSong();
	
	public boolean resetCurrentPlayingSong(int currentSongId, int nextSongId, double order) throws SQLException;
	
	public boolean clearSongQueue();
	
	public Song addAllPlaylistSongsToQueue(boolean isQueueCleared, int playListId) throws SQLException;
	
	public Song addAllSongsInQueue(boolean isQueueCleared) throws SQLException;
	
	public void setCurrentPlayingSong(int songId) throws NumberFormatException, SQLException;
	
	public void addSongToQueue(int songId, double order, int playListId) throws SQLException;
	
	public double getHighestOrder() throws SQLException;
	
	public Song getCurrentPlayingSongDetails();
	
	public double getCurrentPlayingSongOrder();
	
	public List<Integer> getAllQueueSongs() throws SQLException;
	
	public boolean updateQueuedSongOrders(List<Integer> songIds, double order) throws SQLException;
	
	public boolean retainOriginalSongOrders() throws SQLException;
}
