/*
 */

package messengerapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Shung-Hsi Yu <syu07@nyit.edu> ID#0906172
 */
public class TcpReceiverTest {
    
    public TcpReceiverTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class TcpReceiver.
     */
    @Test(timeout = 1000)
    public void testRun() {
        System.out.println("run");
        String message = "test TcpReceiver";
        Queue uiMessageQueue = new ArrayBlockingQueue(50);
        TcpReceiver instance = new TcpReceiver(uiMessageQueue);
        Thread receiverThread = new Thread(instance);
        receiverThread.setDaemon(true);
        receiverThread.start();
        
        while(!instance.isReady()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {}
        }
        
        InetAddress ip = InetAddress.getLoopbackAddress();
        try (Socket senderSocket = new Socket(ip, TcpReceiver.PORT);
                PrintWriter out = 
                        new PrintWriter(senderSocket.getOutputStream())
                ) {
            out.println(message);
        } catch (IOException ex) {
            //
        }
        
        while(uiMessageQueue.isEmpty()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {}
        }
        
        String assertMessage = 
                String.format("%s: %s%n", ip.getHostAddress(), message);
        assertEquals(assertMessage, uiMessageQueue.peek());
        
        instance.close();
        receiverThread.interrupt();
    }
    
    /**
     * Test of run method, of class TcpReceiver.
     */
    @Test(timeout = 1000)
    public void testRunMultiLine() {
        System.out.println("runMultiLine");
        String message1 = "1. test TcpReceiver";
        String message2 = "2. hi";
        String message3 = "3.";
        Queue uiMessageQueue = new ArrayBlockingQueue(50);
        TcpReceiver instance = new TcpReceiver(uiMessageQueue);
        Thread receiverThread = new Thread(instance);
        receiverThread.setDaemon(true);
        receiverThread.start();
        
        while(!instance.isReady()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {}
        }
        
        InetAddress ip = InetAddress.getLoopbackAddress();
        try (Socket senderSocket = new Socket(ip, TcpReceiver.PORT);
                PrintWriter out = 
                        new PrintWriter(senderSocket.getOutputStream())
                ) {
            out.println(message1);
            out.println(message2);
            out.println(message3);
        } catch (IOException ex) {
            //
        }
        
        while(uiMessageQueue.isEmpty()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {}
        }
        
        String assertMessage = 
                String.format("%s: %s%n%s%n%s%n", ip.getHostAddress(), message1, 
                        message2, message3);
        String gotMessage = (String) uiMessageQueue.poll();
        assertEquals(assertMessage, gotMessage);
        
        instance.close();
        receiverThread.interrupt();
    }
    
    /**
     * Test of run method, of class TcpReceiver.
     */
    @Test(timeout = 1000)
    public void testRunMultiple() {
        System.out.println("runMulitiple");
        String message = "test TcpReceiver";
        Queue uiMessageQueue = new ArrayBlockingQueue(50);
        TcpReceiver instance = new TcpReceiver(uiMessageQueue);
        Thread receiverThread = new Thread(instance);
        receiverThread.setDaemon(true);
        receiverThread.start();
        
        while(!instance.isReady()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {}
        }
        
        InetAddress ip = InetAddress.getLoopbackAddress();
        try (Socket senderSocket1 = new Socket(ip, TcpReceiver.PORT);
                PrintWriter out = 
                        new PrintWriter(senderSocket1.getOutputStream())
                ) {
            out.println(message + "1");
        } catch (IOException ex) {
            //
        }
        
        try (Socket senderSocket2 = new Socket(ip, TcpReceiver.PORT);
                PrintWriter out = 
                        new PrintWriter(senderSocket2.getOutputStream())
                ) {
            out.println(message + "2");
        } catch (IOException ex) {
            //
        }
        
        while(uiMessageQueue.isEmpty()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {}
        }
        
        String assertMessage1 = 
                String.format("%s: %s1%n", ip.getHostAddress(), message);
        String assertMessage2 = 
                String.format("%s: %s2%n", ip.getHostAddress(), message);
        String assertMessageAltern = "";
        
        String gotMessage1 = (String) uiMessageQueue.poll();
        if(assertMessage1.equals(gotMessage1)) {
            System.out.println("::  assertMessage1 = " + assertMessage1);
            System.out.println("::  gotMessage1 = " + gotMessage1);
            assertEquals(assertMessage1, gotMessage1);
            assertMessageAltern = assertMessage2;
        } else if (assertMessage2.equals(gotMessage1)) {
            System.out.println("::  assertMessage2 = " + assertMessage2);
            System.out.println("::  gotMessage1 = " + gotMessage1);
            assertEquals(assertMessage2, gotMessage1);
            assertMessageAltern = assertMessage1;
        } else {
            fail();
        }
        
        
        
        while(uiMessageQueue.isEmpty()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {}
        }
        
        String gotMessage2 = (String) uiMessageQueue.poll();
        System.out.println("::  assertMessageAltern = " + assertMessageAltern);
        System.out.println("::  gotMessage2 = " + gotMessage2);
        assertEquals(assertMessageAltern, gotMessage2);
        
        instance.close();
        receiverThread.interrupt();
    }
}
