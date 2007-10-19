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
 * Created on Mar 21, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import net.bluecow.robot.resource.event.ResourceManagerListener;

/**
 * A ResourceManager is a ListableResourceLoader which also provides operations
 * for adding, removing, and updating the resources it contains.
 * <p>
 * All paths in this interface follow the same convention as the ResourceLoader
 * interface: They use the forward slash as the separator character, regardless
 * of the local native platform's convention.
 * <p>
 * Important note: you must call the {@link #close} method when you are finished
 * with a resource manager.
 * 
 * @author fuerth
 * @version $Id$
 */
public interface ResourceManager extends ListableResourceLoader {

    /**
     * Opens the requested resource for writing, possibly creating it if it
     * did not already exist.  If the resource did already exist, it will be
     * truncated to 0 length. In either case, the OutputStream will be positioned
     * at the beginning of the file.
     * 
     * @param path The resource to open.
     * @param create If true, the resource will be created if it did not exist.
     * If false, and the requested resource did not exist, a FileNotFoundException will
     * be thrown.
     * @return An OutputStream which writes to the named resource.  You must close
     * this OutputStream when you are finished writing to it.
     * @throws FileNotFoundException if the resource did not already exist and the
     * create flag was false.
     * @throws IOException for other IO related problems.
     */
    OutputStream openForWrite(String path, boolean create) throws IOException;
    
    /**
     * Removes the specified resource from this resource manager.
     * 
     * @param path The resource to remove.  If it does not exist, a FileNotFound
     * exception will be thrown.
     * @throws IOException If the path does not exist, or cannot be removed for
     * any reason.
     */
    void remove(String path) throws IOException;

    /**
     * Creates a new directory with the given name as a direct child of the
     * given target directory. The target must already exist, and the new
     * directory name may not contain the '/' (forward slash) character.
     * If there is already a resource (be it a directory or a regular resource)
     * with the given name, an IOException will be thrown.
     * 
     * @param targetDir
     *            The existing directory that will be the parent of the new
     *            directory. Trailing slash is optional.
     * @param newDirName
     *            The new directory name. No slashes allowed!
     * @throws IOException if there is already a resource with the given path.
     */
    void createDirectory(String targetDir, String newDirName) throws IOException;
    
    /**
     * Adds the given listener to this resource manager.
     */
    public void addResourceManagerListener(ResourceManagerListener listener);

    /**
     * Removes the given listener from the list if it is present.  Otherwise,
     * returns with no side effects.
     */
    public void removeResourceManagerListener(ResourceManagerListener listener);
}
