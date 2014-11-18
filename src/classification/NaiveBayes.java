package classification;

import java.util.HashMap;
import java.util.LinkedList;

import HelpPacket.FileIO;
import HelpPacket.Record;

//Binary NaiveBayes
public class NaiveBayes {
	
	int totalSample = 0;
	int positiveSample = 0;
	int negativeSample = 0;
	int totalAttributes = 0; //start from 0
	
	LinkedList<Record> trainSample = null;
	LinkedList<Record> testSample = null;
	
	double pPositive = 0;
	double pNegative = 0;
	double[] positiveExistConditions;
	double[] negativeExistConditions;
	double[] positiveNonExistConditions;
	double[] negativeNonExistConditions;
	
	public NaiveBayes(LinkedList<Record> _trainSample, LinkedList<Record> _testSample){
		this.trainSample = _trainSample;
		this.testSample = _testSample;
		
		for(Record r : trainSample){
			if(r.getMaxIndex() > totalAttributes){
				totalAttributes = r.getMaxIndex();
			}
		}
		for(Record r : testSample){
			if(r.getMaxIndex() > totalAttributes){
				totalAttributes = r.getMaxIndex();
			}
		}
		positiveExistConditions = new double[totalAttributes + 1];
		negativeExistConditions = new double[totalAttributes + 1];
		positiveNonExistConditions = new double[totalAttributes + 1];
		negativeNonExistConditions = new double[totalAttributes + 1];
		train();
	}
	
	public NaiveBayes(String trainFileName, String testFileName){
		this(FileIO.readRecords(trainFileName),FileIO.readRecords(testFileName));
	}
	
	
	public void train(){
		HashMap<Integer,Integer> positiveMap = new HashMap<Integer, Integer>();
		HashMap<Integer,Integer> negativeMap = new HashMap<Integer, Integer>();
		if(trainSample == null){
			System.out.println("You have to specify a train file.");
			return;
		}
		for(Record r : trainSample){
			totalSample++;
			processTrainRecord(positiveMap, negativeMap, r);
		}
		for(int i = 0; i <= totalAttributes; i++){
			int indexCountsInPositive = positiveMap.get(i) == null ? 1 : positiveMap.get(i) + 1;
			int indexCountsInNegative = negativeMap.get(i) == null ? 1 : negativeMap.get(i) + 1;
			int indexNonExistPositive = positiveSample - indexCountsInPositive + 2;
			int indexNonExistNegative = negativeSample - indexCountsInNegative + 2; 
			positiveExistConditions[i] = (double)indexCountsInPositive / (positiveSample + 2);
			negativeExistConditions[i] = (double)indexCountsInNegative/ (negativeSample + 2);
			positiveNonExistConditions[i] = (double)indexNonExistPositive / (positiveSample + 2);
			negativeNonExistConditions[i] = (double)indexNonExistNegative/ (negativeSample + 2);
		}
		
		pPositive = (double)(positiveSample + 2) / (totalSample + 4);
		pNegative = (double)(negativeSample + 2) / (totalSample + 4);
		
//		System.out.println("pPositive:" + pPositive);
//		System.out.println("pNegative" + pNegative);
//		for(int i = 0; i <= totalAttributes; i++){
//			System.out.println("Index " + i + " Positive: " + positiveConditions[i] + " Negative: " + negativeConditions[i]);
//		}
//		pPositive:0.6428571428571429
//		pNegative0.35714285714285715
//		Index 0 Positive: 0.2222222222222222 Negative: 0.6
//		Index 1 Positive: 0.4444444444444444 Negative: 0.0
//		Index 2 Positive: 0.3333333333333333 Negative: 0.4
//		Index 3 Positive: 0.2222222222222222 Negative: 0.4
//		Index 4 Positive: 0.4444444444444444 Negative: 0.4
//		Index 5 Positive: 0.3333333333333333 Negative: 0.2
//		Index 6 Positive: 0.6666666666666666 Negative: 0.2
//		Index 7 Positive: 0.3333333333333333 Negative: 0.8
//		Index 8 Positive: 0.3333333333333333 Negative: 0.6
//		Index 9 Positive: 0.6666666666666666 Negative: 0.4
//		9 0 1 4
//		9 0 1 4

	}
	
	public String classify(Record r){
		if(trainSample == null){
			System.out.println("You have to train the classifier first.");
			return null;
		}
		LinkedList<Integer[]> attributes = r.getAttributes();
		double pxPositive = 1;
		double pxNegative = 1;
		
		for(int i = 0, j = 0; i <= totalAttributes; i++){
			if(j < attributes.size() && i == attributes.get(j)[0]){
				pxPositive *= positiveExistConditions[i];
				pxNegative *= negativeExistConditions[i];
				j++;
			} else {
				pxPositive *= positiveNonExistConditions[i];
				pxNegative *= negativeNonExistConditions[i];
			}
		}
		
//		for(Integer[] pair : attributes){
//			pxPositive *= positiveExistConditions[pair[0]];
//			pxNegative *= negativeExistConditions[pair[0]];
//		}
		double possibilityOfPositive = pxPositive * pPositive;
		double possibilityOfNegative = pxNegative * pNegative;
		
		return possibilityOfPositive >= possibilityOfNegative ? "+1" : "-1";
	}
	
	public String testTrainSample(){
		return test(trainSample);
	}
	
	public String testTestSample(){
		return test(testSample);
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
	
	private void processTrainRecord(HashMap<Integer,Integer> positiveMap, HashMap<Integer,Integer> negativeMap, Record r){
		LinkedList<Integer[]> attributes = r.getAttributes();
		HashMap<Integer, Integer> map = null;
		if(r.getLabel().equals("-1")){
			map = negativeMap;
			negativeSample++;
		} else {
			map = positiveMap;
			positiveSample++;
		}
		for(Integer[] pair : attributes){			
			int count = map.get(pair[0]) == null ? 1 : map.get(pair[0]) + 1;
			map.put(pair[0], count);
		}
	}
	
	
	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("usage: java NaiveBayes training_file test_file");
			return;
		}
		NaiveBayes nb = new NaiveBayes(args[0],args[1]);
		//NaiveBayes nb = new NaiveBayes("test.train", "test.train");
		String result = nb.testTrainSample();
		result += "\n" + nb.testTestSample();
		System.out.println(result);
	}
}
