package jvn;

import java.io.*;

/**
 * Interface of a JVN object.
 * The serializable property is required in order to be able to transfer
 * a reference to a JVN object remotely
 */
public interface JvnObject extends Serializable {
    /**
     * Get a Read lock on the object
     **/
    void jvnLockRead() throws JvnException;

    /**
     * Get a Write lock on the object
     **/
    void jvnLockWrite() throws JvnException;

    /**
     * Unlock the object
     **/
    void jvnUnLock() throws JvnException;

    /**
     * Get the object identification
     **/
    int jvnGetObjectId() throws JvnException;

    /**
     * Get the object state
     **/
    Serializable jvnGetSharedObject() throws JvnException;

    /**
     * Invalidate the Read lock of the JVN object
     **/
    void jvnInvalidateReader() throws JvnException, InterruptedException;

    /**
     * Invalidate the Write lock of the JVN object
     * @return the current JVN object state
     **/
    Serializable jvnInvalidateWriter() throws JvnException, InterruptedException;

    /**
     * Reduce the Write lock of the JVN object
     * @return the current JVN object state
     **/
    Serializable jvnInvalidateWriterForReader() throws JvnException, InterruptedException;
    public void setObject(Serializable o)
            throws jvn.JvnException;
}
