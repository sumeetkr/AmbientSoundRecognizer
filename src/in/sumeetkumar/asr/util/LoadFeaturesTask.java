package in.sumeetkumar.asr.util;

import java.util.List;

import edu.mit.media.funf.util.LogUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class LoadFeaturesTask  extends AsyncTask<DatabaseHandler, Integer, List<Feature>>{
	 private List<Feature> features;
	 private Context context;

	 public LoadFeaturesTask(Context ctx) {
		 context = ctx;
	 }
	 
     protected void onPostExecute(Integer result) {
    	 Toast.makeText(context, "Existing Featues Count " + features.size(), Toast.LENGTH_LONG).show();
     }

	@Override
	protected List<Feature> doInBackground(DatabaseHandler... params) {
		features = params[0].getAllFeatures();
		return features;
	}

}
