/*
 * Created on Aug 6, 2007
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.bluecow.robot.Circuit;
import net.bluecow.robot.gate.Gate;

/**
 * The CircuitEvent represents some change (either structural or
 * stateful) in a circuit.
 */
public class CircuitEvent {

    /**
     * The circuit in which the change occurred.
     */
    private final Circuit source;
    
    /**
     * An unmodifiable copy of the list of gates given in the constructor.
     */
    private List<Gate> gates;
    
    /**
     * Creates a new event object for the given circuit.
     */
    public CircuitEvent(Circuit source, List<Gate> gatesAffected) {
        this.source = source;
        this.gates = Collections.unmodifiableList(new ArrayList<Gate>(gatesAffected));
    }
    
    /**
     * Returns the circuit in which the change occurred.
     */
    public Circuit getSource() {
        return source;
    }
    
    /**
     * Returns the list of gates affected by this change.  The list is not
     * modifiable.
     */
    public List<Gate> getGatesAffected() {
        return gates;
    }
}
