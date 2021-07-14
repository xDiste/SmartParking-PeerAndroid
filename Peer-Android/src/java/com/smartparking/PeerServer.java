package com.smartparking;

import android.widget.TextView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PeerServer extends Thread{
    private ServerSocket serverSocket;				// Socket that serves to receive the requests
    private ThreadPoolExecutor threadPool;			// Threadpool for handle the requests
    private Peer peer;								// It needs to get information about status, address and port
    private TextView gui;

    // Constructor
    public PeerServer(Peer peer, TextView gui) {
        this.peer = peer;
        this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.gui = gui;
    }

    // Method in order to shutdown the servers
    public void Stop() {
        try {
            this.serverSocket.close();	// close the socket that handle the requests
            this.threadPool.shutdown();	// end the current transmission

            if (!this.threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                this.threadPool.shutdownNow(); // cancel currently executing tasks
            }
        } catch (Exception e) {
            gui.append("\nError closing server");
        }
    }

    public void run(){
        try {
            // Open the socket in order to receive the requests
            this.serverSocket = new ServerSocket(this.peer.getPort());
            this.serverSocket.setSoTimeout(20*1000);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        while (!this.peer.isClosed()) {
            try {
                Socket clientSocket = this.serverSocket.accept();	// Wait until a request is found
                // create thread to execute the request
                threadPool.execute(new PeerServerThread(clientSocket, peer));
            } catch (IOException ignored) {
            }
        }
        gui.append("\nClosing peer-server...");
        this.Stop();
        gui.append("\nPeer-server closed.");
    }
}
