package in.sumeetkumar.asr;

import in.sumeetkumar.asr.util.SDCardWriter;

import java.util.Iterator;
import java.util.LinkedList;

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
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ListenerActivity extends Activity {

	private String[] matches;
	private ArrayAdapter<String> arrayAdapter;
	private ListView matchesListView;
	public static final String PIPELINE_NAME = "default";
	private FunfManager funfManager;
	private BasicPipeline pipeline;
	private AudioFeaturesProbe audioFeaturesProbe;
	private Handler handler;
	private DataListener listener;
	private LinkedList<IJsonObject> dataLog;
	private int dataCount;
	ListenButton listenButton;
	Button editButton;
	String labelString = ""; 
	
	private void onListen(boolean start) {
		if (start) {
			startListening();
		} else {
			stopListening();
		}
	}

	private void startListening() {
		if (pipeline.isEnabled()) {
			// Manually register the pipeline
			audioFeaturesProbe.registerListener(listener);
		} else {
			Toast.makeText(getBaseContext(), "Pipeline is not enabled.",
					Toast.LENGTH_SHORT).show();
		}
		
		editButton.setVisibility(Button.INVISIBLE);
		updateUI(" started listening");
	}

	private void updateUI(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < matches.length; i++) {
					matches[i] = "Updated Song " + i + message;
				}

				arrayAdapter.notifyDataSetChanged();
			}

		});
	}

	private void stopListening() {

		editButton.setVisibility(Button.VISIBLE);
		audioFeaturesProbe.unregisterListener(listener);
		updateUI(" stopped listening");
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

	private ServiceConnection funfManagerConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			funfManager = ((FunfManager.LocalBinder) service).getManager();

			Gson gson = funfManager.getGson();
			audioFeaturesProbe = gson.fromJson(new JsonObject(),
					AudioFeaturesProbe.class);

			pipeline = (BasicPipeline) funfManager
					.getRegisteredPipeline(PIPELINE_NAME);

			audioFeaturesProbe.registerPassiveListener(listener);

			if (funfManager != null) {

				funfManager.enablePipeline(PIPELINE_NAME);
				pipeline = (BasicPipeline) funfManager
						.getRegisteredPipeline(PIPELINE_NAME);
			}

			// Set UI ready to use, by enabling buttons
			listenButton.setText("Ready to listen");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			funfManager = null;
		}

	};

	class ListenButton extends Button {
		boolean mStartListening = true;

		OnClickListener clicker = new OnClickListener() {
			public void onClick(View v) {
				onListen(mStartListening);
				if (mStartListening) {
					setText("Stop listening");
				} else {
					setText("Start listening");
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

	public void onClickEditBtn(View v)
    {
		if(dataCount>0) {
			editButton.setVisibility(Button.VISIBLE);
			getLabel();
			if(labelString != "") {
				writeDataToFile();	
			}

			labelString = "";
		}
		
        Toast.makeText(this, "Sound Labeled", Toast.LENGTH_LONG).show();
    } 
	
	private void getLabel() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Sound Recognizer");
		alert.setMessage("Label the sound");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  Editable value = input.getText();
		  	labelString = value.toString();
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initializeDependencies();
		setContentView(R.layout.activity_listen);
		bindService(new Intent(this, FunfManager.class), funfManagerConn,
				BIND_AUTO_CREATE);

		listenButton = new ListenButton(this);

		LinearLayout ll = (LinearLayout) findViewById(R.id.listener_view);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		ll.addView(listenButton, lp);

		// Initialize the UI components
		editButton = (Button)findViewById(R.id.button_edit);
		editButton.setVisibility(Button.INVISIBLE);
		
		matchesListView = (ListView) findViewById(R.id.list_suggestions);
		matches = new String[5];
		for (int i = 0; i < matches.length; i++) {
			matches[i] = "Song " + i;
		}

		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, matches);
		matchesListView.setAdapter(arrayAdapter);

	}

	private void initializeDependencies() {
		dataCount = 0;
		dataLog = new LinkedList<IJsonObject>();
		handler = new Handler();
		listener = new DataListener() {
			@Override
			public void onDataReceived(IJsonObject probeConfig, IJsonObject data) {
				// sample
				// "diffSecs":1.007000207901001,"l1Norm":12.620125,"l2Norm":15.705592156935694,"linfNorm":7.280109889280518,
				// "mfccs":[73.45780417766468,5.94377714299081,5.455124004455864,3.0556660679566736,2.0598351501842735,2.002186784717608,1.8324120016654795,1.2994454771981243,0.28606397137553624,0.19724540819105937,-0.6826126969146666,-0.6496579581333589],
				// "psdAcrossFrequencyBands":[5839808.109941236,387345.94407770695,89208.72127287657,22934.469227307152],"timestamp":1367304327.164}

				// {"diffSecs":41.07800006866455,"l1Norm":12.509,"l2Norm":21.531314404838366,"linfNorm":15.905973720586866,
				// "mfccs":[73.35071081139397,5.739566049218328,5.246120094945011,3.5691801907991576,3.235893874421781,2.9626390325264174,1.6552681174985016,0.029873280640131637,-1.3718713947743624,-0.165697525744605,-1.0225630834199853,-0.3089386212482912],
				// "psdAcrossFrequencyBands":[6708390.1264343085,280748.54138316837,83940.65067538692,24816.63704724058],
				// "rawData":[40,38,38,36,40,34,33,36,36,33,30,26,29,25,20,16,10,17,10,6,1,1,4,5,4,4,9,6,0,0,-6,-3,0,-7,2,2,1,5,13,17,17,14,16,21,21,20,16,22,17,18,22,22,32,29,24,29,26,24,26,22,18,24,24,24,18,18,17,14,18,26,25,21,21,20,18,14,17,20,21,20,18,20,21,20,24,22,21,24,20,16,16,17,14,12,14,10,8,2,8,8,6,2,2,2,1,2,1,5,5,2,1,-2,-2,4,2,-2,4,13,12,6,4,2,5,6,10,14,14,20,21,21,17,20,21,21,26,28,30,34,30,33,28,28,28,29,36,30,36,30,29,29,24,30,25,26,26,22,25,24,20,18,16,12,9,16,14,14,21,16,16,16,18,29,25,26,25,28,34,33,33,33,32,32,33,33,34,41,34,38,37,36,32,25,24,21,22,16,17,14,10,9,6,5,1,2,-3,-10,-9,-10,-18,-17,-21,-22,-21,-18,-17,-25,-23,-21,-19,-17,-10,-9,-7,-9,-11,-6,-2,0,0,-3,1,0,1,1,0,-3,4,2,-6,-2,-2,-3,-9,-9,-9,-10,-5,-2,-6,-7,-5,-5,-5,-3,-7,-6,-6,-6,-11,-9,-2,-2,4,-3,-2,-2,-2,-3,-3,-6,-14,-7,-6,-5,-2,0,0,1,0,2,1,1,1,1,0,1,6,5,8,6,12,10,8,8,10,17,17,18,18,26,32,33,36,36,36,34,30,34,30,32,34,32,34,29,24,25,22,25,24,21,21,20,18,12,10,4,5,4,-2,1,2,5,0,1,8,8,4,2,5,8,8,14,18,14,16,22,21,20,17,16,18,12,13,13,13,12,10,10,12,13,10,8,12,10,8,13,10,5,9,4,2,5,6,14,12,9,5,2,1,-6,-2,-2,-3,2,0,-3,-2,-6,-9,-13,-17,-15,-19,-23,-26,-31,-33,-30,-34,-34,-37,-39,-35,-35,-35,-33,-33,-30,-22,-21,-19,-14,-14,-9,-7,-9,1,-3,-9,-10,-9,-9,-10,-9,-3,2,1,0,4,0,4,1,5,9,6,8,4,8,5,0,0,1,5,5,6,6,5,6,9,9,12,10,10,10,16,13,13,14,14,13,8,9,6,8,8,4,-5,2,2,-3,-5,-9,-9,-5,-3,0,4,4,0,-2,-2,-3,-3,-9,-9,-7,-14,-13,-7,-9,-10,-2,-5,-6,-6,0,2,2,6,4,5,5,5,9,10,12,6,5,4,-2,-3,1,1,0,2,2,4,-3,-3,-5,-10,-14,-19,-17,-17,-17,-14,-13,-13,-13,-19,-25,-21,-21,-21,-18,-13,-9,-3,-3,-2,2,-2,-5,-2,-3,-3,0,0,1,1,1,-10,-10,-7,-11,-13,-17,-14,-18,-253,-251,-246,-237,-231,-227,-223,-221,-211,-210,-214,-207,-210,-207,-209,-205,-201,-206,-202,-205,-198,-198,-190,-183,-185,-179,-187,-185,-181,-182,-175,-166,-162,-158,-155,-158,-151,-153,-157,-150,-150,-153,-154,-147,-145,-141,-143,-141,-141,-146,-143,-146,-141,-139,-138,-134,-129,-125,-125,-121,-117,-117,-115,-105,-107,-106,-99,-97,-94,-95,-90,-87,-85,-79,-74,-71,-66,-66,-70,-70,-13,-9,-10,-7,-5,-6,-11,-9,-6,-2,-3,-2,4,-3,5,-19,-47,-42,-45,-35,-31,-31,-27,-27,-27,-26,-26,-25,-18,-18,-19,-15,-15,-13,-11,-7,-6,-6,-7,-9,-11,-13,-3,-11,-11,-9,-2,-2,-3,-3,-10,-7,-7,-7,-10,-11,-7,-5,-5,0,-2,-5,-5,-3,1,-2,1,0,0,1,-2,-2,-2,-5,-9,0,-3,-2,-6,-5,21,18,18,20,16,13,14,14,8,8,10,4,-2,-2,0,1,0,-5,-2,2,10,16,6,5,9,12,14,20,20,26,32,30,30,32,37,32,36,40,40,37,36,37,32,29,32,34,30,26,32,28,24,25,21,18,16,13,16,17,18,20,21,20,16,16,14,13,14,18,14,13,17,13,16,16,17,21,20,17,16,13,4,4,4,1,1,8,2,-2,-2,0,-2,0,1,-2,-2,-3,-6,-3,-2,-2,-3,-2,4,1,6,5,4,5,2,2,5,6,6,13,8,1,2,5,8,2,1,-2,-5,-9,-14,-15,-17,-18,-17,-18,-18,-18,-25,-25,-21,-25,-26,-26,-29,-31,-23,-21,-19,-10,-9,-10,-3,-6,0,0,-5,-3,0,6,6,6,8,8,8,9,10,8,6,6,4,0,-2,-3,-3,-2,-7,-13,-15,-9,-11,-9,-7,-10,-9,-10,-9,-7,-2,0,2,1,1,9,6,8,10,8,12,10,12,13,12,13,13,10,12,10,6,4,8,5,4,2,4,6,2,5,2,8,6,1,0,-5,-3,-5,-2,5,8,10,6,13,14,6,10,5,6,6,1,2,5,1,5,5,4,4,-2,-2,-5,-6,-9,-17,-19,-19,-22,-25,-29,-29,-31,-25,-23,-23,-25,-29,-25,-23,-26,-27,-26,-21,-18,-22,-23,-25,-23,-17,-11,-11,-7,-9,-5,-5,-7,1,4,8,6,10,12,8,9,10,9,13,14,16,16,13,12,5,2,-3,-2,-3,0,6,8,9,2,4,4,0,1,-2,0,-2,-6,-5,0,-3,-2,-7,-7,-2,-3,1,0,-3,0,-2,-5,-2,-2,-2,-2,1,4,8,8,5,8,10,10,12,10,10,16,13,12,16,9,16,21,22,24,25,28,30,29,26,17,10,12,12,6,8,5,2,5,2,2,1,4,-2,-3,-3,-5,-7,-3,-9,-10,-9,-9,-13,-13,-10,-10,-10,-18,-17,-23,-22,-19,-23,-21,-19,-17,-19,-19,-19,-14,-18,-22,-19,-11,-13,-14,-11,-13,-11,-13,-13,-11,-13,-10,-10,-13,-9,-9,-11,-7,-5,-11,-9,-9,-9,-3,-5,-3,-5,-3,-3,-10,-6,-7,-7,-7,-13,-10,-11,-11,-14,-14,-13,-11,-9,-10,-13,-14,-11,-10,-6,-13,-15,-19,-14,-13,-9,-18,-15,-14,-14,-11,-13,-9,-10,-3,0,1,-2,-3,-6,-3,0,-6,0,6,5,2,-5,-2,-5,-3,-3,-5,-5,-10,-7,-9,-7,-5,-6,-5,-7,-13,-6,-9,-9,-7,-3,-7,-11,-14,-19,-19,-22,-22,-22,-17,-18,-15,-11,-6,-9,-10,-10,-13,-13,-10,-11,-10,-5,-13,-10,-9,-7,-10,-13,-9,-7,-6,-5,-6,-7,-5,-7,-10,-11,-15,-18,-19,-19,-22,-22,-19,-15,-18,-19,-19,-18,-13,-18,-18,-17,-15,-14,-13,-11,-10,-6,-5,-9,-9,-14,-17,-9,-15,-13,-15,-18,-19,-17,-18,-23,-22,-26,-23,-23,-27,-25,-25,-22,-30,-31,-38,-37,-39,-38,-34,-34,-33,-30,-34,-31,-22,-22,-19,-19,-15,-17,-14,-15,-14,-15,-19,-17,-17,-19,-19,-22,-21,-17,-15,-19,-17,-13,-11,-11,-11,-11,-13,-13,-14,-13,-11,-11,-15,-15,-23,-27,-25,-31,-35,-38,-35,-39,-39,-38,-38,-37,-41,-38,-37,-39,-38,-37,-34,-37,-34,-33,-37,-39,-34,-38,-37,-35,-33,-31,-33,-33,-34,-29,-26,-23,-23,-25,-25,-23,-26,-18,-17,-15,-10,-14,-9,-9,-9,-5,-3,-2,0,2,6,6,4,12,9,8,10,12,16,16,18,17,16,12,9,9,8,9,8,10,9,2,0,0,-3,-5,-5,-7,-5,-3,-3,-2,0,4,4,-2,4,5,10,10,13,13,12,12,17,18,21,24,21,20,17,20,21,21,21,25,25,22,25,22,22,17,22,26,26,28,24,26,22,20,18,18,16,9,16,14,16,16,12,12,12,5,6,8,5,9,5,5,8,6,1,-2,-5,-3,-2,-3,-6,-7,-6,-7,-6,-3,-7,-6,-10,-13,-11,-10,-10,-9,-11,-11,-13,-10,-14,-13,-9,-14,-14,-13,-13,-13,-15,-10,-9,-15,-15,-14,-15,-14,-14,-13,-6,-10,-6,-6,-10,-7,-10,-5,-2,0,1,-2,-2,-5,-7,-7,-7,-9,-7,-6,-11,-7,-3,-2,0,-6,-6,-7,-7,-3,-3,0,4,5,2,2,4,4,1,0,-6,-2,2,0,4,0,4,1,1,-2,-2,0,1,1,-2,2,8,12,8,12,8,9,5,12,9,5,8,13,10,6,14,5,2,2,-3,-3,-6,-7,-11,-10,-17,-17,-13,-17,-19,-19,-19,-18,-19,-22,-18,-15,-15,-15,-19,-19,-19,-21,-22,-21,-25,-25,-23,-25,-26,-27,-30,-34,-34,-29,-26,-30,-27,-30,-29,-31,-30,-29,-29,-27,-31,-31,-39,-41,-42,-42,-41,-45,-46,-47,-42,-47,-46,-45,-49,-39,-39,-37,-37,-34,-26,-22,-25,-25,-18,-15,-15,-15,-13,-17,-17,-13,-11,-10,-6,-2,4,2,4,0,0,1,-5,0,-2,5,5,4,5,4,0,1,4,1,4,4,6,6,9,8,6,8,8,13,14,12,13,13,14,20,16,20,16,12,10,6,6,4,5,4,1,2,-2,2,2,0,4,5,2,0,-6,-3,-3,-5,-7,-9,-3,-5,-5,2,2,2,2,5,5,2,5,9,5,4,2,8,14,13,16,12,12,18,14,13,5,4,6,5,2,2,5,2,4,0,0,-2,0,2,2,-2,-5,-2,-5,-5,-9,-10,-7,-9,-11,-9,-14,-11,-11,-13,-14,-18,-15,-18,-21,-25,-21,-22,-23,-23,-22,-19,-27,-29,-30,-29,-33,-30,-31,-30,-23,-27,-19,-15,-18,-13,-11,-11,-17,-14,-14,-19,-18,-18,-17,-14,-14,-18,-17,-18,-18,-17,-11,-7,-13,-15,-11,-5,-2,0,6,2,-3,0,5,5,9,10,12,10,13,14,18,17,21,24,24,28,24,20,20,16,16,17,10,13,8,6,4,0,0,1,4,-2,1,-2,-3,-2,0,-2,-2,-3,4,2,1,8,5,8,5,10,13,10,16,16,21,24,24,24,20,20,17,18,16,16,20,22,16,17,16,12,14,9,13,6,5,4,2,1,-5,-2,-6,-9,-9,-11,-11,-15,-22,-21,-25,-27,-23,-23,-25,-29,-23,-21,-21,-21,-21,-19,-18,-17,-13,-14,-14,-9,-9,-6,0,-6,-3,0,0,0,-2,2,-3,-2,-2,-5,1,-2,-5,1,1,1,-2,0,1,1,2,-3,-2,2,-2,-2,-2,-7,-6,-5,-5,-3,-2,-3,-3,-3,1,-2,-9,-6,-9,-11,-15,-15,-13,-13,-10,-13,-14,-14,-15,-15,-17,-17,-14,-6,-11,-10,-6,-9,-9,-9,-9,-10,-7,-11,-7,-2,-3,0,-2,1,4,-3,-3,-2,0,0,8,5,6,8,2,10,6,9,14,10,13,13,10,16,14,14,10,13,12,10,9,13,16,14,12,10,6,8,5,2,5,-2,5,9,9,2,4,6,0,1,-3,0,0,1,-7,-11,-9,-9,-6,-6,-6,-6,-6,-9,-7,-6,-9,-10,-5,-3,-2,1,1,2,4,4,1,8,8,4,2,2,0,-3,-3,-7,-7,-7,-9,-14,-13,-13,-18,-21,-21,-18,-21,-18,-17,-13,-19,-15,-11,-11,-11,-13,-6,-5,5,5,5,9,12,12,12,13,12,20,17,20,24,21,18,16,17,13,8,5,9,6,6,0,2,1,2,4,1,-2,-3,-6,-3,-6,-9,-6,-9,-3,-7,-3,1,5,1,-2,4,4,6,9,10,14,16,17,18,14,12,13,13,16,17,14,12,10,8,8,6,5,6,6,8,10,16,21,24,28,26,26,28,25,26,25,28,28,22,26,26,28,28,22,21,26,28,26,24,22,25,24,22,18,18,17,12,6,4,4,-2,2,-2,-5,1,-3,-6,-10,-14,-15,-13,-11,-7,-3,-9,-15,-10,-9,-9,-15,-14,-9,-9,-10,-11,-7,-5,-7,-3,0,4,6,6,4,9,10,8,16,16,14,10,6,13,12,10,4,5,9,13,8,6,9,6,4,4,8,8,6,4,6,8,6,6,5,2,4,2,5,2,-3,0,0,4,2,5,5,0,-3,-3,-2,-3,-5,-5,-3,-3,-5,-7,-7,-7,-3,0,4,6,6,8,6,2,4,8,9,10,10,10,13,14,10,14,14,9,4,2,2,2,4,5,9,9,5,2,-2,0,-3,-6,-2,0,-7,-10,-9,-7,0,1,1,-6,-9,-14,-15,-15,-15,-19,-19,-17,-19,-21,-19,-22,-27,-26,-25,-31,-38,-37,-35,-35,-33,-38,-41,-34,-29,-29,-30,-31,-33,-31,-30,-30,-35,-30,-31,-33,-27,-33,-29,-33,-34,-33,-37,-30,-29,-29,-27,-26,-25,-23,-23,-25,-27,-30,-30,-29,-27,-25,-27,-27,-29,-29,-30,-29,-18,-18,-22,-21,-21,-19,-21,-19,-25,-21,-18,-19,-18,-10,-10,-9,-13,-10,-9,0,6,-3,2,1,-3,-7,-7,-2,2,8,5,1,4,2,2,-2,4,1,4,9,5,10,10,8,9,6,4,2,-2,5,2,2,1,5,9,5,2,5,5,2,5,5,8,10,4,0,1,-2,0,-2,1,0,1,1,-2,-3,-7,-7,-6,-3,-6,-6,-9,-9,-11,-14,-10,-14,-6,-2,0,1,-6,-3,-5,-7,-6,-6,-5,-9,-9,-10,-6,-7,-3,-3,-7,-5,-9,-3,-5,-6,-2,-9,-11,-9,-6,-7,-6,-2,-3,0,4,-2,2,2,2,6,2,1,1,-6,-5,-10,-7,-10,-11,-13,-13,-15,-13,-13,-19,-15,-17,-21,-18,-18,-17,-17,-19,-17,-19,-18,-18,-13,-10,-7,-5,-5,1,1,0,0,4,2,2,6,8,12,13,9,10,12,12,9,9,13,16,20,17,13,8,4,8,8,5,2,5,10,10,10,12,13,16,14,10,13,16,18,14,12,12,12,13,13,12,10,6,4,2,8,10,10,8,9,13,13,14,12,9,5,6,4,1,1,0,2,2,1,0,5,4,0,2,1,-2,0,-3,-7,-2,1,2,2,0,2,-2,-5,-3,0,4,4,1,-2,2,2,2,-5,-9,-6,-13,-13,-21,-22,-18,-21,-17,-23,-19,-18,-15,-18,-18,-17,-13,-14,-14,-17,-15,-11,-11,-6,-7,-6,-9,-9,-14,-11,-13,-14,-15,-10,-6,-10,-10,-15,-14,-21,-18,-11,-13,-6,-5,-9,-9,-11,-13,-14,-18,-19,-21,-22,-23,-21,-17,-18,-23,-21,-21,-26,-23,-22,-26,-21,-22,-18,-15,-21,-17,-14,-15,-17,-17,-15,-14,-11,-13,-17,-19,-26,-27,-29,-30,-29,-29,-26,-30,-27,-22,-25,-23,-21,-21,-18,-15,-11,-7,0,0,4,2,1,1,4,6,8,13,9,12,12,10,10,12,8,4,0,1,4,-2,1,0,-3,-5,-5,-2,-2,-2,-5,-3,-3,-7,-7,0,0,-6,-3,-7,-5,-3,1,0,-5,1,4,4,-2,-3,1,0,-6,-6,-10,-11,-10,-10,-10,-13,-15,-9,-7,-9,-9,-7,-6,-9,-5,0,0,2,1,-2,-5,4,2,2,2,4,9,4,6,6,2,2,-2,-2,-2,-3,-6,-10,-11,-11,-11,-17,-17,-17,-18,-22,-25,-25,-26,-23,-22,-23,-23,-26,-30,-34,-27,-29,-22,-21,-22,-19,-15,-14,-21,-15,-10,-9,-6,-10,-7,-5,-5,0,1,1,6,0,-3,2,2,4,0,2,1,1,8,9,5,9,9,5,4,6,4,1,1,1,5,6,10,8,8,12,8,8,5,2,1,-3,-2,2,4,6,5,1,2,5,4,-2,0,2,2,8,10,12,9,10,10,12,10,13,20,17,18,20,21,22,33,30,34,38,42,40,42,40,41,45,38,44,41,41,44,42,45,41,44,44,44,44,36,37,40,34,32,26,28,22,25,21,17,18,17,14,9,6,-2,-7,-3,-6,-5,-7,-11,-15,-17,-19,-25,-29,-27,-25,-22,-26,-21,-21,-23,-21,-25,-26,-25,-26,-23,-23,-26,-21,-19,-21,-21,-19,-18,-17,-19,-21,-21,-21,-21,-23,-21,-21,-15,-14,-11,-9,-15,-14,-14,-15,-13,-11,-11,-6,-10,-9,-5,-9,-9,-7,-5,-6,-3,-7,-5,-7,-18,-10,-13,-19,-14,-14,-17,-18,-18,-21,-18,-18,-15,-14,-15,-17,-23,-22,-...
				// //

				final IJsonObject dataCopy = data;
				dataCount += 1;
				dataLog.add(dataCopy);
				Log.d(LogUtil.TAG, "RunningApplications: " + dataCopy);

				runOnUiThread(new Runnable() {
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
						//
						// for (int i = 0; i < matches.length; i++) {
						// matches[i] = "Updated Song " + i + ;
						// }

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.listen, menu);
		return true;
	}

//	@Override
//	public void onStop() {
//		
//		//throwing exception
////		if (funfManager != null) {
////			funfManager.disablePipeline(PIPELINE_NAME);
////		}
//
//	}
}
