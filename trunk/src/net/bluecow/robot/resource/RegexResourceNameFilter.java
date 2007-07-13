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
 * Created on Apr 10, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.resource;

import java.util.regex.Pattern;

/**
 * A resource name filter that accepts resources based on regular expressions.
 * Can be used to either include matching resource paths or exclude matching
 * resource paths.
 *
 * @author fuerth
 * @version $Id$
 */
public class RegexResourceNameFilter implements ResourceNameFilter {

    /**
     * Compiled version of the regex this instance matches on.
     */
    private final Pattern pattern;
    
    /**
     * Controls whether this filter accepts pathnames matching the pattern
     * and rejects all others, or rejects pathnames matching the pattern
     * and accepts all others.
     */
    private final boolean filterOut;
    
    /**
     * Creates a new filter with the given regular expression and filtering
     * mode.
     * 
     * @param regex The regular expression to match on.  Must not be null.
     * @param filterOut The matching mode.  False means that pathnames will
     * be accepted iff they match given regex; true means that pathnames
     * will be accepted iff they do not match the regex.
     */
    public RegexResourceNameFilter(String regex, boolean filterOut) {
        this.pattern = Pattern.compile(regex);
        this.filterOut = filterOut;
    }
    
    public boolean accepts(String path) {
        boolean match = pattern.matcher(path).matches();
        if (filterOut) {
            return !match;
        } else {
            return match;
        }
    }

}
