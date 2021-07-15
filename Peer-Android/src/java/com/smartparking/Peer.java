package com.smartparking;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.sql.Timestamp;

public class Peer extends Thread{
    public static final int numParks = 2;	// Each peer knows how many parking spot there are
    public static final int threshold = 10; // Threshold 10ms

    // Information about this peer
    private String address;		// Its address
    private int port;			// Its port
    private boolean status;		// Its status (if it's inside or not)
    private int countInside;
    private Timestamp timestamp;
    private boolean close;
    private TextView gui;
    private Button enter, exit, stop;

    // Constructor
    public Peer(EditText address, EditText port, TextView gui, Button enter, Button exit, Button stop){
        this.address = address.getText().toString().trim();
        this.port = Integer.parseInt(port.getText().toString().trim());
        this.status = false;
        this.close = false;
        this.countInside = 0;
        this.timestamp = null;
        this.gui = gui;
        this.enter = enter;
        this.exit = exit;
        this.stop = stop;
    }

    // Method used to set its status
    public void setStatus(boolean status){this.status = status;}

    // Method used to know its status
    public boolean getStatus(){return status;}

    // Method used to know its address
    public String getAddress(){return this.address;}

    // Method used to know its port
    public int getPort(){return this.port;}

    public void termination(){this.close = true;}

    public boolean isClosed(){return this.close;}

    public void setCountInside(int countInside){this.countInside = countInside;}

    public void setTimestamp(Timestamp timestamp){this.timestamp = timestamp;}

    public Timestamp getTimestamp(){return this.timestamp;}

    public int getCountInside(){return this.countInside;}

    public String response() {
        return (this.getStatus() + ", " + this.getCountInside() + ", " + this.getTimestamp());
    }


    public void run() {
        // Peer create a peer-server
        new PeerServer(this, this.gui).start();

        // Peer create a peer-client
        new PeerClient(this, this.gui, enter, exit, stop).start();
    }
}
