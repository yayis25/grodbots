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
 * Created on Apr 4, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.gate;


public class NandGate extends AbstractAndGate {

    /**
     * Creates a 2-input NAND gate.
     */
    public NandGate() {
        this(2);
    }

    /**
     * Creates a new NAND gate with the specified number of inputs.
     * 
     * @param ninputs The number of inputs.
     */
    public NandGate(int ninputs) {
        super(null);
        inputs = new DefaultInput[ninputs];
        for (int i = 0; i < ninputs; i++) {
            inputs[i] = new DefaultInput();
        }
    }

    /**
     * Returns "NAND".
     */
    public String getType() {
        return "NAND";
    }
    
    public NandGate createDisconnectedCopy() {
        final NandGate newGate = new NandGate(getInputs().length);
        newGate.copyFrom(this);
        return newGate;
    }

    public void evaluateInput() {
        boolean andValue = true;
        for (Gate.Input inp : getInputs()) {
            andValue &= inp.getState();
        }
        nextOutputState = !andValue;
    }
    
    @Override
    protected boolean isInputInverted() {
        return false;
    }

    @Override
    protected boolean isOutputInverted() {
        return true;
    }
}
