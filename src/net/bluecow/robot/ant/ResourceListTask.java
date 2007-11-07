/*
 * Created on Oct 19, 2007
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

package net.bluecow.robot.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import net.bluecow.robot.resource.PreListedResourceLoader;
import net.bluecow.robot.resource.RegexResourceNameFilter;
import net.bluecow.robot.resource.ResourceNameFilter;
import net.bluecow.robot.resource.ResourceUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * An Ant task that creates a resource listing file suitable for use with
 * the {@link PreListedResourceLoader}.
 *
 * @author fuerth
 * @version $Id:$
 */
public class ResourceListTask extends Task {
    
    /**
     * The file to write the listing into.  If it already exists, it
     * will be overwritten.
     */
    private String targetFile;
    
    /**
     * The directory to produce a listing of.
     */
    private String baseDir;
    
    /**
     * All file and directory names that match this regular expression will be
     * filtered out from the listing.  If a directory name matches this regular
     * expression, its contents will not be included in the listing either.
     * <p>
     * If this property is left at its default value (<code>null</code>), all files and directories
     * under {@link #baseDir} will be included in the listing.
     */
    private String filterOut = "(.*/|).svn/.*";
    
    @Override
    public void execute() throws BuildException {
        if (targetFile == null) throw new BuildException("You have to specify a target file");
        if (baseDir == null) throw new BuildException("You have to specify the base directory to list");
        
        ResourceNameFilter filter = null;
        if (filterOut != null) {
            filter = new RegexResourceNameFilter(filterOut, true);
        }
        
        try {
            File dir = new File(baseDir);
            List<String> resources = ResourceUtils.recursiveListResources(dir, filter);
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(targetFile)));
            for (String resource : resources) {
                out.println(resource);
            }
            out.flush();
            out.close();
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }
    
}
