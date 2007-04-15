/*
 * Created on Apr 9, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.resource;

/**
 * A ResourceNameFilter provides a simple mechanism for excluding certain
 * resources from a resource manager based on resource path name.
 * 
 * @author fuerth
 * @version $Id$
 */
public interface ResourceNameFilter {
    public boolean accepts(String path);
}
