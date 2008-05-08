/*
 * Created on Oct 15, 2007
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
import java.util.List;

/**
 * A ListableResourceLoader is a ResourceLoader with the additional
 * functionality that it is capable of producing a listing of any directory it
 * contains.  Conversely, it can be thought of as a read-only ResourceManager.
 * 
 * @author fuerth
 * @version $Id:$
 */
public interface ListableResourceLoader extends ResourceLoader {

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
    List<String> list(String path) throws IOException;

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
    List<String> list(String path, ResourceNameFilter filter) throws IOException;

    /**
     * Returns true if the given resource path represents an existing resource
     * (either file or directory) in this resource manager.
     * 
     * @param path The path to check for existence. Can be either a file or a
     * directory. If checking for the existence of a directory, one trailing
     * slash is optional. If checking for a file, there must not be a trailing
     * slash.
     * @return True if the path exists in this resource manager; false if it
     * does not.
     */
    boolean resourceExists(String path);

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
}
