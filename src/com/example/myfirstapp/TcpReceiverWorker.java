package com.example.myfirstapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Queue;

/**
 * TCP Receiver Worker to handle established incoming connections.
 * @author Shung-Hsi Yu <syu07@nyit.edu> ID#0906172
 * @version Apr 11, 2014
 */
public class TcpReceiverWorker implements Runnable {
    private Socket sender;
    private final Queue<String> uiMessageQueue;

    /**
     * Constructor for TcpReceiverWorker class.
     * @param sender the socket object that is connect to the sender
     * @param uiMessageQueue Queue which will take the received messages
     */
    public TcpReceiverWorker(Socket sender, Queue<String> uiMessageQueue) {
        this.sender = sender;
        this.uiMessageQueue = uiMessageQueue;
    }
    
    /**
     * Default method called by thread objects.
     */
    @Override
    public void run() {
        try(BufferedReader in = new BufferedReader(new InputStreamReader(
                sender.getInputStream()))) {
            String message = getWholeMessage(in);
            showMessage(sender.getInetAddress(), message);
        } catch (IOException e) {
            // TODO write error recovery code
            System.out.println(":: Can't retrieve message");
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieve the whole message, rather than retrieving a line.
     * @param in BufferedReader instance to read from
     * @return the message in the specified BufferedReader instance
     * @throws IOException 
     */
    private String getWholeMessage(BufferedReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        String input = in.readLine();
        while (input != null) {
            sb.append(input);
            sb.append(String.format("%n"));
            input = in.readLine();
        }
        return sb.toString();
    }
    
    /**
     * Display the retrieved message according to the implementation. This
     * implementation simple add the message to a queue.
     * @param senderIp IP adress of the sender
     * @param message the message to display
     */
    private void showMessage(InetAddress senderIp, String message) {
        uiMessageQueue.add(senderIp.getHostAddress() + ": " + message);
    }

}
