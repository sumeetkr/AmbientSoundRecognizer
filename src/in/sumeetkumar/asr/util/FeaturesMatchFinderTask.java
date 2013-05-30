package in.sumeetkumar.asr.util;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import android.os.AsyncTask;
import android.widget.ArrayAdapter;

//public class FeaturesMatchFinderTask extends AsyncTask<List<Feature>, Integer, Void> {
public class FeaturesMatchFinderTask  {

	private String [] matches;
	private ArrayAdapter<String> adapter;
	private Feature feature;
	private PriorityQueue<Match> matchQueue;

	public FeaturesMatchFinderTask(String [] matches, ArrayAdapter<String> adapter, Feature feature) {
		this.matches = matches;
		this.adapter = adapter;
		this.feature = feature;
		
		matchQueue = new PriorityQueue<FeaturesMatchFinderTask.Match>(
				new PriorityQueue<Match>(matches.length, new Comparator<Match>() {
					@Override
				    public int compare(Match x, Match y)
				    {
				        if (x.getMatchValue() < y.getMatchValue())
				        {
				            return -1;
				        }
				        if (x.getMatchValue() > y.getMatchValue())
				        {
				            return 1;
				        }
				        return 0;
				    }
		}));
	}
	
	protected void onPostExecute(Long result) {
        adapter.notifyDataSetChanged();
    }

	
	public Void doInBackground(List<Feature>... params) {
		List<Feature> features = params[0];
		if (features.size() <1 || feature == null) return null;
		
		double [] command = feature.getMfccs();
		
		for (Feature ft : features) {
			
			double [] spectrum = ft.getMfccs();
			double mse = 0;
			 
			for( int i = 0 ; i < spectrum.length ; i++ ) {
			     mse += Math.pow( (spectrum[i] - command[i]), 2);
			}
			mse /= spectrum.length;
			
//		matchQueue.add(	new Match(ft.getName(),0, Math.abs(ft.getL1Norm() - feature.getL1Norm())));
			matchQueue.add(	new Match(ft.getName(),0, mse));
		}
		
		for (int i = 0; i < matches.length; i++) {
			Match match = matchQueue.poll();
			matches[i] = match.getName() + " : value = " + match.getMatchValue();
		}

		adapter.notifyDataSetChanged();
		return null;
	}
	
	public class Match{
		private String name;
		private double percentage;
		private double matchValue;
		
		public Match(String name, double percentage, double matchValue) {
		this.name = name;
		this.percentage = percentage;
		this.matchValue = matchValue;
		}
		
		String getName() {
			return name;
		}
		void setName(String name) {
			this.name = name;
		}
		double getPercentage() {
			return percentage;
		}
		void setPercentage(double percentage) {
			this.percentage = percentage;
		}
		double getMatchValue() {
			return matchValue;
		}
		void setMatchValue(double matchValue) {
			this.matchValue = matchValue;
		}
	}
}
