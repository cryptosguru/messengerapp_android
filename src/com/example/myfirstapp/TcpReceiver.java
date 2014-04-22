/*
 */

package com.example.myfirstapp;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Queue;

/**
 * TCP Receiver Runnable for creating background receiver thread to listen for
 * connections, and pass established connections to TcpReceiverWorker class.
 * Also passes ip address of incoming connections to a TcpSender instance as
 * possible receiver. Should check the isRead() method before passing this 
 * object.
 * @author Shung-Hsi Yu <syu07@nyit.edu> ID#0906172
 * @version Apr 11, 2014
 */
public class TcpReceiver implements Runnable, Closeable {
    public static int PORT = 8888;
    private ServerSocket receiverSocket;
    private boolean hasTcpSender;
    private boolean isReady;
    private final TcpSender tcpSender;
    private final Queue<String> uiMessageQueue;

    /**
     * Constructor of TcpReceiver class. Does not take a TcpSender instance.
     * @param uiMessageQueue Queue which will take the received messages
     */
    public TcpReceiver(Queue<String> uiMessageQueue) {
        this.tcpSender = null;
        this.hasTcpSender = false;
        this.uiMessageQueue = uiMessageQueue;
        this.isReady = false;
    }
    
    /**
     * Constructor of TcpReceiver class. Takes a TcpSender instance and passes
     * the IP address of all incoming connections to it as possible receiver.
     * @param tcpSender TcpSender instance to take the IP address
     * @param uiMessageQueue Queue which will take the received messages
     */
    public TcpReceiver(TcpSender tcpSender, Queue<String> uiMessageQueue) {
        this.tcpSender = tcpSender;
        this.hasTcpSender = true;
        this.uiMessageQueue = uiMessageQueue;
        this.isReady = false;
    }
    
    /**
     * Check if this TcpReceiver instance is ready to take incoming connections.
     * @return whether or not this TcpReceiver instance is ready
     */
    public boolean isReady() {
        return this.isReady;
    }
    
    /**
     * Default method to be called by thread objects.
     */
    @Override
    public void run() {
        receiverSocket = null;
        try {
            // Try to bind to the receiver port
            receiverSocket = new ServerSocket(PORT);
            // Mark this instance as reaady for incoming message
            isReady = true;
            // Continue to loop until the thread is interrupted
            while (!Thread.currentThread().isInterrupted()) {
                // Block and wait for incoming connections
                Socket sender = receiverSocket.accept();
                
                // Pass the IP address of connected machine to a TcpSender 
                // instance
                if(hasTcpSender) {
                    tcpSender.addReceiver(sender.getInetAddress());
                }
                
                // TODO add exector service with cache thread pool
                // Create a new runnable that will retrieve the message from a 
                // sender
                Runnable receiverWorker = 
                        new TcpReceiverWorker(sender, uiMessageQueue);
                // Start a new thread running the runnable
                new Thread(receiverWorker).start();
            }
        } catch (SocketException e) {
            // Do nothing when the socket is close
        }
        catch (IOException e) {
            // TODO implement 
            e.printStackTrace();
        }
    }
    
    /**
     * Close the ServerSocket and break the thread from blocking.
     */
    @Override
    public void close() {
        try {
            receiverSocket.close();
        } catch (IOException ex) {
            // Nothing
        }
    }
}
