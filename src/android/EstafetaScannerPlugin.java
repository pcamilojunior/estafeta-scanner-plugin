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
    private BarcodeScanner barcodeScanner;
    private String ACTION_START_SCANNER = "startScanner";
    private String ACTION_STOP_SCANNER = "stopScanner";
    private PluginResult pluginResult;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        barcodeScanner = new BarcodeScanner(cordova.getContext(), this);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;

        if (ACTION_START_SCANNER.equals(action)) {
            this.startScanner();
            return true;
        }
        if (ACTION_STOP_SCANNER.equals(action)) {
            this.stopScanner();
        }
        return false;
    }

    /**
     * Start to receiver barcode events
     */
    private void startScanner() {
        this.barcodeScanner.startScanningBarcode();
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
                callbackContext.sendPluginResult(this.pluginResult);
            } catch (Exception ex) {
                callbackContext.error(ex.getMessage());
            }
        });
    }
}
