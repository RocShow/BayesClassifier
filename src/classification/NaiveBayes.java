package classification;

import java.util.HashMap;
import java.util.LinkedList;

import HelpPacket.FileIO;
import HelpPacket.Record;

//Binary NaiveBayes
public class NaiveBayes {
	private HashMap<Integer,Integer> positiveMap;
	private HashMap<Integer,Integer> negativeMap;
	int totalSample = 0;
	int positiveSample = 0;
	int negativeSample = 0;
	int totalAttributes = 0; //start from 0
	
	LinkedList<Record> trainSample = null;
	LinkedList<Record> testSample = null;
	
	public NaiveBayes(LinkedList<Record> _trainSample, LinkedList<Record> _testSample){
		this.trainSample = _trainSample;
		this.testSample = _testSample;
		positiveMap = new HashMap<Integer, Integer>();
		negativeMap = new HashMap<Integer, Integer>();
		for(Record r : trainSample){
			if(r.getMaxIndex() > totalAttributes){
				totalAttributes = r.getMaxIndex();
			}
		}
		for(Record r : trainSample){
			if(r.getMaxIndex() > totalAttributes){
				totalAttributes = r.getMaxIndex();
			}
		}
		train();
	}
	
	public NaiveBayes(String trainFileName, String testFileName){
		this(FileIO.readRecords(trainFileName),FileIO.readRecords(testFileName));
	}
	
//	public void train(String trainFile){
//		trainSample = FileIO.readRecords(trainFile);
//		train();
//	}
	
	public void train(){
		if(trainSample == null){
			System.out.println("You have to specify a train file.");
			return;
		}
		for(Record r : trainSample){
			totalSample++;
//			if(r.getMaxIndex() > totalAttributes){
//				totalAttributes = r.getMaxIndex();
//			}
			processTrainRecord(r);
		}
	}
	
	public String classify(Record r){
		if(trainSample == null){
			System.out.println("You have to train the classifier first.");
			return null;
		}
		LinkedList<Integer[]> attributes = r.getAttributes();
		float pxPositive = 1;
		float pxNegative = 1;
		float pPositive = (float)positiveSample / totalSample;
		float pNegative = (float)negativeSample / totalSample;
		for(Integer[] pair : attributes){
			int indexCountsInPositive = positiveMap.get(pair[0]) == null ? 1 : positiveMap.get(pair[0]) + 1;
			int indexCountsInNegative = negativeMap.get(pair[0]) == null ? 1 : negativeMap.get(pair[0]) + 1;
			pxPositive *= (float)indexCountsInPositive / (positiveSample + totalAttributes + 1);
			pxNegative *= (float)indexCountsInNegative / (negativeSample + totalAttributes + 1);
		}
		float possibilityOfPositive = pxPositive * pPositive;
		float possibilityOfNegative = pxNegative * pNegative;
		
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
	
	private void processTrainRecord(Record r){
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
		NaiveBayes nb = new NaiveBayes("adult.train","adult.test");
		String result = nb.testTrainSample();
		result += "\n" + nb.testTestSample();
		System.out.println(result);
	}
}
