# Dwara API
Assuming you have been **on-boarded into dwara** and have **java, maven, eclipse(or your favourite IDE) and mysql 5.7** set up in your local, please continue with the below.

After git cloning and importing this maven project in IDE, please do the following.

Apply the database scripts from *dwara-db/src/data/sql* in the below order

    1. schema.sql(ensure the schema name is what is used in the application.properties)
    2. dwara_master_tables.sql
    3. dwara_master_device_table.sql(NOTE: The quadstor instance 172.18.1.241 has 2 tape libraries one for dev and another for test. This uses the dev VTL)
    4. dwara_master_inactivate_archiveflow.sql(NOTE: Until the above step we have the configuration for storage layer also which involves setting up tapelibrary, initialising volume etc to have ingest working. If you want just the process flow run this script and the archive/storage flow will be inactivated)
    5. dwara_linux_specific_config_values_update.sql(The File.separator used in some of the config is OS dependent. Use this to apply linux based separator for configured paths)

Unzip the *dwara-service/src/test/resources/Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9.zip* file into /data/user/pgurumurthy/ingest/pub-video
	
Refer the confluence page on how to build and start the app https://art.iyc.ishafoundation.org/x/eVAbAQ 

You can now point the Dwara UI to this server or try using *http://localhost:9000/swagger-ui.html*
