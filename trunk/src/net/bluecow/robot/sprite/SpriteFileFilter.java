/*
 * Created on Nov 8, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import net.bluecow.robot.resource.ResourceNameFilter;

/**
 * The SpriteFileFilter is used for distinguishing resources that can be used
 * to create sprites using SpriteManager from those that can't.
 *
 * @author fuerth
 * @version $Id$
 */
public class SpriteFileFilter implements ResourceNameFilter {

    public boolean accepts(String name) {
        if (name == null) return false; 
        else return name.endsWith(".rsf") ||
                    name.endsWith(".png");
    }
    
}
