package com.estafeta.scanner.plugin;

import static android.content.Context.BLUETOOTH_SERVICE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Set;

public class BarcodeScanner {

    private final String SCANNER_INIT     = "unitech.scanservice.init";
    private final String SCAN2KEY_SETTING = "unitech.scanservice.scan2key_setting";
    private final String START_SCANSERVICE = "unitech.scanservice.start";
    private final String CLOSE_SCANSERVICE = "unitech.scanservice.close";
    private final String SOFTWARE_SCANKEY  = "unitech.scanservice.software_scankey";
    private static CallbackListener callbackListener;
    private final  Context          context;
    private final  BarcodeReceiver  mScanReceiver       = new BarcodeReceiver();
    static        String          ACTION_RECEIVE_DATA = "unitech.scanservice.data";
    static boolean isScannerActive = false;

    public BarcodeScanner(Context context, CallbackListener listener) {
        this.context = context;
        callbackListener = listener;
    }

    public void startScanningBarcode() {
        if (this.devicesConnected()) {
            startScanner();
        } else {
            Toast.makeText(context, "No devices connected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startScanner() {
        Log.v("BarcodeScanner", "callScanner()");
        startScanService();
        setScan2Key();
        setInit();

        //start scanning
        Bundle bundle = new Bundle();
        bundle.putBoolean("scan", true);
        Intent mIntent = new Intent().setAction(SOFTWARE_SCANKEY).putExtras(bundle);
        context.sendBroadcast(mIntent);
        isScannerActive = true;

        callbackListener.onScannerStarted();
    }

    private void setScan2Key() {
        //which supports keyboard emulation features
        Bundle bundle = new Bundle();
        bundle.putBoolean("scan2key", false);
        Intent mIntent = new Intent().setAction(SCAN2KEY_SETTING).putExtras(bundle);
        context.sendBroadcast(mIntent);
    }

    private void setInit() {
        //init the scanner
        Bundle bundle = new Bundle();
        bundle.putBoolean("enable", true);
        Intent mIntent1 = new Intent().setAction(SCANNER_INIT).putExtras(bundle);
        context.sendBroadcast(mIntent1);
    }

    /**
     * Start the scan in device
     */
    private void startScanService() {
        //to start scan service
        Bundle bundle = new Bundle();
        bundle.putBoolean("close", true);
        Intent mIntent = new Intent().setAction(START_SCANSERVICE).putExtras(bundle);
        context.sendBroadcast(mIntent);
    }

    /**
     * Close the scan in device
     */
    void closeScanService() {
        //to close scan service
        isScannerActive = false;
        Bundle bundle = new Bundle();
        bundle.putBoolean("close", true);
        Intent mIntent = new Intent().setAction(CLOSE_SCANSERVICE).putExtras(bundle);
        context.sendBroadcast(mIntent);
        unregisterScannerReceiver();
    }

    public void unregisterScannerReceiver() {
        Log.v("BarcodeScanner", "unregisterScannerReceiver()");
        try {
            this.context.unregisterReceiver(mScanReceiver);
        } catch (Exception ex) {
            Log.v("BarcodeScanner", "exception: "+ex.getMessage());
        }
    }

    void registerScannerReceiver() {
        Log.v("BarcodeScanner", "registerScannerReceiver()");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECEIVE_DATA);
        this.context.registerReceiver(mScanReceiver, intentFilter);
    }

    static class BarcodeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Bundle bundle = intent.getExtras();

            if (ACTION_RECEIVE_DATA.equals(action) && isScannerActive) {
                String barcodeStr = bundle.getString("text");
                callbackListener.onSuccessRead(barcodeStr);
            }
        }
    }

    @SuppressLint("MissingPermission")
    boolean devicesConnected() {
        BluetoothManager btManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        Set<BluetoothDevice> pairedDevices = btManager.getAdapter().getBondedDevices();
        if (pairedDevices.isEmpty()) return false;

        for (BluetoothDevice device : pairedDevices) {
            if (isDeviceConnected(device)) {
                return true;
            }
        }
        return false;
    }

    boolean isDeviceConnected(BluetoothDevice device) {
        // invoke: boolean isConnected(BluetoothDevice device);
        try {
            Method m = device.getClass().getMethod("isConnected", (Class[]) null);
            boolean connected = (boolean) m.invoke(device, (Object[]) null);
            return connected;
        } catch(Exception ex) {
            Log.e("BarcodeScanner",  ex.getMessage());
            return false;
        }
    }

    interface CallbackListener {

        /**
         * This functions is called when the barcode read with success data information
         * @param value the String result
         */
        void onSuccessRead(String value);

        /**
         * Notify that the scanner is started
         */
        void onScannerStarted();
    }
}