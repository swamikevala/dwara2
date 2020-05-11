package org.ishafoundation.dwaraapi.unittests.storage.storagetype.tape.label;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.ishafoundation.dwaraapi.storage.storagetype.tape.label.VolumeHeaderLabel2;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VolumeHeaderLabel2Tests {

    @Test
    public void test0() throws Exception {
		VolumeHeaderLabel2 vol2 = new VolumeHeaderLabel2();
		String label = vol2.getLabel();
		assertEquals("VOL2 SOME IMPLEMENTATION SPECIFIC STUFF                                         " , label);
		assertTrue(label.length() == 80);
    }
}


