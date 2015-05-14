package cc.makesense.mop;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends UnityPlayerActivity {

	public static final String TAG = "Unity";
    public static final String UnityObjectName = "AndroidManager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d (TAG, "onCreate");
        UnityPlayer.UnitySendMessage(UnityObjectName, "Echo", "onCreate");
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
