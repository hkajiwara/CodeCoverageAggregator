package cca.core;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;


import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cca.util.Util;

import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.CodeLocation;
import com.sforce.soap.partner.LoginResult;

public class CodeCoverageAggregatorTest {
	final String PROPFILE = "unittest.properties";
	String username;
	String password;
	String authEndpoint;
	String deployFile;
	int maxNumPollRequests;
	
	@Before
	public void setUp() throws Exception {
		Properties testprop = new Properties();
		InputStream inStream = CodeCoverageAggregator.class.getClassLoader().getResourceAsStream("unittest.properties");
		testprop.load(inStream);
		
		this.username = testprop.getProperty("username");
		this.password = testprop.getProperty("password");
		this.authEndpoint = testprop.getProperty("authEndpoint");
		this.deployFile = testprop.getProperty("deployFile");
		this.maxNumPollRequests = Integer.valueOf(testprop.getProperty("maxNumPollRequests"));
	}
	
	@Test
	@Ignore
	public void testCodeCoverageAggregator() throws Exception {
		// Setup
		
		// Exercise
		CodeCoverageAggregator sut = new CodeCoverageAggregator(PROPFILE);
		
		Field authEndpointField = sut.getClass().getDeclaredField("authEndpoint");
		authEndpointField.setAccessible(true);
		Field deployFileField = sut.getClass().getDeclaredField("deployFile");
		deployFileField.setAccessible(true);
		Field maxNumPollRequestsField = sut.getClass().getDeclaredField("maxNumPollRequests");
		maxNumPollRequestsField.setAccessible(true);

		// Verify
		assertEquals(authEndpointField.get(sut), authEndpoint);
		assertEquals(deployFileField.get(sut), deployFile);
		assertEquals(maxNumPollRequestsField.get(sut), maxNumPollRequests);
    }
	
	@Test
	@Ignore
	public void testGetUserInfo() throws Exception {
		// Setup
		String[] userInfo = {"user1", "pass1"};
		CodeCoverageAggregator mock = EasyMock.createMock(CodeCoverageAggregator.class);
		EasyMock.expect(mock.getUserInfo()).andReturn(userInfo);
		EasyMock.replay(mock);
		
		// Exercise
		String[] actual = mock.getUserInfo();
		
		// Verify
		assertThat(actual[0], is("user1"));
		assertThat(actual[1], is("pass1"));
		EasyMock.verify(mock);
		
		// TearDown
	}

	@Test
	@Ignore
	public void testPtnLogin() throws Exception {
		// Setup
		CodeCoverageAggregator sut = new CodeCoverageAggregator(PROPFILE);
		
		// Exercise
		LoginResult actual = sut.ptnLogin(username, password, authEndpoint);
		
		// Verify
		assertThat(actual, is(notNullValue()));
		
		// TearDown
	}
	
	@Test
	@Ignore
	public void testMdLogin() throws Exception {
		// Setup
		CodeCoverageAggregator sut = new CodeCoverageAggregator(PROPFILE);
		
		// Exercise
		boolean expectd = true;
		LoginResult loginResult = sut.ptnLogin(username, password, authEndpoint);
		boolean actual = sut.mdLogin(loginResult);
		
		// Verify
		assertThat(actual, is(expectd));
		
		// TearDown		
	}
	
	@Test
	@Ignore
	public void testDeployZip() throws Exception {
		// Setup
		CodeCoverageAggregator sut = new CodeCoverageAggregator(PROPFILE);
		
		// Exercise
		LoginResult loginResult = sut.ptnLogin(username, password, authEndpoint);
		sut.mdLogin(loginResult);
		DeployResult actual = sut.deployZip(deployFile);
		
		// Verify
		assertThat(actual, is(notNullValue()));
		
		// TearDown
	}
	
	@Test
	@Ignore
	public void testCoverageCalc() throws Exception {
		// Setup
		CodeCoverageAggregator sut = new CodeCoverageAggregator(PROPFILE);
		
		// Exercise
		LoginResult loginResult = sut.ptnLogin(username, password, authEndpoint);
		sut.mdLogin(loginResult);
		DeployResult deployResult = sut.deployZip(deployFile);
		List<CoverageBean> actual = sut.calcCoverage(deployResult);
		
		// Verify
		assertThat(actual, is(notNullValue()));

		// TearDown
	}
	
	@Test
	@Ignore
	public void testCoverageBean() throws Exception {
		// Setup
		CoverageBean sut = new CoverageBean();
		String className = "CodeCoverageAggregatorTest";
		int coveredLine = 5;
		int unCoveredLine = 5;
		int shouldCoveredLine = 10;
		int coverage = 50;
		int culumn = 1;
		
		CodeLocation[] codeLocation = new CodeLocation[1];
		codeLocation[0] = new CodeLocation();
		codeLocation[0].setColumn(culumn);
		
		// Exercise
		sut.setClassName(className);
		sut.setCoveredLine(coveredLine);
		sut.setUnCoveredLine(unCoveredLine);
		sut.setShouldCoverLine(shouldCoveredLine);
		sut.setCoverage(coverage);
		sut.setCodeLocation(codeLocation);
		
		// Verify
		assertThat(sut.getClassName(), is(className));
		assertThat(sut.getCoveredLine(), is(coveredLine));
		assertThat(sut.getUnCoveredLine(), is(unCoveredLine));
		assertThat(sut.getShouldCoverLine(), is(shouldCoveredLine));
		assertThat(sut.getCoverage(), is(coverage));
		assertThat(sut.getCodeLocation()[0].getColumn(), is(culumn));
		
		// TearDown
	}
	
	@Test
	public void testRun() throws Exception {
		// Setup
		CodeCoverageAggregator sut = new CodeCoverageAggregator(PROPFILE);
		
		// Exercise
		LoginResult loginResult = sut.ptnLogin(username, password, authEndpoint);
		sut.mdLogin(loginResult);
		DeployResult deployResult = sut.deployZip(deployFile);
		
		// Verify
		if (deployResult.getNumberTestErrors() == 0) {
			Util.showResultAsTable(sut.calcCoverage(deployResult));
		}

		// TearDown
	}
}