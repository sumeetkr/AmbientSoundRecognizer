package in.sumeetkumar.asr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import javax.security.auth.PrivateCredentialPermission;

import com.google.gson.JsonElement;
import in.sumeetkumar.asr.FindFragment.ListenButton;
import in.sumeetkumar.asr.data.AudioData;
import in.sumeetkumar.asr.data.Feature;
import in.sumeetkumar.asr.data.KeyValuePair;
import in.sumeetkumar.asr.util.ASRFileWriter;
import in.sumeetkumar.asr.util.AudioFeaturesExtractor;
import in.sumeetkumar.asr.util.AudioProcessor;
import in.sumeetkumar.asr.util.DatabaseHandler;
import in.sumeetkumar.asr.util.FeaturesMatchFinderTask;
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
	private int dataCount;
	private String labelString;
	private Boolean isListening;
	private AudioProcessor audioProcessor;
	private AudioFeaturesExtractor featuresExtractor;
	LinkedList<Feature> audioFeatures;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_recorder, container,
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

		dataCount = 0;
		isListening = true;

		audioProcessor = new AudioProcessor() {
			@Override
			protected void dataArrival(long timestamp, short[] data,
					int length, int frameLength) {
				super.dataArrival(timestamp, data, length, frameLength);

				if(isListening){
					AudioData audioData = new AudioData(timestamp, data);
					final Feature feature = featuresExtractor.extractFeatures(audioData);
					audioFeatures.add(feature);
					
					handler.post(new Runnable() {
					@Override
					public void run() {
						matches[0] = "l1Norm "
								+ feature.getL1Norm();
						matches[1] = "l2Norm "
								+ feature.getL2Norm();
						matches[2] = "linfNorm "
								+ feature.getLinfNorm();
						matches[3] = "mfccs "
								+ feature.getMfccsAsString();
						matches[4] = "diffSecs "
								+ feature.getDiffSecs();

						arrayAdapter.notifyDataSetChanged();
					}
				});	
				}
			}

		};

		audioProcessor.startRecord();
		
	}

	private void stopRecording() {
		isListening = false;
		audioProcessor.stopRecord();

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
			dumpAudioData( audioProcessor.getSamples(), labelString);
			writeDataToDatabase(labelString);
		}
		labelString = "";
		
		//need to move to better place
		audioProcessor.clearSamples();
	}
	
	private void writeDataToDatabase(String name) {
		DatabaseHandler db = new DatabaseHandler(this.getActivity());
		
		Iterator<Feature> itr = audioFeatures.iterator();
		while (itr.hasNext()) {
			Feature feature = itr.next();
			feature.setName(name);
			db.addFeature(feature);
		}

		// verify
		int featuresCount = db.getFeaturesCount();
		Toast.makeText(
				this.getActivity(),
				"Featues Added for " + name + " features new count: "
						+ featuresCount, Toast.LENGTH_LONG).show();

		db.close();

		audioFeatures = new LinkedList<Feature>();
		dataCount = 0;
	}

	private void dumpAudioData(ArrayList<KeyValuePair<Long, short[]>> samples, String label) {

		ASRFileWriter fileWriter = new ASRFileWriter(label);

		for (KeyValuePair<Long, short[]> sample : samples) {
			String sampleString = Arrays.toString(sample.getValue());
			int length = sampleString.length();
			fileWriter.appendText(sample.getKey() + ","
					+ sampleString.substring(1, length - 1));
		}

		fileWriter.close();
	}
	
	private void initializeDependencies() {
		
		audioProcessor = new AudioProcessor();
		audioFeatures = new LinkedList<Feature>();
		handler = new Handler();
		featuresExtractor = new AudioFeaturesExtractor();
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
