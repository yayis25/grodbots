/*
 * Created on Jul 16, 2007
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

package net.bluecow.robot.resource.url;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * A simple "factory" which is not a factory at all.  Always returns the
 * same ResourceUrlStreamHandler for a single URL protocol.
 */
public class ResourceURLStreamHandlerFactory implements URLStreamHandlerFactory {

    private final String protocol;
    private final ResourceURLStreamHandler handler;

    /**
     * Creates a factory which always returns the same ResourceUrlStreamHandler
     * for the given protocol.
     * 
     * @param protocol The URL protocol this stream handler factory supports.
     * @param handler The handler to return for URLs that match the protocol.
     */
    public ResourceURLStreamHandlerFactory(String protocol, ResourceURLStreamHandler handler) {
        if (protocol == null) {
            throw new NullPointerException("Null protocol not allowed");
        }
        if (handler == null) {
            throw new NullPointerException("Null handler not allowed");
        }
        this.protocol = protocol;
        this.handler = handler;
    }
    
    /**
     * Returns the handler instance of this factory.
     *  
     * @param protocol The URL protocol to return a handler for
     * @return A reference to {@link #handler} if the given protocol matches the
     * one given in the constructor; null otherwise.
     */
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (this.protocol.equals(protocol)) {
            return handler;
        } else {
            return null;
        }
    }

}
