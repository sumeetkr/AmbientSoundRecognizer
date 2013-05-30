package in.sumeetkumar.asr;

import java.util.Iterator;
import java.util.LinkedList;

import com.google.gson.JsonElement;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.probe.Probe.DataListener;
import edu.mit.media.funf.util.LogUtil;
import in.sumeetkumar.asr.FindFragment.ListenButton;
import in.sumeetkumar.asr.util.DatabaseHandler;
import in.sumeetkumar.asr.util.Feature;
import in.sumeetkumar.asr.util.FeaturesMatchFinderTask;
import in.sumeetkumar.asr.util.MicConnection;
import in.sumeetkumar.asr.util.UserInputManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RecorderFragment extends Fragment {

	public static final String ARG_SECTION_NUMBER = "section_number";

	private RecordButton recordButton;
	private ListView featuresListView;
	private String[] matches;
	private ArrayAdapter<String> arrayAdapter;
	private Handler handler;
	private DataListener listener;
	private LinkedList<IJsonObject> dataLog;
	private int dataCount;
	private MicConnection funfManagerConn;
	private String labelString;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_recorder, container,
				false);

		recordButton = new RecordButton(this.getActivity());

		ViewGroup ll = (ViewGroup) rootView.findViewById(R.id.listener_view);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		ll.addView(recordButton, lp);

		featuresListView = (ListView) rootView
				.findViewById(R.id.list_suggestions);
		matches = new String[5];

		for (int i = 0; i < matches.length; i++) {
			matches[i] = "";
		}

		arrayAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_list_item_1, matches);
		featuresListView.setAdapter(arrayAdapter);

		initializeDependencies();
		
		this.getActivity().bindService(
				new Intent(this.getActivity(), FunfManager.class),
				funfManagerConn, this.getActivity().BIND_AUTO_CREATE);

		return rootView;
	}

	private void onRecord(boolean isStart) {

		if (isStart) {
			startRecording();
		} else {
			stopRecording();
		}
	}

	private void startRecording() {
		dataLog = new LinkedList<IJsonObject>();
		dataCount = 0;

		if (funfManagerConn.isEnabled()) {
			// Manually register the pipeline
			funfManagerConn.registerListener(listener);
		} else {
			Toast.makeText(this.getActivity().getBaseContext(),
					"Pipeline is not enabled.", Toast.LENGTH_SHORT).show();
		}
	}

	private void stopRecording() {

		funfManagerConn.unregisterListener(listener);

		UserInputManager uim = new UserInputManager();
		Callback callback = new Callback() {
			public boolean handleMessage(Message msg) {
				applyLabel(String.valueOf(msg.obj));
				return true;
			}
		};
		uim.getLabel(getActivity(), "Label the sound", callback);
	}
	
	private void applyLabel(String value) {
		labelString = value;

		// TODO - need to move it as event handler
		if (labelString != "") {
			// writeDataToFile();
			writeDataToDatabase(labelString);
		}
		labelString = "";
	}
	
	private void writeDataToDatabase(String name) {
		DatabaseHandler db = new DatabaseHandler(this.getActivity());

		Iterator<IJsonObject> itr = dataLog.iterator();
		while (itr.hasNext()) {
			IJsonObject jsonObject = itr.next();

			Feature feature = new Feature(jsonObject, name);
			db.addFeature(feature);
		}

		// verify
		int featuresCount = db.getFeaturesCount();
		Toast.makeText(
				this.getActivity(),
				"Featues Added for " + name + " features new count: "
						+ featuresCount, Toast.LENGTH_LONG).show();

		db.close();

		dataLog = new LinkedList<IJsonObject>();
		dataCount = 0;
	}

	private void initializeDependencies() {
		funfManagerConn = new MicConnection();
		labelString = "";
		
		dataCount = 0;
		dataLog = new LinkedList<IJsonObject>();
		handler = new Handler();
		listener = new DataListener() {
			@Override
			public void onDataReceived(IJsonObject probeConfig, IJsonObject data) {
				final IJsonObject dataCopy = data;
				dataCount += 1;
				dataLog.add(dataCopy);
				Log.d(LogUtil.TAG, "RunningApplications: " + dataCopy);
				
				handler.post(new Runnable() {
					@Override
					public void run() {
						matches[0] = "l1Norm "
								+ dataCopy.get("l1Norm").toString();
						matches[1] = "l2Norm "
								+ dataCopy.get("l2Norm").toString();
						matches[2] = "linfNorm "
								+ dataCopy.get("linfNorm").toString();
						matches[3] = "mfccs "
								+ dataCopy.get("mfccs").toString();
						matches[4] = "diffSecs "
								+ dataCopy.get("diffSecs").toString();

						arrayAdapter.notifyDataSetChanged();
					}
				});

			}

			@Override
			public void onDataCompleted(IJsonObject probeConfig,
					JsonElement checkpoint) {
				// TODO Auto-generated method stub

			}
		};
	}

	class RecordButton extends Button {
		boolean mStartListening = true;

		OnClickListener clicker = new OnClickListener() {
			public void onClick(View v) {
				onRecord(mStartListening);
				if (mStartListening) {
					setText("Stop");
				} else {
					setText("Start");
				}
				mStartListening = !mStartListening;
			}
		};

		public RecordButton(Context ctx) {
			super(ctx);

			Drawable drawable = getResources().getDrawable(
					R.drawable.ic_listner5);
			drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * 2),
					(int) (drawable.getIntrinsicHeight() * 2));
			setCompoundDrawables(null, drawable, null, null);

			setText("Start");
			setOnClickListener(clicker);
		}
	}
}
