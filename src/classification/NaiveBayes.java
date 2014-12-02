package classification;

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
	double[][][] conditionalProbility; //1:attributes, 2:(1)positive/(0)negative, 3:value
	
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
		//System.out.println("total attribtues:" + totalAttributes);
		train();
	}
	
	public NaiveBayes(LinkedList<Record> _trainSample, int _totalAttributes, int _totalValues){
		this.trainSample = _trainSample;
		
		totalAttributes = _totalAttributes;
		maxValue = _totalValues;
		conditionalProbility = new double[totalAttributes + 1][2][maxValue + 1];
		//System.out.println("total attribtues:" + totalAttributes);
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
		if(trainSample == null){
			System.out.println("You have to specify a train file.");
			return;
		}
		for(Record r : trainSample){
			totalSample++;
			processTrainRecord(conditionalCount, r);
		}
		for(int i = 0; i <= totalAttributes; i++){
			for(int j = 0; j <= maxValue; j++){
				//conditionalProbility[i][0][j] = (double)conditionalCount[i][0][j] / (negativeSample + maxValue + 1);
				//conditionalProbility[i][1][j] = (double)conditionalCount[i][1][j] / (positiveSample + maxValue + 1);
				conditionalProbility[i][0][j] = (double)conditionalCount[i][0][j] / (negativeSample + 2);
				conditionalProbility[i][1][j] = (double)conditionalCount[i][1][j] / (positiveSample + 2);
			}
		}
		
		pPositive = (double)(positiveSample + 2) / (totalSample  + 4);
		pNegative = (double)(negativeSample + 2) / (totalSample  + 4);
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
				} else if(r.getLabel().equals("-1")){
					fp++;
				} else {
					System.out.println("wrong label:" + r.getLabel());
				}
			} else if(result.equals("-1")) {
				if(r.getLabel().equals("+1")){
					fn++;
				} else if(r.getLabel().equals("-1")){
					tn++;
				}else {
					System.out.println("wrong label:" + r.getLabel());
				}
			} else{
				System.out.println("wrong result:" + result);
			}
		}
		
//		int all = tp + tn +fp +fn;
//		double precision = (double)tp / (tp + fp);
//		double recall = (double)tp / (tp + fn);
//		System.out.println("accuracy:" + (double)(tp + tn) / all);
//		System.out.println("error rate:" + (double)(fp + fn) / all);
//		System.out.println("sensitivity:" + (double)tp / (tp + fn));
//		System.out.println("specificity:" + (double)tn / (tn + fp));
//		System.out.println("precision:" + precision);
//		System.out.println("F-1 Score:" + (2 * precision * recall / (precision + recall)));
//		System.out.println("FBeta 0.5 Score:" + ((1 + 0.5 * 0.5) * precision * recall / (0.5 * 0.5 * precision + recall)));
//		System.out.println("FBeta 2 Score:" + ((1 + 2 * 2) * precision * recall / (2 * 2 * precision + recall)));
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
	}
	
	
	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("usage: java NaiveBayes training_file test_file");
			return;
		}
		NaiveBayes nb = new NaiveBayes(args[0],args[1]);
		String result = nb.testTrainSample();
		result += "\n" + nb.testTestSample();
		System.out.println(result);
	}
}
