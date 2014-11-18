package classification;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import HelpPacket.FileIO;
import HelpPacket.Record;

public class NBAdaBoost {
	int totalRound;
	int sampleSize;
	double e;
	NaiveBayes[] classifiers;
	double[] weights;
	LinkedList<Record> fullTrain;
	LinkedList<Record> fullTest;
	
	public NBAdaBoost(int totalRound, String trainFile, String testFile){
		fullTrain = FileIO.readRecords(trainFile);
		fullTest = FileIO.readRecords(testFile);
		this.totalRound = totalRound;
		this.sampleSize = fullTrain.size() / totalRound;
		//this.sampleSize = fullTrain.size();
		classifiers = new NaiveBayes[totalRound];
		weights = new double[totalRound];
		e = 0F;
		resetWeight();
		train();
	}
	
	public void train(){
		for(int i = 0; i < totalRound; i++){
			LinkedList<Record> sample = getSample();
			String[] results = new String[sampleSize];
			classifiers[i] = new NaiveBayes(sample,fullTest);
			e = 0;
			//computing e
			for(int j = 0; j < sampleSize; j++){
				Record r = sample.get(j);
				String result = classifiers[i].classify(r);
				results[j] = result;
				if(!result.equals(r.getLabel())){
					e += r.getWeight();
				}
			}
			System.out.println(e);
			if(e > 0.5){
				System.out.println("e > 0.5, e = " + e);
				for(int j = 0; j < sampleSize; j++){
					sample.get(j).setWeight(1f / sampleSize);
				}
				i--;
				continue;
			}
			//updating weights for tuples 
			double totalWeight = 0;
			for(int j = 0; j < sampleSize; j++){
				Record r = sample.get(j);
				if(r.getLabel().equals(results[j])){
					r.setWeight(r.getWeight() * e / (1 - e));
				}
				totalWeight += r.getWeight();
			}
			//normalize
			for(Record r : sample){
				r.setWeight(r.getWeight() / totalWeight);
			}
			//computing weight for this classifier
			weights[i] = Math.log((1 - e) / e);
		}
	}
	
	public String classify(Record r){
		double positiveVotes = 0;
		double negativeVotes = 0;
		for(int i = 0; i < totalRound; i++){
			if(classifiers[i].classify(r).equals("+1")){
				positiveVotes++;
			} else {
				negativeVotes++;
			}
		}
		if(positiveVotes >= negativeVotes){
			return "+1";
		} else {
			return "-1";
		}
	}
	
	public String testTrainSample(){
		return test(fullTrain);
	}
	
	public String testTestSample(){
		return test(fullTest);
	}
	
	private String test(LinkedList<Record> sample){
		if(sample == null){
			System.out.println("You have to specify a record set.");
			return null;
		}
		int tp = 0, tn = 0, fp = 0, fn = 0;
		for(Record r : sample){
			String result = classify(r);
			if(result.equals("+1")){
				if(r.getLabel().equals("+1")){
					tp++;
				} else {
					fp++;
				}
			} else {
				if(r.getLabel().equals("+1")){
					fn++;
				} else {
					tn++;
				}
			}
		}
		return "" + tp + " " + fn + " " + fp + " " + tn;
	}
	
	private LinkedList<Record> getSample(){
		LinkedList<Record> result = new LinkedList<Record>();
		PriorityQueue<Record> pq = new PriorityQueue<Record>(sampleSize, new Comparator<Record>(){
			public int compare(Record o1, Record o2) {
				if(o2.getWeight() > o1.getWeight()){
					return 1;
				} else if(o2.getWeight() < o1.getWeight()){
					return -1;
				} else {
					return 0;
				}
			}
		});
		for(Record r : fullTrain){
			pq.add(r);
		}
		for(int i = 0; i < sampleSize; i++){
			Record r = pq.poll();
			//System.out.println(r.getWeight());
			result.add(r);
		}
		//System.out.println("----");
		//System.out.println(result.size());
		return result;
	}
	
	
	private void resetWeight(){
		for(Record r : fullTrain){
			r.setWeight(1f/sampleSize);
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("usage: java NaiveBayes training_file test_file");
			return;
		}
		NBAdaBoost nba = new NBAdaBoost(1, args[0], args[1]);
		String result = nba.testTrainSample();
		result += "\n" + nba.testTestSample();
		System.out.println(result);
	}
}
	
