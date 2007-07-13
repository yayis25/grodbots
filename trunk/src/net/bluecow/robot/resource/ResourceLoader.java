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
 * Created on Sep 25, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * ResourceLoader is the interface for locating and retrieving resources from a
 * hierarchical namespace. Implementations will get resources from different
 * places, including jar files, the system classpath, the local filesystem, etc.
 * 
 * <p>
 * In all cases, resource names are paths relative to the root of whatever
 * resource store the implementation uses. For example, a resource namespace
 * might be rooted at a particular directory, URL, or ZIP file. Resource path
 * name separators are always the forward slash character <tt>/</tt>.
 * 
 * @author fuerth
 * @version $Id$
 */
public interface ResourceLoader {
    
    /**
     * Retrieves the contents of the named resource from this resource loader's
     * storage system as an input stream.
     * 
     * @param resourceName the path (relative to this resource loader's base)
     * of the requested resource.  Path elements must be separated by forward
     * slash, regardless of the local native platform's custom.
     * @return An InputStream that is ready to deliver all the bytes of the
     * named resource.
     * @throws FileNotFoundException If the named resource does not exist
     * @throws IOException If another I/O error occurs while retrieving the resource
     */
    public InputStream getResourceAsStream(String resourceName) throws IOException;
    
    /**
     * Retrieves the contents of the named resource from this resource loader's
     * storage system as an array of bytes.
     * 
     * @param resourceName the path (relative to this resource loader's base)
     * of the requested resource.  Path elements must be separated by forward
     * slash, regardless of the local native platform's custom.
     * @return An array of bytes which contains the requested resource's data.
     * @throws FileNotFoundException If the named resource does not exist
     * @throws IOException If another I/O error occurs while retrieving the resource
     */
    public byte[] getResourceBytes(String resourceName) throws IOException;
}
