//$Id$
package queue;

import dbmanager.MusicPlayerDBAPIImpl;
import song.Song;
import ui.DisplayColor;
import ui.MusicPlayerMainMenu;

public class SongQueue extends Thread{
	private volatile boolean isRunning = false;
	private volatile boolean isPlaying  = false;
	private volatile boolean isSkipTrack = false;
	private volatile boolean isQueueEmptied = false;
	public boolean isSkipToPrevTrack() {
		return isSkipToPrevTrack;
	}

	public boolean isQueueEmptied() {
		return isQueueEmptied;
	}

	public void setQueueEmptied(boolean isQueueEmptied) {
		this.isQueueEmptied = isQueueEmptied;
	}

	public void setSkipToPrevTrack(boolean isSkipToPrevTrack) {
		this.isSkipToPrevTrack = isSkipToPrevTrack;
	}

	private volatile boolean isSkipToPrevTrack = false;
	private volatile int currentPlayingSongId;
	private volatile double currentPlayingSongOrder;
	public static SongQueue getInstance() {
		return SongQueueInstance.INSTANCE;
	}

	public void setSkipTrack(boolean isSkipTrack) {
		this.isSkipTrack = isSkipTrack;
	}

	public int getCurrentPlayingSongId() {
		return currentPlayingSongId;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public double getCurrentPlayingSongOrder() {
		return currentPlayingSongOrder;
	}

	public void setCurrentPlayingSongOrder(double currentPlayingSongOrder) {
		this.currentPlayingSongOrder = currentPlayingSongOrder;
	}

	private static class SongQueueInstance {
		private static final SongQueue INSTANCE = new SongQueue();
	}
	
	public void setCurrentPlayingSongId(int currentPlayingSongId) {
		this.currentPlayingSongId = currentPlayingSongId;
	}

	@Override
	public void run() {
		System.out.println("Music Player started...");
		while(true) {
			if(isRunning && isPlaying) {
				Long currentPlayingSongDuration = 0L; 
				try {
					Song currentPlayingSong = MusicPlayerDBAPIImpl.getInstance().getCurrentPlayingSongDetails();
					if(currentPlayingSong != null) {
						currentPlayingSongDuration = currentPlayingSong.getSongDurationInSecs();
					}
					if(currentPlayingSongDuration > 0) {
						long startTime = System.currentTimeMillis();
						long nextTime = System.currentTimeMillis();
						int time = 0;
						while(nextTime - startTime < currentPlayingSongDuration) {
							if(!isRunning || isQueueEmptied || isSkipTrack) {
								// can store the remaining duration on application exit
								break;
							} else if(!isPlaying) {
								// pause music player
								while (!isPlaying) {
	                                Thread.sleep(1000);
	                            }
								// resume player
	                            startTime += System.currentTimeMillis() - nextTime;
							}						
							System.out.println("Playing song: " + currentPlayingSong.getSongTitle() + " " + String.format("%02d:%02d", time/60, (time%60)+1));
							++time;
							Thread.sleep(1000);
							nextTime = System.currentTimeMillis();
						}
						if(isQueueEmptied) {
							setQueueEmptied(false);
						} else if(isSkipTrack) {
							if(isSkipToPrevTrack) {
								SongQueueUtil.getInstance().moveToPrevTrack();
								isSkipToPrevTrack = false;
							} else {
								SongQueueUtil.getInstance().moveToNextTrack();
							}
							isSkipTrack = false;
							
						} else {
							MusicPlayerMainMenu.getInstance().printHighlightMessage("Finished Playing: " + currentPlayingSong.getSongTitle(), DisplayColor.RED);
							 
							if(isPlaying) { // auto-play next song
								SongQueueUtil.getInstance().moveToNextTrack();
							}
						}
						
					}
				} catch (Exception e) {
					System.out.println("Something went wrong with music player...");
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
	}
	
	public void setRunning(boolean running) {
		this.isRunning = running;
	}
}
