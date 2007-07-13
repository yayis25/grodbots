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
 * Created on Aug 21, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

/**
 * The Direction enum represents a compass direction.  It was originally
 * created to specify label directions relative to the items they
 *
 * @author fuerth
 * @version $Id$
 */
public enum Direction {
    
    NORTH("n"),
    NORTHEAST("ne"),
    EAST("e"),
    SOUTHEAST("se"),
    SOUTH("s"),
    SOUTHWEST("sw"),
    WEST("w"),
    NORTHWEST("nw");

    private String code;
    
    Direction(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    /**
     * Returns the direction associated with the code.
     * 
     * @param code A string of the form "n", "nw", "ne", "s", etc.  Null is not allowed.
     * @return The Direction instance associated with the code.
     * @throws NullPointerException if code is null.
     * @throws IllegalArgumentException if code is not recognised as one of the 8 cardinal
     * directions.
     */
    public static Direction get(String code) {
        code = code.toLowerCase();
        for (Direction dir : values()) {
            if (dir.getCode().equals(code)) {
                return dir;
            }
        }
        throw new IllegalArgumentException("Unknown direction code '"+code+"'");
    }
}
