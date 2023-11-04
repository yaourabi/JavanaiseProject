package test;

import irc.Sentence;
import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class BurstTest {

    public static void main(String[] args) {
        try {
            JvnServerImpl js = JvnServerImpl.jvnGetServer();
            JvnObject jo = js.jvnLookupObject("IRC");

            if (jo == null) {
                jo = js.jvnCreateObject(new Sentence());
                jo.jvnUnLock();
                js.jvnRegisterObject("IRC", jo);
            }

            int numberOfThreads = 10;

            //burst of read and write operations
            for (int i = 0; i < numberOfThreads; i++) {
                JvnObject finalJo = jo;
                Thread readThread = new Thread(() -> {
                    try {
                        finalJo.jvnLockRead();
                        String s = ((Sentence) finalJo.jvnGetSharedObject()).read();
                        finalJo.jvnUnLock();
                        System.out.println("Read operation: " + s);
                    } catch (JvnException e) {
                        e.printStackTrace();
                    }
                });

                JvnObject finalJo1 = jo;
                Thread writeThread = new Thread(() -> {
                    try {
                        String message = "Hello from thread " + Thread.currentThread().getId();
                        finalJo1.jvnLockWrite();
                        ((Sentence) finalJo1.jvnGetSharedObject()).write(message);
                        finalJo1.jvnUnLock();
                        System.out.println("Write operation: " + message);
                    } catch (JvnException e) {
                        e.printStackTrace();
                    }
                });

                readThread.start();
                writeThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
