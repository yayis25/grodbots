/*
 * Created on Jun 27, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.io.IOException;

/**
 * The SpriteLoadException represents an exceptional condition arising
 * from the attempt to load a sprite into the JVM.  It may wrap any type
 * of exception, because there are many different implementations of the
 * Sprite interface (and different things can go wrong for each of them).
 *
 * @author fuerth
 * @version $Id$
 */
public class SpriteLoadException extends IOException {
    public SpriteLoadException(Throwable cause) {
        if (cause == null) {
            throw new NullPointerException("SpriteLoadException cause must not be null");
        }
        initCause(cause);
    }
    
    /**
     * Returns the message from the exception that caused this exception.
     */
    @Override
    public String getMessage() {
        return getCause().getMessage();
    }
}
