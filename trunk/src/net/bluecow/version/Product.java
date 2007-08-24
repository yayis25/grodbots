/*
 * Created on Aug 10, 2007
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

package net.bluecow.version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Product class represents downloadable products which have
 * one or more versions.
 *
 * @author fuerth
 * @version $Id:$
 */
public class Product {
    
    private String name;
    private String description;
    private List<ProductVersion> versions = new ArrayList<ProductVersion>();
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void addVersion(ProductVersion v) {
        v.setProduct(this);
        versions.add(v);
    }
    
    /**
     * Gets the requested version description for this product by exact match of
     * version number.
     * 
     * @param v The version to search for
     * @return The version of this product having exactly the given version number,
     * or null if there is no such version. 
     */
    public ProductVersion getVersion(Version v) {
        for (ProductVersion pv : versions) {
            if (pv.getVersion().equals(v)) return pv;
        }
        return null;
    }
    
    /**
     * Returns an unmodifiable view of the list of all versions this product has.
     */
    public List<ProductVersion> getVersions() {
        return Collections.unmodifiableList(versions);
    }
    
    @Override
    public String toString() {
        return "[Product: name=\""+name+"\"; desc=\""+description+"\"; versions="+versions+"]";
    }
}
