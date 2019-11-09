package com.pine.emeter.weService;

import com.pine.emeter.utils.AppPreferences;

/**
 * Created by PinesucceedAndroid on 6/29/2018.
 */

public interface ServerUtility {
    interface url {
        String BASE_URL = "http://emeter.pinesucceed.com:880/ws/";
        String API_URL = BASE_URL + "index.php/";
        String LoginUrl = API_URL + webServiceName.Login;
        String RegisterUrl = API_URL + webServiceName.Register;
        String GetMeterType = API_URL + webServiceName.GetMeterType;
        String Logout = API_URL + webServiceName.Logout;
        String History = API_URL + webServiceName.History;
        String UpdateUser = API_URL+ webServiceName.UpdateUser;
        String ScanMeter = API_URL  + webServiceName.ScanMeter;
        String ForgotPassword = API_URL + webServiceName.ForgotPassword;
    }

    interface webServiceName {
        String Login = "doLogin";
        String Register = "doRegister";
        String GetMeterType = "getMeterType";
        String Logout ="logout";
        String History ="billHistory";
        String UpdateUser = "updateUser";
        String ScanMeter = "scanMeter";
        String ForgotPassword = "forgotPassword";
    }
}
