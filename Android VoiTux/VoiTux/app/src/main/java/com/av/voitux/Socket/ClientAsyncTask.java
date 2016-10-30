package com.av.voitux.Socket;

import android.os.AsyncTask;
import android.util.Log;

import com.av.voitux.Activity.DriveActivity;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by thibaud on 27/09/16.
 */
public class ClientAsyncTask extends AsyncTask<String[], Void, Void> {
    private static final String TAG = "VoiTux";
    private PrintWriter out;

    @Override
    protected Void doInBackground(String[]... params) {

        try {
            String[] connexion = params[0];

            Log.d(TAG, "Socket: " + connexion[0].toString() +":"+ connexion[1].toString());
            InetAddress serverAddr = InetAddress.getByName(connexion[0].toString());
            Socket socket = new Socket(serverAddr, Integer.parseInt(connexion[1].toString()));

            try {
                // Create PrintWriter object for sending messages to server.
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                out.println(connexion[2].toString());
                out.flush();
                Log.d(TAG, "Sent Message: " + connexion[2].toString());

            } catch (Exception e) {

                Log.d(TAG, "Error", e);

            } finally {

                out.flush();
                out.close();
                socket.close();
                Log.d(TAG, "Socket Closed");
            }

        } catch (Exception e) {

            Log.d(TAG, "Error : "+e.getMessage(), e);

        }
        return null;
    }


}
