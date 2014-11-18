package classification;

import java.util.LinkedList;

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
	static int enlargeFactor = 1;
	public NBAdaBoost(int totalRound, String trainFile, String testFile){
		fullTrain = FileIO.readRecords(trainFile);
		fullTest = FileIO.readRecords(testFile);
		this.totalRound = totalRound;
		this.sampleSize = fullTrain.size();
		//this.sampleSize = fullTrain.size() / totalRound;
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
			//what if sample and fullTest can't cover the actual max attributes?
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
			if(e > enlargeFactor){
				System.out.println("e > 1, e = " + e);
			}
			if(e > enlargeFactor * 0.5){
				System.out.println("e > 0.5, e = " + e);
				for(int j = 0; j < sampleSize; j++){
					sample.get(j).setWeight(1f / sampleSize);
				}
				i--;
				continue;
			}
			if(e == 0){
				System.out.println("e == 0");
				//totalRound = i + 1;
				//break;
			}
			//get old weight sum
			double oldSum = 0;
			for(Record r : fullTrain){
				oldSum += r.getWeight();
			}
			//System.out.println("oldSum:" + oldSum);
			//updating weights for tuples 
			for(int j = 0; j < sampleSize; j++){
				Record r = sample.get(j);
				if(r.getLabel().equals(results[j])){ // correctly classified
					r.setWeight(r.getWeight() * e / (enlargeFactor - e));
				}
			}
			//get new weight sum
			double newSum = 0;
			for(Record r : fullTrain){
				newSum = r.getWeight();
			}
			//System.out.println("newSum:" + newSum);
			//normalize
			for(Record r : sample){
				//not same as the text book
				r.setWeight(r.getWeight() * newSum / oldSum);
				//System.out.println(r.getWeight());
			}
			//computing weight for this classifier
			weights[i] = Math.log((enlargeFactor - e) / e);
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
		//System.out.println("getting Sample");
		double maxWeight = Double.MIN_VALUE;
		double minWeight = Double.MAX_VALUE;
		LinkedList<Record> result = new LinkedList<Record>();
		
		//get min and max weight
		for(Record r : fullTrain){
			if(r.getWeight() > maxWeight){
				maxWeight = r.getWeight();
			}
			if(r.getWeight() < minWeight){
				minWeight = r.getWeight();
			}
			if(r.getWeight() >= enlargeFactor){
				System.out.println("weight > 1, " + r.getWeight());
			}
		}
		//System.out.println("minWeight:" + minWeight + ",maxWeight:" + maxWeight);
		
		int index = 0;
		while(result.size() < sampleSize){
			Record r = fullTrain.get(index);
			double random = Math.random() * (maxWeight - minWeight) + minWeight;
			if(random <= r.getWeight()){
				result.add(r);
			}
			index = (index + 1) % sampleSize;
		}
		//System.out.println("getting finished");
		return result;
	}
	
	
	private void resetWeight(){
		for(Record r : fullTrain){
			r.setWeight(1f * enlargeFactor/sampleSize);
			//System.out.println(r.getWeight());
		}
	}
	
	public void printWeights(){
		for(int i = 0; i < weights.length; i++){
			System.out.println(weights[i] + " ");
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
	
