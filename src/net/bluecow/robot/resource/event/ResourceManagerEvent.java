/*
 * Created on Jul 31, 2007
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

package net.bluecow.robot.resource.event;

import net.bluecow.robot.resource.ResourceManager;

/**
 * The ResourceManagerEvent represents some change (addition, removal, or modification)
 * to a resource in a resource manager.
 *
 * @author fuerth
 * @version $Id:$
 */
public class ResourceManagerEvent {
    
    /**
     * The resource manager that caused this event.
     */
    private Object source;
    
    /**
     * The parent path of the resource that was affected.
     */
    private final String parentPath;

    /**
     * The resource name (not a full path) that was added, removed, or changed.
     * This should probably become a collection or array in the future.
     */
    private final String childName;
    
    /**
     * Creates a new event with the given source resource manager and affected
     * path.  The actual type of event this object represents is determined
     * by which ResourceManagerListener method it is passed to.
     * 
     * @param source The resource manager that fired this event.
     * @param parentPath The parent path of the resource that was affected.
     * This string must always denote a directory (that is to say, it must
     * end with a "/" character).
     */
    public ResourceManagerEvent(ResourceManager source, String parentPath, String childName) {
        this.source = source;
        if (!parentPath.endsWith("/")) {
            throw new IllegalArgumentException("Invalid parent path \""+parentPath+"\" (trailing '/' is mandatory)");
        }
        this.parentPath = parentPath;
        
        final int childSlashIndex = childName.indexOf("/");
        if ( (childSlashIndex != -1) && (childSlashIndex != childName.length() - 1) ) {
            throw new IllegalArgumentException(
                    "Invalid child name \""+childName+"\" (resource names can't" +
                    " contain '/' except directories, which must end in '/')");
        }
        this.childName = childName;
    }
    
    public Object getSource() {
        return source;
    }
    
    /**
     * Returns the parent path (whose direct children were affected by this event).
     * This path name has a trailing slash.
     */
    public String getParentPath() {
        return parentPath;
    }
    
    /**
     * Returns the resource name (not a full path, just the name) that is the
     * subject of this event.
     */
    public String getChildName() {
        return childName;
    }
}
