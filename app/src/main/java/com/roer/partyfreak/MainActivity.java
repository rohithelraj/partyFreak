package com.roer.partyfreak;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private static BluetoothSocket mainSocket;
    private static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        ColorPicker picker = (ColorPicker) findViewById(R.id.picker);
        SVBar svBar = (SVBar) findViewById(R.id.svbar);
        ValueBar valueBar = (ValueBar) findViewById(R.id.valuebar);
        picker.addSVBar(svBar);
        picker.addValueBar(valueBar);
        //To get the color
        picker.getColor();
        //To set the old selected color u can do it like this
        picker.setOldCenterColor(picker.getColor());
        // adds listener to the colorpicker which is implemented
        //in the activity
        //to turn of showing the old color
        picker.setShowOldCenterColor(false);
        TextView colorValue = findViewById(R.id.colorValue);
        Switch musicGhost = findViewById(R.id.musicGhost);
        Switch ledControl = findViewById(R.id.ledControl);
        ledControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked){
                if(musicGhost.isChecked()){
                    setColorInDevice("0.0.0)1)");
                }
                else{
                    setColorInDevice("0.0.0)0)");
                }

            }
        });

        musicGhost.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked){
                if(ledControl.isChecked()){
                    String rgbValue = Color.red(picker.getColor())+"."+ Color.green(picker.getColor())+"."+Color.blue(picker.getColor());
                    setColorInDevice(rgbValue+")0)");
                }
                else{
                    setColorInDevice("0.0.0)0)");
                }
            }
            else{
                if(ledControl.isChecked()){
                    String rgbValue = Color.red(picker.getColor())+"."+ Color.green(picker.getColor())+"."+Color.blue(picker.getColor());
                    setColorInDevice(rgbValue+")1)");
                }
                else{
                    setColorInDevice("0.0.0)1)");
                }
            }
        });
        ConstraintLayout mainLayout = findViewById(R.id.mainLayout);
        colorValue.setText(""+picker.getColor());
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String rgbValue = Color.red(picker.getColor())+"."+ Color.green(picker.getColor())+"."+Color.blue(picker.getColor());

                colorValue.setText(rgbValue);
                if(ledControl.isChecked()){
                    if(musicGhost.isChecked()){
                        setColorInDevice(rgbValue+")1)");
                    }
                    else{
                        setColorInDevice(rgbValue+")0)");
                    }
                }
                else{
                    if(musicGhost.isChecked()){
                        setColorInDevice("0.0.0)1)");
                    }
                    else{
                        setColorInDevice("0.0.0)0)");
                    }
                }
                //picker.getOnColorChangedListener();
                setColorInDevice(rgbValue);
            }




        });
        ImageView bluetoothDevices = findViewById(R.id.bluetoothDevices);
        bluetoothDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBluettothClick();
            }
        });
    }
    public void setColorInDevice(String rgbValue) {
        if(mainSocket.isConnected()){
            new ConnectedThread(mainSocket,rgbValue).run();
        }
        else{
            onBluettothClick();
        }
    }

    public void onBluettothClick(){
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == 1){
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    if(deviceName.equalsIgnoreCase("HC-06")){
                        new ConnectThread(device).run();
                    }
                }
            }
        }
        else{

        }
    }



    private class ConnectThread extends Thread {

        private final BluetoothDevice mmDevice;
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        private byte[] mmBuffer;
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;


            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                ParcelUuid[] idArray = device.getUuids();
                java.util.UUID uuidYouCanUse = java.util.UUID.fromString(idArray[0].toString());
                tmp = device.createRfcommSocketToServiceRecord(uuidYouCanUse);
            } catch (IOException e) {
                Log.e("ss", "Socket's create() method failed", e);
            }
            mainSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mainSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mainSocket.close();
                } catch (IOException closeException) {
                    Log.e("ss", "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with

            new ConnectedThread(mainSocket,"0.0.0)").run();
            // the connection in a separate thread.
        }



        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mainSocket.close();
            } catch (IOException e) {
                Log.e("ss", "Could not close the client socket", e);
            }
        }
    }

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private String mrgbValue;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket,String rgbValue) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            mrgbValue = rgbValue;
            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e("cc", "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("cc", "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void read(){
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.

                } catch (IOException e) {
                    Log.d("cc", "Input stream was disconnected", e);
                    break;
                }
            }
        }
        public void run() {

            write(mrgbValue.getBytes());
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

            } catch (IOException e) {
                Log.e("cc", "Error occurred when sending data", e);

            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("cc", "Could not close the connect socket", e);
            }
        }
    }
}