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

/**
 * The GateListener is an interface for receiving notifications about
 * changes to individual gates.
 * <p>
 * Note that there is also a {@link CircuitListener}
 * interface, which reports changes to a circuit as a whole.  Some gate
 * events, such as a gate within a circuit being connected or repositioned,
 * are refired by the circuit as CircuitEvents.  If you are only interested
 * in when it is necessary to repaint a circuit, the CircuitListener
 * of the circuit is sufficient; you do not need to attach a GateListener
 * on each gate in the circuit.
 *
 * @author fuerth
 * @version $Id:$
 */
public interface GateListener {

    /**
     * Notification that one of the gate's inputs has been connected
     * or disconnected.  If the notification is for a disconnection event,
     * <tt>e.getSourceInput().getConnectedGate()</tt> will return null.
     * 
     * @param e The event object associated with the event. Its source input
     * will be the input that has been connected or disconnected.
     */
    public void inputConnected(GateEvent e);
    
    /**
     * Notification that the gate's position has changed.
     * 
     * @param e The event object associated with the event. The source input
     * property will be null.
     */
    public void gateRepositioned(GateEvent e);
}
