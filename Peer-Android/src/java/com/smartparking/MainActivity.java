package com.smartparking;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button buttonStart, buttonEnter, buttonExit;
    EditText address, port;
    TextView informations;
    Peer peer;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart = findViewById(R.id.buttonStart);
        buttonEnter = findViewById(R.id.buttonEnter);
        buttonExit = findViewById(R.id.buttonExit);
        informations = findViewById(R.id.Informations);
        informations.setMovementMethod(new ScrollingMovementMethod());

        // Listener for start and stop
        buttonStart.setOnClickListener(this::onClick);
    }

    private void onClick(View view) {
        if (buttonStart.getText().equals("Start")) {
            address = findViewById(R.id.AddressInput);
            port = findViewById(R.id.portInput);
            buttonStart.setText("Stop");
            buttonEnter.setEnabled(true);
            // Call a creation of peer
            peer = new Peer(address, port, informations, buttonEnter, buttonExit, buttonStart);
            peer.start();
        }
        else{
            // Stop peer
            peer.termination();
            buttonEnter.setEnabled(false);
            buttonExit.setEnabled(false);
            buttonStart.setText("Start");
        }
    }
}