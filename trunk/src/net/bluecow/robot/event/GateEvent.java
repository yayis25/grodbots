/*
 * Created on Aug 31, 2007
 *
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

package net.bluecow.robot.event;

import net.bluecow.robot.gate.Gate;

/**
 * The GateEvent represents some change to a Gate object or one of
 * its inputs.
 *
 * @author fuerth
 * @version $Id:$
 */
public class GateEvent {

    private final Gate sourceGate;

    private final Gate.Input sourceInput;

    /**
     * Creates a new gate event which pertains to the given gate.
     * 
     * @param source The gate this event is about.
     */
    public GateEvent(Gate source) {
        this.sourceGate = source;
        this.sourceInput = null;
    }
    
    /**
     * Creates a new gate event which pertains to the given input. The
     * gate source of this event will be the gate the given input is
     * attached to.
     * 
     * @param source The input this event is about.
     */
    public GateEvent(Gate.Input source) {
        this.sourceGate = source.getGate();
        this.sourceInput = source;
    }
    
    /**
     * Returns the gate that this event pertains to.
     */
    public Gate getSourceGate() {
        return sourceGate;
    }
    
    /**
     * Returns the input that this event pertains to.  For events
     * that don't pertain to one particular input, this will be
     * null.
     */
    public Gate.Input getSourceInput() {
        return sourceInput;
    }
}
