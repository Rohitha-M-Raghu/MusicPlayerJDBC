//$Id$
package ui;

public enum DisplayColor {
	GREEN("\u001B[32m"),
    BLUE("\u001B[36m"),
    RED("\u001B[31m"),
    YELLOW("\u001B[33m"),
    MAGENTA("\u001B[35m"),
    WHITE("\u001B[37m"),
    BLACK("\u001B[30m");
	

	private final String ansiCode;
	
	private DisplayColor(String ansiCode) {
        this.ansiCode = ansiCode;
    }

    public String getAnsiCode() {
        return ansiCode;
    }

}
