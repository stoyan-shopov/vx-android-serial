package com.example.shopov.vx_serial;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private Handler readVxHandler = new Handler() {
        @Override
        public final void handleMessage(Message msg) {
            // Update user interface
            ((EditText)findViewById(R.id.editTextVxLog)).append("handler invoked");
        }
    };

    Runnable runnable = new Runnable() {
        public void run() {
            // Insert network call here!



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
                //((Button)findViewById(R.id.button)).setText("no devices");
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
            UsbSerialPort port = driver.getPorts().get(0);
            try {
                int i = 0;
                port.open(connection);
                if (true) {
                    //((Button) findViewById(R.id.button)).setText("trying to write");
                    port.write(new String("shopov\n").getBytes(), 2000);
                }

                if (false) do {
                    byte[] response = new byte[128];
                    ((Button) findViewById(R.id.button)).setText("trying to read");

                    Log.e("shopov", "shopov xxx");
                    i = port.read(response, 2000);
                    Log.e("shopov", "shopov yyy, read: " + i);
                    if (i == 0)
                        btext += new String("no data");
                    else
                        btext += new String(Arrays.copyOf(response, i), "UTF-8");
                    xxx++;
                }
                while (i > 0);

                //((Button)findViewById(R.id.button)).setText(btext);

            } catch (IOException e) {
                e.printStackTrace();
                //((Button)findViewById(R.id.button)).setText("error accessing port");
            }

            readVxHandler.sendEmptyMessage(0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private static int xxx = 0;

    public void handleConnect(View view) {

        Thread mythread = new Thread(runnable);
        mythread.start();
    }

    public void handleSend(View view) {
    }
}
