/*
 * Copyright (c) 2007, Jonathan Fuerth
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Jonathan Fuerth nor the names of other
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
