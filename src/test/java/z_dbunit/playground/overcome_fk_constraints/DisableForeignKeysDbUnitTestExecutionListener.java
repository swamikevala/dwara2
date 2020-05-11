package z_dbunit.playground.overcome_fk_constraints;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;

import com.github.springtestdbunit.DbUnitTestExecutionListener;

/**
 * Class DisableForeignKeysDbUnitTestExecutionListener
 * Simple wrapper class around DbUnitTestExecutionListener, which - for the time of importing the database -
 * disables Foreign Key Constraints checks.
 * This class can be extended by simply overriding toggleForeignKeysConstraintsForDbEngine(Connection, String, boolean);
 * subclasses should always call super-implementation for default case.
 */
public class DisableForeignKeysDbUnitTestExecutionListener
    extends DbUnitTestExecutionListener
{
	
	private static final Logger logger = LoggerFactory.getLogger(DisableForeignKeysDbUnitTestExecutionListener.class);
    private Connection cachedDbConnection;

    @Override
    public void beforeTestMethod(TestContext testContext)
        throws Exception
    {
        this.toggleForeignKeysConstraints(testContext, false);
        try {
        	super.beforeTestMethod(testContext);
	    }catch (Exception e) {
	    	System.out.println("error" + e);
	    	e.printStackTrace();
			throw e;
		}finally {
	    	this.toggleForeignKeysConstraints(testContext, true);	
		}
    }
    
	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
        this.toggleForeignKeysConstraints(testContext, false);
        try {
        	super.afterTestMethod(testContext);
        }catch (Exception e) {
	    	System.out.println("error" + e);
	    	e.printStackTrace();
			throw e;
        }finally {
        	this.toggleForeignKeysConstraints(testContext, true);	
		}
	}

    /**
     * Method should perform query to disable foreign keys constraints or return false,
     * if it is not able to perform such query (e.g. unknown database engine)
     *
     * @param connection    Database connection
     * @param dbProductName Name of the database product (as reported by connection metadata)
     * @param enabled       Expected state of foreign keys after the call
     *
     * @return True, if there was suitable statement for specified engine, otherwise false
     *
     * @throws SQLException
     */
    protected boolean toggleForeignKeysConstraintsForDbEngine(Connection connection, String dbProductName, boolean enabled)
        throws SQLException
    {
        switch (dbProductName)
        {
            case "HSQL Database Engine":
                connection.prepareStatement("SET DATABASE REFERENTIAL INTEGRITY " + (enabled ? "TRUE" : "FALSE"))
                          .execute();
                return (true);
            case "MySQL":    
                connection.prepareStatement("SET @@GLOBAL.foreign_key_checks = " + (enabled ? 1 : 0))
                .execute();
                connection.prepareStatement("SET @@SESSION.foreign_key_checks = " + (enabled ? 1 : 0))
                .execute();
                return (true);
        }
        return (false);
    }

    private void toggleForeignKeysConstraints(TestContext testContext, boolean enabled)
    {
        try
        {
            Connection connection = this.getDatabaseConnection(testContext);
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            if (!this.toggleForeignKeysConstraintsForDbEngine(connection, databaseProductName, enabled))
            {
                throw new IllegalStateException("Unknown database engine '" + databaseProductName +
                                                    "'. Unable to toggle foreign keys constraints.");
            }
        }
        catch (Throwable throwable)
        {
            logger.error("Unable to toggle Foreign keys constraints: " + throwable.getLocalizedMessage());
        }
    }

    synchronized private Connection getDatabaseConnection(TestContext testContext)
        throws SQLException
    {
        if (this.cachedDbConnection == null)
        {
            DataSource dataSource = testContext.getApplicationContext().getBean(DataSource.class);
            if (dataSource == null)
            {
                throw new IllegalStateException("Unable to obtain DataSource from ApplicationContext. " +
                                                    "Foreign constraints will not be disabled.");
            }

            IDatabaseConnection dsConnection = new DatabaseDataSourceConnection(dataSource);
            this.cachedDbConnection = dsConnection.getConnection();
        }

        return (this.cachedDbConnection);
    }
}