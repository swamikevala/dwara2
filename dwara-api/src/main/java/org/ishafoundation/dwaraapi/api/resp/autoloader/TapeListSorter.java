package org.ishafoundation.dwaraapi.api.resp.autoloader;

import java.util.Comparator;

public class TapeListSorter implements Comparator<Tape>
{
    // Used for sorting in ascending order of slot address
    public int compare(Tape a, Tape b)
    {
        return a.getAddress() - b.getAddress();
    }
}