package HelpPacket;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class FileIO {
	public static LinkedList<Record> readRecords(String fileName){
		LinkedList<Record> result = new LinkedList<Record>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = "";
			while ((line = br.readLine()) != null) {
				if(line.length() > 0){
					result.add(new Record(line));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("File Size:" + result.size());
		return result;
	}
}
