/*
 * Created on Jul 26, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

/**
 * The LockedCircuitException indicates that an attempted method call on
 * a circuit failed because the circuit was locked.
 *
 * @author fuerth
 * @version $Id$
 */
public class LockedCircuitException extends RuntimeException {
    LockedCircuitException() {
        super("The circuit is locked");
    }
}
