//$Id$
package queue;

public interface SongQueueAPI {
	
	void clearQueue();

	boolean removeSongFromQueue();

	void shuffleSongs();

	void unShuffleSongs();

	void displayStatistics();

	void displaySongLyrics(String songTitle, String artistName);

	void displaySongLyrics();

}
