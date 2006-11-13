/*
 * Created on Nov 8, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.io.File;
import java.io.FilenameFilter;

/**
 * The SpriteFileFilter is used for distinguishing files that can be used
 * to create sprites using SpriteManager from those that can't.
 *
 * @author fuerth
 * @version $Id$
 */
public class SpriteFileFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
        if (name == null) return false; 
        else return name.endsWith(".rsf") ||
                    name.endsWith(".png") ||
                    name.endsWith(".gif");
    }
    
}
