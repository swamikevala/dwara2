# Dwara API
Assuming you have **java, maven, eclipse(or your favourite IDE) and mysql 5.7** set up in your local please continue with the below.

After git cloning and importing this maven project in IDE, please do the following.

Apply the database scripts from src/data/sql in the below order

    1. schema.sql(ensure the schema name is what is used in the application.properties)
    2. dwara_master_tables.sql

Please refer the confluence page on how to unit test https://art.iyc.ishafoundation.org/x/eVAbAQ 


TODO: The following are still WIP, please ignore...
	
Start the app with skip tests
    ```mvn clean package/install spring-boot:run```

	
Unzip the *src\test\resources\Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9.zip* file into /data/user/pgurumurthy/ingest/pub-video

You can now try ingesting using *http://localhost:9000/swagger-ui.html*

TODO Add how to run a specific test case