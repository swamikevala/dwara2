package org.ishafoundation.dwaraapi;

import javax.sql.DataSource;

import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;

/**
 * Ensures that
 *  
 * 1) The schema is set to the DatabaseDataSourceConnection so dbunit doesnt throw AmbiguousTableException...
 * 2) The right MySql artifacts(DataTypeFactory n MetadataHandler) are configured.
 */
@Configuration
public class DbUnitConnectionConfig {
	
	@Autowired
	private DataSource dataSource;

	@Value("${dwara.database.name}")
	private String dwaraSchemaName;
	
    @Bean(name="dbUnitDatabaseConnection")
    public DatabaseDataSourceConnectionFactoryBean getConnection() 
    {
    	DatabaseDataSourceConnectionFactoryBean databaseDataSourceConnectionFactoryBean = new DatabaseDataSourceConnectionFactoryBean(dataSource);

    	DatabaseConfigBean databaseConfigBean = new DatabaseConfigBean();
    	databaseConfigBean.setDatatypeFactory(new MySqlDataTypeFactory());
    	databaseConfigBean.setMetadataHandler(new MySqlMetadataHandler());

		databaseDataSourceConnectionFactoryBean.setDatabaseConfig(databaseConfigBean);
    	databaseDataSourceConnectionFactoryBean.setSchema(dwaraSchemaName);
		return databaseDataSourceConnectionFactoryBean;

    }
    
    //@Bean
    PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }
}