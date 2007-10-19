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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The PreListedResourceLoader wraps a ResourceLoader with the ability to produce
 * listings of its resources.  It does this by consulting a resource file which
 * contains a list of all contents within that resource loader.
 *
 * @author fuerth
 * @version $Id:$
 */
public class PreListedResourceLoader implements ListableResourceLoader {

    /**
     * The resource loader that all of the actual resource loading
     * work is delegated to.
     */
    private final ResourceLoader loader;

    /**
     * The set of contents reported by this resource loader.
     */
    private final SortedSet<String> contents;
    
    /**
     * Creates a new PreListedResourceLoader that loads its resources from the
     * given resource loader, and gets its actual list of resources from the
     * given resource within that resource loader.
     * 
     * @param loader The resource loader that all of the actual resource loading
     * work is delegated to.
     * @throws IOException If there is a problem reading the listing from the given resource loader.
     */
    public PreListedResourceLoader(ResourceLoader loader, String listPath) throws IOException {
        if (loader == null) {
            throw new NullPointerException("Null delegate resource loader is not allowed");
        }
        if (listPath == null) {
            throw new NullPointerException("Null list resource path is not allowed");
        }
        this.loader = loader;
        
        InputStream listingStream = loader.getResourceAsStream(listPath);
        try {
            this.contents = parseResourceList(listingStream);
        } finally {
            listingStream.close();
        }
    }

    /**
     * Subroutine of the constructor that produces the set of contents based
     * on the contents of the given input stream.  If necessary, this method could
     * be made public. However, it's probably better to have client code produce
     * a new instance of this class and get the listing information that way.
     * 
     * @param in The input stream to read the list of resources from (text file, one item per line)
     * @return A sorted set containing all of the resource names that were listed in the input stream
     * @throws IOException If there are problems reading the stream
     */
    private static SortedSet<String> parseResourceList(InputStream in) throws IOException {
        SortedSet<String> contents = new TreeSet<String>();
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while ( (line = r.readLine()) != null ) {
            contents.add(line);
        }
        return contents;
    }

    /**
     * Passes on the request to the delegate loader only if the given
     * resource path is in the resource listing.
     */
    public InputStream getResourceAsStream(String resourceName) throws IOException {
        resourceName = cleanPath(resourceName);
        return loader.getResourceAsStream(resourceName);
    }

    /**
     * Passes on the request to the delegate loader only if the given
     * resource path is in the resource listing.
     */
    public byte[] getResourceBytes(String resourceName) throws IOException {
        resourceName = cleanPath(resourceName);
        return loader.getResourceBytes(resourceName);
    }

    public List<String> list(String path) throws IOException {
        return list(path, null);
    }

    public List<String> list(String path, ResourceNameFilter filter) throws IOException {
        path = cleanPath(path);
        System.out.println("Listing path \""+path+"\"");
        List<String> listing = new ArrayList<String>();
        SortedSet<String> dirContents = contents.tailSet(path);
        for (String p : dirContents) {
            System.out.println("  Considering \""+p+"\"");
            if (!p.startsWith(path)) break;
            System.out.println("  It starts with path");
            int nextSlash = p.indexOf('/', path.length() + 1);
            if (nextSlash >= 0 && nextSlash != p.length() - 1) continue;
            if (p.equals(path)) continue;
            System.out.println("  It's not in a subdirectory");
            if (filter == null || filter.accepts(p)) {
                System.out.println("  It passes the filter. Adding to list.");
                listing.add(p);
            }
        }
        return listing;
    }

    public List<String> listAll() throws IOException {
        return listAll(null);
    }

    public List<String> listAll(ResourceNameFilter filter) throws IOException {
        List<String> listing = new ArrayList<String>();
        for (String p : contents) {
            if (filter == null || filter.accepts(p)) {
                listing.add(p);
            }
        }
        return listing;
    }

    public boolean resourceExists(String path) {
        path = cleanPath(path);
        boolean exists = contents.contains(path);
        System.out.println("Resource \""+path+"\" exists? " + exists);
        return exists;
    }

    /**
     * XXX have to decide if paths start with a leading / or not.
     * 
     * @param path
     * @return
     */
    private String cleanPath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
    
    /**
     * This particular close method does nothing.
     */
    public void close() {
        // nothing to do
    }
}
