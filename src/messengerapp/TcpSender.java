package messengerapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * TCP Sender Runnable for creating background sender thread to connect to other
 * machines and send messages. It keeps a list of connected sockets and assumes
 * other machines listens on port TcpReceiver.PORT.
 * @author Shung-Hsi Yu <syu07@nyit.edu> ID#0906172
 * @version Apr 11, 2014
 */
public class TcpSender implements Runnable {
    private final Queue<InetAddress> possibleReceiverQueue;
    private final Queue<String> messageQueue;
    private final List<Socket> connectedSocketList;

    /**
     * Constructor for TcpSender class.
     */
    public TcpSender() {
        possibleReceiverQueue = new ArrayBlockingQueue<>(50);
        messageQueue = new ArrayBlockingQueue<>(20);
        connectedSocketList = new ArrayList<>();
    }
    
    /**
     * Default method being called when passed to thread object.
     */
    @Override
    public void run() {
        // Repeat while the thread is not interrupted
        while(!Thread.currentThread().isInterrupted()) {
            // Connects to all the possible machines first
            checkReceiverQueue();
            // Then send one message
            checkMessageQueue();
        }
        // Remove and close all sockets when interrupted
        removeAllSockets();
    }
    
    /**
     * Remove and close all connect sockets in the list.
     */
    private void removeAllSockets() {
        for(int i = connectedSocketList.size()-1; i >= 0; i--) {
            removeSocket(i);
        }
    }
    
    /**
     * Check the possibleReceiverQueue for possible receivers and try to 
     * connect to them. If successful, the new socket object is added to 
     * connectedSocketList. Only exits when possibleReceiverQueue is empty.
     */
    private void checkReceiverQueue() {
        // Retrieve the IP address of a machine to connect from queue
        InetAddress receiverIp = possibleReceiverQueue.poll();
        // If the retrieve is successful then loop
        while(receiverIp != null) {
            // Make sure the retrieved IP address is not a wildcard and
            // is not already in the list
            if(!receiverIp.isAnyLocalAddress() && !isIpInList(receiverIp)) {
                try {
                    // Open a new socket
                    Socket receiverSocket = new Socket(receiverIp, 
                            TcpReceiver.PORT);
                    // Add the socket to list
                    connectedSocketList.add(receiverSocket);
                } catch (ConnectException e) {
                    // TODO use logger
                    System.out.println("::Can't connect to " + receiverIp.toString());
                } catch (IOException e) {
                    // TODO implement
                    e.printStackTrace();
                }
            }
            
            // Try to retrieve another IP address from queue
            receiverIp = possibleReceiverQueue.poll();
        }
    }
    
    /**
     * Check if the specified IP address already exist in connectedSocketList.
     * @param ip IP address to check upon.
     * @return 
     */
    private boolean isIpInList(InetAddress ip) {
        boolean inList = false;
        for(Socket receiverSocket: connectedSocketList) {
            if(ip.equals(receiverSocket.getInetAddress())) {
                inList = true;
                break;
            }
        }
        return inList;
    }
    
    /**
     * Retrieve a message from messageQueue and send it to all the connected
     * sockets in connectedSocketList. Only retrieve and send one message upon
     * each call.
     */
    private void checkMessageQueue() {
        // Retrieve a message that is waiting to be sent from queue
        String message = messageQueue.poll();
        // Continue and loop if message retrieve is successful
        if(message != null) {
            // Try to send to all connected machines in the list
            for(int i = connectedSocketList.size()-1; i >= 0; i--) {
                try {
                    // If the socket is close, repoen it
                    if (connectedSocketList.get(i).isClosed()) {
                        // Get the IP address and port of the machine
                        InetSocketAddress oldInetSocketAddress =
                                (InetSocketAddress) connectedSocketList.get(i)
                                        .getRemoteSocketAddress();
                        // Create a new socket using that IP address and port
                        Socket newSocket = 
                                new Socket(oldInetSocketAddress.getAddress(), 
                                        oldInetSocketAddress.getPort());
                        // Replace the closed socket in list with the new socket
                        connectedSocketList.set(i, newSocket);
                    }
                    sendMessage(connectedSocketList.get(i), message);
                    connectedSocketList.get(i).close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // Remove socket if it can't be reached
                    System.out.println(":: Can't reach " + 
                            connectedSocketList.get(i).getInetAddress());
                    removeSocket(i);
                }
            }
        } else {
            // If the message queue is empty, pause for 10ms
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println(" :: Sleep interrupted");
            }
        }
    }
    
    /**
     * Send the specified message to the specified socket connection.
     * @param receiverSocket the destination of the message
     * @param message the message to send
     * @throws IOException 
     */
    private void sendMessage(Socket receiverSocket, String message) 
            throws IOException {
        PrintWriter out = new PrintWriter(receiverSocket.getOutputStream());
        out.println(message);
        if(out.checkError()) {
            throw new IOException("::Error in PrintWriter!");
        }
    }
    
    /**
     * Close and remove the i-th socket element in connectedSocketList.
     * @param i 
     */
    private void removeSocket(int i) {
        try {
            connectedSocketList.get(i).close();
        } catch (IOException ex) {}
        connectedSocketList.remove(i);
    }
    
    /**
     * Queue a message to be later sent by this TcpSender instance to all 
     * connected sockets. Can be called by other threads.
     * @param message the message to be sent
     */
    public void send(String message) {
        messageQueue.add(message);
    }
    
    /**
     * Add an IP address of a machine that this TcpSender instance will try
     * to connect and send message to.
     * @param ip IP address of the machine to connect
     */
    public void addReceiver(InetAddress ip) {
        possibleReceiverQueue.add(ip);
    }
}
