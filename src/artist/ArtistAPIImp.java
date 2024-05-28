//$Id$
package artist;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import dbmanager.MusicPlayerDBAPIImpl;

public class ArtistAPIImp implements ArtistAPI{
	
	public static ArtistAPIImp getInstance() {
		return ArtistAPIImpInstance.INSTANCE;
	}
	
	private static class ArtistAPIImpInstance {
		private static final ArtistAPIImp INSTANCE = new ArtistAPIImp();
	}
	
	@Override
	public void getArtistDetails() {
		// get list of artists to display
		HashMap<Integer, String> artistDetailsList = MusicPlayerDBAPIImpl.getInstance().getArtistList();
		if(artistDetailsList.isEmpty()) {
			System.out.println("No Artist Found...");
			return;
		}
		displayArtistList(artistDetailsList);
		Scanner inputReader = new Scanner(System.in);
		System.out.println("Enter Artist name: ");
		String artistName = inputReader.nextLine();
	
		boolean isArtistFound = false;
		// display artist details and get artist ranking
		for (Entry<Integer, String> artistDetails : artistDetailsList.entrySet()) {
		
			if(artistName.equals(artistDetails.getValue())) {
				isArtistFound = true;
				boolean isSuccess = MusicPlayerDBAPIImpl.getInstance().displayArtistDetails(artistDetails.getKey());
				if(!isSuccess) {
					System.out.println("Failed to fetch artist details... Try Again...");
				}
			}
		}
		
		if(!isArtistFound) {
			System.out.println("Artist not found...");
		}
	}
	
	private void displayArtistList(HashMap<Integer, String> artistDetailsList) {
		System.out.println("Artists");
		System.out.println("-------------");
		if(artistDetailsList.isEmpty()) {
			System.out.println("No Artists found");
			return;
		}
		int index = 0;
		for (Entry<Integer, String> artistDetails : artistDetailsList.entrySet()) {
			System.out.println(++index + ". " + artistDetails.getValue());
		}
	}
}
