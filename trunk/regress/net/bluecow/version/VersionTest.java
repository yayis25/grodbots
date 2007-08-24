/*
 * Created on Aug 12, 2007
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

package net.bluecow.version;

import junit.framework.TestCase;

public class VersionTest extends TestCase {
    
    private Version version;
    
    public void testParseWithSuffix() {
        version = new Version("1.2.3.4alpha");
        
        assertEquals(5, version.getParts().length);
        
        assertEquals(1, version.getParts()[0]);
        assertEquals(2, version.getParts()[1]);
        assertEquals(3, version.getParts()[2]);
        assertEquals(4, version.getParts()[3]);
        assertEquals("alpha", version.getParts()[4]);
    }

    public void testParseWithoutSuffix() {
        version = new Version("1.2.3.4");

        assertEquals(4, version.getParts().length);

        assertEquals(1, version.getParts()[0]);
        assertEquals(2, version.getParts()[1]);
        assertEquals(3, version.getParts()[2]);
        assertEquals(4, version.getParts()[3]);
    }
    
    public void testBadPatternWithDotBeforeSuffix() {
        try {
            version = new Version("1.2.3.alpha");
            fail("Bad version format was accepted");
        } catch (VersionParseException ex) {
            // good
        }
    }
    
    public void testCompareFullOnlySuffixesDiffer() {
        Version older = new Version("1.2.3cow");
        Version olderToo = new Version("1.2.3cow");
        Version newer = new Version("1.2.3moo");
        
        assertTrue(older.compareTo(olderToo) == 0);
        assertTrue(older.compareTo(newer) < 0);
        assertTrue(newer.compareTo(older) > 0);
    }

    public void testCompareFullNoSuffixes() {
        Version older = new Version("1.2.2");
        Version olderToo = new Version("1.2.2");
        Version newer = new Version("1.2.3");
        
        assertTrue(older.compareTo(olderToo) == 0);
        assertTrue(older.compareTo(newer) < 0);
        assertTrue(newer.compareTo(older) > 0);
    }

    public void testCompareDifferentLengths() {
        Version v1 = new Version("1.2");
        Version v2 = new Version("1.2.3");
        
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }
    
    public void testNoSuffixNewerThanSuffix() {
        Version older = new Version("1.2cow");
        Version newer = new Version("1.2");
        
        assertTrue(older.compareTo(newer) < 0);
        assertTrue(newer.compareTo(older) > 0);
    }

    public void testCompareSameLengthsOnlyOneHavingSuffix() {
        Version older = new Version("1.2cow");
        Version newer = new Version("1.2.3");
        
        assertTrue(older.compareTo(newer) < 0);
        assertTrue(newer.compareTo(older) > 0);
    }
    
    public void testToString() {
        Version v = new Version("1.2.3a");
        assertEquals("1.2.3a", v.toString());

        v = new Version("1.2.3");
        assertEquals("1.2.3", v.toString());

        v = new Version("1suffix");
        assertEquals("1suffix", v.toString());

        v = new Version("1");
        assertEquals("1", v.toString());
}

}
