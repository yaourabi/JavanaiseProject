package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {
    private Serializable obj;
    private State jvnObjectState;
    private int joi;

    public JvnObjectImpl(Serializable state, int id) {
        this.obj = state;
        this.jvnObjectState = State.W;
        this.joi = id;
    }

    @Override
    public synchronized void jvnLockRead() throws JvnException {
        System.out.println("jvnLockRead");
        switch (jvnObjectState) {
            case RC:
                jvnObjectState = State.R;
                break;
            case WC:
                jvnObjectState = State.RWC;
                break;
            case W:
                jvnObjectState = State.R;
                break;
            case NL:
                JvnServerImpl.jvnGetServer().jvnLockRead(joi);
                jvnObjectState = State.R;
                break;
            case R:
            case RWC:
                break;
        }
    }

    @Override
    public synchronized void jvnLockWrite() throws JvnException {
        switch (jvnObjectState) {
            case WC:
                jvnObjectState = State.W;
                break;
            case RWC:
                jvnObjectState = State.W;
            case R:
            case RC:
            case NL:
                JvnServerImpl.jvnGetServer().jvnLockWrite(joi);
                jvnObjectState = State.W;
                break;
            case W:
                break;
        }
    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {
        switch (jvnObjectState) {
            case R:
                jvnObjectState = State.RC;
                break;
            case W:
                jvnObjectState = State.WC;
                break;
            case RWC:
                jvnObjectState = State.WC;
                break;
        }
        notify();
        System.out.println("jvnUnlock");
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return joi;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        return obj;
    }

    public void setObject(Serializable o) {
        this.obj = o;
    }

    @Override
    public synchronized void jvnInvalidateReader() throws JvnException, InterruptedException {
        switch (jvnObjectState) {
            case R:
                wait();
                jvnObjectState = State.NL;
                break;
            case RC:
                jvnObjectState = State.NL;
                break;
        }
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException, InterruptedException {
        switch (jvnObjectState) {
            case W:
                wait();
                jvnObjectState = State.NL;
                return obj;
            case WC:
                jvnObjectState = State.NL;
                return obj;
            case RWC:
                wait();
                jvnObjectState = State.NL;
                return obj;
            default:
                break;
        }

        return obj;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException, InterruptedException {
        switch (jvnObjectState) {
            case W:
                wait();
                jvnObjectState = State.RC;
                return obj;
            case WC:
                jvnObjectState = State.RC;
                return obj;
            case RWC:
                wait();
                jvnObjectState = State.RC;
                return obj;
            default:
                break;
        }

        return obj;
    }

}
