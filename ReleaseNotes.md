# Dwara App Version - 2.1.08 (10G conversion - 31st Jan 2021)
# Dwara DB Version - 2.1.1

### New features
1 DB schema changes replacing ArtifactclassTask 

2 10g converter

3 Existing File copier replaces New RsyncCopier

4 Maintenance mode quick clear - clears already lined up jobs in Threadpoolexecuter queue when app enters maintenance mode.

### Bug fixes

1 Fix for hung File Descriptors 
 
2 Avoid drive yielding for write jobs completing after scheduler prepares storagejoblist 

3 Delete transcoded files after request completion

4 fix for ltowala

5 restore request - when tape not loaded shouldnt fail

6 Finalize failure

### checklist

1 Restart ingest server

2 Use different http port for pre-prod 

3 Start directorywatcher with GC options

### Upgrade steps

0 Follow the step by step instruction guide

1 Set up Rsync in remote server where 10G need to go. Create the configured destination directory and also a .copying directory under the destination directory 

2 Ensure application.properties has the following entry

> checksumRsync=true/false - used in configuring rsync with or without checksum verify

# Dwara App Version - 2.1.07 (storage blocker fix - 19th Jan 2021)

### LIVE Incident

1 Files renamed and released from Digitisation project had length set close to 245.

2 Dwara wrote the content to tape with no problems but couldnt write the artifact label as the tmp filename we used is artifactName + someextrasuffix and thus exceeding the char limit on filenames in linux. The write job failed even after retries and marked the tape suspect.

3 Subsequent writes are held up forcing a hotfix release

### NOTE to apply patch

1 Run the hotfix_2.1.07 endpoint - This will dump the artifactlabel in a preconfigured location

2 Manually write the label outside dwara

3 Manually Mark the failed job as completed

4 Run the job.createdependents endpoint to create dependent jobs for the failed writes

5 Unflag suspect on the volume  

# Dwara App Version - 2.1.06 (data porting hot fixer - 17th Jan 2021)

### LIVE Incident - 

1 With 2.1.05 we ported the tfile and tfilevolume records from file and filevolume respectively, but missed out updating tfilesequence. 

2 All Processing jobs failed. App moved to management mode. 

3 Video pub bulk ingest happened - Ingest failed - 

4 Digi rename and auto release video-mkv-pfr-metadata-extract and video-proxy-low-gen

To immediately unblock ingests, min impacted Videopub remediation happened as follows
0 Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_2.sql)

1 soft Delete artifact 4616

2 move back all folders from staged to ramkumarj

3 move S*

4 move P*

5 remove the seq code from pancha butha artifact

6 ingest "panch bhuta" see if all goes well

7 ingest all the green ones

8 residue
	deleted artifact
	jumping sequence
	
	
### NOTE
Digi had greater Impact with 63 artifacts failing on video-mkv-pfr-metadata-extract and video-proxy-low-gen. The processingtask got over but while updating TFile records the jobs failed so to clean this up Fix_2_1_06 controller was introduced.
 

# Dwara App Version - 2.1.05 (Bug fix and data porting - 17th Jan 2021)

### Bug fixes

1 Fix for nullable TFile.FileRefId using primitive datatype

2 self referencing and unresolved sym link occurences are a warning and not error anymore

### Upgrade steps
Now apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_1.sql)

# Dwara App Version - 2.1.04 (Edited videos focus - 16th Jan 2021)
# Dwara DB Version - 2.1.0
### New features
1 Support for ingesting edited videos

2 LTOwala feed is on completed request and not on copy complete jobs

### Bug fixes
1 Artifact.total_size and File.size record where artifact.name = file.pathname fixed with proper size updates

2 Restore process throws no flow configured error

3 Fix for sequence.keepcode usecase missing artifact.sequence

### Upgrade steps

In DB version 2.1.0 we are moving from latin1 to unicode charset and to achieve this 

* Following lines to be added to /etc/my.conf

> innodb_file_format = Barracuda

> innodb_large_prefix = 1

> innodb_file_per_table = ON

> character-set-server = utf8mb4

> collation-server = utf8mb4_unicode_520_ci

* Restart mysqld 

* Apply the charset conv script(/dwara-db/src/data/sql/dwara_update_2_1_0_charset-conv.sql)

* Finally

> mysqlcheck -u root -p --auto-repair --optimize --all-databases

* Now apply the edited videos feature specific upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_0.sql)


# Dwara App Version - 2.1.03 (Storage optimisation focus - 10th Jan 2021)
### New features
1 Extend validating to be ingested file during ingest too and not just during scan times
	
2 Request completion date. Need reconcilation for already completed requests. 

3 Server maintenance/administration capability added
	NOTE: After every restart of the app, we need to manually turn the system online for jobmanager to start dequeuing 

4 DU-343 Increased Job manager Scheduler frequency

> scheduler.jobManager.fixedDelay=30000/45000

5 DU-345 ArtifactAutoDeleter hourly than daily

> scheduler.ingestedArtifactAutoDeleter.fixedDelay=3600000
> retentionPeriod.video-pub=0 ???

6 DU-342 TapeJobselector sort by seqId as integer and not string

7 DU-344 Dont wait for verify to complete for subsequent writes
 

### Upgrade steps

##### Deployment Note
1 application.properties seems to have log enabled. We need to ensure its disabled.

2 check why job 26224 got stuck by restoring the same file again and move the folder when restore is happening and see what happens. Ensure we turn ON the storage layer logs

3 watcher changed for artifact validation - need to be redeployed afresh 

  * Stop Watcher
  * Stop any ltowala cron polling on Dwara
  * Stop the app
  * Take backups
  * Deploy the latest watcher from dev server
  * Deploy the latest war from dev/test server
  * Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_0_3_hotfix4.sql)
  * ensure application.properties has entries highlighted in #4 and #5 in New features section above  
  * Restart the app
  * Enable the app by calling the maintanence API with mode = "online"
    
# Dwara App Version - 2.1.02 (Minor Digi support items - 4th Jan 2021)
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
   
# Dwara App Version - 2.1.01 (Follow up Digitisation support items - 4th Jan 2021)
1 Preservation Job split

2 Cues File format correction

3 if flow missing fail the request

4 comment out the output filetype validation 

5 LTOwala defaulted to copy2

### Upgrade steps
  * Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_0_3_hotfix2.sql)

# Dwara App Version - 2.1 (Digitisation focus - )
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