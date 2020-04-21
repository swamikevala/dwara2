# Dwara API
Assuming you have **java, maven, eclipse(or your favourite IDE) and mysql 5.7** set up in your local continue with the below.

After cloning and setting up this project in IDE please do the following.

apply the database scripts from src/data/sql in the below order

    1. schema.sql(ensure the schema name is whats used in the application.properties)
    2. dwara_master_tables.sql
    3. test_master_tables.sql *(if you want to skip tape mechanism)*
    4. stage_specific_values_update.sql *(if you are setting this up in non-windows environment)*
	
start the app with skip tests
    ```mvn clean package/install spring-boot:run```

Apply the views sql script after the app is started. The sql files dont have the transaction tables and hence running this before the app start would throw error.

    - dwara_views.sql()	
	
Unzip the *src\test\resources\Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9.zip* file into /data/user/pgurumurthy/ingest/pub-video

You can now try ingesting using *http://localhost:9000/swagger-ui.html*

TODO Add how to run a specific test case