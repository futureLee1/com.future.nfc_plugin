package com.future.nfc_plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.nfc.*;
import android.nfc.tech.IsoDep;
import android.util.Log;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import java.io.IOException;
import android.widget.Toast;

public class NfcPlugin extends CordovaPlugin {

    private static final String STATUS_NFC_OK = "NFC_OK";
    private static final String STATUS_NO_NFC = "NO_NFC";
    private static final String STATUS_NFC_DISABLED = "NFC_DISABLED";

    private static final String TAG = "NfcPlugin";

    private PendingIntent pendingIntent = null;

    String jsonData = "";

    CallbackContext mainCallbackContext = null;

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "execute_Action " + action);
        Log.d(TAG, "execute_args " + args.getString(0));
        Log.d(TAG, "execute_callbackContext " + callbackContext);

        String encodingData = Conversion.AES_CBC_128_ENCRYPT("12345678123456781234567800000000", "12345612345612345612345612345622");
        
        String decodingData = Conversion.AES_CBC_128_DECRYPT(encodingData, "12345612345612345612345612345622");

        String sha256Data = Conversion.SHA256("Test Data is Futurenmore");
        
        String hexToDexData = Conversion.getHexToDec("abcdefff");
        
        int a = 20;
        int b = 4;
        
        int calData = a / b;
        
        Log.e("encoding", ""+encodingData);
        Log.e("decoding", ""+decodingData);
        Log.e("sha256_Data", ""+sha256Data);
        Log.e("hexToDex", ""+hexToDexData);
        Log.e("calculate", ""+calData);

        jsonData = args.getString(0);

        if (getNfcStatus().equals(STATUS_NFC_DISABLED)) {
            Log.d(TAG, "getNfcStatus().equals(STATUS_NFC_DISABLED)");
            String message = "NFC is off.";
            callbackContext.error(message);
            return true; // short circuit
        } else if (getNfcStatus().equals(STATUS_NO_NFC)) {
            Log.d(TAG, "getNfcStatus().equals(STATUS_NO_NFC)");
            String message = "Device can not use NFC.";
            callbackContext.error(message);
            return true;
        } else {
            Log.d(TAG, "getNfcStatus().equals(STATUS_NFC_OK)");
            String message = "Welcome ! ";
            //callbackContext.success(message);

            mainCallbackContext = callbackContext;
            
            Log.e("execute_mainCallbackContext", ""+mainCallbackContext);

            startNfc();

        }

        return true;

	}

    private void createPendingIntent() {
        Log.d(TAG, "createPendingIntent()");
        if (pendingIntent == null) {
            Log.d(TAG, "createPendingIntent()_null");
            Activity activity = getActivity();
            Intent intent = new Intent(activity, activity.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
        }
    }

    private String getNfcStatus() {
        Log.d(TAG, "In_getNfcStatus()");

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (nfcAdapter == null) {
            return STATUS_NO_NFC;
        } else if (!nfcAdapter.isEnabled()) {
            return STATUS_NFC_DISABLED;
        } else {
            return STATUS_NFC_OK;
        }
    }

    private Activity getActivity() {
        Log.d(TAG, "In_getActivity()");
        return this.cordova.getActivity();
    }

    private PendingIntent getPendingIntent() {
        Log.d(TAG, "getPendingIntent()");
        return pendingIntent;
    }

    private Intent getIntent() {
        Log.d(TAG, "getIntent()");
        return getActivity().getIntent();
    }

    private void setIntent(Intent intent) {
        Log.d(TAG, "setIntent()");
        getActivity().setIntent(intent);
    }

    private void startNfc() {
        Log.d(TAG, "startNfc()");
        createPendingIntent(); // onResume can call startNfc before execute

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

                if (nfcAdapter != null && !getActivity().isFinishing()) {
                    nfcAdapter.enableForegroundDispatch(getActivity(), getPendingIntent(), null, null);
                }
            }
        });
    }

    private void stopNfc() {
        Log.d(TAG, "stopNfc");
        getActivity().runOnUiThread(new Runnable() {
            public void run() {

                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

                if (nfcAdapter != null) {
                    nfcAdapter.disableForegroundDispatch(getActivity());
                }
            }
        });
    }

    /*@Override
    public void onPause(boolean multitasking) {
        Log.d(TAG, "onPause " + getIntent());
        super.onPause(multitasking);
        if (multitasking) {
            // nfc can't run in background
            stopNfc();            
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        Log.d(TAG, "onResume " + getIntent());
        super.onResume(multitasking);
        startNfc();
    }*/

    @Override
    public void onPause(boolean multitasking) {
        Log.d(TAG, "onPause()");
        Log.d(TAG, "onPause " + getIntent()+", "+multitasking);
        super.onPause(multitasking);
        
        if(multitasking) {
            stopNfc();
        }
        
    }

    @Override
    public void onResume(boolean multitasking) {
        Log.d(TAG, "onResume()");
        Log.d(TAG, "onResume " + getIntent()+", "+multitasking);
        super.onResume(multitasking);
        startNfc();
    }

    @Override
    public void onNewIntent(Intent intent){
        Log.d(TAG, "onNewIntent()");
        Log.d(TAG, "onNewIntent " + intent);

        Log.e("onNewIntent_mainCallbackContext", ""+mainCallbackContext);

        super.onNewIntent(intent);
        setIntent(intent);
        // parseMessage();

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                
        String[] TechList = tag.getTechList();
        if(find(TechList, "android.nfc.tech.IsoDep") < 0){
            return;
        }

        IsoDep iso = IsoDep.get(tag);

        try{
            if(!iso.isConnected()){
                iso.connect();
            }

            if(jsonData.equals("Select")) {
                Toast.makeText(getActivity(), "Select_jsonData : "+jsonData, Toast.LENGTH_SHORT).show();

                Certification(iso, mainCallbackContext);
            } 

            if(jsonData.equals("Get_Vender_Code")) {
                Toast.makeText(getActivity(), "Vender_jsonData : "+jsonData, Toast.LENGTH_SHORT).show();

                getData_Vender_Code(iso, mainCallbackContext);
            }

            if(jsonData.equals("Get_Valid_Date")) {
                Toast.makeText(getActivity(), "Valid_date_jsonData : "+jsonData, Toast.LENGTH_SHORT).show();
                
                getData_Valid_Date(iso, mainCallbackContext);
            }

            if(jsonData.equals("Get_Serial_Number")) {
                Toast.makeText(getActivity(), "SN_jsonData : "+jsonData, Toast.LENGTH_SHORT).show();
                
                getData_Serial_Num(iso, mainCallbackContext);
            }

            if(jsonData.equals("Generate_OTP")) {
                Toast.makeText(getActivity(), "G_OTP_jsonData : "+jsonData, Toast.LENGTH_SHORT).show();
                
                generate_OTP(iso, mainCallbackContext);
            }
                        
            if(iso.isConnected()){
                iso.close();
            }

        } catch(IOException e) {
            Log.e("IsoDep Error", e.toString());
        }
        
    }

    /*public void onNewIntent(Intent intent, String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "onNewIntent()");
        Log.d(TAG, "onNewIntent " + intent);

        Log.e(TAG, "onNewIntent_Action " + action);
        Log.e(TAG, "onNewIntent_args " + args.getString(0));
        Log.e(TAG, "onNewIntent_callbackContext " + callbackContext);

        super.onNewIntent(intent);
        setIntent(intent);
        // parseMessage();

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                
        String[] TechList = tag.getTechList();
        if(find(TechList, "android.nfc.tech.IsoDep") < 0){
            return;
        }

        IsoDep iso = IsoDep.get(tag);

        try{
            if(!iso.isConnected()){
                iso.connect();
            }

            if(jsonData.equals("Select")) {
                Toast.makeText(getActivity(), "Select_jsonData : "+jsonData, Toast.LENGTH_SHORT).show();
                Certification(iso);
            } 

            if(jsonData.equals("Get_Vender_Code")) {
                Toast.makeText(getActivity(), "Vender_jsonData : "+jsonData, Toast.LENGTH_SHORT).show();
                //getData_Vender_Code(iso);
            }

            if(jsonData.equals("Get_Valid_Date")) {
                Toast.makeText(getActivity(), "Valid_date_jsonData : "+jsonData, Toast.LENGTH_SHORT).show();
                //getData_Vender_Code(iso);
            }

            if(jsonData.equals("Get_Serial_Number")) {
                Toast.makeText(getActivity(), "SN_jsonData : "+jsonData, Toast.LENGTH_SHORT).show();
                //getData_Vender_Code(iso);
            }

            if(jsonData.equals("Generate_OTP")) {
                Toast.makeText(getActivity(), "G_OTP_jsonData : "+jsonData, Toast.LENGTH_SHORT).show();
                //getData_Vender_Code(iso);
            }
                        
            if(iso.isConnected()){
                iso.close();
            }

        } catch(IOException e) {
            Log.e("IsoDep Error", e.toString());
        }
        
    }*/
	
    /*void parseMessage() {
        Log.d(TAG, "parseMessage()");
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "parseMessage " + getIntent());
                Intent intent = getIntent();
                String action = intent.getAction();
                Log.d(TAG, "intent_action " + action);
                
                if (action == null) {
                    return;
                }

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                
                String[] TechList = tag.getTechList();
                if(find(TechList, "android.nfc.tech.IsoDep") < 0){
                    return;
                }

                IsoDep iso = IsoDep.get(tag);

                try{
                    if(!iso.isConnected()){
                        iso.connect();
                    }

                    Certification(iso);
                        
                    if(iso.isConnected()){
                        iso.close();
                    }

                } catch(IOException e) {
                    Log.e("IsoDep Error", e.toString());
                }

            }
        });
    }*/

    private void Certification(IsoDep iso, CallbackContext mainCallbackContext) {
        Log.d(TAG, "Certification()");
        Log.e("Certification", ""+mainCallbackContext);

        int res = 0;
        String strResponse[] = new String[1];
        String strErrMsg[] = new String[1];
        
        res = 0;
        res = Function.SelectFile_New(iso, strResponse, strErrMsg);
        if(res < 0){
            Log.e("SelectFile","Card Select Failed");

            String errorMsg = "Response : "+strResponse[0]+"\n"+"Err Msg : "+strErrMsg[0];
            mainCallbackContext.error(errorMsg);

            strResponse = null;
            strErrMsg = null;
            return;
        }

        Log.e("Card_Result",""+strResponse[0]);

        String strResult = strResponse[0].substring(strResponse[0].length() - 4, strResponse[0].length());

        Log.e("strResult",""+strResult);        

        Toast.makeText(getActivity(), ""+strResult, Toast.LENGTH_LONG).show();

        /*res = 0;
        res = Function.GetChallenge(iso, strResponse, strErrMsg);
        if(res < 0){
            Log.e("GetChallenge","R1 Create Failed");
            strResponse = null;
            strErrMsg = null;
            return;
        }
        
        Log.e("GetChallenge_Data", strResponse[0]);*/

        String message = strResponse[0];
        mainCallbackContext.success(message);
                    
        if(iso.isConnected()){
            try {
                iso.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }  
    }

    private void getData_Vender_Code(IsoDep iso, CallbackContext mainCallbackContext) {
        Log.d(TAG, "getData_Vender_Code()");

        int res = 0;

        String strResponse[] = new String[1];
        String strErrMsg[] = new String[1];
        
        res = 0;
        res = Function.SelectFile_New(iso, strResponse, strErrMsg);
        if(res < 0){
            Log.e("SelectFile_New", "Card Select Failed");

            String errorMsg = "Vender Code_Select"+"\n"+"Response : "+strResponse[0]+"\n"+"Err Msg : "+strErrMsg[0];
            mainCallbackContext.error(errorMsg);

            strResponse = null;
            strErrMsg = null;
            return;
        }

        Log.e("SelectFile_New",""+strResponse[0]);

        String strResult = strResponse[0].substring(strResponse[0].length() - 4, strResponse[0].length());

        Log.e("strResult",""+strResult);        

        /*Toast.makeText(getActivity(), ""+strResult, Toast.LENGTH_LONG).show();*/

        res = 0;
        res = Function.GetData_VENDOR_CODE(iso, strResponse, strErrMsg);
        if(res < 0){
            Log.e("getData_Vender_Code", "Vender Code Read Failed");

            String errorMsg = "Vender Code_GetData"+"\n"+"Response : "+strResponse[0]+"\n"+"Err Msg : "+strErrMsg[0];
            mainCallbackContext.error(errorMsg);

            strResponse = null;
            strErrMsg = null;
            return;
        }
        
        Log.e("Vender Code", strResponse[0]);

        String vendor_Code = strResponse[0];

        mainCallbackContext.success(vendor_Code);
                    
        if(iso.isConnected()){
            try {
                iso.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void getData_Valid_Date(IsoDep iso, CallbackContext mainCallbackContext) {
        Log.d(TAG, "getData_Valid_Date()");

        int res = 0;

        String strResponse[] = new String[1];
        String strErrMsg[] = new String[1];
        
        res = 0;
        res = Function.SelectFile_New(iso, strResponse, strErrMsg);
        if(res < 0){
            Log.e("SelectFile_New", "Card Select Failed");

            String errorMsg = "Valid Date_Select"+"\n"+"Response : "+strResponse[0]+"\n"+"Err Msg : "+strErrMsg[0];
            mainCallbackContext.error(errorMsg);

            strResponse = null;
            strErrMsg = null;
            return;
        }

        Log.e("SelectFile_New",""+strResponse[0]);

        String strResult = strResponse[0].substring(strResponse[0].length() - 4, strResponse[0].length());

        Log.e("strResult",""+strResult);        

        /*Toast.makeText(getActivity(), ""+strResult, Toast.LENGTH_LONG).show();*/

        res = 0;
        res = Function.GetData_VALID_DATE(iso, strResponse, strErrMsg);
        if(res < 0){
            Log.e("getData_Valid_Date", "Valid Date Read Failed");

            String errorMsg = "Valid Date_GetData"+"\n"+"Response : "+strResponse[0]+"\n"+"Err Msg : "+strErrMsg[0];
            mainCallbackContext.error(errorMsg);

            strResponse = null;
            strErrMsg = null;
            return;
        }
        
        Log.e("Valid Date Data", strResponse[0]);

        String valid_date = strResponse[0];

        mainCallbackContext.success(valid_date);
                    
        if(iso.isConnected()){
            try {
                iso.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void getData_Serial_Num(IsoDep iso, CallbackContext mainCallbackContext) {
        Log.d(TAG, "getData_Serial_Num()");

        int res = 0;

        String strResponse[] = new String[1];
        String strErrMsg[] = new String[1];
        
        res = 0;
        res = Function.SelectFile_New(iso, strResponse, strErrMsg);
        if(res < 0){
            Log.e("SelectFile_New", "Card Select Failed");

            String errorMsg = "Serial Number_Select"+"\n"+"Response : "+strResponse[0]+"\n"+"Err Msg : "+strErrMsg[0];
            mainCallbackContext.error(errorMsg);

            strResponse = null;
            strErrMsg = null;
            return;
        }

        Log.e("SelectFile_New",""+strResponse[0]);

        String strResult = strResponse[0].substring(strResponse[0].length() - 4, strResponse[0].length());

        Log.e("strResult",""+strResult);        

        /*Toast.makeText(getActivity(), ""+strResult, Toast.LENGTH_LONG).show();*/

        res = 0;
        res = Function.GetData_Serial_Num(iso, strResponse, strErrMsg);
        if(res < 0){
            Log.e("getData_Serial_Num", "Serial Number Read Failed");

            String errorMsg = "Serial Number_GetData"+"\n"+"Response : "+strResponse[0]+"\n"+"Err Msg : "+strErrMsg[0];
            mainCallbackContext.error(errorMsg);

            strResponse = null;
            strErrMsg = null;
            return;
        }
        
        Log.e("Serial Number Data", strResponse[0]);

        String serial_number = strResponse[0];

        mainCallbackContext.success(serial_number);
                    
        if(iso.isConnected()){
            try {
                iso.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void generate_OTP(IsoDep iso, CallbackContext mainCallbackContext) {
        Log.d(TAG, "generate_OTP()");

        int res = 0;

        String strResponse[] = new String[1];
        String strErrMsg[] = new String[1];
        
        res = 0;
        res = Function.SelectFile_New(iso, strResponse, strErrMsg);
        if(res < 0){
            Log.e("SelectFile_New", "Card Select Failed");

            String errorMsg = "Generate OTP_Select"+"\n"+"Response : "+strResponse[0]+"\n"+"Err Msg : "+strErrMsg[0];
            mainCallbackContext.error(errorMsg);

            strResponse = null;
            strErrMsg = null;
            return;
        }

        Log.e("SelectFile_New",""+strResponse[0]);

        String strResult = strResponse[0].substring(strResponse[0].length() - 4, strResponse[0].length());

        Log.e("strResult",""+strResult);

        res = 0;
        res = Function.GenerateOTP(iso, strResponse, strErrMsg, "01003");
        if(res < 0){
            Log.e("generate_OTP", "Create OTP Number Failed");

            String errorMsg = "Generate OTP_GetData"+"\n"+"Response : "+strResponse[0]+"\n"+"Err Msg : "+strErrMsg[0];
            mainCallbackContext.error(errorMsg);

            strResponse = null;
            strErrMsg = null;
            return;
        }
        
        Log.e("OTP Number", strResponse[0]);

        String otp_Number = strResponse[0] = strResponse[0].substring(0, 6);
        
        mainCallbackContext.success(otp_Number);
                    
        if(iso.isConnected()){
            try {
                iso.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void generate_OTP_0831(IsoDep iso, CallbackContext mainCallbackContext, String input_opin) {
        Log.d(TAG, "generate_OTP()");

        int res = 0;

        String strResponse[] = new String[1];
        String strErrMsg[] = new String[1];
        
        res = 0;
        res = Function.SelectFile_New(iso, strResponse, strErrMsg);
        if(res < 0){
            Log.e("SelectFile_New", "Card Select Failed");

            String errorMsg = "Generate OTP_Select"+"\n"+"Response : "+strResponse[0]+"\n"+"Err Msg : "+strErrMsg[0];
            mainCallbackContext.error(errorMsg);

            strResponse = null;
            strErrMsg = null;
            return;
        }

        Log.e("SelectFile_New",""+strResponse[0]);

        String strResult = strResponse[0].substring(strResponse[0].length() - 4, strResponse[0].length());

        Log.e("strResult",""+strResult);

        res = 0;
        res = Function.GenerateOTP_0831(iso, strResponse, strErrMsg, "01003", input_opin);
        if(res < 0){
            Log.e("generate_OTP", "Create OTP Number Failed");

            String errorMsg = "Generate OTP_GetData"+"\n"+"Response : "+strResponse[0]+"\n"+"Err Msg : "+strErrMsg[0];
            mainCallbackContext.error(errorMsg);

            strResponse = null;
            strErrMsg = null;
            return;
        }
        
        Log.e("OTP Number", strResponse[0]);

        String otp_Number = strResponse[0] = strResponse[0].substring(0, 6);
        
        mainCallbackContext.success(otp_Number);
                    
        if(iso.isConnected()){
            try {
                iso.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public int find(String[] arr, String s){
        Log.d(TAG, "find()");
        for(int i=0; i<arr.length; i++){
            if(arr[i].indexOf(s) >= 0){
                return i;
            }
        }
        return -1;
    }


}
