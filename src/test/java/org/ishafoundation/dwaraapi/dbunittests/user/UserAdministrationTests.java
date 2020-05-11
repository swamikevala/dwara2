package org.ishafoundation.dwaraapi.dbunittests.user;

import org.ishafoundation.dwaraapi.entrypoint.resource.controller.admin.UserAdminController;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
//Disabling FK check using the listener didnt work @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DisableForeignKeysDbUnitTestExecutionListener.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@SpringBootTest
public class UserAdministrationTests {

	@Autowired
	private UserAdminController userAdminController;
	
    // TODO parameterise these ...
    String username = "Sadhguru";
    String password = "Shambho";
    String newPassword = "ShivaShambho";
    
    // Commenting the below test method as we dont know, how to ... 
    // ... verify/compare the varying hash which is the test in question
    public void testSetResetPassword() throws Exception {
    	userAdminController.setpassword(username, newPassword);
    }    
    
    
    /*
     * Showcasing multiple ways of achieving the same results of resetting the DB to original state because of FK constraints...
     * 
     * 1) testRegn1 - We are verifying(@ExpectedDatabase) and deleting(TearDown) only the added row
     * 2) testRegn2 - Added all the associated tables in the dataset and delete the associated table entries first and then delete the user entries.
     * 		NOTE : User is also used in Request table and having the requests mentioned in the dataset is not feasible.
     * 3) Also checked on https://dzone.com/articles/solve-foreign-key-problems. Didnt understand what needs doing... 
     * 4) Disabling foreign key constraints check. Didnt work as the global session is created after the Hikari DB connection pool is created and the connection setting the session is different from the connection executing the query... 
     */
    
    @Test
    //Dynamic query framing not possible @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED, table="user", query="select * from user where name = \"" + username +  "\"")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED, table="user", query="select * from user where name = \"ShivaShambho\"")
	@DatabaseTearDown(type=DatabaseOperation.DELETE, value="classpath:/dbunit_test_inputs/original/user_to_be_deleted.xml")
    public void testRegn1() throws Exception {
    	username = "ShivaShambho";
    	userAdminController.processRegister(username, password);
    }

    /*
     *  Notes
     *  1) CLEAN_INSERT = DELETE_ALL + INSERT
     *  2) The dataset ordering should be the insert order we wanted to achieve. In this case, user first followed by action_user/libraryclass_action_user. springdbunit reverses the table order for deletion...
     */
    @Test
    @DatabaseSetups({
    	@DatabaseSetup(type=DatabaseOperation.DELETE_ALL, value="classpath:/dbunit_test_inputs/original/user.xml"),
    	@DatabaseSetup(type=DatabaseOperation.INSERT, value="classpath:/dbunit_test_inputs/original/user.xml")
    })
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED, value="classpath:/dbunit_test_inputs/expected/user.xml")
	@DatabaseTearDown(type=DatabaseOperation.CLEAN_INSERT, value="classpath:/dbunit_test_inputs/original/user.xml")
    public void testRegn2() throws Exception {
    	//userAdminController.setpassword(username, newPassword);
    	username = "ShivaShambho";
    	userAdminController.processRegister(username, password);
    }
}
