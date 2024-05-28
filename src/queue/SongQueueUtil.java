//$Id$
package queue;

import java.sql.SQLException;

import cache.Cache;
import dbmanager.MusicPlayerDBAPIImpl;
import song.SettingsMode;
import song.Song;

public class SongQueueUtil {
	
	public static SongQueueUtil getInstance() {
		return SongQueueUtilInstance.INSTANCE;
	}
	
	private static class SongQueueUtilInstance {
		private static final SongQueueUtil INSTANCE = new SongQueueUtil();
	}
	
	public void moveToNextTrack() throws NumberFormatException, SQLException {
		Song nextSongDetails = MusicPlayerDBAPIImpl.getInstance().getNextSongInQueue(false);
		if(nextSongDetails == null) { // and if there is no loop...clear queue
			boolean isAutoplayEnabled = Cache.getInstance().getDataFromCache(SettingsMode.AUTOPLAY.toString()).equals("ON");
			boolean isLoopEnabled = Cache.getInstance().getDataFromCache(SettingsMode.LOOP.toString()).equals("ON");
			if(isAutoplayEnabled) {
				// load songs from song details to queue and play
				// and set nextSongId in queue
				nextSongDetails = MusicPlayerDBAPIImpl.getInstance().addAllSongsInQueue(false);
			} else if(isLoopEnabled) {
				nextSongDetails = MusicPlayerDBAPIImpl.getInstance().getFirstSongInQueue(false);
			} else {
				System.out.println("Player paused....");
				SongQueue.getInstance().setPlaying(false);
				// clear queue if queue is over and there is no loop
				// handles for no looping
				MusicPlayerDBAPIImpl.getInstance().clearSongQueue();
				SongQueue.getInstance().setCurrentPlayingSongId(0);
				SongQueue.getInstance().setCurrentPlayingSongOrder(0);
				System.out.println("No more song in queue...");
				return;
			}
			
		}
		boolean isSuccess = false;
		if(nextSongDetails != null) {
			isSuccess = MusicPlayerDBAPIImpl.getInstance().resetCurrentPlayingSong(SongQueue.getInstance().getCurrentPlayingSongId(), nextSongDetails.getSongId(), nextSongDetails.getOrder());
		}
		
		if(!isSuccess) {
			System.out.println("Something went wrong while skipping to next track");
			
		}
	}
	
	public void moveToPrevTrack() throws SQLException  {
		Song nextSongDetails = MusicPlayerDBAPIImpl.getInstance().getPrevSongInQueue(false);
		boolean isLoopEnabled = Cache.getInstance().getDataFromCache(SettingsMode.LOOP.toString()).equals("ON");
		if (isLoopEnabled) {
			nextSongDetails = MusicPlayerDBAPIImpl.getInstance().getLastSongInQueue(false);
		} else if(nextSongDetails == null) {
			// repeat the same track
			nextSongDetails = MusicPlayerDBAPIImpl.getInstance().getCurrentPlayingSongDetails();
			nextSongDetails.setOrder(SongQueue.getInstance().getCurrentPlayingSongOrder());
		}
		int currentPlayingSongId = SongQueue.getInstance().getCurrentPlayingSongId();
		boolean isSuccess = MusicPlayerDBAPIImpl.getInstance().resetCurrentPlayingSong(currentPlayingSongId, nextSongDetails.getSongId(), nextSongDetails.getOrder());
		if(!isSuccess) {
			System.out.println("Something went wrong while skipping to next track");
			
		}
	}
}
