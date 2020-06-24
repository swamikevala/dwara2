package org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status;

public enum TapeDriveStatusCode {
	/*
		ONLINE The drive is ready with a loaded tape.

		DR_OPEN The drive is empty (possibly with the door open).

		BOT The current position is the beginning of the tape.

		EOF The current position is the beginning of a file (the end of a file mark). This is a somewhat misleading code, because you can confuse it with the end of file.

		EOT The current position is the end of the tape.

		EOD The current position is the end of recorded media. You can reach this by trying to mt fsf past the very last file marker.

		WR_PROT The current tape is read-only.
	*/

	
	ONLINE,//The drive is ready with a loaded tape.
	DR_OPEN,// The drive is empty (possibly with the door open).
	BOT,// The current position is the beginning of the tape.
	EOF,// The current position is the beginning of a file (the end of a file mark). This is a somewhat misleading code, because you can confuse it with the end of file.
	EOT,// The current position is the end of the tape.
	EOD,// The current position is the end of recorded media. You can reach this by trying to mt fsf past the very last file marker.
	WR_PROT;// The current tape is read-only.
}
