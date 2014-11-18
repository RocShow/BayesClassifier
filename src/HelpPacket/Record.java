package HelpPacket;

import java.util.LinkedList;

public class Record {
	float weight = 1F;
	String label = "";
	int maxIndex = 0;
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
		}
	}
	
	public String getLabel(){
		return label;
	}
	
	public float getWeight(){
		return weight;
	}
	
	public void setWeight(float weight){
		this.weight = weight;
	}
	
	public int getMaxIndex(){
		return maxIndex;
	}
	
	public LinkedList<Integer[]> getAttributes(){
		return attributes;
	}
}
