package org.ishafoundation.dwaraapi.api.resp.autoloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TapeListSorterUsingSlot implements Comparator<Tape>
{
    // Used for sorting in ascending order of slot address
    public int compare(Tape a, Tape b)
    {
    	// if UI is not able to show in the reverse order then use this return b.getAddress() - a.getAddress();
        return a.getAddress() - b.getAddress();
    }
    
    public static void main(String[] args) {
    	List<Tape> handleTapeList = new ArrayList<Tape>();
		Tape tape1 = new Tape();
		tape1.setBarcode("R10001L7");
		tape1.setAddress(15);
		handleTapeList.add(tape1);
		
		Tape tape2 = new Tape();
		tape2.setBarcode("R10003L7");
		tape2.setAddress(9);
		handleTapeList.add(tape2);
		
		Tape tape3 = new Tape();
		tape3.setBarcode("R10002L7");
		tape3.setAddress(18);
		handleTapeList.add(tape3);

		for (Tape tape : handleTapeList) {
			System.out.println(tape.getBarcode() + ":" + tape.getAddress());
		}
		System.out.println("***");
		
		Collections.sort(handleTapeList, new TapeListSorterUsingBarcode());
		for (Tape tape : handleTapeList) {
			System.out.println(tape.getBarcode() + ":" + tape.getAddress());
		}
		System.out.println("***");
		
		Collections.sort(handleTapeList, new TapeListSorterUsingSlot());
		for (Tape tape : handleTapeList) {
			System.out.println(tape.getBarcode() + ":" + tape.getAddress());
		}
	}
}