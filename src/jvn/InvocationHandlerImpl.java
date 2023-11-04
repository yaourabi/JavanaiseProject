package jvn;


import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class InvocationHandlerImpl implements InvocationHandler {
    //JvnObject
    private JvnObject jvnObject;

    public InvocationHandlerImpl(JvnObject jo) {
        jvnObject = jo;
    }
    public static Object newInstance(JvnObject object) throws JvnException {
        if (object != null) {
            Serializable obj = object.jvnGetSharedObject();
            return Proxy.newProxyInstance(
                    obj.getClass().getClassLoader(),
                    obj.getClass().getInterfaces(),
                    new InvocationHandlerImpl(object));
        }

        return null;
    }

    public Object invoke(Object proxy, Method m, Object[] args)
    {
        try {
            if (m.isAnnotationPresent(ReadAnnotation.class)) {
                System.out.println("InvocationHandler: lockRead");
                jvnObject.jvnLockRead();
            } else if (m.isAnnotationPresent(WriteAnnotation.class)) {
                System.out.println("InvocationHandler: lockWrite");
                jvnObject.jvnLockWrite();
            }

            Object result = m.invoke(jvnObject.jvnGetSharedObject(), args);
            jvnObject.jvnUnLock();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
