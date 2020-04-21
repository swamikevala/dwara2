# Dwara API

After cloning and setting up this project in your favourite IDE, please do the following.

apply the database scripts from src/data/sql in the below order
	schema.sql(ensure the schema name is whats used in the application.properties)
	dwara_master_tables.sql
	dwara_views.sql
	stage_specific_values_update.sql
	
start the app with skip tests
	mvn clean package/install spring-boot:run
	
TODO Add how to run a specific test case