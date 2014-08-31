package com.example.voituxandroid;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.freedesktop.gstreamer.GStreamer;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Joystick.JoystickMovedListener;
import com.example.Joystick.JoystickView;
import com.example.voituxandroid.R;

public class DriveActivity extends Activity implements SurfaceHolder.Callback {
    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeSetAdress(String adressCamera, String portCamera); // initialise l'adresse et le port
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private long native_custom_data;      // Native code will use this to keep private data

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING


    TextView txtX, txtY, txtangle;
    JoystickView joystick ,joystick2;
    
    private Socket socket = null;
    Thread cThread = null;
    private static int SERVERPORT = 0;
    private static String SERVER_IP = null;
//    Camera
    private static String SERVERPORT_CAMERA = null;
    private static String SERVER_IP_CAMERA = null;



    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish(); 
            return;
        }

        setContentView(R.layout.drive_view);

        ImageButton play = (ImageButton) this.findViewById(R.id.button_play);
        play.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                is_playing_desired = true;
                nativePlay();
            }
        });

        ImageButton pause = (ImageButton) this.findViewById(R.id.button_stop);
        pause.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                is_playing_desired = false;
                nativePause();
            }
        });

        SurfaceView sv = (SurfaceView) this.findViewById(R.id.surface_video);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);

        if (savedInstanceState != null) {
            is_playing_desired = savedInstanceState.getBoolean("playing");
            Log.i ("GStreamer", "Activity created. Saved state is playing:" + is_playing_desired);
        } else {
            is_playing_desired = false;
            Log.i ("GStreamer", "Activity created. There is no saved state, playing: false");
        }

        // Start with disabled buttons, until native code is initialized
        this.findViewById(R.id.button_play).setEnabled(false);
        this.findViewById(R.id.button_stop).setEnabled(false);
        
        
        // on recupére les données de l'activity precedante 
        Intent intent = getIntent();
        TextView ipAdress = (TextView)findViewById(R.id.ipAdress);
        
        if (intent != null) {
            SERVERPORT = intent.getIntExtra("SERVERPORT", 0);
            SERVER_IP = intent.getStringExtra("SERVER_IP");
            
            SERVERPORT_CAMERA = intent.getStringExtra("SERVERPORT_CAMERA");
            SERVER_IP_CAMERA = intent.getStringExtra("SERVER_IP_CAMERA");
            
            ipAdress.setText(SERVER_IP+":"+ SERVERPORT );

         }
        
        
        
        nativeSetAdress(SERVER_IP_CAMERA,SERVERPORT_CAMERA);
        nativeInit();



    }



    @Override
    public void onStart(){
        super.onStart();
    
    
    cThread = new Thread(new ClientThread());
    cThread.start();
    
    txtX = (TextView)findViewById(R.id.TextViewX);
    txtY = (TextView)findViewById(R.id.TextViewY);
    // Horisontal
    joystick = (JoystickView)findViewById(R.id.joystickView);
    joystick.contrainteType = "Horisontal";
    joystick.setOnJostickMovedListener(_listener);
    
    // Vertical
    joystick2 = (JoystickView)findViewById(R.id.joystickView2);
    joystick2.contrainteType = "Vertical";
    joystick2.setOnJostickMovedListener(_listener2);
  
    }





    
    protected void onSaveInstanceState (Bundle outState) {
        Log.d ("GStreamer", "Saving state, playing:" + is_playing_desired);
        outState.putBoolean("playing", is_playing_desired);
    }

    protected void onDestroy() {
//        nativeFinalize();
        super.onDestroy();
    }

    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {
        final TextView tv = (TextView) this.findViewById(R.id.textview_message);
        runOnUiThread (new Runnable() {
          public void run() {
            tv.setText(message);
          }
        });
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized () {
        Log.i ("GStreamer", "Gst initialized. Restoring state, playing:" + is_playing_desired);
        // Restore previous playing state
        if (is_playing_desired) {
            nativePlay();
        } else {
            nativePause();
        }

        // Re-enable buttons, now that GStreamer is initialized
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            public void run() {
                activity.findViewById(R.id.button_play).setEnabled(true);
                activity.findViewById(R.id.button_stop).setEnabled(true);
            }
        });
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("VoiTux");
        nativeClassInit();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit (holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyed");
        nativeSurfaceFinalize ();
    }




   private JoystickMovedListener _listener = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt, int angle) {
            txtX.setText(Integer.toString(pan));
//          evoieString("X:'"+Integer.toString(pan)+"'");
            evoieJson("X",Integer.toString(pan));
        }

        @Override
        public void OnReleased() {
            txtX.setText("released");
//          evoieString("X:'"+"released");
            evoieJson("X","released");
        }
        
        public void OnReturnedToCenter() {
            txtX.setText("stopped");
//          evoieString("X:'"+"stopped"+"'");
            evoieJson("X","stopped");
        };
    }; 
    private JoystickMovedListener _listener2 = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt, int angle) {
            txtY.setText(Integer.toString(tilt));
//          evoieString("Y:'"+Integer.toString(tilt)+"'");
            evoieJson("Y",Integer.toString(tilt));
        }

        @Override
        public void OnReleased() {
            txtY.setText("released");
//          evoieString("Y:'"+"released"+"'");
            evoieJson("Y","released");
        }
        
        public void OnReturnedToCenter() {
            txtY.setText("stopped");
//          evoieString("Y:'"+"stopped"+"'");
            evoieJson("Y","stopped");
        };
    };
    
    
    public void evoieString(String message) {
//      TextView terminal = null;
        try {
//          EditText et = (EditText) findViewById(R.id.EditText01);
//          String str = et.getText().toString();
            
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            out.println(message);
            out.flush();
//          terminal = (TextView)findViewById(R.id.terminal);
//          CharSequence backT = terminal.getText();
//
//          terminal.setText(backT+"this >"+message+"\n");
//          
//          // on vite et
//          et.setText("");
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void evoieJson(String attr, String value) {
//      TextView terminal = null;
        try {
//          EditText et = (EditText) findViewById(R.id.EditText01);
//          String str = et.getText().toString();
            
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            JSONObject json = new JSONObject();
            json.put(attr, value);
            
//          out.write(json.toString());
//          out.write("[{\""+ attr +"\":\""+ value +"\"}]");
            out.println("<servo id='"+ attr +"'>"+ value +"</servo>");
//          out.println(json);
            out.flush();
//          terminal = (TextView)findViewById(R.id.terminal);
//          CharSequence backT = terminal.getText();

//          terminal.setText(backT+"this >"+message+"\n");
//          
//          // on vite et
//          et.setText("");
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        
        
        
    }
    
    
    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket(serverAddr, SERVERPORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
            	cThread = null;
            }
            

        }
    }
    
//    Pour revenir a la page d'init quand on quite l'app 
//    @Override
//       protected void onStop() {
//        super.onStop();
//        finish();
////      onBackPressed();
//        try {
//            // make sure you close the socket upon exiting
//        	cThread.interrupt();
//            socket.close();
//            onBackPressed();
//            nativeFinalize();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        // Normalement dans le onDestroy. Je le met ici car on  quite gstreamer 
////        nativeFinalize();
//      }


    
    
}
