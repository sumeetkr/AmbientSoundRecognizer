package in.sumeetkumar.asr.data;

import java.util.Arrays;

public class Feature {
	//private variables
    int id;
    private String name;
    private double l1Norm;
    private double l2Norm;
    private double linfNorm;
    private double [] mfccs;
    private String mfccsAsString;
    private double [] psdAcrossFrequencyBands;
    private String psdAcrossFrequencyBandsAsString;
    private double timestamp;
    private double diffSecs;
    
	// sample
	// "diffSecs":1.007000207901001,"l1Norm":12.620125,"l2Norm":15.705592156935694,"linfNorm":7.280109889280518,
	// "mfccs":[73.45780417766468,5.94377714299081,5.455124004455864,3.0556660679566736,2.0598351501842735,2.002186784717608,1.8324120016654795,1.2994454771981243,0.28606397137553624,0.19724540819105937,-0.6826126969146666,-0.6496579581333589],
	// "psdAcrossFrequencyBands":[5839808.109941236,387345.94407770695,89208.72127287657,22934.469227307152],"timestamp":1367304327.164}

	// {"diffSecs":41.07800006866455,"l1Norm":12.509,"l2Norm":21.531314404838366,"linfNorm":15.905973720586866,
	// "mfccs":[73.35071081139397,5.739566049218328,5.246120094945011,3.5691801907991576,3.235893874421781,2.9626390325264174,1.6552681174985016,0.029873280640131637,-1.3718713947743624,-0.165697525744605,-1.0225630834199853,-0.3089386212482912],
	// "psdAcrossFrequencyBands":[6708390.1264343085,280748.54138316837,83940.65067538692,24816.63704724058],
	// "rawData":[40,38,38,36,40,34,33,36,36,33,30,26,29,25,20,16,10,17,10,6,1,1,4,5,4,4,....

	public Feature() {
		// TODO Auto-generated constructor stub
		mfccs = new double[12];
		psdAcrossFrequencyBands = new double[4];
	}
	
	public Feature(AudioData data){
//		setL1Norm(data.getL1_NORM());
//		setL2Norm(data.getL2_NORM());
//		setLinfNorm(data.getLinf_NORM());
		setTimestamp(data.getTime());
		setDiffSecs(data.getTime());
//		setPsdAcrossFrequencyBands(data.getPsdAcrossFrequencyBands());
//		setMfccs(data.getFeatureCepstrum());
	}

	public Feature(IJsonObject json) {
		this();
		
		setL1Norm(json.get("l1Norm").getAsDouble());
		setL2Norm(json.get("l2Norm").getAsDouble());
		setLinfNorm(json.get("linfNorm").getAsDouble());
		setTimestamp(json.get("timestamp").getAsDouble());
		setDiffSecs(json.get("diffSecs").getAsDouble());
		setPsdAcrossFrequencyBandsAsString(json.get("psdAcrossFrequencyBands").toString());
		setMfccsAsString(json.get("mfccs").toString());
		
	}
	
	public Feature(IJsonObject json, String name) {
		this();
		
		setL1Norm(json.get("l1Norm").getAsDouble());
		setL2Norm(json.get("l2Norm").getAsDouble());
		setLinfNorm(json.get("linfNorm").getAsDouble());
		setName(name);
		setTimestamp(json.get("timestamp").getAsDouble());
		setDiffSecs(json.get("diffSecs").getAsDouble());
		setPsdAcrossFrequencyBandsAsString(json.get("psdAcrossFrequencyBands").toString());
		setMfccsAsString(json.get("mfccs").toString());
		
	}
	// getting ID
    public int getID(){
        return this.id;
    }
     
    // setting id
    public void setID(int id){
        this.id = id;
    }
     
    // getting name
    public String getName(){
        return this.name;
    }
     
    // setting name
    public void setName(String name){
        this.name = name;
    }

    public double getL1Norm() {
		return l1Norm;
	}

    public void setL1Norm(double l1Norm) {
		this.l1Norm = l1Norm;
	}

    public double getL2Norm() {
		return l2Norm;
	}

    public void setL2Norm(double l2Norm) {
		this.l2Norm = l2Norm;
	}

    public double getLinfNorm() {
		return linfNorm;
	}

    public void setLinfNorm(double linfNorm) {
		this.linfNorm = linfNorm;
	}

    public double [] getMfccs() {
		return mfccs;
	}

    public void setMfccs(double [] mfccs) {
		this.mfccs = mfccs;
	}

    public double [] getPsdAcrossFrequencyBands() {
		return psdAcrossFrequencyBands;
	}

    public void setPsdAcrossFrequencyBands(double [] psdAcrossFrequencyBands) {
		this.psdAcrossFrequencyBands = psdAcrossFrequencyBands;
	}

    public double getTimestamp() {
		return timestamp;
	}

    public void setTimestamp(double timestamp) {
		this.timestamp = timestamp;
	}

    public double getDiffSecs() {
		return diffSecs;
	}

    public void setDiffSecs(double diffSecs) {
		this.diffSecs = diffSecs;
	}

    public String getMfccsAsString() {
    	if(mfccsAsString==null){
    		mfccsAsString = Arrays.toString(mfccs);
    	}
		return mfccsAsString;
	}

    public void setMfccsAsString(String mfccsAsString) {
    	String cleanString = mfccsAsString.replace("[", "")   //remove the right bracket
    						.replace("]", "");
    	String[] ary = cleanString.split(",");
    	
    	for (int i = 0; i < ary.length; i++) {
			mfccs[i] = Double.parseDouble(ary[i]);
		}
    	
		this.mfccsAsString = mfccsAsString;
	}

	public String getPsdAcrossFrequencyBandsAsString() {
		return psdAcrossFrequencyBandsAsString;
	}

	public void setPsdAcrossFrequencyBandsAsString(
			String psdAcrossFrequencyBandsAsString) {
		this.psdAcrossFrequencyBandsAsString = psdAcrossFrequencyBandsAsString;
	}

}
