package com.pine.emeter.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abbyy.mobile.rtr.Engine;
import com.abbyy.mobile.rtr.ITextCaptureService;
import com.abbyy.mobile.rtr.Language;
import com.pine.emeter.BuildConfig;
import com.pine.emeter.R;
import com.pine.emeter.utils.VerticalSeekBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ScanReading extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {
    private NavigationView navigationView;
    private DrawerLayout drawer;
    // tags used to attach the fragments
    private Toolbar toolbar;

    private static final String licenseFileName = "AbbyyRtrSdk.license";
    private static final String TAG = "message";
    private static final int FOCUS_AREA_SIZE = 300;

    ///////////////////////////////////////////////////////////////////////////////
    // Some application settings that can be changed to modify application behavior:
    // The camera zoom. Optically zooming with a good camera often improves results
    // even at close range and it might be required at longer ranges.
    private static int cameraZoom = 1;
    // The default behavior in this sample is to start recognit ion when application is started or
    // resumed. You can turn off this behavior or remove it completely to simplify the application
    private static final boolean startRecognitionOnAppStart = true;
    // Area of interest specified through margin sizes relative to camera preview size
    private static final int areaOfInterestMargin_PercentOfWidth = 10;
    private static final int areaOfInterestMargin_PercentOfHeight = 45;
    // A subset of available languages shown in the UI. See all available languages in Language enum.
    // To show all languages in the UI you can substitute the list below with:
    // Language[] languages = Language.values();
    private Language[] languages = {
            Language.ChineseSimplified,
            Language.ChineseTraditional,
            Language.English,
            Language.French,
            Language.German,
            Language.Italian,
            Language.Japanese,
            Language.Korean,
            Language.Polish,
            Language.PortugueseBrazilian,
            Language.Russian,
            Language.Spanish,
    };
    ///////////////////////////////////////////////////////////////////////////////

    private Engine engine;
    private ITextCaptureService textCaptureService;
    ProgressDialog progressDialog;
    // The camera and the preview surface
    private Camera camera;
    private SurfaceViewWithOverlay surfaceViewWithOverlay;
    private SurfaceHolder previewSurfaceHolder;
    VerticalSeekBar seekBar;
    //TextView displaytext;
    // Actual preview size and orientation
    private Camera.Size cameraPreviewSize;
    private int orientation;
    // Webview for display api response
    WebView webView;
    // UI component
    // Image for camera led light
    ImageView led;
    private boolean isLighOn = false;
    TextView textView;
    // Auxiliary variables
    private boolean inPreview = false; // Camera preview is started
    private boolean stableResultHasBeenReached; // Stable result has been reached
    private boolean startRecognitionWhenReady; // Start recognition next time when ready (and reset this flag)
    private Handler handler = new Handler(); // Posting some delayed actions;

    // UI components
    private Button startButton; // The start button
    private TextView warningTextView; // Show warnings from recognizer
    private TextView errorTextView; // Show errors from recognizer
    RelativeLayout relativeLayout;
    // Text displayed on start button
    private static final String BUTTON_TEXT_START = "Start";
    private static final String BUTTON_TEXT_STOP = "Stop";
    private static final String BUTTON_TEXT_STARTING = "Starting...";
    private static final String BUTTON_TEXT_RESUME = "Resume";
    Button print;

    TextView myLabel;

    // will enable user to enter any text to be printed

    // android built in classes for bluetooth operations
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    // needed for communication to bluetooth device / network
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    // Variable
    private int counter = 9;
    private CountDownTimer yourCountDownTimer;
    private String finalString;
    private CountDownTimer trueCountDownTimer;
    private View bar;
    private Animation animation;
    private ScaleGestureDetector mScaleDetector;
    private int zoom;

    // To communicate with the Text Capture Service we will need this callback:
    private ITextCaptureService.Callback textCaptureCallback = new ITextCaptureService.Callback() {

        @Override
        public void onRequestLatestFrame(byte[] buffer) {
            // The service asks to fill the buffer with image data for the latest frame in NV21 format.
            // Delegate this task to the camera. When the buffer is filled we will receive
            // Camera.PreviewCallback.onPreviewFrame (see below)
            camera.addCallbackBuffer(buffer);
        }

        @Override
        public void onFrameProcessed(ITextCaptureService.TextLine[] lines,
                                     ITextCaptureService.ResultStabilityStatus resultStatus, ITextCaptureService.Warning warning) {
            // Frame has been processed. Here we process recognition results. In this sample we
            // stop when we get stable result. This callback may continue being called for some time
            // even after the service has been stopped while the calls queued to this thread (UI thread)
            // are being processed. Just ignore these calls:
            if (!stableResultHasBeenReached) {
                if (resultStatus.ordinal() >= 3) {
                    // The result is stable enough to show something to the user
                    // surfaceViewWithOverlay.setLines(lines, resultStatus);
                } else {
                    // The result is not stable. Show nothing
                    // surfaceViewWithOverlay.setLines(null, ITextCaptureService.ResultStabilityStatus.NotReady);
                }

                // Show the warning from the service if any. The warnings are intended for the user
                // to take some action (zooming in, checking recognition language, etc.)
                //warningTextView.setText(warning != null ? warning.name() : "");

                if (resultStatus == ITextCaptureService.ResultStabilityStatus.Stable) {
                    stableResultHasBeenReached = true;
                    // stop recognize when scanning done
                    stopRecognition();
                    // Get string from scanning result
                    String str = "";
                    for (int i = 0; i < lines.length; i++) {
                        ITextCaptureService.TextLine line = lines[i];
                        if (str != "") {
                            str = str + ", " + line.Text;
                        } else {
                            str = line.Text;
                        }
                    }
                    // Strip all space, comma, and special character
                    Log.d("prestr", str);
                   str = str.replaceAll("[,\\s]", "");
                   str = str.replaceAll("[^0-9]+", "");
                    Toast.makeText(ScanReading.this, str, Toast.LENGTH_SHORT).show();
//                    // Replace and strip by character in final scanning string
//                    // Get last character , starting character, and middle number of car plate
//                    try {
//
//                        Log.d("finalstr", str);
//                        String lastChar = "";
//                        try {
//                            lastChar = String.valueOf(str.charAt(str.length() - 1));
//
//                        } catch (Exception e) {
//                            Log.d("errorlast", e.toString());
//                        }
//
//
////                        Log.d("lastchar", lastChar);
//
//
//                        StringBuffer strChar = new StringBuffer();
//                        for (int k = 0; k < str.length(); k++) {
//                            if (Character.isLetter(str.charAt(k))) {
//                                strChar.append(str.charAt(k));
//                            } else {
//                                break;
//                            }
//
//                        }
//                        // get first four character
//                        String finalChar = strChar.toString();
//                        String finalNum = str.substring(finalChar.length(), str.length() - 1);
//                        if (str.length() > 8 && finalNum.length() > 3 && finalChar.length() > 2) {
//                            finalNum = str.substring(finalChar.length(), str.length() - 2);
//                        }
//
//
////                        Log.d("numstr", finalNum);
//
//                        if (finalChar.length() > 3 && finalNum.length() >= 4 && str.length() > 8) {
//
//                            str = str.substring(0, str.length() - 1);
//
//                        }
////                        Log.d("strtemp", str);
//
////                        Replace number by character
//                        StringBuilder myName = new StringBuilder(str);
//                        if (lastChar.equals("1")) {
//
//                            myName.setCharAt(str.length() - 1, 'T');
//
//                        } else if (lastChar.equals("6")) {
//                            myName.setCharAt(str.length() - 1, 'B');
//
//                        } else if (lastChar.equals("0")) {
//                            myName.setCharAt(str.length() - 1, 'Q');
//
//                        } else if (lastChar.equals("8")) {
//                            myName.setCharAt(str.length() - 1, 'B');
//
//                        } else if (lastChar.equals("7")) {
//                            myName.setCharAt(str.length() - 1, 'X');
//                        }
//                        str = myName.toString();
//
////                      get first character string
//                        if (finalChar.length() > 3 && finalNum.length() >= 4) {
//
//                            finalChar = finalChar.substring(finalChar.length() - 3);
//                        }
//
//
////                        replace number to character if string length more than 4
//                        if (finalNum.length() > 4) {
//                            String sub = finalNum.substring(0, finalNum.length() - 4);
//                            StringBuilder temp = new StringBuilder(finalNum);
//                            for (int i = 0; i < sub.length(); i++) {
//                                if (sub.charAt(i) == '0') {
//                                    temp.setCharAt(i, 'Q');
//                                } else if (sub.charAt(i) == '6') {
//                                    temp.setCharAt(i, 'B');
//                                } else if (sub.charAt(i) == '8') {
//                                    temp.setCharAt(i, 'B');
//                                } else if (sub.charAt(i) == '1') {
//                                    temp.setCharAt(i, 'T');
//                                }
//
//                            }
//                            finalNum = temp.toString();
//
//                        }
//
////                        Log.d("finalnumtem", finalNum);
////                        replace character to number after first 3 character
//                        StringBuilder temp = new StringBuilder(finalNum);
//                        String strtemp = finalNum;
//                        if (temp.length() > 4) {
//                            strtemp = temp.substring(temp.length() - 4);
//                        }
//                        for (int i = 0; i < strtemp.length(); i++) {
//                            if (strtemp.charAt(i) == 'B') {
//                                temp.setCharAt(i + (finalNum.length() - strtemp.length()), '8');
//                            } else if (strtemp.charAt(i) == 'Q') {
//                                temp.setCharAt(i + (finalNum.length() - strtemp.length()), '0');
//                            } else if (strtemp.charAt(i) == 'T') {
//                                temp.setCharAt(i + (finalNum.length() - strtemp.length()), '1');
//                            } else if (strtemp.charAt(i) == 'D') {
//                                temp.setCharAt(i + (finalNum.length() - strtemp.length()), '0');
//                            } else if (strtemp.charAt(i) == 'I') {
//                                temp.setCharAt(i + (finalNum.length() - strtemp.length()), '1');
//                            }
//
//                        }
//                        finalNum = temp.toString();
//
////                  replace character to number from first string of character if size id gatter than 3
//                        if (finalChar.length() > 3) {
//                            temp = new StringBuilder(finalChar);
//                            String mtemp = finalChar.substring(3);
//                            for (int i = 0; i < mtemp.length(); i++) {
//                                if (mtemp.charAt(i) == 'B') {
//                                    temp.setCharAt(i + 3, '8');
//                                } else if (mtemp.charAt(i) == 'Q') {
//                                    temp.setCharAt(i + 3, '0');
//                                } else if (mtemp.charAt(i) == 'T') {
//                                    temp.setCharAt(i + 3, '1');
//                                } else if (mtemp.charAt(i) == 'D') {
//                                    temp.setCharAt(i + 3, '0');
//                                } else if (mtemp.charAt(i) == 'I') {
//                                    temp.setCharAt(i + 3, '1');
//                                }
//
//                            }
//                            finalChar = temp.toString();
//                        }
//
////                        replace 'o' to 'Q' from first 3 character
//                        if (finalChar.length() == 3) {
//                            temp = new StringBuilder(finalChar);
//                            String lChar = String.valueOf(str.charAt(finalChar.length() - 1));
//                            if (lChar.equals("O")) {
//                                temp.setCharAt(finalChar.length() - 1, 'Q');
//                            }
//                            finalChar = temp.toString();
//                        }
//
////                         remove first i from string
//                        if (finalChar.length() > 3) {
//                            if (finalChar.charAt(0) == "I".charAt(0)) {
//                                finalChar = finalChar.substring(1);
//                            }
//                        }
//
////                        Log.d("cahrstr", finalChar);
////                        Log.d("numstr", finalNum);
//                        // Final string by updating scanning string
//                        str = finalChar + finalNum + str.substring(str.length() - 1);
////                        Log.d("finalstring", str);
//                    } catch (Exception e) {
//                        Log.e("error", e.toString());
//                    }
//                    // Match Pattern by regex
//                    Pattern p = Pattern.compile("[A-Z]{1,3}[0-9]{1,4}[A-Z]{1}");
//                    Matcher m = p.matcher(str);

                    startButton.setVisibility(View.GONE);
                    clearRecognitionResults();
                    stableResultHasBeenReached = false;
//                         start recorgnize again
                    onStartButtonClick();

//                    if (m.matches()) {
//                        // if matches then vibrate for 1 sec and call api
//                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                        v.vibrate(100);
//                       // apiCall(str);
//                    } else {
//                        // if not match then clear all and start recognize
//                        Toast.makeText(ScanReading.this, "Not a valid Carplate" + " " + str, Toast.LENGTH_SHORT).show();
//                        startButton.setVisibility(View.GONE);
//                        clearRecognitionResults();
//                        stableResultHasBeenReached = false;
////                         start recorgnize again
//                        onStartButtonClick();
//                    }

                    // Stable result has been reached. Stop the service
                    // stopRecognition();

                    // Show result to the user. In this sample we whiten screen background and play
                    // the same sound that is used for pressing buttons
                    surfaceViewWithOverlay.setFillBackground(true);
                    startButton.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                }
            }
        }

        @Override
        public void onError(Exception e) {
            // An error occurred while processing. Log it. Processing will continue
            Log.e(getString(R.string.app_name), "Error: " + e.getMessage());
            if (BuildConfig.DEBUG) {
                // Make the error easily visible to the developer
                String message = e.getMessage();
                if (message == null) {
                    message = "Unspecified error while creating the service. See logcat for details.";
                } else {
                    if (message.contains("ChineseJapanese.rom")) {
                        message = "Chinese, Japanese and Korean are available in EXTENDED version only. Contact us for more information.";
                    }
                    if (message.contains("Russian.edc")) {
                        message = "Cyrillic script languages are available in EXTENDED version only. Contact us for more information.";
                    } else if (message.contains(".trdic")) {
                        message = "Translation is available in EXTENDED version only. Contact us for more information.";
                    }
                }

            }
        }
    };

    // This callback will be used to obtain frames from the camera
    private Camera.PreviewCallback cameraPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // The buffer that we have given to the camera in ITextCaptureService.Callback.onRequestLatestFrame
            // above have been filled. Send it back to the Text Capture Service
            textCaptureService.submitRequestedFrame(data);
        }
    };

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // When surface is created, store the holder
            previewSurfaceHolder = holder;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // When surface is changed (or created), attach it to the camera, configure camera and start preview
            if (camera != null) {
                setCameraPreviewDisplayAndStartPreview();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // When surface is destroyed, clear previewSurfaceHolder
            previewSurfaceHolder = null;
        }
    };
    // Start recognition when autofocus completes (used when continuous autofocus is not enabled)
    private Camera.AutoFocusCallback startRecognitionCameraAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            onAutoFocusFinished(success, camera);
            startRecognition();
        }
    };

    // Simple autofocus callback
    private Camera.AutoFocusCallback simpleCameraAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            onAutoFocusFinished(success, camera);
        }
    };


    // Enable 'Start' button and switching to continuous focus mode (if possible) when autofocus completes
    private Camera.AutoFocusCallback finishCameraInitialisationAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            onAutoFocusFinished(success, camera);
            //startButton.setText(BUTTON_TEXT_START);
            startButton.setEnabled(true);
            if (startRecognitionWhenReady) {
                startRecognition();
                startRecognitionWhenReady = false;
            }
        }
    };
    // Autofocus by tap
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // if BUTTON_TEXT_STARTING autofocus is already in progress, it is incorrect to interrupt it
            if (!startButton.getText().equals(BUTTON_TEXT_STARTING)) {
                autoFocus(simpleCameraAutoFocusCallback);
            }
        }
    };
    private float mDist = 0;
    private int maxZoom;

    private void onAutoFocusFinished(boolean success, Camera camera) {
        if (isContinuousVideoFocusModeEnabled(camera)) {
            setCameraFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        } else {
            if (!success) {
                autoFocus(simpleCameraAutoFocusCallback);
            }
        }
    }
    private void autoFocus(Camera.AutoFocusCallback callback) {
        if (camera != null) {
            try {
                setCameraFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                camera.autoFocus(callback);

            } catch (Exception e) {
                Log.e(getString(R.string.app_name), "Error: " + e.getMessage());
            }
        }
    }

    // Checks that FOCUS_MODE_CONTINUOUS_VIDEO supported
    private boolean isContinuousVideoFocusModeEnabled(Camera camera) {
        return camera.getParameters().getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
    }

    // Sets camera focus mode and focus area
    private void setCameraFocusMode(String mode) {
        // Camera sees it as rotated 90 degrees, so there's some confusion with what is width and what is height)
        int width = 0;
        int height = 0;
        int halfCoordinates = 1000;
        int lengthCoordinates = 2000;
        Rect area = surfaceViewWithOverlay.getAreaOfInterest();
        switch (orientation) {
            case 0:
            case 180:
                height = cameraPreviewSize.height;
                width = cameraPreviewSize.width;
                break;
            case 90:
            case 270:
                width = cameraPreviewSize.height;
                height = cameraPreviewSize.width;
                break;
        }

        camera.cancelAutoFocus();
        Camera.Parameters parameters = camera.getParameters();
        // Set focus and metering area equal to the area of interest. This action is essential because by defaults camera
        // focuses on the center of the frame, while the area of interest in this sample application is at the top
        List<Camera.Area> focusAreas = new ArrayList<>();
        Rect areasRect;

        switch (orientation) {
            case 0:
                areasRect = new Rect(
                        -halfCoordinates + area.left * lengthCoordinates / width,
                        -halfCoordinates + area.top * lengthCoordinates / height,
                        -halfCoordinates + lengthCoordinates * area.right / width,
                        -halfCoordinates + lengthCoordinates * area.bottom / height
                );
                break;
            case 180:
                areasRect = new Rect(
                        halfCoordinates - area.right * lengthCoordinates / width,
                        halfCoordinates - area.bottom * lengthCoordinates / height,
                        halfCoordinates - lengthCoordinates * area.left / width,
                        halfCoordinates - lengthCoordinates * area.top / height
                );
                break;
            case 90:
                areasRect = new Rect(
                        -halfCoordinates + area.top * lengthCoordinates / height,
                        halfCoordinates - area.right * lengthCoordinates / width,
                        -halfCoordinates + lengthCoordinates * area.bottom / height,
                        halfCoordinates - lengthCoordinates * area.left / width
                );
                break;
            case 270:
                areasRect = new Rect(
                        halfCoordinates - area.bottom * lengthCoordinates / height,
                        -halfCoordinates + area.left * lengthCoordinates / width,
                        halfCoordinates - lengthCoordinates * area.top / height,
                        -halfCoordinates + lengthCoordinates * area.right / width
                );
                break;
            default:
                throw new IllegalArgumentException();
        }

        focusAreas.add(new Camera.Area(areasRect, 800));
        if (parameters.getMaxNumFocusAreas() >= focusAreas.size()) {
            parameters.setFocusAreas(focusAreas);
        }
        if (parameters.getMaxNumMeteringAreas() >= focusAreas.size()) {
            parameters.setMeteringAreas(focusAreas);
        }

        parameters.setFocusMode(mode);

        // Commit the camera parameters
        camera.setParameters(parameters);

    }

    // Attach the camera to the surface holder, configure the camera and start preview
    private void setCameraPreviewDisplayAndStartPreview() {
        try {
            camera.setPreviewDisplay(previewSurfaceHolder);
        } catch (Throwable t) {
            Log.e(getString(R.string.app_name), "Exception in setPreviewDisplay()", t);
        }
        configureCameraAndStartPreview(camera);
    }

    // Stop preview and release the camera
    private void stopPreviewAndReleaseCamera() {
        if (camera != null) {
            camera.setPreviewCallbackWithBuffer(null);
            stopPreview();
            camera.release();
            camera = null;
        }
    }

    // Stop preview if it is running
    private void stopPreview() {
        if (inPreview) {
            camera.stopPreview();
            inPreview = false;
        }
    }

    // Show error on startup if any
    private void showStartupError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("ABBYY RTR SDK")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ScanReading.this.finish();
                    }
                });
    }

    // Load ABBYY RTR SDK engine and configure the text capture service
    private boolean createTextCaptureService() {
        // Initialize the engine and text capture service
        try {
            engine = Engine.load(this, licenseFileName);
            textCaptureService = engine.createTextCaptureService(textCaptureCallback);

            return true;
        } catch (IOException e) {
            // Troubleshooting for the developer
            Log.e(getString(R.string.app_name), "Error loading ABBYY RTR SDK:", e);
            showStartupError("Could not load some required resource files. Make sure to configure " +
                    "'assets' directory in your application and specify correct 'license file name'. See logcat for details.");
        } catch (Engine.LicenseException e) {
            // Troubleshooting for the developer
            Log.e(getString(R.string.app_name), "Error loading ABBYY RTR SDK:", e);
            showStartupError("License not valid. Make sure you have a valid license file in the " +
                    "'assets' directory and specify correct 'license file name' and 'application id'. See logcat for details.");
        } catch (Throwable e) {
            // Troubleshooting for the developer
            Log.e(getString(R.string.app_name), "Error loading ABBYY RTR SDK:", e);
            showStartupError("Unspecified error while loading the engine. See logcat for details.");
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        previewSurfaceHolder.setKeepScreenOn(false);
        super.onBackPressed();
    }


    // Start recognition
    @SuppressLint("ResourceType")
    private void startRecognition() {
        // Do not switch off the screen while text capture service is running
        try {
            // start scaning bar
            bar.setVisibility(View.VISIBLE);
            bar.startAnimation(animation);
            surfaceViewWithOverlay.setBackgroundResource(Color.TRANSPARENT);
            webView.setVisibility(View.GONE);
            previewSurfaceHolder.setKeepScreenOn(true);
            // Get area of interest (in coordinates of preview frames)
            Rect areaOfInterest = new Rect(surfaceViewWithOverlay.getAreaOfInterest());
            // Clear error message
            errorTextView.setText("");
            // Start the service
            textCaptureService.start(cameraPreviewSize.width, cameraPreviewSize.height, orientation, areaOfInterest);
            // Change the text on the start button to 'Stop'
            startButton.setVisibility(View.GONE);
//            Log.d("startisthere", "hello");
            startButton.setEnabled(true);
        } catch (Exception e) {
            Log.e("error", e.toString());
        }

    }

    // Stop recognition
    void stopRecognition() {
        // Disable the 'Stop' button
        startButton.setEnabled(false);
        // Stop the service asynchronously to make application more responsive. Stopping can take some time
        // waiting for all processing threads to stop
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                textCaptureService.stop();
                return null;
            }

            protected void onPostExecute(Void result) {
                if (previewSurfaceHolder != null) {
                    // Restore normal power saving behaviour
                    previewSurfaceHolder.setKeepScreenOn(false);
                    //displaytext.setText((CharSequence) surfaceViewWithOverlay);
                }
                // Change the text on the stop button back to 'Start'
                startButton.setVisibility(View.VISIBLE);
                //startButton.setText(BUTTON_TEXT_RESUME);
                startButton.setEnabled(true);
            }
        }.execute();
    }

    // Clear recognition results
    void clearRecognitionResults() {
        startButton.setText(BUTTON_TEXT_RESUME);
        stableResultHasBeenReached = false;
        surfaceViewWithOverlay.setLines(null, ITextCaptureService.ResultStabilityStatus.NotReady);
        surfaceViewWithOverlay.setFillBackground(false);
        errorTextView.setText("");
    }

    // Returns orientation of camera
    private int getCameraOrientation() {
        Display display = getWindowManager().getDefaultDisplay();
        int orientation = 0;
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                orientation = 0;
                break;
            case Surface.ROTATION_90:
                orientation = 90;
                break;
            case Surface.ROTATION_180:
                orientation = 180;
                break;
            case Surface.ROTATION_270:
                orientation = 270;
                break;
        }
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return (cameraInfo.orientation - orientation + 360) % 360;
            }
        }
        // If Camera.open() succeed, this point of code never reached
        return -1;
    }


    private void configureCameraAndStartPreview(Camera camera) {
        // Setting camera parameters when preview is running can cause crashes on some android devices
        stopPreview();

        // Configure camera orientation. This is needed for both correct preview orientation
        // and recognition
        orientation = getCameraOrientation();
        camera.setDisplayOrientation(orientation);

        // Configure camera parameters
        Camera.Parameters parameters = camera.getParameters();

        // Select preview size. The preferred size for Text Capture scenario is 1080x720. In some scenarios you might
        // consider using higher resolution (small text, complex background) or lower resolution (better performance, less noise)
        cameraPreviewSize = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.height <= 720 || size.width <= 720) {
                if (cameraPreviewSize == null) {
                    cameraPreviewSize = size;
                } else {
                    int resultArea = cameraPreviewSize.width * cameraPreviewSize.height;
                    int newArea = size.width * size.height;
                    if (newArea > resultArea) {
                        cameraPreviewSize = size;
                    }
                }
            }
        }
        parameters.setPreviewSize(cameraPreviewSize.width, cameraPreviewSize.height);

        // Zoom
        maxZoom = parameters.getMaxZoom();
        //Method for create vertcal seek bar for zoom in and zoom out
        seekValue();
        // set zoom level to medium of camera and seekbar
        seekBar.setProgress(maxZoom / 2);
        seekBar.updateThumb();
        parameters.setZoom(maxZoom / 2);
        //Log.d("maxzoom", String.valueOf(maxZoom));

        // Buffer format. The only currently supported format is NV21
        parameters.setPreviewFormat(ImageFormat.NV21);
        // Default focus mode
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        // Done
        camera.setParameters(parameters);

        // The camera will fill the buffers with image data and notify us through the callback.
        // The buffers will be sent to camera on requests from recognition service (see implementation
        // of ITextCaptureService.Callback.onRequestLatestFrame above)
        camera.setPreviewCallbackWithBuffer(cameraPreviewCallback);


        // Clear the previous recognition results if any
        clearRecognitionResults();

        // Width and height of the preview according to the current screen rotation
        int width = 0;
        int height = 0;
        switch (orientation) {
            case 0:
            case 180:
                width = cameraPreviewSize.width;
                height = cameraPreviewSize.height;
                break;
            case 90:
            case 270:
                width = cameraPreviewSize.height;
                height = cameraPreviewSize.width;
                break;
        }

        // Configure the view scale and area of interest (camera sees it as rotated 90 degrees, so
        // there's some confusion with what is width and what is height)
        surfaceViewWithOverlay.setScaleX(surfaceViewWithOverlay.getWidth(), width);
        surfaceViewWithOverlay.setScaleY(surfaceViewWithOverlay.getHeight(), height);
        // Area of interest
        int marginWidth = (areaOfInterestMargin_PercentOfWidth * width) / 100;
        int marginHeight = (areaOfInterestMargin_PercentOfHeight * height) / 100;
        surfaceViewWithOverlay.setAreaOfInterest(
                new Rect(marginWidth, marginHeight, width - marginWidth,
                        height - marginHeight));

        // Start preview
        camera.startPreview();

        setCameraFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        autoFocus(finishCameraInitialisationAutoFocusCallback);

        inPreview = true;
    }

    private void seekValue() {
        seekBar = (VerticalSeekBar) findViewById(R.id.seek);
        seekBar.setMax(maxZoom);
        Log.d("maxzoomnew", String.valueOf(maxZoom));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //  configureCameraAndStartPreview(camera);
                if (camera.getParameters().isZoomSupported()) {

                    Camera.Parameters params = camera.getParameters();
                    params.setZoom(i);
                    camera.setParameters(params);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void onStartButtonClick(View view) {
        onStartButtonClick();
    }

    // The 'Start' and 'Stop' button
    @SuppressLint("ResourceType")
    public void  onStartButtonClick() {
        try {
//             set vibrator
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
            // hide print button
            print.setVisibility(View.GONE);
            // change color of surface view
            surfaceViewWithOverlay.setBackgroundResource(Color.TRANSPARENT);
//          clear result from view of text
            clearRecognitionResults();

//          stop counter
            if (yourCountDownTimer != null) {
                yourCountDownTimer.cancel();
            }

//           hide text view
            textView.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT < 18) {
                webView.clearView();
            } else {
                webView.loadUrl("about:blank");
            }
// hide webview of error
            webView.setVisibility(View.GONE);

//          hide resume button
            startButton.setVisibility(View.GONE);
            startButton.setEnabled(false);

//            start recognize text
            if (!isContinuousVideoFocusModeEnabled(camera)) {
                autoFocus(startRecognitionCameraAutoFocusCallback);
            } else {
                startRecognition();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_reading);
      //  drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
       // navigationView = (NavigationView) findViewById(R.id.nav_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //setUpNavigationView();
        init();
    }

    private void init() {
        // Retrieve some ui components
        warningTextView = (TextView) findViewById(R.id.warningText);
        errorTextView = (TextView) findViewById(R.id.errorText);
        startButton = (Button) findViewById(R.id.startButton);
        print = (Button) findViewById(R.id.print);
        webView = (WebView) findViewById(R.id.webview);
        relativeLayout = (RelativeLayout) findViewById(R.id.layout);
        textView = (TextView) findViewById(R.id.counter);
        bar = findViewById(R.id.bar);
        animation = AnimationUtils.loadAnimation(ScanReading.this, R.anim.myanim);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });


        led = (ImageView) findViewById(R.id.led);
        print.setOnClickListener(this);
        Context context = this;
        PackageManager pm = context.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e("err", "Device has no camera!");
            return;
        }
        led.setOnClickListener(this);

        // Manually create preview surface. The only reason for this is to
        // avoid making it public top level class
        final RelativeLayout layout = (RelativeLayout) startButton.getParent();

        surfaceViewWithOverlay = new SurfaceViewWithOverlay(this);
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        surfaceViewWithOverlay.setLayoutParams(params);
        // Add the surface to the layout as the bottom-most view filling the parent
        layout.addView(surfaceViewWithOverlay, 0);

        // Create text capture service
        if (createTextCaptureService()) {
            // Set the callback to be called when the preview surface is ready.
            // We specify it as the last step as a safeguard so that if there are problems
            // loading the engine the preview will never start and we will never attempt calling the service
            surfaceViewWithOverlay.getHolder().addCallback(surfaceCallback);
        }

        layout.setOnClickListener(clickListener);
        surfaceViewWithOverlay.setOnTouchListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reinitialize the camera, restart the preview and recognition if required
        startButton.setEnabled(false);
        clearRecognitionResults();
        startRecognitionWhenReady = startRecognitionOnAppStart;
        camera = Camera.open();
        if (previewSurfaceHolder != null) {
            setCameraPreviewDisplayAndStartPreview();
        }
    }

    @Override
    public void onPause() {
        // Clear all pending actions
        handler.removeCallbacksAndMessages(null);
        // Stop the text capture service
        if (textCaptureService != null) {
            textCaptureService.stop();
        }
        // startButton.setText(BUTTON_TEXT_START);
        // Clear recognition results
        clearRecognitionResults();
        stopPreviewAndReleaseCamera();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (camera != null) {
            camera.release();
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // Get the pointer ID
        Camera.Parameters params = camera.getParameters();
        int action = motionEvent.getAction();

        if (motionEvent.getPointerCount() > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(motionEvent);
            } else if (action == MotionEvent.ACTION_MOVE
                    && params.isZoomSupported()) {
                camera.cancelAutoFocus();
            }
            handleZoom(motionEvent, params);
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_UP) {
                handleFocus(motionEvent, params);
            }
        }
        return true;
    }

    private void handleZoom(MotionEvent event, final Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        zoom = params.getZoom();
        float newDist = getFingerSpacing(event);

//         calculate percentage of zoom
        double percentage = (mDist + newDist) / mDist;
        if (mDist > newDist) {
            percentage *= -3;
        }
        if (mDist < newDist) {
            percentage *= 3;
        }

//        set zoom lavel
        zoom = new Double(zoom + percentage).intValue();

        // if intial percentage is infinite
        if (Double.isInfinite(percentage)) {
            zoom = maxZoom / 2;

        }

        if (zoom > maxZoom) {
            zoom = maxZoom;
        }
        if (zoom < 0) {
            zoom = 0;
        }

        mDist = newDist;
        // Update seekbar Position
        seekBar.setProgress(zoom);
        seekBar.updateThumb();
        params.setZoom(zoom);
        camera.setParameters(params);
    }


    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null
                && supportedFocusModes
                .contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                    if (camera != null) {
                        try {
                            setCameraFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                        } catch (Exception e) {
                            Log.e(getString(R.string.app_name), "Error: " + e.getMessage());
                        }
                    }

                }
            });
        }
    }


    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }


    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.profile:
                        Intent intent0 = new Intent(ScanReading.this, MyProfile.class);
                        intent0.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent0);
                        break;
                    case R.id.history:
                        Intent intent1 = new Intent(ScanReading.this, History.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent1);
                        break;
                    case R.id.scan:
                        Intent intent2 = new Intent(ScanReading.this, ScanReading.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent2);
                        break;
                    default:
                        Intent intent3 = new Intent(ScanReading.this, HomeActivity.class);
                        intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent3);
                }
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                item.setChecked(true);

                return true;
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }


    @Override
    public void onClick(View v) {
        if (v == led) {
            if (isLighOn) {
                Log.i("info", "torch is turn off!");

                Camera.Parameters parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                led.setImageResource(R.drawable.flashon);
                isLighOn = false;
            } else {
                Log.i("info", "torch is turn on!");

                Camera.Parameters parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                led.setImageResource(R.drawable.flashoff);
                camera.startPreview();
                isLighOn = true;
            }
        }

    }

    // Surface View combined with an overlay showing recognition results and 'progress'
    static class SurfaceViewWithOverlay extends SurfaceView {
        private Point[] quads;
        private String[] lines;
        private Rect areaOfInterest;
        private int stability;
        private int scaleNominatorX = 1;
        private int scaleDenominatorX = 1;
        private int scaleNominatorY = 1;
        private int scaleDenominatorY = 1;
        private Paint textPaint;
        private Paint lineBoundariesPaint;
        private Paint backgroundPaint;
        private Paint areaOfInterestPaint;

        public SurfaceViewWithOverlay(Context context) {
            super(context);
            this.setWillNotDraw(false);

            lineBoundariesPaint = new Paint();
            lineBoundariesPaint.setStyle(Paint.Style.STROKE);
            lineBoundariesPaint.setARGB(255, 128, 128, 128);
            textPaint = new Paint();
            areaOfInterestPaint = new Paint();
            areaOfInterestPaint.setARGB(100, 0, 0, 0);
            areaOfInterestPaint.setStyle(Paint.Style.FILL);
        }

        public void setScaleX(int nominator, int denominator) {
            scaleNominatorX = nominator;
            scaleDenominatorX = denominator;
        }

        public void setScaleY(int nominator, int denominator) {
            scaleNominatorY = nominator;
            scaleDenominatorY = denominator;
        }

        public void setFillBackground(Boolean newValue) {
            if (newValue) {
                backgroundPaint = new Paint();
                backgroundPaint.setStyle(Paint.Style.FILL);
                backgroundPaint.setARGB(100, 255, 255, 255);
            } else {
                backgroundPaint = null;
            }
            invalidate();
        }


        public void setAreaOfInterest(Rect newValue) {
            areaOfInterest = newValue;
            invalidate();
        }

        public Rect getAreaOfInterest() {
            return areaOfInterest;
        }

        public void setLines(ITextCaptureService.TextLine[] lines,
                             ITextCaptureService.ResultStabilityStatus resultStatus) {
            if (lines != null && scaleDenominatorX > 0 && scaleDenominatorY > 0) {
                this.quads = new Point[lines.length * 4];
                this.lines = new String[lines.length];
                for (int i = 0; i < lines.length; i++) {
                    ITextCaptureService.TextLine line = lines[i];
                    for (int j = 0; j < 4; j++) {
                        this.quads[4 * i + j] = new Point(
                                (scaleNominatorX * line.Quadrangle[j].x) / scaleDenominatorX,
                                (scaleNominatorY * line.Quadrangle[j].y) / scaleDenominatorY
                        );
                    }
                    this.lines[i] = line.Text;
                }
                switch (resultStatus) {
                    case NotReady:
                        textPaint.setARGB(255, 128, 0, 0);
                        break;
                    case Tentative:
                        textPaint.setARGB(255, 128, 0, 0);
                        break;
                    case Verified:
                        textPaint.setARGB(255, 128, 64, 0);
                        break;
                    case Available:
                        textPaint.setARGB(255, 128, 128, 0);
                        break;
                    case TentativelyStable:
                        textPaint.setARGB(255, 64, 128, 0);
                        break;
                    case Stable:
                        textPaint.setARGB(255, 0, 128, 0);
                        break;
                }
                stability = resultStatus.ordinal();

            } else {
                stability = 0;
                this.lines = null;
                this.quads = null;
            }
            this.invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            canvas.save();
            // If there is any result
            if (lines != null) {
                // Shade (whiten) the background when stable
                if (backgroundPaint != null) {
                    canvas.drawRect(0, 0, width, height, backgroundPaint);
                }
            }
            if (areaOfInterest != null) {
                // Shading and clipping the area of interest
                int left = (areaOfInterest.left * scaleNominatorX) / scaleDenominatorX;
                int right = (areaOfInterest.right * scaleNominatorX) / scaleDenominatorX;
                int top = (areaOfInterest.top * scaleNominatorY) / scaleDenominatorY;
                int bottom = (areaOfInterest.bottom * scaleNominatorY) / scaleDenominatorY;
                canvas.drawRect(0, 0, width, top, areaOfInterestPaint);
                canvas.drawRect(0, bottom, width, height, areaOfInterestPaint);
                canvas.drawRect(0, top, left, bottom, areaOfInterestPaint);
                canvas.drawRect(right, top, width, bottom, areaOfInterestPaint);
                canvas.drawRect(left, top, right, bottom, lineBoundariesPaint);
                canvas.clipRect(left, top, right, bottom);
            }
            // If there is any result
            if (lines != null) {
                // Draw the text lines
                for (int i = 0; i < lines.length; i++) {
                    // The boundaries
                    int j = 4 * i;
                    Path path = new Path();
                    Point p = quads[j + 0];
                    path.moveTo(p.x, p.y);
                    p = quads[j + 1];
                    path.lineTo(p.x, p.y);
                    p = quads[j + 2];
                    path.lineTo(p.x, p.y);
                    p = quads[j + 3];
                    path.lineTo(p.x, p.y);
                    path.close();
                    canvas.drawPath(path, lineBoundariesPaint);

                    // The skewed text (drawn by coordinate transform)
                    canvas.save();
                    Point p0 = quads[j + 0];
                    Point p1 = quads[j + 1];
                    Point p3 = quads[j + 3];

                    int dx1 = p1.x - p0.x;
                    int dy1 = p1.y - p0.y;
                    int dx2 = p3.x - p0.x;
                    int dy2 = p3.y - p0.y;

                    int sqrLength1 = dx1 * dx1 + dy1 * dy1;
                    int sqrLength2 = dx2 * dx2 + dy2 * dy2;

                    double angle = 180 * Math.atan2(dy2, dx2) / Math.PI;
                    double xskew = (dx1 * dx2 + dy1 * dy2) / Math.sqrt(sqrLength2);
                    double yskew = Math.sqrt(sqrLength1 - xskew * xskew);

                    textPaint.setTextSize((float) yskew);
                    String line = lines[i];
                    Rect textBounds = new Rect();
                    textPaint.getTextBounds(lines[i], 0, line.length(), textBounds);
                    double xscale = Math.sqrt(sqrLength2) / textBounds.width();

                    canvas.translate(p0.x, p0.y);
                    canvas.rotate((float) angle);
                    canvas.skew(-(float) (xskew / yskew), 0.0f);
                    canvas.scale((float) xscale, 1.0f);

                    canvas.drawText(lines[i], 0, 0, textPaint);
                    canvas.restore();
                }
            }
            canvas.restore();

            // Draw the 'progress'
            if (stability > 0) {
//                int r = width / 50;
//                int y = height - 175 - 2 * r;
//                for (int i = 0; i < stability; i++) {
//                    int x = width / 2 + 3 * r * (i - 2);
//                    canvas.drawCircle(x, y, r, textPaint);
//                }
            }
        }
    }
}

