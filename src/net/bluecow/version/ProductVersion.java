/*
 * Created on Aug 9, 2007
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

import java.net.URI;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * The ProductVersion class represents a particular version of
 * a product.
 *
 * @author fuerth
 * @version $Id:$
 */
public class ProductVersion {

    private Product product;
    private Version version;
    private Date releaseDate;
    private Set<ProductVersion> dependencies = new TreeSet<ProductVersion>();
    private String description;
    private URI downloadUri;
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public URI getDownloadUri() {
        return downloadUri;
    }
    
    public void setDownloadUri(URI downloadUri) {
        this.downloadUri = downloadUri;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public Date getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
    
    /**
     * Adds a particular version of another product that this product
     * version depends on.  It is illegal for a product version to depend on
     * any version of its own product.
     * 
     * @param dependsOn A version or another product that this one depends
     * on.
     */
    public void addDependency(ProductVersion dependsOn) {
        if (dependsOn.getProduct() == getProduct()) {
            throw new IllegalArgumentException("A product version cannot depend on another version of the same product");
        }
        dependencies.add(dependsOn);
    }
    
    @Override
    public String toString() {
        return "[ProductVersion: ver=\""+version+"\" date=\""+releaseDate+"\" uri=\""+downloadUri+"\" description=\""+description+"\"]";
    }
}
