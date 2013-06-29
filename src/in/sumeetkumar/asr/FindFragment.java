package in.sumeetkumar.asr;

import in.sumeetkumar.asr.data.AudioData;
import in.sumeetkumar.asr.data.Feature;
import in.sumeetkumar.asr.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import in.sumeetkumar.asr.data.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.R.bool;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class FindFragment extends Fragment {

	private String[] matches;
	private ArrayAdapter<String> arrayAdapter;
	private ListView matchesListView;
	private ListenButton listenButton;
	private List<Feature> existingFeaturesInDatabase;
	private Handler handler;
	private int dataCount;
	private AudioProcessor audioProcessor;
	private AudioFeaturesExtractor featuresExtractor;
	private ASRFileWriter fileWriter;
	private Boolean isListening;

	// private MicConnection funfManagerConn;

	private void onListen(boolean start) {
		if (start) {
			startListening();
		} else {
			stopListening();
		}
	}

	private void startListening() {
		dataCount = 0;
		isListening = true;

		getFeaturesFromDatabase();
		audioProcessor = new AudioProcessor() {
			int i = 0;

			@Override
			protected void dataArrival(long timestamp, short[] data,
					int length, int frameLength) {
				super.dataArrival(timestamp, data, length, frameLength);

				if (i % 5 == 0 && isListening) {// skip 5 frames
					AudioData audioData = new AudioData(timestamp, data);
					matchFeatures(featuresExtractor.extractFeatures(audioData));
					updateUI("");
				}

				if (i == 100) {
					i = 0;
				}
				i++;

			}

			protected void onRecordEnded() {
				//dumpAudioData(this);
				this.clearSamples();
			}
		};

		audioProcessor.startRecord();
	}

	private void updateUI(final String message) {
		this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				arrayAdapter.notifyDataSetChanged();
			}

		});
	}

	private void stopListening() {
		isListening = false;
		audioProcessor.stopRecord();
	}

	class ListenButton extends Button {
		boolean mStartListening = true;

		OnClickListener clicker = new OnClickListener() {
			public void onClick(View v) {
				onListen(mStartListening);
				if (mStartListening) {
					setText("Stop");
				} else {
					setText("Start");
				}
				mStartListening = !mStartListening;
			}
		};

		public ListenButton(Context ctx) {
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_find, container,
				false);

		initializeDependencies();

		listenButton = new ListenButton(this.getActivity());

		ViewGroup ll = (ViewGroup) rootView.findViewById(R.id.listener_view);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		ll.addView(listenButton, lp);


		matchesListView = (ListView) rootView
				.findViewById(R.id.list_suggestions);
		matches = new String[5];

		for (int i = 0; i < matches.length; i++) {
			matches[i] = "";
		}

		arrayAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_list_item_1, matches);
		matchesListView.setAdapter(arrayAdapter);

		getFeaturesFromDatabase();
		return rootView;
	}

	private void getFeaturesFromDatabase() {

		final DatabaseHandler db = new DatabaseHandler(this.getActivity());

		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					// get few sounds from database
					existingFeaturesInDatabase = db.getAllFeatures();
					Log.d(LogUtil.TAG, "Features count: "
							+ existingFeaturesInDatabase.size());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};

		thread.start();
		// Toast.makeText(this, "Existing Featues Count " +
		// existingFeaturesInDatabase.size(), Toast.LENGTH_LONG).show();
	}

	private void initializeDependencies() {
		dataCount = 0;
		featuresExtractor = new AudioFeaturesExtractor();
		handler = new Handler();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.listen, menu);
		return true;
	}

	private void dumpAudioData(AudioProcessor processor) {
		ArrayList<KeyValuePair<Long, short[]>> samples = audioProcessor
				.getSamples();

		fileWriter = new ASRFileWriter("AudioData");

		for (KeyValuePair<Long, short[]> sample : samples) {
			String sampleString = Arrays.toString(sample.getValue());
			int length = sampleString.length();
			fileWriter.appendText(sample.getKey() + ","
					+ sampleString.substring(1, length - 1));
		}

		fileWriter.close();
	}

	private void matchFeatures(Feature features) {
		final Feature featureCopy = features;
		// dataCount += 1;
		// dataLog.add(dataCopy);
		// Log.d(LogUtil.TAG, "RunningApplications: " + dataCopy);

		// new FeaturesMatchFinderTask(matches, arrayAdapter, new
		// Feature(dataCopy) ).execute(existingFeaturesInDatabase);
		handler.post(new Runnable() {
			@Override
			public void run() {
				FeaturesMatchFinderTask task = new FeaturesMatchFinderTask(
						matches, arrayAdapter, featureCopy);
				task.doInBackground(existingFeaturesInDatabase);
			}
		});
	}

	@Override
	public void onStop() {

		if (audioProcessor.isAlive()) {
			audioProcessor.stopRecord();
		}
		audioProcessor = null;

		super.onStop();
	}
}
