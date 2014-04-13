/*
 */
package messengerapp;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.extensions.PA;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Shung-Hsi Yu <syu07@nyit.edu> ID#0906172
 */
public class TcpSenderTest {

    public static final String messageQueueField = "messageQueue";
    public static final String possibleReceiverQueueField
            = "possibleReceiverQueue";
    public static final String connectedSocketListField = "connectedSocketList";
    public static final String checkReceiverQueueMethod
            = "checkReceiverQueue()";
    public static final String checkMessageQueueMethod = "checkMessageQueue()";
    public static final String removeAllSocketsMethod = "removeAllSockets()";

    public TcpSenderTest() {

    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Test of run method, of class TcpSender.
     */
    /*@Test
    public void testRun() {
        System.out.println("run");
        TcpSender instance = new TcpSender();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of checkReceiver method, of class TcpSender.
     */
    @Test(timeout = 1000)
    public void testCheckReceiverQueue() {
        System.out.println("checkReceiverQueue");
        TcpSender instance = new TcpSender();
        InetAddress ip = InetAddress.getLoopbackAddress();
        SimpleReceiver simpleReceiver = new SimpleReceiver();
        Thread receiverThread = new Thread(simpleReceiver);
        receiverThread.setDaemon(true);
        receiverThread.start();
        
        while(!simpleReceiver.isReady()) {
            threadSleep(50);
        }

        instance.addReceiver(ip);
        PA.invokeMethod(instance, checkReceiverQueueMethod);

        Queue possibleReceiverQueue = (Queue) PA.getValue(instance,
                possibleReceiverQueueField);
        assertTrue(possibleReceiverQueue.isEmpty());

        List connectedSocketList = (List) PA.getValue(instance, 
                connectedSocketListField);
        assertEquals(1, connectedSocketList.size());
        Socket connectSocket = (Socket) connectedSocketList.get(0);
        assertEquals(ip, connectSocket.getInetAddress());

        PA.invokeMethod(instance, removeAllSocketsMethod);
        simpleReceiver.close();
        receiverThread.interrupt();
    }

    /**
     * Test of checkReceiver method, of class TcpSender.
     */
    @Test(timeout = 1000)
    public void testCheckReceiverQueueEmpty() {
        System.out.println("checkReceiverQueueEmpty");
        TcpSender instance = new TcpSender();
        InetAddress ip = InetAddress.getLoopbackAddress();

        PA.invokeMethod(instance, checkReceiverQueueMethod);

        Queue possibleReceiverQueue = (Queue) PA.getValue(instance,
                possibleReceiverQueueField);
        assertTrue(possibleReceiverQueue.isEmpty());

        List connectedSocketList = (List) PA.getValue(instance, connectedSocketListField);
        assertTrue(connectedSocketList.isEmpty());

        PA.invokeMethod(instance, removeAllSocketsMethod);
    }

    /**
     * Test of checkReceiver method, of class TcpSender.
     */
    @Test(timeout = 1000)
    public void testCheckReceiverQueueMultiple() {
        System.out.println("checkReceiverQueueMultiple");
        TcpSender instance = new TcpSender();
        InetAddress ip = InetAddress.getLoopbackAddress();
        SimpleReceiver simpleReceiver = new SimpleReceiver();
        Thread receiverThread = new Thread(simpleReceiver);
        receiverThread.setDaemon(true);
        receiverThread.start();
        
        while(!simpleReceiver.isReady()) {
            threadSleep(50);
        }

        instance.addReceiver(ip);
        instance.addReceiver(ip);
        instance.addReceiver(ip);
        instance.addReceiver(ip);
        instance.addReceiver(ip);
        PA.invokeMethod(instance, checkReceiverQueueMethod);

        Queue possibleReceiverQueue = (Queue) PA.getValue(instance,
                possibleReceiverQueueField);
        assertTrue(possibleReceiverQueue.isEmpty());

        List connectedSocketList = (List) PA.getValue(instance, connectedSocketListField);
        assertEquals(1, connectedSocketList.size());

        Socket connectSocket = (Socket) connectedSocketList.get(0);
        assertEquals(ip, connectSocket.getInetAddress());

        PA.invokeMethod(instance, removeAllSocketsMethod);
        simpleReceiver.close();
        receiverThread.interrupt();
    }

    private static class SimpleReceiver implements Runnable, Closeable {
        private ServerSocket receiver = null;
        private boolean isReady = false;
        public boolean isReady() {
            return this.isReady;
        }
        @Override
        public void run() {
            try (ServerSocket receiver = new ServerSocket(TcpReceiver.PORT)) {
                this.receiver = receiver;
                this.isReady = true;
                while(!Thread.currentThread().isInterrupted()) {
                    receiver.accept();
                }
            } catch (SocketException ex) {
                // Do nothing if the socket is closed
                System.out.println(":: socket closed");
            } catch (IOException ex) {
                ex.printStackTrace();
                fail("::Socket is already in use!");
            }
        }

        @Override
        public void close(){
            try {
                this.receiver.close();
            } catch (IOException ex) {}
        }
    }

    /**
     * Test of checkMessageQueue method, of class TcpSender.
     */
    @Test(timeout = 2000)
    public void testCheckMessageQueue() {
        System.out.println("checkMessageQueue");
        String message = "testing checkMessageQueue";
        TcpSender instance = new TcpSender();
        InetAddress ip = InetAddress.getLoopbackAddress();
        messageReceiver messageReceiver = new messageReceiver();
        Thread receiverThread = new Thread(messageReceiver);
        receiverThread.setDaemon(true);
        receiverThread.start();
        
        while(!messageReceiver.isReady()) {
            threadSleep(50);
        }

        try (Socket receiverSocket = new Socket(ip, TcpReceiver.PORT)) {
            List connectedSocketList = (List) PA.getValue(instance, 
                connectedSocketListField);
            connectedSocketList.add(receiverSocket);
            instance.send(message);
            PA.invokeMethod(instance, checkMessageQueueMethod);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("::Can't create socket");
        }

        String receivedMessage = "";
        while(!messageReceiver.hasMessage()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {}
        }
        
        receivedMessage = messageReceiver.getReceivedMessage();
        System.out.println(":: assertReceivedMessage = " + receivedMessage);
        assertEquals(message, receivedMessage);
        
        PA.invokeMethod(instance, removeAllSocketsMethod);
        receiverThread.interrupt();
        messageReceiver.close();
    }
    
    /**
     * Test of checkMessageQueue method, of class TcpSender.
     */
    @Test(timeout = 2000)
    public void testCheckMessageQueueMultiple() {
        System.out.println("checkMessageQueueMultiple");
        String message = "testing checkMessageQueue";
        TcpSender instance = new TcpSender();
        InetAddress ip = InetAddress.getLoopbackAddress();
        messageReceiver messageReceiver = new messageReceiver();
        Thread receiverThread = new Thread(messageReceiver);
        receiverThread.setDaemon(true);
        receiverThread.start();
        
        while(!messageReceiver.isReady()) {
            threadSleep(50);
        }

        try (Socket receiverSocket = new Socket(ip, TcpReceiver.PORT)) {
            List connectedSocketList = (List) PA.getValue(instance, 
                connectedSocketListField);
            connectedSocketList.add(receiverSocket);
            instance.send(message);
            instance.send(message);
            instance.send(message);
            PA.invokeMethod(instance, checkMessageQueueMethod);
            PA.invokeMethod(instance, checkMessageQueueMethod);
            PA.invokeMethod(instance, checkMessageQueueMethod);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("::Can't create socket");
        }

        String receivedMessage = "";
        while(!messageReceiver.hasMessage()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {}
        }
        
        receivedMessage = messageReceiver.getReceivedMessage();
        String assertMessage = String.format("%1$s%1$s%1$s", message);
        System.out.println(":: assertReceivedMessage = " + 
                receivedMessage);
        assertEquals(assertMessage, receivedMessage);
        
        PA.invokeMethod(instance, removeAllSocketsMethod);
        receiverThread.interrupt();
        messageReceiver.close();
    }
    
    /**
     * Test of checkMessageQueue method, of class TcpSender.
     */
    @Test(timeout = 5000)
    public void testCheckMessageQueueClosed() {
        System.out.println("checkMessageQueueClosed");
        String message = "testing checkMessageQueue";
        TcpSender instance = new TcpSender();
        InetAddress ip = InetAddress.getLoopbackAddress();
        messageReceiver messageReceiver = new messageReceiver();
        Thread receiverThread = new Thread(messageReceiver);
        receiverThread.start();
        
        while(!messageReceiver.isReady()) {
            threadSleep(50);
        }

        try (Socket receiverSocket = new Socket(ip, TcpReceiver.PORT)) {
            List connectedSocketList = (List) PA.getValue(instance, 
                connectedSocketListField);
            connectedSocketList.add(receiverSocket);
            instance.send(message);
            PA.invokeMethod(instance, checkMessageQueueMethod);
            receiverThread.interrupt();
            messageReceiver.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("::Can't create socket");
        }

        while(!messageReceiver.hasMessage()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {}
        }
        
        String receivedMessage = messageReceiver.getReceivedMessage();
        System.out.println(":: assertReceivedMessage = " + receivedMessage);
        assertEquals(message, receivedMessage);
        
        PA.invokeMethod(instance, removeAllSocketsMethod);
    }
        
    private static class messageReceiver implements Runnable, Closeable {
        private ServerSocket receiver;
        private Socket receiverSocket;
        private String receviedMessage;
        private boolean hasMessage;
        private boolean isReady;

        public messageReceiver() {
            this.receviedMessage = "";
            this.hasMessage = false;
            this.isReady = false;
        }
        
        public boolean isReady() {
            return this.isReady;
        }
        
        public boolean hasMessage() {
            return this.hasMessage;
        }

        public synchronized String getReceivedMessage() {
            return this.receviedMessage;
        }
        
        public synchronized void setReceivedMessage(String receviedMessage) {
            this.receviedMessage = receviedMessage;
        }

        @Override
        public void run() {
            try (ServerSocket receiver = new ServerSocket(TcpReceiver.PORT)
                    ) {
                this.receiver = receiver;
                this.isReady = true;
                while(!Thread.currentThread().isInterrupted()) {
                    try (Socket receiverSocket = receiver.accept();
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                        receiverSocket.getInputStream()))
                        ) {
                        this.receiverSocket = receiverSocket;
                        String input = in.readLine();
                        while (input != null) {
                            System.out.println(":: oldReceivedMessage = " + getReceivedMessage());
                            System.out.println(":: input = " + input);
                            String oldReceivedMessage = getReceivedMessage();
                            setReceivedMessage(oldReceivedMessage + input);
                            System.out.println(":: newReceivedMessage = " + getReceivedMessage());
                            input = in.readLine();
                        }
                        System.out.println(":: message ready");
                        hasMessage = true;
                    }
                }
            } catch (SocketException ex) {
                // Do nothing when socket is closed
                System.out.println(":: Socket closed");
            } catch (IOException ex) {
                fail("::Can't receive message");
                ex.printStackTrace();
            }
        }

        @Override
        public void close() {
            try {
                receiverSocket.close();
                receiver.close();
            } catch (IOException ex) {
            } catch (NullPointerException ex) {
            }
        
        }
    }

    /**
     * Test of send method, of class TcpSender.
     */
    @Test
    public void testSend() {
        System.out.println("send");
        String message = "test message";
        TcpSender instance = new TcpSender();
        instance.send(message);
        Queue messageQueue = (Queue) PA.getValue(instance,
                messageQueueField);
        assertEquals(message, messageQueue.peek());
    }

    /**
     * Test of send method, of class TcpSender.
     */
    @Test(expected = NoSuchElementException.class)
    public void testSendFail() {
        System.out.println("sendFail");
        String message = "test message";
        TcpSender instance = new TcpSender();
        Queue messageQueue = (Queue) PA.getValue(instance,
                messageQueueField);
        messageQueue.remove();
    }

    /**
     * Test of addReceiver method, of class TcpSender.
     */
    @Test
    public void testAddReceiver() {
        System.out.println("addReceiver");
        InetAddress ip = InetAddress.getLoopbackAddress();
        TcpSender instance = new TcpSender();
        instance.addReceiver(ip);
        Queue possibleReceiverQueue = (Queue) PA.getValue(instance,
                possibleReceiverQueueField);
        assertEquals(ip, possibleReceiverQueue.peek());
    }

    /**
     * Test of addReceiver method, of class TcpSender.
     */
    @Test
    public void testAddReceiverMultiple() {
        System.out.println("addReceiverMultiple");
        InetAddress ip = InetAddress.getLoopbackAddress();
        TcpSender instance = new TcpSender();
        instance.addReceiver(ip);
        instance.addReceiver(ip);
        instance.addReceiver(ip);
        instance.addReceiver(ip);
        instance.addReceiver(ip);
        Queue possibleReceiverQueue = (Queue) PA.getValue(instance,
                possibleReceiverQueueField);
        assertEquals(5, possibleReceiverQueue.size());
    }

    private void threadSleep() {
        this.threadSleep(500);
    }
    
    private void threadSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
        }
    }
}
