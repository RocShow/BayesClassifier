package classification;

import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import HelpPacket.FileIO;
import HelpPacket.Record;

public class NBAdaBoost {
	int totalRound;
	//int sampleSize;
	double e;
	NaiveBayes[] classifiers;
	double[] weights;
	LinkedList<Record> fullTrain;
	LinkedList<Record> fullTest;
	static double enlargeFactor = 1f;
	Random ra = new Random();
	int maxAttribute = 0;
	int totalValue = 0;
	public NBAdaBoost(int totalRound, String trainFile, String testFile){
		fullTrain = FileIO.readRecords(trainFile);
		fullTest = FileIO.readRecords(testFile);
		
		//get totalAttributes and totalValues
		for(Record r : fullTrain){
			if(r.getMaxIndex() > maxAttribute){
				maxAttribute = r.getMaxIndex();
			}
			if(r.getMaxValue() > totalValue){
				totalValue = r.getMaxValue();
			}
		}
		for(Record r : fullTest){
			if(r.getMaxIndex() > maxAttribute){
				maxAttribute = r.getMaxIndex();
			}
			if(r.getMaxValue() > totalValue){
				totalValue = r.getMaxValue();
			}
		}
		
		this.totalRound = totalRound;
		//this.sampleSize = fullTrain.size();
		classifiers = new NaiveBayes[totalRound];
		weights = new double[totalRound];
		e = 0F;
		ra.setSeed(1);
		resetWeight(fullTrain);
		train();
	}
	
	public void train(){
		for(int i = 0; i < totalRound; i++){
			//System.out.println("round " + i);
			LinkedList<Record> sample = getSample();
			int sampleSize = sample.size();
			//System.out.println(sample.size());
//			for(int h = 0; h < sample.size(); h++){
//				System.out.println(sample.get(h).getWeight() + sample.get(h).getLabel());
//			}
			
			String[] results = new String[sampleSize];
			//what if sample and fullTest can't cover the actual max attributes?
			classifiers[i] = new NaiveBayes(sample, maxAttribute, totalValue);
			e = 0;
			double wrongCount = 0;
			//computing e
			for(int j = 0; j < sampleSize; j++){
				Record r = sample.get(j);
				String result = classifiers[i].classify(r);
				results[j] = result;
				if(!result.equals(r.getLabel())){
					e += r.getWeight();
					wrongCount++;
					//System.out.println(r.getWeight());
					//e++;
					//System.out.println(e);
				}
			}
			wrongCount /= sampleSize;
			//e = e / sampleSize;
			//System.out.println(e);
//			if(e > enlargeFactor){
//				System.out.println("e > 1, e = " + e);
//			}
			if(wrongCount > 0.5){
				//System.out.println("Wrong Count > 0.5 " + wrongCount);
				i--;
				continue;
			}
			if(e > enlargeFactor * 0.5){
				//System.out.println("e > 0.5, e = " + e);
//				for(int j = 0; j < sampleSize; j++){
//					sample.get(j).setWeight(1f / sampleSize);
//				}
				i--;
				continue;
			}
			if(e == 0){
				//System.out.println("e == 0");
				//e = Double.MIN_NORMAL;
				i--;
				continue;
				//totalRound = i + 1;
				//break;
			}
			//System.out.println(e);
			//updating weights for tuples 
			for(int j = 0; j < sampleSize; j++){
				Record r = sample.get(j);
				if(r.getLabel().equals(results[j])){ // correctly classified
					r.setWeight(r.getWeight() * e / (enlargeFactor - e));
				}
			}
			normalize(fullTrain);
			//System.out.println("Normalized newSum:" + newSum);
			//computing weight for this classifier
			weights[i] = Math.log((enlargeFactor - e) / e);
		}
//		for(int i = 0; i < weights.length; i++){
//			System.out.println(weights[i]);
//		}
	}
	
	public String classify(Record r){
		double positiveVotes = 0;
		double negativeVotes = 0;
		for(int i = 0; i < totalRound; i++){
			if(classifiers[i].classify(r).equals("+1")){
				positiveVotes += weights[i];
			} else {
				negativeVotes += weights[i];
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
				} else if(r.getLabel().equals("-1")){
					fp++;
				}
			} else if(result.equals("-1")){
				if(r.getLabel().equals("+1")){
					fn++;
				} else if(r.getLabel().equals("-1")){
					tn++;
				}
			}
		}
		
		int all = tp + tn +fp +fn;
		double precision = (double)tp / (tp + fp);
		double recall = (double)tp / (tp + fn);
		System.out.println("accuracy:" + (double)(tp + tn) / all);
		System.out.println("error rate:" + (double)(fp + fn) / all);
		System.out.println("sensitivity:" + (double)tp / (tp + fn));
		System.out.println("specificity:" + (double)tn / (tn + fp));
		System.out.println("precision:" + precision);
		System.out.println("F-1 Score:" + (2 * precision * recall / (precision + recall)));
		System.out.println("FBeta 0.5 Score:" + ((1 + 0.5 * 0.5) * precision * recall / (0.5 * 0.5 * (precision + recall))));
		System.out.println("FBeta 2 Score:" + ((1 + 2 * 2) * precision * recall / (2 * 2 * (precision + recall))));
		if(all != sample.size()){
			System.out.println("WRONG! all = " + all + ", sampleSize = " + sample.size());
		}
		return "" + tp + " " + fn + " " + fp + " " + tn;
	}
	
	private LinkedList<Record> getSample(){
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
//			if(r.getWeight() >= enlargeFactor){
//				System.out.println("weight > 1, " + r.getWeight());
//			}
		}
		//System.out.println("minWeight:" + minWeight + ",maxWeight:" + maxWeight);
		
		for(Record r : fullTrain){
			double random = 0;
			if(maxWeight == minWeight){
				random = maxWeight;
			}
			random = ra.nextDouble() * (maxWeight - minWeight) + minWeight;
			//System.out.println("random:" + random + ", weight:" + r.getWeight());
			if(random / 2 <= r.getWeight()){
				result.add(r);
				//System.out.println(index % sampleSize);
			}
		}
		//System.out.println("getting finished");
		//normalize(result);
		return result;
	}
	
	private void normalize(LinkedList<Record> sample){
		double totalWeight = 0;
		for(Record r : sample){
			totalWeight += r.getWeight();
		}
		//System.out.println("normalize:" + totalWeight);
		for(Record r : sample){
			r.setWeight(r.getWeight() / totalWeight);
		}
//		totalWeight = 0;
//		for(Record r : sample){
//			totalWeight += r.getWeight();
//		}
//		System.out.println("after Normalize: " + totalWeight);
	}
	
	
	private void resetWeight(LinkedList<Record> sample){
		for(Record r : sample){
			r.setWeight(1d * enlargeFactor/sample.size());
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
		NBAdaBoost nba = new NBAdaBoost(50, args[0], args[1]);
		String result = nba.testTrainSample();
		result += "\n" + nba.testTestSample();
		System.out.println(result);
		//nba.printWeights();
	}
}
	
