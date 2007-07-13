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
package net.bluecow.robot.gate;

public class OrGate extends AbstractOrGate {
	
	/**
	 * Creates a 2-input OR gate.
	 */
	public OrGate() {
		this(2);
	}
	
	/**
	 * Creates a new OR gate with the specified number of inputs.
	 * 
	 * @param ninputs The number of inputs.
	 */
	public OrGate(int ninputs) {
		super(null);
		inputs = new DefaultInput[ninputs];
		for (int i = 0; i < ninputs; i++) {
			inputs[i] = new DefaultInput();
		}
	}
	
    /**
     * Returns "OR".
     */
    public String getType() {
        return "OR";
    }
    
    public OrGate createDisconnectedCopy() {
        final OrGate newGate = new OrGate(getInputs().length);
        newGate.copyFrom(this);
        return newGate;
    }

    public void evaluateInput() {
		boolean state = false;
		Gate.Input[] inputs = getInputs();
		for (int i = 0; i < inputs.length; i++) {
			state |= inputs[i].getState();
		}
		nextOutputState = state;
	}

    @Override
    protected boolean isInputInverted() {
        return false;
    }

    @Override
    protected boolean isOutputInverted() {
        return false;
    }

}
