package com.smartparking;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Peer extends Thread{
    public static final int numParks = 2;	// Each peer knows how many parking spot there are

    // Information about this peer
    private String address;		// Its address
    private int port;			// Its port
    private boolean inside;		// Its status (if it's inside or not)
    private boolean close;
    private TextView gui;
    private Button enter, exit, stop;

    // Constructor
    public Peer(EditText address, EditText port, TextView gui, Button enter, Button exit, Button stop){
        this.address = address.getText().toString().trim();
        this.port = Integer.parseInt(port.getText().toString().trim());
        this.inside = false;
        this.close = false;
        this.gui = gui;
        this.enter = enter;
        this.exit = exit;
        this.stop = stop;
    }

    // Method used to set its status
    public void setStatus(boolean status){this.inside = status;}

    // Method used to know its status
    public boolean getStatus(){return inside;}

    // Method used to know its address
    public String getAddress(){return this.address;}

    // Method used to know its port
    public int getPort(){return this.port;}

    public void termination(){this.close = true;}

    public boolean isClosed(){return this.close;}

    public void run() {
        // Peer create a peer-server
        new PeerServer(this, this.gui).start();

        // Peer create a peer-client
        new PeerClient(this, this.gui, enter, exit, stop).start();
    }
}
