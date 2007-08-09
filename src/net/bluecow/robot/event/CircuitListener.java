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

/**
 * The CircuitListener interface offers implementers a way of
 * being notified when certain aspects of a circuit change.
 *
 * @author fuerth
 * @version $Id:$
 */
public interface CircuitListener {

    /**
     * Messaged when one or more gates have been added to the circuit.
     * The event object's list of gates is the list of added gates.
     */
    void gatesAdded(CircuitEvent evt);
    
    /**
     * Messaged when one or more gates have been removed from the circuit.
     * The event object's list of gates is the list of removed gates.
     */
    void gatesRemoved(CircuitEvent evt);
    
    /**
     * Messaged when gates are connected or disconnected.  The list of
     * gates in the event object is the list of gates whose outputs
     * were affected. There is presently no way of knowing which inputs
     * were disconnected if this is a disconnection notification.
     */
    void gatesConnected(CircuitEvent evt);
    
    /**
     * Messaged when a gate changes state. This message is sent once per circuit
     * clock cycle, and only if at least one gate changed state. The list of
     * gates in the event object are the gates whose output state has just
     * flipped on this circuit clock cycle.
     */
    void gatesChangedState(CircuitEvent evt);
    
}
