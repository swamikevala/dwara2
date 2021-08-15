# Dwara App Version - 2.1.26 (16th Aug 2021)
### New features

1) Tags support in process layer

2) New mkv2mov converter

3) Cancel/Delete reason capture

4) Mark volume status API and schema changes


### Bug fixes

1) Fix rewrite top 2



### Upgrade steps

1) mkv2mov story 
 * tagging needs to happen for ntsc
 * change vyom endpoint and postbody
 * change pgururmurthy password and change user id in vyom

# Dwara App Version - 2.1.25 (2nd Aug 2021)
### New features
1) Request API now supports single request

2) Folder catalog changes


### Bug fixes

1) PROD Restore failures - Renamed artifacts move failures fixed and failed restores running in a loop

2) Tape UI - avoid showing dupes, sort by slot id for removes, sort by barcode for adds   

3) Rewrite need to flag *filevolume.deleted for the defective ones

4) Handle "Unknown" tapes in UI view and autoinitialisation

5) Extra guard while saving the job object in TapeJobManager to avoid concurrent request on it(eg., Cancel) causing jobs status to be overwritten

6) Directory watcher - Dont register subfolders under watched dir - For eg.,  H-BETACAM-541/H-BETACAM-543 - H-BETACAM-543 should not be registered and only H-BETACAM-541 need to be registered which will fail validation

  
### Upgrade steps

1) Tapes UI - sorting etc., - not tested ensure its working after rollout and fix if anything needed right there and roll it out again... 

2) Rollout Directory watcher

# Dwara App Version - 2.1.24 (12th Jul 2021)
### New features
1) Support for marked_failed functionality

2) Request API - Enhanced to support artifactclass filter for Ingest summary

3) Auto initializer as API endpoint

### Bug fixes
1) Bru restores - handle non 0 status appropriately


# Dwara App Version - 2.1.23 (27th Jun 2021)
### New features
1) Proxy conversion command now includes audio channel 2 too

### Bug fixes

1) Some loglevel changes to avoid cluttering logs which is bothering Swami's eyes - Hopefully no more excuses not to look at app logs anymore


2) Jobcreator prod bug fix - dependency jobs not getting created for processingtaskWithDependencyStoragetask for a specific scenario. Impacted requests sorted out. Verified using 

> select * from file1_volume join file1 on file1.id = file_id where verified_at is null and directory = 0;


3) Used the opportunity presented by above and fixed setting job.volume for update missed out usecase


4) Fix for restores complete - but weirdly no files present in destination scenario


5) Create dummy artifact label in disk with placeholder data to avoid write failures during no free disk space times.


6) Fix for "Restore verify" requests throwing exception

# Dwara App Version - 2.1.22 (16th Jun 2021)
### New features 

1) Emedia and impressions support


2) JobManager improvements for rewrite scenario 


3) Restore by artifactname


4) Human readable tape size in dashboard


5) User Roles support


6) HDV support


7) Job Inclusion/Exclusion based on Flowelement.taskconfig inc/exc props

### Upgrade steps

0) Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_13.sql)

# Dwara App Version - 2.1.21 (19th May 2021)
### New features 

1) Photo pub file name validation change request


# Dwara App Version - 2.1.20 (16th May 2021)
### New features 

1) Cancelling failed restore requests

2) Job manager changes

3) Ingester supporting Hi8

### Bug fixes

1) changeArtifactclass placeholderJob manipulation and logging fixed

### Upgrade steps

0) Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_12.sql)

1) application.properties to have the below entry

> wowo.useNewJobManagementLogic=true

# Dwara App Version - 2.1.19 (9th May 2021)
### New features

1) Defective tape rewrite

2) Request API enhancements(groupedPlaceholderJobs)

3) Change artifactclass API 


### Upgrade steps

0) Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_11.sql)

# Dwara App Version - 2.1.18 (25th Apr 2021)
### Bug fix


0) avoid clean up tapes during mapdrive and initialization

1) Fixed restore for soft renamed artifacts

2) Fixed handleTapes api removeAfterJob NPE 

3) Added support for xcat on ingester


### Upgrade steps

0) Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_10_HotFix.sql)

1) clean up drive -

4) update ltowala sequence code for overlapping Zs

279375 - hard shut down impacted job -- 

rerun the failed VPUB restore jobs

ingest the digi priv1(4) and pub(2) - ingester up app down(5), app not responding window(1)

  

# Dwara App Version - 2.1.17 (18th Apr 2021)
### New features

0) Edited (Translations) support - flowelement.pathnameregex processing layer changed - needs regression

1) Support for scanning artifacts globally across artifactclasses

2) Auto initialising blank tapes - Needs the application.properties entry mentioned in upgrade steps section

3) Next barcode to be printed

4) Support showing username and not path in scanned resultset

5) Tapes Handling - load/unload tapes from library

6) Support search on completed date range on /request API

### Maintenance

1) Soft rename artifacts with overlapping Sequence code - Call API end point from swagger: /fixSeqCodeForEdited 

### Upgrade steps

0) Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_10.sql)

1) application.properties to have the below entry

> scheduler.blankTapeAutoInitializer.cronExpression=0 0 * ? * *

2) Update dwara-operations shell script

# Dwara App Version - 2.1.16 (4th Apr 2021)
### New features

1) Support for photo pub

2) Soft rename

3) Mark completed action for a job

### Bug fix

1) Exclude deleted artifacts from dupe check

2) Api updateUsedSpace to calculate tape free capacity

3) Locations: default location LTO Room doesn't map to a copy

### Upgrade steps

0) Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_9.sql)

1) Install ImageMagick ufraw exiv2

2) create photo-proxy/.copying folders in MAM

3) Call api end points from swagger: /catalog/updateUsedSpace

4) Clean tape drive

5) reminder to update props file with some junk additions

# Dwara App Version - 2.1.15 (28th Mar 2021)
### New features

1) Bulk update locations

2) Processing layer changes to support outputfiletype's extensionFiletype suffix;

### Upgrade steps

0) Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_8.sql)

# Dwara App Version - 2.1.14 (21st Mar 2021)
### New features

1) Tape catalog searching

2) Artifact catalog searching

### Bug fix

1) File name too long Mam update failures

2) RemoteCmdLineExecuter tmp std channel file deletion

3) Directory watcher changes on folder ownership

4) Artifact dupe fix

5) Resetting the system request status to queued on a job being requeued

6) clean up Sql patches
	dwara-db\src\data\sql\Z140.sql
	dwara-db\src\data\sql\dwara_db_cleanUp_Mar_2021.sql
	
### Upgrade steps

1) Call api end points from swagger: /catalog/updateFinalizedDate, /catalog/updateUsedSpace

# Dwara App Version - 2.1.13 (11th Mar 2021)
### Bug fix

1) Artifact with Symbolic links not to be warned to the user as its of no consequence

2) Fix for write restore holding on a tape indefinitely - corrected

# Dwara App Version - 2.1.12 (10th Mar 2021)
### New features

1) Tagging support for Artifacts for quick search

2) Catalog changes - 
	
> Artifact View - Search on Artifact results in list of tapes Artifact has been written
	
> Tape View - Search on specific Tape number lists all artifacts written on it
	
3) Support for hard and soft links
	
> Pending 1 - Icon? folder under Poem-Sadhguru-Poem-For-New-Year-2021-Timespace_English_01Min-07Secs_Unconsolidated/FOOTAGE/Neue World - Free for Personal Use 2/Icon? still missing block details
	
> Pending 2 - Volume index after finalization to have link details... Need to spec it out

4) UI Enhancements

> Show pools running out of space on top

> Abilitiy to Sort Artifact by verified status(red/yellow/green thumbsup)

### Bug fix

1) Failed user request not reflecting status

2) Set Job.Volume used for dependent processing tasks

3) File path with space on mkvtoolnix

4) Rsync bwlimit configurable for Filecopier

5) Fix for write restore holding on a tape indefinitely

6) Keep jobs queued that dont have needed Tape on Library


### Upgrade steps

0) Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_7.sql) to fix the file size discrepancies

1) application.properties to have the below entry

> bwLimitRsync=25000/30000

> threadpoolexecutor.processingtask.core/maxPoolSize=4

> threadpoolexecutor.video-proxy-low-gen.core/maxPoolSize=2


2) AdminController to use payload like [{"task": "video-proxy-low-gen", "corePoolSize" : 5, "maxPoolSize" : 5, "priority" :3}]

# Dwara App Version - 2.1.11 (14th Feb 2021)
### Hot fix

LIVE Incident

Artifact sized > 1TB failed Tar writes because of archive block datatype was int and hence not able to hold the long value

Fixed table and fixed code

# Dwara App Version - 2.1.10 (Space [Control] - 7th Feb 2021)

### New features

1) Ability to control both Dwara and ffmpeg thread at runtime to facilitate optimising thread allocation. Runtime properties update using swagger.

### Bug fix

1) Maintenance mode - Ability to gracefully let already in_progress jobs to completion, but stop processing any further[Dont process queued jobs] - Fixed processing layer draining all jobs inspite of maintenance mode. Now app goes maintenance quickly 

### Upgrade steps

1) application.properties to have the below entry

> appMode=online


# Dwara App Version - 2.1.09 (Space - 31st Jan 2021 - Evening)
### Live Incident 
Introduced QC gen with a global pool backfired us with more qc gen jobs taken up parallely and with uncontrolled ffmpeg core threads increased the job completion of every process. So we want to control the ffmpeg threads and want it configurable. Hence the change   

### Upgrade steps

0 Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_4.sql)

1 Follow the step by step instruction guide

2 application.properties to have the following entries
>ffmpeg.video-proxy-low-gen.threads=2

>ffmpeg.video-digi-2020-preservation-gen.threads=2

>ffmpeg.video-digi-2020-qc-gen.threads=2

>scheduler.statusUpdater.fixedDelay=30000


# Dwara App Version - 2.1.08 (Space - 31st Jan 2021)
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

0 Apply the upgrade sql script(/dwara-db/src/data/sql/dwara_update_2_1_3.sql)

1 Follow the step by step instruction guide

2 Set up Rsync in remote server where 10G need to go. Create the configured destination directory and also a .copying directory under the destination directory 

3 Ensure application.properties has the following entry

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

* Following lines to be added to /etc/my.cnf

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