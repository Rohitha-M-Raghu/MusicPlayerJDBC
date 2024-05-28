//$Id$
package song;

public enum SettingsMode {
	SHUFFLE(1),
	LOOP(2),
	AUTOPLAY(4);
	

	private final int mode;

	SettingsMode(int value) {
		this.mode = value;
	}
	
	public boolean isTypeMatch(int settingsType) {
        return (settingsType & mode) == mode;
    }

	public int getMode() {
		return mode;
	}
}
