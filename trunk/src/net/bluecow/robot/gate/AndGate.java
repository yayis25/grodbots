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
 * Created on Jun 23, 2005
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.gate;

/**
 * The AndGate represents an AND gate with any number of inputs.
 *
 * @author fuerth
 * @version $Id$
 */
public class AndGate extends AbstractAndGate {

    /**
	 * Creates a 2-input AND gate.
	 */
	public AndGate() {
		this(2);
	}

	/**
	 * Creates a new AND gate with the specified number of inputs.
	 * 
	 * @param ninputs The number of inputs.
	 */
	public AndGate(int ninputs) {
		super(null);
		inputs = new DefaultInput[ninputs];
		for (int i = 0; i < ninputs; i++) {
			inputs[i] = new DefaultInput();
		}
	}
	
    /**
     * Returns "AND".
     */
    public String getType() {
        return "AND";
    }
    
    public AndGate createDisconnectedCopy() {
        final AndGate gate = new AndGate(getInputs().length);
        gate.copyFrom(this);
        return gate;
    }

	public void evaluateInput() {
		boolean state = true;
		Gate.Input[] inputs = getInputs();
		for (int i = 0; i < inputs.length; i++) {
			state &= inputs[i].getState();
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
