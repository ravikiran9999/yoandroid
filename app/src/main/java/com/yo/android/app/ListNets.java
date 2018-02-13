package com.yo.android.app;

import android.annotation.TargetApi;
import android.os.Build;

import java.io.Console;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by rajesh on 11/5/17.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class ListNets {
    private static final Console console = System.console();

    public static void main(String args[]) throws SocketException {
        Enumeration<NetworkInterface> nets =
                NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            System.out.println("netint name: %s%n" +
                    netint);
            displayInterfaceInformation(netint);
        }
    }

    private static void displayInterfaceInformation(
            NetworkInterface netint) throws SocketException {
        System.out.println("Display name: %s%n"+
                netint.getDisplayName());
        System.out.println("Name: %s%n"+ netint.getName());
        Enumeration<InetAddress> inetAddresses =
                netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(
                inetAddresses)) {
            System.out.println("InetAddress: %s%n"+ inetAddress);
        }

        System.out.println("Parent: %s%n"+ netint.getParent());
        System.out.println("Up? %s%n"+ netint.isUp());
        System.out.println("Loopback? %s%n"+ netint.isLoopback());
        System.out.println(
                "PointToPoint? %s%n"+ netint.isPointToPoint());
        System.out.println(
                "Supports multicast? %s%n"+ netint.isVirtual());
        System.out.println("Virtual? %s%n"+ netint.isVirtual());
        System.out.println("Hardware address: %s%n"+
                Arrays.toString(netint.getHardwareAddress()));
        System.out.println("MTU: %s%n"+ netint.getMTU());

        List<InterfaceAddress> interfaceAddresses =
                netint.getInterfaceAddresses();
        for (InterfaceAddress addr : interfaceAddresses) {
            System.out.println(
                    "InterfaceAddress: %s%n"+ addr.getAddress());
        }
        System.out.println("%n");
        Enumeration<NetworkInterface> subInterfaces =
                netint.getSubInterfaces();
        for (NetworkInterface networkInterface : Collections.list(
                subInterfaces)) {
            System.out.println("%nSubInterface%n");
            displayInterfaceInformation(networkInterface);
        }
        System.out.println("%n");
    }
}
