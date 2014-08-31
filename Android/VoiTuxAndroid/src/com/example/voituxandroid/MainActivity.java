package com.example.voituxandroid;


import com.example.voituxandroid.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends Activity {



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		init();
		
		
	}
	public void init(){
		
		setContentView(R.layout.init);
		
		final EditText ipAdress = (EditText)findViewById(R.id.ipAdress);
		final EditText port = (EditText)findViewById(R.id.port);
		final EditText ipAdress_Camera = (EditText)findViewById(R.id.ipAdressCamera);
		final EditText port_Camera = (EditText)findViewById(R.id.portCamera);
		final Button initConnect = (Button)findViewById(R.id.initConnect);
		
		initConnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, DriveActivity.class);
			
			intent.putExtra("SERVER_IP", ipAdress.getText().toString());
			intent.putExtra("SERVERPORT", Integer.parseInt(port.getText().toString()) );
			intent.putExtra("SERVER_IP_CAMERA", ipAdress_Camera.getText().toString());
			intent.putExtra("SERVERPORT_CAMERA", port_Camera.getText().toString()) ;
			startActivity(intent);
			}
		});
		
	}
	
//
//	public void onClick(View view) {
//		TextView terminal = null;
//		try {
//			EditText et = (EditText) findViewById(R.id.EditText01);
//			String str = et.getText().toString();
//			PrintWriter out = new PrintWriter(new BufferedWriter(
//					new OutputStreamWriter(socket.getOutputStream())),
//					true);
//			out.println(str);
//			terminal = (TextView)findViewById(R.id.terminal);
//			CharSequence backT = terminal.getText();
//			str = "this >"+str+"\n";
//			terminal.setText(backT+str);
//			
//			// on vite et
//			et.setText("");
//			
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	class ClientThread implements Runnable {
//
//		@Override
//		public void run() {
//
//			try {
//				InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
//
//				socket = new Socket(serverAddr, SERVERPORT);
//
//			} catch (UnknownHostException e1) {
//				e1.printStackTrace();
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//
//		}
//
//	}
	
	
	
	
	
//	private ServerSocketWrapper serverSocketWrapper;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//
//		// Start listening for commands from RaspberryPi...
//		serverSocketWrapper = new ServerSocketWrapper();
//		serverSocketWrapper.startSocket();
//	}
//	
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		serverSocketWrapper.stopSocket();
//	}
}
