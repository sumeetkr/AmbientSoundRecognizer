package in.sumeetkumar.asr.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.probe.Probe.DataListener;
import edu.mit.media.funf.probe.builtin.AudioFeaturesProbe;

public class MicConnection implements ServiceConnection {

	public static final String PIPELINE_NAME = "default";
	private FunfManager funfManager;
	private BasicPipeline pipeline;
	private AudioFeaturesProbe audioFeaturesProbe;
	private List<DataListener> listners;
	
	public MicConnection() {
		listners = new ArrayList<DataListener>();
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		funfManager = ((FunfManager.LocalBinder) service).getManager();

		Gson gson = funfManager.getGson();
		audioFeaturesProbe = gson.fromJson(new JsonObject(),
				AudioFeaturesProbe.class);

		pipeline = (BasicPipeline) funfManager
				.getRegisteredPipeline(PIPELINE_NAME);

		// audioFeaturesProbe.registerListener(listener);

		if (funfManager != null) {

			funfManager.enablePipeline(PIPELINE_NAME);
			pipeline = (BasicPipeline) funfManager
					.getRegisteredPipeline(PIPELINE_NAME);
		}

	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		funfManager = null;
	}
	
	public boolean isEnabled() {
		return pipeline.isEnabled();
	}

	public void registerListener(DataListener listener) {
		listners.add(listener);
		audioFeaturesProbe.registerListener(listener);
		
	}

	public void unregisterListener(DataListener listener) {
		listners.remove(listener);
		audioFeaturesProbe.unregisterListener(listener);
		
	}
}