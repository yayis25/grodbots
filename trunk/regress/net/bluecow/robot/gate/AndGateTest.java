/*
 * Created on Feb 14, 2006
 *
 * This code belongs to Jonathan Fuerth.
 */
package net.bluecow.robot.gate;

import junit.framework.TestCase;

public class AndGateTest extends TestCase {

    private class AlwaysOnGate implements Gate {
        public boolean getOutputState() { return true; }

        public String getLabel() {
            return "Always On";
        }

        public Input[] getInputs() {
            return new Input[0];
        }

        public void evaluateInput() {
            // no op
        }

        public void latchOutput() {
            // no op
        }

        public void reset() {
            // no op
        }
    };
    
    private AndGate gate;
    private Gate.Input in0;
    private Gate.Input in1;
    
    protected void setUp() throws Exception {
        super.setUp();
        gate = new AndGate(2);
        in0 = gate.getInputs()[0];
        in1 = gate.getInputs()[1];
        assertFalse("Gate should be off by default", gate.getOutputState());
    }

    public void testLatchRisingEdge() {
        in0.connect(new AlwaysOnGate());
        in1.connect(new AlwaysOnGate());
        
        assertFalse("Haven't evaluated yet; should still be off", gate.getOutputState());
        
        gate.evaluateInput();
        
        assertFalse("Should still be off after evaluation", gate.getOutputState());
        
        gate.latchOutput();
        
        assertTrue("Should be on now (second evaluation)", gate.getOutputState());
    }

    public void testDelayedFallingEdge() {
        in0.connect(new AlwaysOnGate());
        in1.connect(new AlwaysOnGate());
        
        gate.evaluateInput();
        gate.latchOutput();
        
        assertTrue("Should be on now", gate.getOutputState());
        
        in0.connect(null);
        
        assertTrue("Should still be on; haven't evaluated yet", gate.getOutputState());
        
        gate.evaluateInput();
        
        assertTrue("Should still be on after evaluation", gate.getOutputState());
        
        gate.latchOutput();
        
        assertFalse("Should be off now (latched evaluation)", gate.getOutputState());
    }
}
