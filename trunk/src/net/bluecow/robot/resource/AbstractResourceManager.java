/*
 * Created on Oct 5, 2007
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.bluecow.robot.resource.event.ResourceManagerEvent;
import net.bluecow.robot.resource.event.ResourceManagerListener;


public abstract class AbstractResourceManager extends AbstractResourceLoader implements ResourceManager {
    
    /**
     * Indicates whether or not this resource manager has been closed.
     */
    private boolean closed = false;
    
    /**
     * Throws an IOException with an appropriate message if this resource manager
     * is closed. Otherwise returns with no side effects.
     */
    protected void checkClosed() throws IOException {
        if (closed) throw new IOException("This resource manager is closed");
    }
    
    public void close() throws IOException {
        closed = true;
    }

    public List<String> list(String path) throws IOException {
        return list(path, null);
    }

    public List<String> listAll() throws IOException {
        return listAll(null);
    }

    // ------------- Events! ----------------
    
    /**
     * All the listners of this resource manager.
     */
    private final List<ResourceManagerListener> listeners = new ArrayList<ResourceManagerListener>();

    /**
     * Adds the given listener.  No attempt is made to prevent duplication
     * in the listener list.
     */
    public void addResourceManagerListener(ResourceManagerListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the given listener from the list if it is present.  Otherwise,
     * returns with no side effects.
     */
    public void removeResourceManagerListener(ResourceManagerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Delivers a "resource added" event to all currently-registered listeners.
     */
    protected void fireResourceAdded(String parentPath, String resourceName) {
        ResourceManagerEvent evt = new ResourceManagerEvent(this, parentPath, resourceName);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            listeners.get(i).resourceAdded(evt);
        }
    }

}
