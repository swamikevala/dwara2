# Dwara Version - 2.0.05
### New features 
1 Support for Edited videos
  * Validate cyclic loop and unresolved Symlinks 
  * Generate proxies only for specified folder inside the artifact
   
> Schema change - New table artifactclass_processingtask addition only - Source will automatically create the table...
	
2 Move completed ingest request's artifact to ingested folder from staged folder and retain or delete immediately based on the application.properties configuration

> ingestCompleteDirRoot=/data/ingested

> -# default retention period of ingest completed artifacts - in days

> retentionPeriod=7

> -# artifactclass-specific override - 0 will delete immediately (or the next time the auto-delete process runs)

> retentionPeriod.dvcapture-2020=0

3 DB Version support

> Schema change - New table version addition only - Source will automatically create the table...

> version.version need to be set with 2.0.05
	
### Bug fixes
1 Volume index catalog should be written after last artifact label + filemark and not right after the last artifact label

2 Artifact table fields not fully updated for derived artifacts

3 File table should have file entries in alphabetical order
 
4 Restored files for verification not getting deleted from the tmp directory
		 
### Upgrade steps
  * Take DB backup.......
  * Deploy the app
  * Apply the application.properties configuration mentioned above in "New features".2
  * Restart the app
  * Configure the artifactclass_processingtask DB table