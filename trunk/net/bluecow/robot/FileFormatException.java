/*
 * Created on Mar 31, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.io.IOException;

public class FileFormatException extends IOException {
    private int lineNum;
    private String badLine;
    private int badCharPos;

    /**
     * Creates a new FileFormatException, which is suited to reporting problems with
     * parsing the content of a file.
     * 
     * @param message The error message to show to the user.
     * @param lineNum The line number in the input file that was unparseable.  The
     * special value -1 indicates end-of-file.
     * @param badLine The complete text of the offending line.  This can be null
     * if the file is past EOF, or no lines have been read yet.
     * @param badCharPos The character position of the offending data, if available.
     * The special value -1 means the whole line. 
     */
    public FileFormatException(String message, int lineNum, String badLine, int badCharPos) {
        super(message);
        this.lineNum = lineNum;
        this.badLine = badLine;
        this.badCharPos = badCharPos;
    }

    public int getBadCharPos() {
        return badCharPos;
    }

    public String getBadLine() {
        return badLine;
    }

    public int getLineNum() {
        return lineNum;
    }
}
