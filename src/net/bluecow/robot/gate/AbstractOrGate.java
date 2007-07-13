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
 * Created on Jun 9, 2006
 *
 * This code belongs to Jonathan Fuerth.
 */
package net.bluecow.robot.gate;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.QuadCurve2D;

public abstract class AbstractOrGate extends AbstractGate {

    protected AbstractOrGate(String label) {
        super(label);
    }

    @Override
    public void drawBody(Graphics2D g2) {
        Rectangle r = getBounds();
        int backX = getInputStickLength() - getInputStickLength()/10;  // XXX: adjustment compensates for curve, but should take circleSize into account
        double backDepth = r.height/6.0;
        int pointyX = r.width - getOutputStickLength();
        
        // The back part
        g2.draw(new QuadCurve2D.Double(backX, 0, backX + backDepth, r.height/2, backX, r.height));
        
        // Top curve
        g2.draw(new QuadCurve2D.Double(backX, 0, backX + pointyX/2, 0, pointyX, r.height/2));
        
        // Bottom curve
        g2.draw(new QuadCurve2D.Double(backX, r.height, backX + pointyX/2, r.height, pointyX, r.height/2));

    }
}
