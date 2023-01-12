var exec = require('cordova/exec');

exports.startScanner = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback,  'EstafetaScannerPlugin', 'startScanner');
};

exports.stopScanner = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback,  'EstafetaScannerPlugin', 'scannerListener');
};

exports.stopScanner = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback,  'EstafetaScannerPlugin', 'stopScanner');
};