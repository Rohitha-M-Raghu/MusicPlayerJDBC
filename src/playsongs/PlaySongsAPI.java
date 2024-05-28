//$Id$
package playsongs;

public interface PlaySongsAPI {

	int playSong();

	int playPlayList();

	void addSongToQueue();

	void playSongNext();

	void skipToNextTrack();

	void skipToPrevTrack();

	int searchSongOrPlaylist();

	int playPlaylist(int playListId);

	int playSong(int songId);

}
