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
 * The Labelable interface provides all the methods required for labeling
 * an object in the robot game.
 *
 * @author fuerth
 * @version $Id$
 */
public interface Labelable {
    
    /**
     * Returns the text of this object's label.
     * @return The label for this object.  If this object should not have a
     * label, the return value is null.
     */
    String getLabel();
    
    /**
     * Sets the text of this object's label.
     * @param label The label text for this object.  Null is allowed, and means
     * that this object has no label.
     */
    void setLabel(String label);
    
    /**
     * Returns true if the label should be painted; false otherwise.
     */
    boolean isLabelEnabled();
    
    /**
     * Enables or disables painting of the label for this object.
     * 
     * @param enabled true for enabled; false for disabled.
     */
    void setLabelEnabled(boolean enabled);
    
    /**
     * Returns the direction, away from this Labelable object, that the label
     * should be positioned.
     */
    Direction getLabelDirection();
    
    
    /**
     * Sets the direction, away from this Labelable object, that the label
     * should be positioned.
     */
    void setLabelDirection(Direction direction);
}
