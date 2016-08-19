package cc.makesense.mop;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends UnityPlayerActivity {

	public static final String TAG = "Unity";
    public static final boolean D = true;

	private BluetoothAdapter mBluetoothAdapter = null;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final String DEVICE_NAME = "SiCiBluetooth";
    public static final String UnityObjectName = "AndroidManager";
    private BTManager mBTManager = null;

	private Camera mCamera;
	// Holds the Face Detection result:
    private Camera.Face[] mFaces;	
    
    // Start Region @ Function about BT communication
    public void SendMessage(String hexMessage) {
        // Check that we're actually connected before trying anything
        if (mBTManager.getState() != BTManager.STATE_CONNECTED) {
            Log.d (TAG, "Bluetooth is not connected!");
            return;
        }

        int num = hexMessage.length();
        byte[] message = new byte[num / 2];
        for (int i = 0; i < num; i += 2) {
            String hex = hexMessage.substring(i, i + 2);
            message[i / 2] = (byte) Integer.parseInt(hex, 16);
//            Log.d("Unity", hex + " -> " + message[i / 2]);
        }
//        Log.d("Unity", hexMessage + " => " + message[0] + ":" + message[1]
//                + ":" + message[2] + ":" + message[3] + ":" + message[4] + ":"
//                + message[5]);
        
        try {
            mBTManager.write(message);
        } catch (Exception e) {
            Log.d (TAG, "Write Error:" + e);
        }
    }
    
    public void SearchBluetoothDevice() {
        try {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                    .getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice bd : pairedDevices) {
                    String deviceName = bd.getName();
                    Log.d (TAG, deviceName + " => " + bd.getAddress());

                    UnityPlayer.UnitySendMessage(UnityObjectName, "BluetoothDevice", deviceName);
                }
            } else {
                Log.d (TAG, "Paired devices not found!");
            }

            UnityPlayer.UnitySendMessage(UnityObjectName, "BluetoothDevice", "END");
            Log.d (TAG, "SearchBluetoothDevice " + "END");
        } catch (Exception e) {
            Log.d (TAG, "Error:" + e.getMessage());
        }
    }
    
    public void ConnectDevice(String deviceName) {
        Log.d(TAG, "ConnectDevice");

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : pairedDevices) {
            if (bd.getName().equalsIgnoreCase(deviceName)) {
                mBTManager.connect(bd);
                UnityPlayer.UnitySendMessage(UnityObjectName, "BluetoothConnectState", "BT_Success");
                return;
            }
        }
        UnityPlayer.UnitySendMessage(UnityObjectName, "BluetoothConnectState", "Fail");
    }

    public void DisconnectBluetooth() {
	    try {
	        Log.d (TAG, "Attempting to break BT connection");
	        if (mBTManager != null) {
	            mBTManager.stop();
	            mBTManager = null;
	        }
	        UnityPlayer.UnitySendMessage(UnityObjectName,
	                "BluetoothConnectState", "Fail");
	    } catch (Exception e) {
	        Log.d (TAG, "Error in DoDisconnect [" + e.getMessage() + "]");
	    }
    }
    
    public void EnableFaceDetect () {
//        Log.d(TAG, "[EnableFaceDetect]");
    	mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
    	mCamera.setFaceDetectionListener(faceDetectionListener);
        
    	// Get the supported preview sizes:
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size previewSize = previewSizes.get(0);
        // And set them:
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        mCamera.setParameters(parameters);
        mCamera.startPreview();

        mCamera.startFaceDetection();
    }
    
    public void DisableFaceDetect () {
    	mCamera.stopFaceDetection();
    	mCamera.stopPreview();

    	mCamera.setPreviewCallback(null);
        mCamera.setFaceDetectionListener(null);
        mCamera.setErrorCallback(null);
        mCamera.release();
        mCamera = null;
    }
    
    /**
     * Sets the faces for the overlay view, so it can be updated
     * and the face overlays will be drawn again.
     */
    private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
//            Log.d (TAG, "[onFaceDetection Number of Faces:" + faces.length);
            if (faces.length > 0) {
            	String pos = Integer.toString(faces[0].rect.centerX()) + "," + Integer.toString(faces[0].rect.centerY());
                UnityPlayer.UnitySendMessage(UnityObjectName, "FacePos",  pos);
//            	Log.d (TAG, "[onFaceDetection] x: " + faces[0].rect.centerX() + ", y: " + faces[0].rect.centerY());
            }
		}
    };
    
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if (D)
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BTManager.STATE_CONNECTED:
                    UnityPlayer.UnitySendMessage(UnityObjectName, "BluetoothConnectState", "BT_STATE_CONNECTED");
                    Log.d (TAG, "STATE_CONNECTED");
                    break;
                case BTManager.STATE_CONNECTING:
                    UnityPlayer.UnitySendMessage(UnityObjectName, "BluetoothConnectState", "BT_STATE_CONNECTING");
                    Log.d (TAG, "STATE_CONNECTING");
                    break;
                case BTManager.STATE_LISTEN:
                case BTManager.STATE_NONE:
                    UnityPlayer.UnitySendMessage(UnityObjectName, "BluetoothConnectState", "BT_STATE_NOTCONNECTED");
                    Log.d (TAG, "STATE_NOTCONNECTED");
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                int[] writeData = new int[writeBuf.length];

                for (int i = 0; i < writeBuf.length; i++) {
                    if (writeBuf[i] < 0)
                        writeData[i] = (int) writeBuf[i] + 256;
                    else
                        writeData[i] = (int) writeBuf[i];
                }

                int writeValue = (int) ((writeData[4] << 8) | (writeData[2]));
                Log.d (TAG, "Write:" + Integer.toString(writeValue));
                break;
            case MESSAGE_READ:
                int len = (int) msg.arg1;
                byte[] readBuf = Arrays.copyOf((byte[]) msg.obj, len);
                UnityPlayer.UnitySendMessage(UnityObjectName,"BluetoothData", bytesToHex(readBuf));
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                Log.d (TAG, "Connected Device:" + msg.getData().getString(DEVICE_NAME));
                break;
            }
        }
    };
    
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int i = 0; i < bytes.length; i++ ) {
            v = bytes[i] & 0xFF;
            hexChars[i*2]     = hexArray[v >>> 4];
            hexChars[i*2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d (TAG, "onCreate");
        UnityPlayer.UnitySendMessage(UnityObjectName, "Echo", "onCreate");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTManager == null)
            mBTManager = new BTManager(this, mHandler);
 
        // Create an instance of Camera
        EnableFaceDetect ();
    }
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
        Log.d (TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
        Log.d (TAG, "onResume");
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
        Log.d (TAG, "onDestroy");
		super.onDestroy();
	}

	public void Echo (String message) {
    	Log.d (TAG, "[Android]Get echo message: " + message);
    	UnityPlayer.UnitySendMessage(UnityObjectName, "Echo", message);
    }
}
