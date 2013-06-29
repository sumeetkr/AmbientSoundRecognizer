package in.sumeetkumar.asr;

import java.util.List;

import in.sumeetkumar.asr.data.Feature;
import in.sumeetkumar.asr.util.DatabaseHandler;
import in.sumeetkumar.asr.util.LogUtil;
import android.R.integer;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class PlayFragment extends ListFragment {

	List<Feature> features;
	private ArrayAdapter<String> adapter;
	String[] recordings = new String[10];
	 
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
	 
	    	recordings[0] ="Loading...";
	    	final DatabaseHandler db = new DatabaseHandler(this.getActivity());

			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						// get few sounds from database
						features = db.getAllFeatures();
						updateUIWithFeatures();
						Log.d(LogUtil.TAG, "Features count: "
								+ features.size());

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			};

			thread.start();
	  
//	        adapter = new ArrayAdapter<String>(inflater.getContext(), 
//	        		android.R.layout.simple_list_item_1,
//	        		android.R.id.text1,
//	        		recordings);
	 
	        adapter= new ArrayAdapter<String>(inflater.getContext(),
	        	       android.R.id.text1, recordings);
	        
	        setListAdapter(adapter);
	 
	        return super.onCreateView(inflater, container, savedInstanceState);
	    }
	    
	    private void updateUIWithFeatures(){
	    	int i=0;
	    	for(Feature feature: features){
	    		if(i<9){
	    			recordings[i] = feature.getName();
	    		}
	    		
	    		i++;
	    	}
	    	
	    	adapter.notifyDataSetChanged();
	    }
}

