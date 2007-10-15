/*
 * Created on Oct 10, 2007
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
import java.io.InputStream;

/**
 * A ResourceLoader implementation that applies a prefix to all the path
 * names given to it, then delegates those requests to another resource
 * loader that does the actual work.
 * <p>
 * Normally, the prefix will be the name of a directory
 * that contains the resource tree which will appear to be at the root of
 * the namespace, but this is not enforced.  For example, if you want to
 * make the resource subtree under foo/bar/ appear to be the root of the
 * resource namespace, specify a prefix of "foo/bar/". The trailing slash
 * is crticial.
 * <p>
 * If, for some reason, you want to wrap a resource loader and prefix all
 * the path names with "baz" (which is not a directory), then you can specify
 * just "baz" (no trailing slash) as the prefix.  This will have the effect
 * of looking for the resouce "bazaar" when clients of this PrefixLoader
 * ask for "aar", or "bazam/zazoom" when clients request "am/zazoom". Useful?
 * Probably not. You be the judge.
 * 
 * @author fuerth
 * @version $Id:$
 */
public class PrefixResourceLoader implements ResourceLoader {
    
    /**
     * The ResourceLoader that all the actual resource loading activity is
     * delegated to.
     */
    private final ResourceLoader loader;

    /**
     * The prefix that will be appended to all path names before forwarding
     * the request to the delegate ResourceLoader.
     */
    private final String prefix;
    
    /**
     * Creates a new PrefixResourceLoader that wraps the given resource loader.
     * 
     * @param loader The resource loader to delegate I/O requests to.
     * @param prefix The prefix to apply to path names before delegating
     * requests.  Normally you will want to ensure there is a trailing slash
     * at the end of the prefix string.  See the class-level comment for
     * more details.
     */
    public PrefixResourceLoader(ResourceLoader loader, String prefix) {
        this.loader = loader;
        this.prefix = prefix;
    }

    /**
     * Prepends the given resource path name as described in {@link #addPrefix()},
     * then delegates the request to the resource loader that was given in the
     * constructor.
     */
    public InputStream getResourceAsStream(String resourceName) throws IOException {
        return loader.getResourceAsStream(addPrefix(resourceName));
    }

    /**
     * Prepends the given resource path name as described in {@link #addPrefix()},
     * then delegates the request to the resource loader that was given in the
     * constructor.
     */
    public byte[] getResourceBytes(String resourceName) throws IOException {
        return loader.getResourceBytes(addPrefix(resourceName));
    }

    /**
     * Adds the prefix that was given in the constructor to the front of the
     * given resource name, returning the resultant string. Prefixes are
     * described in detail in the class-level comment for
     * {@link PrefixResourceLoader}.
     */
    private String addPrefix(String resourceName) {
        return prefix + resourceName;
    }
}
