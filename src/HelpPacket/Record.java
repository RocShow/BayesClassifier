package HelpPacket;

import java.util.LinkedList;

public class Record {
	double weight = 1d;
	String label = "";
	int maxIndex = 0;
	int maxValue = 0;
	LinkedList<Integer[]> attributes;
	
	public Record(String line){
		attributes = new LinkedList<Integer[]>();
		String[] fields = line.split(" ");
		label = fields[0];
		for(int i = 1; i < fields.length; i++){
			String[] pair = fields[i].split(":");
			Integer[] data = new Integer[2];
			data[0] = Integer.parseInt(pair[0]);
			data[1] = Integer.parseInt(pair[1]);
			attributes.add(data);
			if(data[0] > maxIndex){
				maxIndex = data[0];
			}
			if(data[1] > maxValue){
				maxValue = data[1];
			}
		}
	}
	
	public String getLabel(){
		return label;
	}
	
	public double getWeight(){
		return weight;
	}
	
	public void setWeight(double weight){
		this.weight = weight;
	}
	
	public int getMaxIndex(){
		return maxIndex;
	}
	
	public int getMaxValue(){
		return maxValue;
	}
	
	public LinkedList<Integer[]> getAttributes(){
		return attributes;
	}
}
