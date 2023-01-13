package com.estafeta.scanner.plugin;

import android.Manifest;
import android.os.Build;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class EstafetaScannerPlugin extends CordovaPlugin implements BarcodeScanner.CallbackListener {

    private CallbackContext callbackContext;
    private CallbackContext callbackScannerListener;
    private BarcodeScanner barcodeScanner;
    private String ACTION_START_SCANNER = "startScanner";
    private String ACTION_STOP_SCANNER = "stopScanner";
    private String ACTION_SCANNER_LISTENER = "scannerListener";
    private PluginResult pluginResult;
    private int BLUETOOTH_RESULT = 3001;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        barcodeScanner = new BarcodeScanner(cordova.getContext(), this);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;

        if (action != null) {
            try {
                if (ACTION_START_SCANNER.equals(action)) {
                    this.checkBluetoothConnectivity();
                }
                if (ACTION_STOP_SCANNER.equals(action)) {
                    this.stopScanner();
                }
                if (ACTION_SCANNER_LISTENER.equals(action)) {
                    this.callbackScannerListener = callbackContext;
                    this.scannerListener();
                }
            } catch (Exception ex) {
                this.callbackContext.error(ex.getMessage());
                return false;
            }
        } else {
            this.callbackContext.error("Action invalid!");
            return false;
        }

        return true;
    }

    private void scannerListener() {
        this.barcodeScanner.registerScannerReceiver();
    }

    private void checkBluetoothConnectivity() {
        if (!this.cordova.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                this.cordova.requestPermissions(this, BLUETOOTH_RESULT, new String[] { Manifest.permission.BLUETOOTH_CONNECT });
            } else {
                this.barcodeScanner.startScanningBarcode();
            }
        } else {
            this.barcodeScanner.startScanningBarcode();
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        if (grantResults.length > 0 && grantResults[0] == -1 && Manifest.permission.BLUETOOTH_CONNECT.equals(permissions[0])) {
            this.callbackContext.error("Bluetooth permission denied");
        } else {
            this.barcodeScanner.startScanningBarcode();
        }
    }

    /**
     * Stop receiver scan barcode events
     */
    private void stopScanner() {
        try {
            this.barcodeScanner.closeScanService();
            this.callbackContext.success();
        } catch (Exception ex) {
            this.callbackContext.error(ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        stopScanner();
        super.onDestroy();
    }

    @Override
    public void onSuccessRead(String value) {
        this.cordova.getActivity().runOnUiThread(() -> {
            try {
                JSONObject response = new JSONObject();
                response.put("barcode", value);
                this.pluginResult = new PluginResult(PluginResult.Status.OK, response);
                this.pluginResult.setKeepCallback(true);
                this.callbackScannerListener.sendPluginResult(this.pluginResult);
            } catch (Exception ex) {
                this.callbackScannerListener.error(ex.getMessage());
            }
        });
    }

    @Override
    public void onScannerStarted() {
        this.callbackContext.success();
    }
}