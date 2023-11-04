package jvn;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.io.*;

public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {
    // A JVN server is managed as a singleton
    private static JvnServerImpl js = null;

    private JvnRemoteCoord coord;
    private CacheManager<Integer, JvnObject> jvnObjects;

    /**
     * Default constructor
     **/
    private JvnServerImpl() throws Exception {
        super();

        this.jvnObjects = new CacheManager<>();

        coord = (JvnRemoteCoord) Naming.lookup("rmi://localhost:2007/JvnCoordinator");
    }

    /**
     * Static method allowing an application to get a reference to
     * a JVN server instance
     **/
    public static JvnServerImpl jvnGetServer() {
        if (js == null) {
            try {
                js = new JvnServerImpl();
            } catch (Exception e) {
                return null;
            }
        }

        return js;
    }

    /**
     * The JVN service is not used anymore
     **/
    public void jvnTerminate() throws JvnException {

        try {
            coord.jvnTerminate(this);
        } catch (RemoteException e) { }
    }

    /**
     * creation of a JVN object
     * @param o : the JVN object state
     **/
    public JvnObject jvnCreateObject(Serializable o) throws JvnException {
        try {
            int nid = coord.jvnGetObjectId();
            return new JvnObjectImpl(o, nid);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Associate a symbolic name with a JVN object
     * @param jon : the JVN object name
     * @param jo : the JVN object
     **/
    public void jvnRegisterObject(String jon, JvnObject jo) throws JvnException {
        this.jvnObjects.put(jo.jvnGetObjectId(), jo);

        try {
            coord.jvnRegisterObject(jon, jo, this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Provide the reference of a JVN object being given its symbolic name
     * @param jon : the JVN object name
     * @return the JVN object
     **/
    public JvnObject jvnLookupObject(String jon) throws JvnException {

        try {
            JvnObject o = coord.jvnLookupObject(jon, this);

            if (o != null) {
                this.jvnObjects.put(o.jvnGetObjectId(), o);
                return o;
            }

            return null;
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get a Read lock on a JVN object
     * @param joi : the JVN object identification
     * @return the current JVN object state
     **/
    public Serializable jvnLockRead(int joi) throws JvnException {

        try {
            Serializable state = coord.jvnLockRead(joi, this);
            ((JvnObjectImpl) this.jvnObjects.get(joi)).setObject(state);
        } catch (final RemoteException | InterruptedException re) {
            re.printStackTrace();
        }

        return null;
    }

    /**
     * Get a Write lock on a JVN object
     * @param joi : the JVN object identification
     * @return the current JVN object state
     **/
    public Serializable jvnLockWrite(int joi) throws JvnException {

        try {
            Serializable state = coord.jvnLockWrite(joi, this);
            ((JvnObjectImpl) this.jvnObjects.get(joi)).setObject(state);
        } catch (final RemoteException | InterruptedException re) {
            re.printStackTrace();
        }

        return null;
    }

    /**
     * Invalidate the Read lock of the JVN object identified by id
     * called by the JvnCoord
     * @param joi the JVN object id
     **/
    public synchronized void jvnInvalidateReader(int joi) throws RemoteException, JvnException, InterruptedException {

        this.jvnObjects.get(joi).jvnInvalidateReader();
    }
    /**
     * Invalidate the Write lock of the JVN object identified by id
     * @param joi : the JVN object id
     * @return the current JVN object state
     **/
    public synchronized Serializable jvnInvalidateWriter(int joi) throws RemoteException, JvnException, InterruptedException {

        return this.jvnObjects.get(joi).jvnInvalidateWriter();
    }

    /**
     * Reduce the Write lock of the JVN object identified by id
     * @param joi : the JVN object id
     * @return the current JVN object state
     **/
    public synchronized Serializable jvnInvalidateWriterForReader(int joi) throws RemoteException, JvnException, InterruptedException {
        return this.jvnObjects.get(joi).jvnInvalidateWriterForReader();
    }
}
