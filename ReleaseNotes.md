# Dwara App Version - 2.1.03
### New features
1 Extend validating to be ingested file during ingest too and not just during scan times
	Talk to Swami about the digi hack 
	
2 Request completion date. Need reconcilation for already completed requests. 
	Take Swami's help on the sql update Script

3 Server maintenance/administration capability added 

4 DU-343 Increased Job manager Scheduler frequency

> scheduler.jobManager.fixedDelay=30000/45000

5 DU-345 ArtifactAutoDeleter hourly than daily

> scheduler.ingestedArtifactAutoDeleter.fixedDelay=3600000

6 DU-342 TapeJobselector sort by seqId as integer and not string

7 DU-344 Dont wait for verify to complete for subsequent writes
 


### Deployment Note
1 application.properties seems to have log enabled. We need to ensure its disabled.
2 check why job 26224 got stuck by restore the same file again and move the folder when restore is happening and see what happens. Ensure we turn ON the storage layer logs
3 watcher changed for artifact validation - need to be redeployed afresh 

### Upgrade steps
  * Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_03.sql)
  
# Dwara App Version - 2.1.02
### New features 
1 Rename on hold

2 Generic Configurable File copier

3 Directory watcher changes

4 Support for bulk requeue of failed jobs

### Bug fixes

1 Fix for Race condition on update job between scheduledStatusUpdater and processing layer.

2 Digi Artifact File count and size

3 Multi release???


### Upgrade steps
  * Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_0_3_hotfix3.sql)
   
# Dwara App Version - 2.1.01-hotfix1
1 Preservation Job split

2 Cues File format correction

3 if flow missing fail the request

4 comment out the output filetype validation 

5 LTOwala defaulted to copy2

### Upgrade steps
  * Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_0_3_hotfix2.sql)

# Dwara App Version - 2.1
# Dwara DB Version - 2.0.3
### New features 
1 Digitization support 
  
  * Header and Footer extraction for binary reversal
  
  * Header and cues file extraction for future PFR support
 
2 Framework changes
  
  * Job Creation
    * Core Flow and flowelements out of DB into app code
    * Dont create jobs for dependent jobs upfront
    * Dont create jobs if we know that job is definitely bound to fail
    * placeholderJob endpoint Linking the dependencies for UI to show the hierarchy 
  
  * Job Management
    * create dependent jobs if any after a job is complete
  
  * Processing layer changes
    * Support Output Artifactclass same as Input artifactclass
    * Outputfiletype support
    * artifactclass to processingtask specific configurable derived files destination 
    * Processingtask specific overwritable Global thread pool 
    * Set permissions on derived files
  
  * Restore
    * New Restore/Verify action replacing verify embedded with restore as a storage task
    * job-id subfolders taken off from the restored destination 
  
  * User/System request Status updation logic refined 
  
  * Delete Artifact API
  
  * Hold and Release Requests

### Bug fixes
1 Subsequent Writes on a volume is blocked until all previous Write Job and all its dependencies are completed

2 Multiple files getting restored

3 Show reason for write jobs queued

### Upgrade steps
  * Stop the app(For instructions please refer TODO confluence link here)
  * Deploy the latest war from dev/test server
  * Take DB backup.......
  * For test/preprod environments - Apply the config dump from prod(/dwara-db/src/data/sql/dwara_update_2_0_0.sql). This excludes the config tables device,sequence and volume for which we need to apply env specific files...
  * Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_0_3.sql)
  * Restart the app 
  * ensure application.properties has entries for the global thread pool with appropriate values. The other threadpoolexecutor.*.* can all be cleaned up...

> threadpoolexecutor.processingtask.corePoolSize=5
> threadpoolexecutor.processingtask.maxPoolSize=5

> threadpoolexecutor.video-mam-update.corePoolSize=1
> threadpoolexecutor.video-mam-update.maxPoolSize=1

> catdv.groupId.*digi*

  * Deploy new setpermissions script
  
*******************************************************************************************************************************************************************************************
  
# Dwara App Version - 2.0.06
# Dwara DB Version - 2.0.2
### New features 
1 Support for persisting number of header blocks consumed by every file on the archive
   
> Schema change - New column file_volume.header_blocks addition only - Source will automatically create the new column...

### Bug fixes
1 Volume end block calculation for tar now is using tell rather than the existing way of summing up the consumed header block, blocks used based on file size and end of archive blocks involved.

2 Fixed mam insertclip media.dwara2id pointing to proxy's file id in place of the source fileId.

### Upgrade steps
  * Take DB backup.......
  * Deploy the app
  * Change the DB version to 2.0.2
  * Restart the app
  * Use the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_0_2.sql) to update already created rows missing values because of above Bug #2
  * Update script to correct the dwara2id references pointing to proxy file id than the source file id 
    
# Dwara App Version - 2.0.05
# Dwara DB Version - 2.0.1
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

> version.version need to be set with 2.0.1
	
### Bug fixes
1 Volume index catalog should be written after last artifact label + filemark and not right after the last artifact label

2 Artifact table fields not fully updated for derived artifacts

3 File table should have file entries in alphabetical order
 
4 Restored files for verification not getting deleted from the tmp directory
		 
### Upgrade steps
  * Take DB backup.......
  * Deploy the app
  * Apply the application.properties configuration mentioned above in "New features" #2
  * Restart the app (app will create the tables but fail to start)
  * Configure the version DB table
  * Configure the artifactclass_processingtask DB table
  * Restart the app  
  * Use the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_0_1.sql) to update already created rows missing values because of above Bug #2