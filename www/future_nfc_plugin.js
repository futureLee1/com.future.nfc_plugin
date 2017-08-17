/*global cordova, module*/

module.exports = {
    /*greet: function (name, successCallback, errorCallback, action) {
        cordova.exec(successCallback, errorCallback, "TestPlugin", ""+action, [name]);
    }*/

    /*nfcState: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TestPlugin", "checkNfc");
    }*/

    /*nfcState: function (name, successCallback, errorCallback) {*/
    nfcState: function (name, action, successCallback, errorCallback) {
        /*cordova.exec(successCallback, errorCallback, "NfcPlugin", "checkNfc", [name]);*/
        cordova.exec(successCallback, errorCallback, "NfcPlugin", [action], [name]);
    }
};
