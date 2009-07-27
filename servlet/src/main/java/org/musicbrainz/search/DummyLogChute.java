package org.musicbrainz.search;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

public class DummyLogChute implements LogChute {

	public void init(RuntimeServices arg0) throws Exception {
	}

	public boolean isLevelEnabled(int arg0) {
		return true;
	}

	public void log(int arg0, String arg1) {
	}

	public void log(int arg0, String arg1, Throwable arg2) {
	}

}
