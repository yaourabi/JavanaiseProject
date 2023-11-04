package test;

import irc.ISentence;
import irc.Sentence;
import jvn.InvocationHandlerImpl;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class BurstTest2 {
    public static void main(String[] args) {
        try {
            JvnServerImpl js = JvnServerImpl.jvnGetServer();
            ISentence jo = (ISentence) InvocationHandlerImpl.newInstance(js.jvnLookupObject("IRC"));

            if (jo == null) {
                JvnObject o = js.jvnCreateObject(new Sentence());
                o.jvnUnLock();
                js.jvnRegisterObject("IRC", o);
                jo = (ISentence) InvocationHandlerImpl.newInstance(o);
            }

            int numberOfThreads = 20;

            //burst of read and write operations
            for (int i = 0; i < numberOfThreads; i++) {
                ISentence finalJo = jo;
                Thread readThread = new Thread(() -> {
                    String s = finalJo.read();
                    System.out.println("Read operation: " + s);
                });

                ISentence finalJo1 = jo;
                Thread writeThread = new Thread(() -> {
                    String message = "Hello from thread " + Thread.currentThread().getId();
                    finalJo1.write(message);
                    System.out.println("Write operation: " + message);
                });

                readThread.start();
                writeThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

