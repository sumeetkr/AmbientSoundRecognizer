package in.sumeetkumar.asr;

import in.sumeetkumar.asr.util.DatabaseHandler;
import in.sumeetkumar.asr.util.Feature;
import in.sumeetkumar.asr.util.FeaturesMatchFinderTask;
import in.sumeetkumar.asr.util.LoadFeaturesTask;
import in.sumeetkumar.asr.util.MicConnection;
import in.sumeetkumar.asr.util.SDCardWriter;
import in.sumeetkumar.asr.util.UserInputManager;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.probe.Probe.DataListener;
import edu.mit.media.funf.probe.builtin.AudioFeaturesProbe;
import edu.mit.media.funf.util.LogUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
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
	private DataListener listener;
	private LinkedList<IJsonObject> dataLog;
	private int dataCount;
	private MicConnection funfManagerConn;

	private void onListen(boolean start) {
		if (start) {
			startListening();
		} else {
			stopListening();
		}
	}

	private void startListening() {
		dataLog = new LinkedList<IJsonObject>();
		dataCount = 0;

		getFeaturesFromDatabase();

		if (funfManagerConn.isEnabled()) {
			// Manually register the pipeline
			funfManagerConn.registerListener(listener);
		} else {
			Toast.makeText(this.getActivity().getBaseContext(),
					"Pipeline is not enabled.", Toast.LENGTH_SHORT).show();
		}

		//updateUI(" started listening");
	}

	private void updateUI(final String message) {
		this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < matches.length; i++) {
					matches[i] = "Updated Sound " + i + message;
				}

				arrayAdapter.notifyDataSetChanged();
			}

		});
	}

	private void stopListening() {
		funfManagerConn.unregisterListener(listener);
		//updateUI(" stopped listening");
	}

	private void writeDataToFile() {
		StringBuilder stringBuilder = new StringBuilder();
		Iterator<IJsonObject> itr = dataLog.iterator();
		while (itr.hasNext()) {
			IJsonObject jsonObject = itr.next();
			stringBuilder.append(jsonObject.toString());
			stringBuilder.append('\n');
		}
		SDCardWriter.generateNoteOnSD(SDCardWriter.FILE_NAME,
				stringBuilder.toString());

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

			setText("Connecting Mic");
			setOnClickListener(clicker);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_find, container,
				false);

		initializeDependencies();

		this.getActivity().bindService(
				new Intent(this.getActivity(), FunfManager.class),
				funfManagerConn, this.getActivity().BIND_AUTO_CREATE);

		listenButton = new ListenButton(this.getActivity());

		ViewGroup ll = (ViewGroup) rootView.findViewById(R.id.listener_view);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		ll.addView(listenButton, lp);

		getFeaturesFromDatabase();

		matchesListView = (ListView) rootView
				.findViewById(R.id.list_suggestions);
		matches = new String[5];

		for (int i = 0; i < matches.length; i++) {
			matches[i] = "";
		}

		arrayAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_list_item_1, matches);
		matchesListView.setAdapter(arrayAdapter);

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

		funfManagerConn = new MicConnection();
		
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

				// new FeaturesMatchFinderTask(matches, arrayAdapter, new
				// Feature(dataCopy) ).execute(existingFeaturesInDatabase);
				handler.post(new Runnable() {
					@Override
					public void run() {
						FeaturesMatchFinderTask task = new FeaturesMatchFinderTask(
								matches, arrayAdapter, new Feature(dataCopy));
						task.doInBackground(existingFeaturesInDatabase);
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

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.listen, menu);
		return true;
	}

	// @Override
	// public void onStop() {
	//
	// //throwing exception
	// // if (funfManager != null) {
	// // funfManager.disablePipeline(PIPELINE_NAME);
	// // }
	//
	// }
}
