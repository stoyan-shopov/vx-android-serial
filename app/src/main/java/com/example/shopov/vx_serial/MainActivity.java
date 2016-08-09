package com.example.shopov.vx_serial;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static private UsbSerialPort port = null;

    private Handler readVxHandler = new Handler() {
        @Override
        public final void handleMessage(Message msg) {
            // Update user interface

            ((EditText)findViewById(R.id.editTextVxLog)).append(":");
            ((EditText)findViewById(R.id.editTextVxLog)).append((String) msg.obj);
        }
    };

    Runnable runnableReadVxThread = new Runnable() {
        public void run() {
            // Insert network call here!

            Log.e("shopov", "shopov communicating with the vx");

// Read some data! Most have just one port (port 0).
            //synchronized (port)
            {
                try {
                    int i = 0;

                    do {
                        byte[] response = new byte[128];

                        Log.e("shopov", "shopov xxx");
                        i = port.read(response, 2000);
                        Log.e("shopov", "shopov yyy, read: " + i);

                        Message msg = Message.obtain();
                        msg.obj = new String(Arrays.copyOf(response, i), "UTF-8");
                        readVxHandler.sendMessage(msg);
                    }
                    while (i > 0);

                } catch (Exception e) {
                    Log.e("shopov", "shopov exception");
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void handleConnect(View view) {

        String btext = new String("???");
        Log.e("shopov", "shopov communicating with the vx");


        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);


        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x1ad4, 0xb000, CdcAcmSerialDriver.class);

        UsbSerialProber prober = new UsbSerialProber(customTable);
        List<UsbSerialDriver> drivers = prober.findAllDrivers(manager);

        List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            ((Button)findViewById(R.id.button)).setText("no devices");
            return;
        }

// Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            ((Button)findViewById(R.id.button)).setText("need permissions?");
            return;
        }

// Read some data! Most have just one port (port 0).
        port = driver.getPorts().get(0);
        try {
            int i = 0;
            port.open(connection);

        } catch (IOException e) {
            e.printStackTrace();
            ((Button)findViewById(R.id.button)).setText("error accessing port");
            return;
        }
        ((Button)findViewById(R.id.button)).setText("connected");
        Thread mythread = new Thread(runnableReadVxThread);
        mythread.start();
    }

    public void handleSend(View view) {
        //synchronized (port)
        {
            try {
                port.write(new String(((EditText) findViewById(R.id.editTextVxDataToSend)).getText().toString() + "\n").getBytes(), 2000);
                ((EditText) findViewById(R.id.editTextVxDataToSend)).getText().clear();
            } catch (Exception e) {
                Log.e("shopov", "shopov exception");
                e.printStackTrace();
            }
        }
    }
}
