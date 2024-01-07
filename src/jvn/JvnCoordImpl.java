package jvn;

import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {
    private static final long serialVersionUID = 1L;
    AtomicInteger objId = new AtomicInteger(0);
    private static final String FILE_SAVE = "coordObject.text";
    private final Hashtable<String, Integer> jvnObjectIdTable;
    //private final Hashtable<Integer, JvnObject> jvnObjectTable;
    private Hashtable<Integer, JvnObject> jvnObjectTable;
    private final Hashtable<Integer, JvnRemoteServer> jvnRemoteServerWriteTable;
    private final Hashtable<Integer, Set<JvnRemoteServer>> jvnRemoteServerReadTable;

    public static void main(String[] args) throws Exception {
        JvnCoordImpl obj = new JvnCoordImpl();
        System.out.println("Server running");
        new Thread(()->{
            System.out.println("if you want to quit, press enter");
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
            System.out.println("are you sure you want to quit? (y/n)");
            String answer = scanner.nextLine();
            if(answer.equals("y")) {
                System.out.println("quitting...");
                File file = new File(FILE_SAVE);
                if(file.exists()) {
                    file.delete();
                }
                System.exit(0);
            }
        }).start();
    }

    /**
     * Default constructor
     **/
    private JvnCoordImpl() throws Exception {
        LocateRegistry.createRegistry(2007);
        Naming.rebind("rmi://localhost:2007/JvnCoordinator", this);
        this.jvnObjectTable = new Hashtable<>();
        this.jvnObjectIdTable = new Hashtable<>();
        this.jvnRemoteServerWriteTable = new Hashtable<>();
        this.jvnRemoteServerReadTable = new Hashtable<>();

        if (new File(FILE_SAVE).exists()) {
            System.out.println("old data of coord has been found. Restoring...");

            FileInputStream fis = new FileInputStream(FILE_SAVE);
            ObjectInputStream input = new ObjectInputStream(fis);
            JvnCoordImpl restored = (JvnCoordImpl) input.readObject();

           // this.jvnObjectIdTable = restored.jvnObjectIdTable;
           // this.jvnObjectTable = restored.jvnObjectTable ;
           // this.jvnRemoteServerWriteTable = restored.jvnRemoteServerWriteTable;
           // this.jvnRemoteServerReadTable = restored.jvnRemoteServerReadTable;
           // this.objId = restored.objId;


            System.out.println(restored);
            input.close();
        }


    }

    /**
     * Allocate a NEW JVN object id (usually allocated to a
     * newly created JVN object)
     **/
    public synchronized int jvnGetObjectId() throws RemoteException, JvnException {
        return objId.incrementAndGet();
    }

    /**
     * Associate a symbolic name with a JVN object
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @param js  : the remote reference of the JVNServer
     **/
    public synchronized void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws RemoteException, JvnException {
        this.jvnObjectIdTable.put(jon, jo.jvnGetObjectId());
        this.jvnObjectTable.put(jo.jvnGetObjectId(), jo);
        this.jvnRemoteServerWriteTable.put(jo.jvnGetObjectId(), js);
        this.jvnRemoteServerReadTable.put(jo.jvnGetObjectId(), new HashSet<>());
        System.out.println("Putting Object in cache.");
    }

    /**
     * Get the reference of a JVN object managed by a given JVN server
     * @param jon : the JVN object name
     * @param js : the remote reference of the JVNServer
     **/
    public synchronized JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws RemoteException, JvnException, InterruptedException {
        JvnObject jvnObject = jvnObjectTable.get(jon);
        if (jvnObject != null) {
            System.out.println("Object found in cache. Returning cached object.");
            return jvnObject;
        }
        System.out.println("Object not found in cache. Retrieving from the server.");
        if (this.jvnObjectIdTable.get(jon) != null) {
            Integer id = this.jvnObjectIdTable.get(jon);
            Serializable data = this.jvnLockWrite(id, js);
            JvnObjectImpl o = (JvnObjectImpl) this.jvnObjectTable.get(id);
            o.setObject(data);
            this.jvnRemoteServerWriteTable.put(id, js);
            return o;
        }
        return null;
    }
    public synchronized void jvnSaveCoord() {
        try {

            FileOutputStream saveFile = new FileOutputStream(FILE_SAVE);
            ObjectOutputStream output = new ObjectOutputStream(saveFile);
            output.writeObject(this);

            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     **/
    public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js) throws RemoteException, JvnException, InterruptedException {
        System.out.println("jvnLockRead");
        JvnRemoteServer server = this.jvnRemoteServerWriteTable.get(joi);
        if (server != null) {
            Serializable lastobj = server.jvnInvalidateWriterForReader(joi);
            this.jvnRemoteServerWriteTable.remove(joi);
            this.jvnRemoteServerReadTable.get(joi).add(server);
            ((JvnObjectImpl) this.jvnObjectTable.get(joi)).setObject(lastobj);
        }

        this.jvnRemoteServerReadTable.get(joi).add(js);

        System.out.println("jvnLockRead");
        jvnSaveCoord();
        return this.jvnObjectTable.get(joi).jvnGetSharedObject();
    }

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     **/
    public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws RemoteException, JvnException, InterruptedException {
        System.out.println("jvnLockWrite");
        Set<JvnRemoteServer> jvnServerReadTable = jvnRemoteServerReadTable.get(joi);
        if (jvnServerReadTable.size() > 0) {
            int i = 0;
            for (JvnRemoteServer s : jvnServerReadTable) {
                if (!s.equals(js)) {
                    s.jvnInvalidateReader(joi);
                }
            }
            jvnServerReadTable.clear();
        } else {
            // Remove writer
            JvnRemoteServer writeServer = jvnRemoteServerWriteTable.get(joi);
            if (writeServer != null && !writeServer.equals(js)) {
                Serializable lastobj = writeServer.jvnInvalidateWriter(joi);
                ((JvnObjectImpl) this.jvnObjectTable.get(joi)).setObject(lastobj);
                jvnRemoteServerWriteTable.remove(joi);
            }
        }
        jvnRemoteServerWriteTable.put(joi, js);
        System.out.println("jvnLockWrite");
        jvnSaveCoord();
        return this.jvnObjectTable.get(joi).jvnGetSharedObject();
    }

    /**
     * A JVN server terminates
     * @param js  : the remote reference of the server
     **/
    public synchronized void jvnTerminate(JvnRemoteServer js) throws RemoteException, JvnException {
        for(Set<JvnRemoteServer> list : jvnRemoteServerReadTable.values()) {
            list.remove(js);
        }
        if(jvnRemoteServerWriteTable.contains(js)) {
            jvnRemoteServerWriteTable.remove(js);
        }
    }

    @Override
    public String toString() {
        return "JvnCoordImpl{" +
                "objId=" + objId +
                ", jvnObjectIdTable=" + jvnObjectIdTable +
                ", jvnObjectTable=" + jvnObjectTable +
                ", jvnRemoteServerWriteTable=" + jvnRemoteServerWriteTable +
                ", jvnRemoteServerReadTable=" + jvnRemoteServerReadTable +
                '}';
    }
}
