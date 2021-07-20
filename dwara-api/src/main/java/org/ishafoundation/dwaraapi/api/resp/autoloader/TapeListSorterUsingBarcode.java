package org.ishafoundation.dwaraapi.api.resp.autoloader;

import java.util.Comparator;

public class TapeListSorterUsingBarcode implements Comparator<Tape>
{
    // Used for sorting in ascending order of barcode
    public int compare(Tape a, Tape b)
    {
        return a.getBarcode().compareTo(b.getBarcode());
    }
}