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
import java.util.List;

/**
 * A ResourceManager is a ResourceLoader which also provides operations
 * for listing, adding, removing, and updating the resources it contains.
 * <p>
 * All paths in this interface follow the same convention as the ResourceLoader
 * interface: They use the forward slash as the separator character, regardless
 * of the local native platform's convention.
 * <p>
 * Important note: you must call the {@link #close} method when you are finished
 * with a recource manager.
 * 
 * @author fuerth
 * @version $Id$
 */
public interface ResourceManager extends ResourceLoader {

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
     * Produces a list of all resources currently available in this resource manager.
     * Directories will be included in this list, and they will come earlier in
     * the list than any file or subdirectory they contain.  Directory entries are
     * defined as resource names ending with the "/" character.  Regular files
     * have names that do not end in "/".
     * 
     * @return A list of path names which can be opened for read or write, or removed.
     * @throws IOException If there is some unexpected difficulty in producing the
     * resource list.
     */
    List<String> listAll() throws IOException;

    /**
     * Produces a list of all resources currently available in this resource
     * manager, subject to the given filter. Directories will be included in
     * this list, and they will come earlier in the list than any file or
     * subdirectory they contain. Directory entries are defined as resource
     * names ending with the "/" character. Regular files have names that do not
     * end in "/".
     * 
     * @param filter
     *            The filter to apply to each resource name. Resource names not
     *            accepted by the filter will not be included in the listing.
     *            If this argument is null, no filtering will be performed.
     * @return A list of path names which can be opened for read or write, or
     *         removed.
     * @throws IOException
     *             If there is some unexpected difficulty in producing the
     *             resource list.
     */
    List<String> listAll(ResourceNameFilter filter) throws IOException;

    /**
     * Closes this resource manager and frees any system resources it may
     * have allocated.  Once this resource manager is closed, all methods
     * related to file I/O and file management will no longer function. They
     * will throw an IOException. It is your responsibility to call this 
     * method when you are finished with a resource manager.
     * 
     * @throws IOException if problems are encountered during the cleanup
     * process.
     */
    void close() throws IOException;

    /**
     * Lists the resources directly contained under the given resource directory
     * (not a recursive listing) in sorted order. If the given path exists but
     * represents a file resource (rather than a directory resource), the return
     * value is the empty list. If the path doesn't exist at all, then an
     * IOException will be thrown.
     * 
     * @param path
     *            The path name to a directory to list the contents of. Must not
     *            be null. To list the contents of the root directory, use an
     *            empty string.
     * @return A list of the immediate child resources under the given path.
     *         Children that are themselves directories will have a trailing "/"
     *         in their names.
     */
    List<String> list(String path);

    /**
     * Lists the resources directly contained under the given resource directory
     * (not a recursive listing) in sorted order. If the given path exists but
     * represents a file resource (rather than a directory resource), the return
     * value is the empty list. If the path doesn't exist at all, then an
     * IOException will be thrown.
     * 
     * @param path
     *            The path name to a directory to list the contents of. Must not
     *            be null. To list the contents of the root directory, use an
     *            empty string.
     * @param filter
     *            The filter to apply to each resource name. Resource names not
     *            accepted by the filter will not be included in the listing.
     *            If this argument is null, no filtering will be performed.
     * @return A list of the immediate child resources under the given path.
     *         Children that are themselves directories will have a trailing "/"
     *         in their names.
     */
    List<String> list(String path, ResourceNameFilter filter);
}
