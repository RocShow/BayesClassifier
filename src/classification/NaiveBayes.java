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
	int maxValue = 0;
	
	LinkedList<Record> trainSample = null;
	LinkedList<Record> testSample = null;
	
	double pPositive = 0;
	double pNegative = 0;
	double[][][] conditionalProbility; //1:attributes, 2:(1)positive/(0)negative, 3:categories,0(+1),1(-1)
	
	public NaiveBayes(LinkedList<Record> _trainSample, LinkedList<Record> _testSample){
		this.trainSample = _trainSample;
		this.testSample = _testSample;
		
		for(Record r : trainSample){
			if(r.getMaxIndex() > totalAttributes){
				totalAttributes = r.getMaxIndex();
			}
			if(r.getMaxValue() > maxValue){
				maxValue = r.getMaxValue();
			}
		}
		for(Record r : testSample){
			if(r.getMaxIndex() > totalAttributes){
				totalAttributes = r.getMaxIndex();
			}
			if(r.getMaxValue() > maxValue){
				maxValue = r.getMaxValue();
			}
		}
		conditionalProbility = new double[totalAttributes + 1][2][maxValue + 1];
		
		train();
	}
	
	public NaiveBayes(String trainFileName, String testFileName){
		this(FileIO.readRecords(trainFileName),FileIO.readRecords(testFileName));
	}
	
	
	public void train(){
		int[][][] conditionalCount = new int[totalAttributes + 1][2][maxValue + 1];
		for(int i = 0; i <= totalAttributes; i++){
			for(int j = 0; j <= maxValue; j++){
				conditionalCount[i][0][j] = 1;
				conditionalCount[i][1][j] = 1;
			}
		}
		//HashMap<Integer,Integer> positiveMap = new HashMap<Integer, Integer>();
		//HashMap<Integer,Integer> negativeMap = new HashMap<Integer, Integer>();
		if(trainSample == null){
			System.out.println("You have to specify a train file.");
			return;
		}
		for(Record r : trainSample){
			totalSample++;
			processTrainRecord(conditionalCount, r);
		}
		for(int i = 0; i <= totalAttributes; i++){
//			int indexCountsInPositive = positiveMap.get(i) == null ? 1 : positiveMap.get(i) + 1;
//			int indexCountsInNegative = negativeMap.get(i) == null ? 1 : negativeMap.get(i) + 1;
//			conditionalProbility[i][0][0] = (double)indexCountsInPositive/ (positiveSample + 2);
//			conditionalProbility[i][0][1] = 1 - conditionalProbility[i][0][0];
//			conditionalProbility[i][1][0] = (double)indexCountsInNegative / (negativeSample + 2);
//			conditionalProbility[i][1][1] = 1 - conditionalProbility[i][1][0];
			for(int j = 0; j <= maxValue; j++){
				conditionalProbility[i][0][j] = (double)conditionalCount[i][0][j] / (negativeSample + 2);
				conditionalProbility[i][1][j] = (double)conditionalCount[i][1][j] / (positiveSample + 2);
			}
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
			Integer pair[] = null;
			if(j < attributes.size()){
				pair = attributes.get(j);
			}
			
			if(pair != null && i == pair[0]){
				pxPositive *= conditionalProbility[i][1][pair[1]];
				pxNegative *= conditionalProbility[i][0][pair[1]];
				j++;
			} else {
				pxPositive *= conditionalProbility[i][1][0];
				pxNegative *= conditionalProbility[i][0][0];
			}
		}
		
		double possibilityOfPositive = pxPositive * pPositive;
		double possibilityOfNegative = pxNegative * pNegative;
		//System.out.println("p:" + possibilityOfPositive + "--n:" + possibilityOfNegative);
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
	
	private void processTrainRecord(int[][][] conditionalCount, Record r){
		LinkedList<Integer[]> attributes = r.getAttributes();
		int classLabel = -1;
		if(r.getLabel().equals("-1")){
			classLabel = 0;
			negativeSample++;
		} else if(r.getLabel().equals("+1")){
			classLabel = 1;
			positiveSample++;
		}
		for(int i = 0, j = 0; i <= totalAttributes; i++){
			Integer[] pair = null;
			if(j < attributes.size()){
				pair = attributes.get(j);
			}
			if(pair != null && i == pair[0]){
				conditionalCount[i][classLabel][pair[1]]++;
				j++;
			} else {
				conditionalCount[i][classLabel][0]++;
			}
		}
//		HashMap<Integer, Integer> map = null;
//		if(r.getLabel().equals("-1")){
//			map = negativeMap;
//			negativeSample++;
//		} else {
//			map = positiveMap;
//			positiveSample++;
//		}
//		for(Integer[] pair : attributes){			
//			int count = map.get(pair[0]) == null ? 1 : map.get(pair[0]) + 1;
//			map.put(pair[0], count);
//		}
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
