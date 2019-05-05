// var exec = require('cordova/exec');

// exports.lockLauncher = function(enabled, success, error) {
//     exec(null, error, 'Kiosk', 'lockLauncher', [!!enabled]);
// };

// exports.isLocked = function(arg0, success, error) {
//     exec(success, null, 'Kiosk', 'isLocked', null);
// };

// exports.switchLauncher = function() {
//     exec(null, null, 'Kiosk', 'switchLauncher', null);
// };

// exports.deleteDeviceAdmin = function() {
//     exec(null, null, 'Kiosk', 'deleteDeviceAdmin', null);
// };

cordova.define("cordova-plugin-android-kiosk.Kiosk", function(require, exports, module) {

    module.exports = {
        lockLauncher: function (enabled, success, error) {
            return new Promise((resolve, reject) => cordova.exec(resolve, reject, 'Kiosk', 'lockLauncher', [!!enabled]));
        },
        isLocked: function (arg0, success, error) {
            return new Promise((resolve, reject) => cordova.exec(resolve, reject, 'Kiosk', 'isLocked', null));
        },
        switchLauncher: function () {
            return new Promise((resolve, reject) => cordova.exec(resolve, reject, 'Kiosk', 'switchLauncher', null));
        },
        deleteDeviceAdmin: function () {
            return new Promise((resolve, reject) => cordova.exec(resolve, reject, 'Kiosk', 'deleteDeviceAdmin', null));
        },

    }

    });
