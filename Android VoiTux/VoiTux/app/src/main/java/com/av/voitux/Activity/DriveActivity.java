package com.av.voitux.Activity;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.av.voitux.Joystick.JoystickMovedListener;
import com.av.voitux.Joystick.JoystickView;
import com.av.voitux.Models.Connexion;
import com.av.voitux.Models.ConnexionsManager;
import com.av.voitux.voituxandroid.R;

import org.freedesktop.gstreamer.GStreamer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.UUID;


public class DriveActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = "VoiTux";
    private PrintWriter out;
    private Socket socket;
    private Thread clientThread;
    private String lastMessageSend = "";
    private Connexion connexion;

    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeSetAdress(String gs_pipeline); // initialise l'adresse et le port
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private long native_custom_data;      // Native code will use this to keep private data

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING


    TextView tv_drive_com_x,     tv_drive_com_y, txtangle;
    JoystickView joystickRight, joystickLeft;

    public static final String KEY_UUID_CONNEXION = "KEY_UUID_CONNEXION";

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("VoiTux");
        nativeClassInit();
    }

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // on récupère la connexion passée
        Intent intent = getIntent();
        if (intent == null) {
            Log.i ("GStreamer", "Erreur: intent is null");
            finish();
        }

        UUID connexionUUID = UUID.fromString(intent.getStringExtra(KEY_UUID_CONNEXION));
        connexion = ConnexionsManager.get(this).getConnexionById(connexionUUID);

        if(connexion == null) {
            Log.i ("GStreamer", "Erreur: Pas de connexion");
            finish();
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_drive);

        // Init de GStreamer
        initGStreamer(savedInstanceState);

        // Init des commandes
        initCommande();

    }


    private void  initGStreamer(Bundle savedInstanceState){

        ImageButton btn_play = (ImageButton) this.findViewById(R.id.button_play);
        ImageButton btn_pause = (ImageButton) this.findViewById(R.id.button_stop);
        SurfaceView sv = (SurfaceView) this.findViewById(R.id.gs_surface_video);

        // si la video est désactivée on n'init pas GS
        if(!connexion.isVideoAcive()) {
            btn_play.setVisibility(View.GONE);
            btn_pause.setVisibility(View.GONE);
            sv.setVisibility(View.GONE);
            return;
        }

        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Log.i ("GStreamer", "Init Exception:" + e.getMessage());
            finish();
            return;
        }

        // Init Play et pause btn

        btn_play.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                is_playing_desired = true;
                nativePlay();
            }
        });

        btn_pause.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                is_playing_desired = false;
                nativePause();
            }
        });

        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);

        if (savedInstanceState != null) {
            is_playing_desired = savedInstanceState.getBoolean("playing");
            Log.i ("GStreamer", "Activity created. Saved state is playing:" + is_playing_desired);
        } else {
            is_playing_desired = true;
            Log.i ("GStreamer", "Activity created. There is no saved state, playing: true");
        }
        // Start with disabled buttons, until native code is initialized
        btn_play.setEnabled(false);
        btn_pause.setEnabled(false);

        // Init de la pipeline GS
        nativeSetAdress(connexion.getVideoGSpipeline());
        nativeInit();

        // Ajout les infos video dans la bule d'info
        TextView ipVideo = (TextView)findViewById(R.id.ipAdressCamera);
        ipVideo.setText(connexion.getIp()+":"+ connexion.getVideoPort() );
    }


    private void initCommande(){

        TextView ipCommande = (TextView)findViewById(R.id.ipAdress);
        joystickRight = (JoystickView)findViewById(R.id.joystick_right);
        joystickLeft = (JoystickView)findViewById(R.id.joystick_left);
        tv_drive_com_x = (TextView)findViewById(R.id.tv_drive_com_x);
        tv_drive_com_y = (TextView)findViewById(R.id.tv_drive_com_y);

        // si les commandes sont désactivées on ne les affiches pas
        if(!connexion.isCommandeActive()) {
            ipCommande.setText("");
            joystickRight.setVisibility(View.GONE);
            joystickLeft.setVisibility(View.GONE);
            return;
        }

        // Ajout les infos de commande dans la bule d'info
        ipCommande.setText(connexion.getIp()+":"+ connexion.getCommandePort() );

        // Init de la connexion socket
        clientThread = new Thread(new ClientThread(connexion.getIp(), connexion.getCommandePort()));
        clientThread.start();

        // Init des joysticks
        // - Horisontal
        joystickRight.contrainteType = "Horisontal";
        joystickRight.setOnJostickMovedListener(joystickRightListener);
        // - Vertical
        joystickLeft.contrainteType = "Vertical";
        joystickLeft.setOnJostickMovedListener(joystickLeftListener);
    }


    @Override
    protected void onPause() {
        // Du moment que l'application passe en pause on ferme l'activité
        try {
            if(out != null)
                out.close();
            if(socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        nativeFinalize();
        super.onPause();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    class ClientThread implements Runnable {
        String serverIp;
        String serverport;

        public ClientThread(String serverIp, String serverport) {
            this.serverIp = serverIp;
            this.serverport  =serverport;
        }

        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIp);
                socket = new Socket();
                socket.connect(new InetSocketAddress( serverAddr, Integer.parseInt(serverport) ),1000000);
                Log.d(TAG, "Socket Conexion");

                while (!Thread.currentThread().isInterrupted()) {

                }

            } catch (Exception e) {
                Log.d(TAG, "Error Socket :", e);
            }
        }
    }


    protected void onSaveInstanceState (Bundle outState) {
        Log.d ("GStreamer", "Saving state, playing:" + is_playing_desired);
        outState.putBoolean("playing", is_playing_desired);
    }

    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {
        runOnUiThread (new Runnable() {
            public void run() {
                final Snackbar snack = Snackbar.make(findViewById(R.id.cl_drive), message, Snackbar.LENGTH_LONG);
                snack.setAction("FERMER", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snack.dismiss();
                    }
                });
                View view = snack.getView();
                CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams)view.getLayoutParams();
                params.gravity = Gravity.TOP;
                view.setLayoutParams(params);
                snack.show();
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
        try {
            if(out != null)
                out.close();
            if(socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   // Envoi de la commande par socket
    class EnvoieCommandeTask extends AsyncTask<Object, Void, Void> {

        protected Void doInBackground(Object... params) {
            try {
                String message = "<servo id='"+ params[0] +"'>"+ params[1] +"</servo>";

                // ne pas envoyer 2 fois le meme message ;)
                if(!lastMessageSend.equals(message)) {
                    lastMessageSend = message;

                    try {
                        // Create PrintWriter object for sending messages to server.
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                        if (!out.checkError()) {
                            out.println(message);
                            out.flush();
                            Log.d(TAG, "Sent Message: " + message);
                        } else {
                            Log.d(TAG, "Error Sent Message: " + message);
                        }

                    } catch (Exception e) {
                        Log.d(TAG, "Error", e);
                    }

                }
            } catch (Exception e) {
                Log.d("VoiTux", "Err EnvoieCommandeTask :"+e);

            }
            return null;
        }

    }

    // Récupère la valeur des Joystick et fait appel à une tache asynchrone pour l'envoyer
    private JoystickMovedListener joystickRightListener = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt, int angle) {
            tv_drive_com_x.setText(Integer.toString(pan));
            new EnvoieCommandeTask().execute("X",Integer.toString(pan));
        }

        @Override
        public void OnReleased() {
            tv_drive_com_x.setText("released");
            new EnvoieCommandeTask().execute("X","released");
        }

        public void OnReturnedToCenter() {
            tv_drive_com_x.setText("stopped");
            new EnvoieCommandeTask().execute("X","stopped");
        };
    };
    private JoystickMovedListener joystickLeftListener = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt, int angle) {
            tv_drive_com_y.setText(Integer.toString(tilt));
            new EnvoieCommandeTask().execute("Y",Integer.toString(tilt));
        }

        @Override
        public void OnReleased() {
            tv_drive_com_y.setText("released");
            new EnvoieCommandeTask().execute("Y","released");
        }

        public void OnReturnedToCenter() {
            tv_drive_com_y.setText("stopped");
            new EnvoieCommandeTask().execute("Y","stopped");
        };
    };






}
