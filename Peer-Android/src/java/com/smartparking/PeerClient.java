package com.smartparking;

import android.widget.Button;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;

public class PeerClient extends Thread {
    private Peer peer;	// It needs to get information about status, address and port
    private TextView gui;
    private Button enter, exit, stop;

    // Information about Discovery server (change to make the app work with the server)
    private static final String discoveryAddress = "localhost";
    private static final int discoveryPort = 8080;

    private Socket discoverySocket;		// Socket that serves to connect to Discovery server
    private Socket peerServer;			// Socket that serves to connect to other peer-server

    private InetSocketAddress[] overlayNetwork;		// Array that contains the overlay network returned by Discovery server

    private int nPeers;		// Information about how many peers there are in the network

    // Constructor
    public PeerClient(Peer peer, TextView gui, Button enter, Button exit, Button stop) {
        this.peer = peer;
        this.nPeers = 0;
        this.gui = gui;
        this.enter = enter;
        this.exit = exit;
        this.stop = stop;
        overlayNetwork = new InetSocketAddress[100];	// Modify this value in order to change how many peers can be there are in the network
    }

    // Methods in order to format the request for the Discovery server
    private String formatRequest(String type, String address, int port){
        return type + "," + address + "," + port;
    }

    // Contact a discovery server in order to register this peer
    private void registerToDiscovery() throws IOException{
        gui.append("\n##########\nRegistration to Discovery server...");
        // Open the connection with Discovery
        discoverySocket = new Socket(discoveryAddress, discoveryPort);

        // Send the informations for the registration into the network
        OutputStream output = discoverySocket.getOutputStream();
        String request = formatRequest("0", peer.getAddress(), peer.getPort());
        output.write(request.getBytes());

        // Close connection with Discovery
        discoverySocket.close();
        gui.append("\nRegistration completed.");
    }

    // Contact a discovery server in order to know all information about the other peers
    private void callDiscovery() throws IOException, ClassNotFoundException {
        gui.append("\n##########\nAsk the informations about other peers...");
        // Open connection with Discovery
        discoverySocket = new Socket(discoveryAddress, discoveryPort);

        // Send the request in order to receive the overlay network
        OutputStream output = discoverySocket.getOutputStream();
        String request = formatRequest("1", peer.getAddress(), peer.getPort());
        output.write(request.getBytes());

        // Receive the overlay network from Discovery
        ObjectInputStream input = new ObjectInputStream(discoverySocket.getInputStream());
        Object obj = input.readObject();
        overlayNetwork = (InetSocketAddress[]) obj;

        // Receive the overlay network from the Discovery server
        DataInputStream in = new DataInputStream(discoverySocket.getInputStream());
        this.nPeers = in.readInt();

        // Close connection with the Discovery server
        discoverySocket.close();
        gui.append("\nRetrieved informations.");
    }

    // Ask to the other peers inside the network if they are inside the parking
    private int countInside() throws IOException, ClassNotFoundException {
        int nInside = 0;	// Variable that servers to count how many peers are inside the parking
        callDiscovery();	// Contact the Discovery in order to know who there is in the network
        DataInputStream input;
        // Contact all the peers that are inside the network
        for(int i = 0; i < this.nPeers; ++i) {
            if(overlayNetwork[i].getPort() != peer.getPort()) {
                try {
                    // Open the comunication with other peers
                    peerServer = new Socket(overlayNetwork[i].getAddress(), overlayNetwork[i].getPort());
                } catch (IOException e) {
                    continue;
                }

                // Receive the status from the other peers
                input = new DataInputStream(peerServer.getInputStream());

                // Read the answer from the other peers
                byte[] b = new byte[100];    // max length for server response (pre-established)
                input.read(b);
                String response = new String(b).trim();

                // Split the answer from each peer
                //[0] --> Status; [1] --> num of peer inside; [0] --> timestamp
                String[] parts = response.split(",");

                Timestamp now = new Timestamp(new Date().getTime());

                // If the timestamp does not exceed the threshold
                if (!parts[2].trim().equals("null") && (now.getTime() - Timestamp.valueOf(parts[2].trim()).getTime()) < Peer.threshold) {

                    this.peer.setCountInside(Integer.parseInt(parts[1].trim()));
                    // If timestamp is good I keep it the same
                    this.peer.setTimestamp(Timestamp.valueOf(parts[2].trim()));

                    gui.append(this.peer.getCountInside() + " car inside the parking");
                    return Integer.parseInt(parts[1].trim());
                } else {
                    // If it is inside I count it
                    if (parts[0].equals("true")) {
                        ++nInside;
                    }
                }
                // Close connection
                peerServer.close();
            }
        }
        this.peer.setCountInside(nInside);
        this.peer.setTimestamp(new Timestamp(new Date().getTime()));
        gui.append("\n" + nInside + " car inside the parking");
        return nInside;
    }

    // Method to enter inside the parking
    private boolean entrata() throws IOException, ClassNotFoundException {
        // If there isn't avaible parking spot: failed
        if(countInside() >= Peer.numParks) return false;
        peer.setStatus(true);// Set status equal true
        gui.append("\n##########\nEntered");
        return true;
    }

    // Method to exit from the parking
    private boolean uscita() {
        // Set status equal false
        peer.setStatus(false);
        gui.append("\nExited");
        return true;
    }

    public void run(){
        try {
            // Registration into the network
            registerToDiscovery();

            // Listener for enter when it's enable
            enter.setOnClickListener(v -> {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            if(!entrata()) gui.append("\nTry again in a few seconds");
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                exit.setEnabled(true);
                enter.setEnabled(false);
            });

            // Lister for exit when it's enable
            exit.setOnClickListener(view -> {
                if(uscita()){
                    exit.setEnabled(false);
                    enter.setEnabled(true);
                }
            });
        } catch (Exception e) {
            // errors or server closed
            gui.append("\nUnable to reach other peers");
        }
    }
}
