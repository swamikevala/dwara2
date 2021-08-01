package org.ishafoundation.dwaraapi.api.resp.autoloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TapeListSorterUsingBarcode implements Comparator<Tape>
{
    // Used for sorting in ascending order of barcode
    public int compare(Tape a, Tape b)
    {
        return a.getBarcode().compareTo(b.getBarcode());
    }
    
    public static void main(String[] args) {
    	List<Tape> handleTapeList = new ArrayList<Tape>();
		Tape tape1 = new Tape();
		tape1.setBarcode("R10001L7");
		handleTapeList.add(tape1);
		
		Tape tape2 = new Tape();
		tape2.setBarcode("R10003L7");
		handleTapeList.add(tape2);
		
		Tape tape3 = new Tape();
		tape3.setBarcode("R10002L7");
		handleTapeList.add(tape3);

		Tape tape4 = new Tape();
		tape4.setBarcode("R10001L7");
//		handleTapeList.add(tape4);
		if(!handleTapeList.stream().anyMatch(x -> x.equals(tape4))) // avoid dupe entries...
			handleTapeList.add(tape4);

		for (Tape tape : handleTapeList) {
			System.out.println(tape.getBarcode());
		}
		Collections.sort(handleTapeList, new TapeListSorterUsingBarcode());
		for (Tape tape : handleTapeList) {
			System.out.println(tape.getBarcode());
		}
	}
}