package HelpPacket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class LIBSVMReader {
	LinkedList<Record> train;
	LinkedList<Record> test;
	int maxAttributIndex;
	
	public LIBSVMReader(String trainFile, String testFile){
		train = FileIO.readRecords(trainFile);
		test = FileIO.readRecords(testFile);
		maxAttributIndex = Integer.MIN_VALUE;
		for(Record r : train){
			if(r.getMaxIndex() > maxAttributIndex){
				maxAttributIndex = r.getMaxIndex();
			}
		}
		for(Record r : test){
			if(r.getMaxIndex() > maxAttributIndex){
				maxAttributIndex = r.getMaxIndex();
			}
		}
	}
	
	public void writeTrain(String fileName){
		writeDataSet(train, fileName);
	}
	
	public void writeTest(String fileName){
		writeDataSet(test, fileName);
	}
	
	private void writeDataSet(LinkedList<Record> dataset,String fileName){
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))){
			for(Record r : dataset){
				String s = r.getLabel() + " ";
				LinkedList<Integer[]> attributes = r.getAttributes();
				for(int i = 0, j = 0; i <= maxAttributIndex; i++){
					if(j < attributes.size() && i == attributes.get(j)[0]){
						s += "1 ";
						j++;
					} else {
						s += "0 ";
					}
				}
				s += "\n";
				bw.write(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		LIBSVMReader l = new LIBSVMReader("adult.train", "adult.test");
		l.writeTest("adultTest.txt");
		l.writeTrain("adultTrain.txt");
	}
}
