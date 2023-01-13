package com.estafeta.scanner.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
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
                    this.barcodeScanner.startScanner();
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