package irc;

import jvn.JvnCoordImpl;
import jvn.JvnRemoteCoord;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Main {
    public static void main(String[] args) {

        try {
           // JvnRemoteCoord coord = JvnCoordImpl
            System.out.println("Running server");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
