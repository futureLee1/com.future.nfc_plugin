/*global cordova, module*/

module.exports = {
    nfcState: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "NfcPlugin", "NFC_Action", [name]);
    }
};
