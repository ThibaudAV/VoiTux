package com.av.voitux.Socket;

import android.util.Log;

import com.av.voitux.Activity.MainActivity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by thibaud on 26/09/16.
 */
public class ClientThread extends Thread {

    private static final String TAG = "VoiTux";
    private String SERVER_IP;
    private String SERVERPORT;
    private PrintWriter out;

    public ClientThread(String SERVER_IP, String SERVERPORT) {
        this.SERVER_IP = SERVER_IP;
        this.SERVERPORT = SERVERPORT;
    }

    @Override
    public void run() {

        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            Socket socket = new Socket(serverAddr, Integer.parseInt(SERVERPORT));

            try {
                // Create PrintWriter object for sending messages to server.
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            } catch (Exception e) {

                Log.d(TAG, "Error", e);

            } finally {

                out.flush();
                out.close();
                socket.close();
                Log.d(TAG, "Socket Closed");
            }

        } catch (Exception e) {

            Log.d(TAG, "Error", e);

        }

    }

    /**
     * Public method for sending the message via OutputStream object.
     * @param message Message passed as an argument and sent via OutputStream object.
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
            Log.d(TAG, "Sent Message: " + message);

        }
    }
}