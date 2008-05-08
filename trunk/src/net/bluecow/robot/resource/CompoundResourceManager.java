/*
 * Created on Oct 5, 2007
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The CompoundResourceManager serves to combine two other resource managers
 * together into one unified namespace. The intended practical use is to augment
 * one complete basic, default set of resources with another customized subset
 * of resources. For example, a robot level project need not include all of the
 * graphics and sprites that support robots and tiles on the playfield, sound
 * effects, music, and button graphics unless it wants to customize those
 * aspects of the game's look and feel. In the simplest case, the project's own
 * resource manager might only provide the map file and leave everything else to
 * the default resources.
 * <p>
 * This scheme also provides better backward compatibility with old level packs:
 * If future versions of the game become more thoroughly skinnable, the built-in
 * resources that support the default skin will still be available after merging
 * in an old level pack that couldn't possibly have anticipated the need for the
 * new button graphics, fonts, sound effects, and so on.
 * <p>
 * Here is a summary of this resource manager's behaviour under all circumstances:
 * <table border=1 style="border-collapse: collapse">
 *  <tr>
 *   <th>Operation     <th>Exists in<br>primary <th>Exists in<br>secondary <th>Operation Performed In
 *  <tr>
 *   <td rowspan=4 valign=top>getResourceAsStream, getResourceBytes
 *                     <td>Yes                  <td>Yes                    <td>Primary
 *  <tr>               <td>Yes                  <td>No                     <td>Primary
 *  <tr>               <td>No                   <td>Yes                    <td>Secondary
 *  <tr>               <td>No                   <td>No                     <td>Neither (FileNotFoundException)
 *  <tr>
 *   <td rowspan=4 valign=top>openForWrite
 *                     <td>Yes                  <td>Yes                    <td>Primary
 *  <tr>               <td>Yes                  <td>No                     <td>Primary
 *  <tr>               <td>No                   <td>Yes                    <td>Primary, and parent directories will be creatd if necessary
 *  <tr>               <td>No                   <td>No                     <td>Primary
 *  <tr>
 *   <td rowspan=4 valign=top>remove
 *                     <td>Yes                  <td>Yes                    <td>Primary
 *  <tr>               <td>Yes                  <td>No                     <td>Primary
 *  <tr>               <td>No                   <td>Yes                    <td>Error (IOException: permission denied)
 *  <tr>               <td>No                   <td>No                     <td>Error (IOException: permission denied)
 *  <tr>
 *   <td rowspan=4 valign=top>list
 *                     <td>Yes                  <td>Yes                    <td>Results are combined (no duplicate entries will be listed)
 *  <tr>               <td>Yes                  <td>No                     <td>Primary
 *  <tr>               <td>No                   <td>Yes                    <td>Secondary
 *  <tr>               <td>No                   <td>No                     <td>Error (FileNotFoundException)
 *  <tr>
 *   <td rowspan=4 valign=top>createDirectory
 *                     <td>Yes                  <td>Yes                    <td>Error (directory already exists)
 *  <tr>               <td>Yes                  <td>No                     <td>Error (directory already exists)
 *  <tr>               <td>No                   <td>Yes                    <td>Error (directory already exists)
 *  <tr>               <td>No                   <td>No                     <td>Primary
 * </table>
 * 
 * Note that listAll() always combines the results from both the primary and secondary
 * resource managers.
 * 
 * @author fuerth
 * @version $Id:$
 */
public class CompoundResourceManager extends AbstractResourceManager {

    /**
     * Controls the debugging features of this class.
     */
    private static final boolean debugOn = false;
    
    /**
     * Prints the given message to System.out if debugOn is true.
     */
    private static void debug(String msg) {
        if (debugOn) System.out.println(msg);
    }
    
    /**
     * The primary resource manager within this one (see class-level comment
     * for details).
     */
    private final ResourceManager primary;
    
    /**
     * The secondary resource manager within this one (see class-level comment
     * for details).
     */
    private final ListableResourceLoader secondary;
    
    public CompoundResourceManager(ResourceManager primary, ListableResourceLoader secondary) {
        debug("Creating new resource manager");
        this.primary = primary;
        this.secondary = secondary;
    }
    
    public void close() throws IOException {
        try {
            primary.close();
        } catch (Exception ex) {
            System.err.println("Closing primary resource manager failed! Moving on to secondary...");
            ex.printStackTrace();
        }
        secondary.close();
    }

    /**
     * Creates the given directory within the given target (parent) directory.
     * The new directory will be created in the primary resource manager.
     * 
     * @param targetDir The parent of the new directory to create.  This directory
     * must already exist in at least one of the primary or secondary resource
     * managers.
     * @param newDirName The name of the new directory to create. Must not contain
     * (or end with) a slash '/' character.
     */
    public void createDirectory(String targetDir, String newDirName) throws IOException {
        String newDirPath = targetDir + "/" + newDirName;
        if (primary.resourceExists(newDirPath) || secondary.resourceExists(newDirPath)) {
            throw new IOException("Directory "+newDirName+" already exists");
        }
        if ( (!primary.resourceExists(targetDir)) && (!secondary.resourceExists(targetDir)) ) {
            throw new IOException("Target directory "+targetDir+" does not exist");
        }
        
        ResourceUtils.mkdirs(primary, targetDir);
        
        if (!primary.resourceExists(targetDir)) {
            throw new AssertionError(
                    "Failed to ensure target dir "+targetDir+
                    " exists in primary resource manager");
        }
        
        primary.createDirectory(targetDir, newDirName);
    }

    public List<String> list(String path, ResourceNameFilter filter)
    throws IOException {
        if ( ! (primary.resourceExists(path) || secondary.resourceExists(path)) ) {
            throw new FileNotFoundException("No such directory \""+path+"\"");
        }
        SortedSet<String> mergedListing = new TreeSet<String>();
        if (primary.resourceExists(path)) {
            mergedListing.addAll(primary.list(path, filter));
        }
        if (secondary.resourceExists(path)) {
            mergedListing.addAll(secondary.list(path, filter));
        }
        return new ArrayList<String>(mergedListing);
    }

    public List<String> listAll(ResourceNameFilter filter) throws IOException {
        SortedSet<String> mergedListing = new TreeSet<String>();
        mergedListing.addAll(primary.listAll(filter));
        mergedListing.addAll(secondary.listAll(filter));
        return new ArrayList<String>(mergedListing);
    }

    public OutputStream openForWrite(String path, boolean create) throws IOException {
        int lastSlash = path.lastIndexOf('/');
        String parentDir;
        if (lastSlash == -1) {
            // creating a file in the root
            parentDir = "";
        } else {
            // include trailing slash
            parentDir = path.substring(0, lastSlash + 1);
        }
        
        if (!primary.resourceExists(parentDir)) {
            if (!secondary.resourceExists(parentDir)) {
                throw new IOException("Cannot write to \""+path+"\": Parent path does not exist");
            }
            
            if (create || secondary.resourceExists(path)) {
                ResourceUtils.mkdirs(primary, parentDir);
            }
        }
        return primary.openForWrite(path, create);
    }

    public void remove(String path) throws IOException {
        if (primary.resourceExists(path)) {
            primary.remove(path);
        } else if (secondary.resourceExists(path)) {
            throw new IOException("Cannot remove resources from secondary resource manager");
        } else {
            throw new FileNotFoundException("No such resource \""+path+"\"");
        }
    }

    public InputStream getResourceAsStream(String resourceName)
            throws IOException {
        if (primary.resourceExists(resourceName)) {
            return primary.getResourceAsStream(resourceName);
        } else if (secondary.resourceExists(resourceName)) {
            return secondary.getResourceAsStream(resourceName);
        } else {
            throw new FileNotFoundException("No such resource \""+resourceName+"\"");
        }
    }

    public boolean resourceExists(String path) {
        return primary.resourceExists(path) || secondary.resourceExists(path);
    }

    /**
     * Returns the primary resource manager wrapped by this compound resource
     * manager. This is useful if you want to make an archive of only the
     * resources contained in the primary, ignoring those in the secondary.
     */
    public ResourceManager getPrimary() {
        return primary;
    }

    /**
     * Returns the secondary resource manager wrapped by this compound resource
     * manager. This may not be useful in most cases, but it is provided for
     * symmetry with {@link #getPrimary()}.
     */
    public ListableResourceLoader getSecondary() {
        return secondary;
    }
}
