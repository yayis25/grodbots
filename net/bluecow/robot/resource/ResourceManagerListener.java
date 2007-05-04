/*
 * Created on Apr 24, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.resource;

public interface ResourceManagerListener {

    void resourceAdded(ResourceManager source, String path);
    void resourceRemoved(ResourceManager source, String path);
    void resourceChanged(ResourceManager source, String path);

}
