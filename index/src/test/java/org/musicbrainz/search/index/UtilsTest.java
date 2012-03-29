package org.musicbrainz.search.index;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
	   public void testFormatDate() throws Exception {

		   assertEquals("", Utils.formatDate(null, null, null));
		   
		   assertEquals("1990", Utils.formatDate(1990, null, null));
		   assertEquals("1990-04", Utils.formatDate(1990, 4, null));
		   assertEquals("1990-04-01", Utils.formatDate(1990, 4, 1));
		   
		   assertEquals("1990", Utils.formatDate(1990, 0, null));
		   assertEquals("1990", Utils.formatDate(1990, 0, 0));
		   assertEquals("", Utils.formatDate(0, 0, 0));
		   
		   assertEquals("1990", Utils.formatDate(1990, 0, 1));
		   
	    }
	
}
