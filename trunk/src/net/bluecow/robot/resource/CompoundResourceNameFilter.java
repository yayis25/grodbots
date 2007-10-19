/*
 * Created on Oct 16, 2007
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

package net.bluecow.robot.resource;

/**
 * A class that combines two ResourceNameFilters into one filter
 * which only accepts the resource if both of the other filters
 * accept it.
 *
 * @author fuerth
 * @version $Id:$
 */
public class CompoundResourceNameFilter implements ResourceNameFilter {

    /**
     * One of the two filers that must accept a resource for it to be
     * accepted by this filter.
     */
    private final ResourceNameFilter filter1;

    /**
     * One of the two filers that must accept a resource for it to be
     * accepted by this filter.
     */
    private final ResourceNameFilter filter2;
    
    /**
     * Creates a new resource filter that accepts a resource iff both filter1
     * and filter2 accept it.
     * 
     * @param filter1 One of the filters to consult for resource acceptance.  Must not be null.
     * @param filter2 One of the filters to consult for resource acceptance.  Must not be null.
     */
    public CompoundResourceNameFilter(final ResourceNameFilter filter1, final ResourceNameFilter filter2) {
        if (filter1 == null) throw new NullPointerException("filter1 was null");
        if (filter2 == null) throw new NullPointerException("filter2 was null");
        this.filter1 = filter1;
        this.filter2 = filter2;
    }

    public boolean accepts(String path) {
        return filter1.accepts(path) && filter2.accepts(path);
    }
    
}
