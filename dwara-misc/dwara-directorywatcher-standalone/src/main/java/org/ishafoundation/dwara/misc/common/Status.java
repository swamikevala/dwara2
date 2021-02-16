package org.ishafoundation.dwara.misc.common;

public enum Status {
	copying,
	copy_complete,
	verifying,
	verified,
	md5_mismatch,
	moved_to_validated_dir,
	moved_to_failed_dir,
	move_failed,
	ingested,
	failed;
}
