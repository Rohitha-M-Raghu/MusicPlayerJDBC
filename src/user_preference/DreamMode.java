//$Id$
package user_preference;

import java.util.Scanner;

import queue.SongQueue;

public class DreamMode {
	private boolean dreamModeActive; 
    private Thread dreamModeThread;
    
    public static DreamMode getInstance() {
    	return DreamModeInstance.INSTANCE;
    }
    
    private static class DreamModeInstance {
    	private static final DreamMode INSTANCE = new DreamMode();
    }

	public DreamMode() {
		super();
		dreamModeActive = false;
		dreamModeThread = null;
	}

	public void setDreamMode() {
		if (dreamModeActive && dreamModeThread != null) {
            // Cancelling the existing dream mode session
            dreamModeThread.interrupt();
            System.out.println("Existing dream mode session canceled.");
		}
		
		Scanner inputReader = new Scanner(System.in);
		System.out.println("Enter dream mode duration(mins): ");
        long dreamModeTime = inputReader.nextInt();
        System.out.println("Dream mode set for " + dreamModeTime + " minutes.");
        dreamModeActive = true;
        
     // Creating a separate thread for dream mode 
        dreamModeThread = new Thread(() -> {
            try {
                Thread.sleep(dreamModeTime * 60000); 
                dreamModeActive = false;
                System.out.println("Dream Mode over...");
                SongQueue.getInstance().setPlaying(false);
            } catch (InterruptedException e) {
                System.out.println("Dream Mode stopped.");
            }
        });
        dreamModeThread.start();
	}
	
	public void resetDreamMode() {
		if(dreamModeActive && dreamModeThread != null) {
			dreamModeThread.interrupt();
			dreamModeActive = false;
			System.out.println("Dream Mode is turned off...");
		} else {
			System.out.println("Dream Mode is already off...");
		}
	}
}
