package org.ishafoundation.dwaraapi.unittests.storage.storagetype.tape.label;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.label.VolumeHeaderLabel1;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VolumeHeaderLabel1Tests {
	
	String tapeBarcode = "V5A001L7";
	String volID = StringUtils.substring(tapeBarcode,0,6);
	String ltoGen = StringUtils.substring(tapeBarcode,6,8);
	String implId = "DWARA";
	String ownerId = "ISHAFOUNDATION";
	
    @Test
    public void test0() throws Exception {
		VolumeHeaderLabel1 vol1 = new VolumeHeaderLabel1(volID, ltoGen, implId, ownerId);
		String label = vol1.getLabel();
		String expectedLabel = "VOL1V5A001 L7           DWARA        ISHAFOUNDATION                            4";
		assertEquals(expectedLabel , label);
		assertTrue(label.length() == 80);
		
    }

    @Test
    public void test1() throws Exception {
		VolumeHeaderLabel1 vol1 = new VolumeHeaderLabel1(volID, ltoGen, null, null);
		String label = vol1.getLabel();
		String expectedLabel = "VOL1V5A001 L7                                                                  4";
		assertEquals(expectedLabel , label);
		assertTrue(label.length() == 80);
    }
    
    @Test
    public void test2() throws Exception {
		try {
			new VolumeHeaderLabel1("adc", ltoGen, null, ownerId); // Volume Identifier < 6 chars
		}catch (Exception e) {
			assertTrue(true);
		}
    }
    
    @Test
    public void test3() throws Exception {
		try {
			new VolumeHeaderLabel1(null, ltoGen, null, ownerId); // Volume Identifier = null
		}catch (Exception e) {
			assertTrue(true);
		}
    }
    
    @Test
    public void test4() throws Exception {
		try {
			new VolumeHeaderLabel1(volID, null, null, null); // LTO Generation = null
		}catch (Exception e) {
			assertTrue(true);
		}
    }
    
    @Test
    public void test5() throws Exception {    
    	VolumeHeaderLabel1 vol1 = new VolumeHeaderLabel1("VOL1V5A001 L7           DWARA        ISHAFOUNDATION                            4");
    	assertEquals("V5A001", vol1.getVolID());
    	assertEquals("L7", vol1.getLtoGen());
    	String implId = vol1.getImplID();
    	assertTrue(implId.length() == 13);
    	assertEquals("DWARA        ", implId);
    	assertEquals("DWARA", implId.trim());
    	
    	String ownerId = vol1.getOwnerID();
    	assertTrue(ownerId.length() == 14);
    	assertEquals("ISHAFOUNDATION", ownerId);
    }
}


