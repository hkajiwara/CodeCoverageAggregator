package cca.core;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import cca.util.Util;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.CodeCoverageResult;
import com.sforce.soap.metadata.CodeCoverageWarning;
import com.sforce.soap.metadata.DeployDetails;
import com.sforce.soap.metadata.DeployMessage;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.RunTestFailure;
import com.sforce.soap.metadata.RunTestsResult;
import com.sforce.soap.partner.*;
import com.sforce.ws.*;

public class CodeCoverageAggregator {
	private final long ONE_SECOND = 1000;
	private static String PROPFILE = "cca.properties";
	private MetadataConnection mdConnection;
	private String authEndpoint;
	private String deployFile;
	private int maxNumPollRequests;
	
	/**
	 * Constructor
	 * @param propFile
	 * @throws IOException
	 */
	public CodeCoverageAggregator(String propFile) throws IOException {
		if (propFile == null || propFile == "") {
			propFile = PROPFILE;
		}
		Properties prop = new Properties();
		InputStream inStream = null;
		inStream = CodeCoverageAggregator.class.getClassLoader().getResourceAsStream(propFile);
		prop.load(inStream);
		
		this.authEndpoint = prop.getProperty("authEndpoint");
		this.deployFile = prop.getProperty("deployFile");
		this.maxNumPollRequests = Integer.valueOf(prop.getProperty("maxNumPollRequests"));
	}
	
	/**
	 * Main
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 1) {
			PROPFILE = args[0];
		}
		
		CodeCoverageAggregator cca = new CodeCoverageAggregator(PROPFILE);
		cca.run();
	}
	
	/**
	 * Run
	 * @throws Exception
	 */
	protected void run() throws Exception {
		String[] userInfo = getUserInfo();
		LoginResult loginResult = ptnLogin(userInfo[0], userInfo[1], authEndpoint);
		if (mdLogin(loginResult)) {
			DeployResult deployResult = deployZip(deployFile);
			Util.showResultAsTable(calcCoverage(deployResult));
		}
	}
	
	/**
	 * Get username and password
	 * @return userInfo
	 */
	protected String[] getUserInfo() {
		String[] userInfo = new String[2];
		
		Console console = System.console();
		userInfo[0] = console.readLine("Username: ");
		char[] password = console.readPassword("Password: ");
		userInfo[1] = String.valueOf(password);
		
		return userInfo;
	}
	
	/**
	 * Login as Partner
	 * @param _username
	 * @param _password
	 * @param _authEndpoint
	 * @return loginResult
	 */
	protected LoginResult ptnLogin(String _username, String _password, String _authEndpoint) {
		LoginResult loginResult = null;
		
		ConnectorConfig config = new ConnectorConfig();
		config.setAuthEndpoint(_authEndpoint);
		config.setServiceEndpoint(_authEndpoint);
		config.setManualLogin(true);
		try {
			loginResult = new PartnerConnection(config).login(_username, _password);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
		return loginResult;
	}
		
	/**
	 * Login as MetaData
	 * @param _loginResult
	 * @return success
	 */
	protected boolean mdLogin(LoginResult _loginResult) {
		boolean success = false;
		
		ConnectorConfig config = new ConnectorConfig();
	    config.setServiceEndpoint(_loginResult.getMetadataServerUrl());
	    config.setSessionId(_loginResult.getSessionId());
		try {
			mdConnection = new MetadataConnection(config);
			success = true;
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
		return success;
	}
	
	/**
	 * Deploy the dummy resource
	 * @param _deployFile
	 * @return deployResult
	 * @throws Exception
	 */
	protected DeployResult deployZip(String _deployFile) throws Exception {
		byte zipBytes[] = Util.readZipFile(_deployFile);

		DeployOptions deployOptions = new DeployOptions();
		deployOptions.setPerformRetrieve(false);
		deployOptions.setRollbackOnError(true);
		deployOptions.setRunAllTests(true);
		deployOptions.setCheckOnly(true);

		AsyncResult asyncResult = mdConnection.deploy(zipBytes, deployOptions);
		DeployResult deployResult = waitForDeployCompletion(asyncResult.getId());
		if (!deployResult.isSuccess()) {
			printErrors(deployResult, "Final list of failures:\n");
			throw new Exception("The files were not successfully deployed");
		}
		
		return deployResult;
	}
	
	/**
	 * Wait for the completion of the deploy
	 * @param asyncResultId
	 * @return deployResult
	 * @throws Exception
	 */
	protected DeployResult waitForDeployCompletion(String asyncResultId) throws Exception {
		System.out.println("[Status]");
		
		int poll = 0;
		long waitTimeMilliSecs = ONE_SECOND * 3;
		DeployResult deployResult;
		do {
			Thread.sleep(waitTimeMilliSecs);
			if (poll++ > maxNumPollRequests) {
				throw new Exception(
					"Request timed out. If this is a large set of metadata components, " +
					"ensure that MAX_NUM_POLL_REQUESTS is sufficient.");
			}

			deployResult = mdConnection.checkDeployStatus(asyncResultId, true);
			System.out.print("Status is: " + deployResult.getStatus());
			if (!(deployResult.getStatus().toString()).equals("Pending") && 
				deployResult.getStateDetail() != null) {
				
				System.out.println(
					" | " +
					deployResult.getNumberTestsCompleted() + "/" + deployResult.getNumberTestsTotal() + " " +
					"(" + deployResult.getNumberTestErrors() + " errors)" + " | " + 
					deployResult.getStateDetail());
			} else {
				System.out.println("");
			}
		}
		while (!deployResult.isDone());

		if (!deployResult.isSuccess() && deployResult.getErrorStatusCode() != null) {
			throw new Exception(deployResult.getErrorStatusCode() + " msg: " + deployResult.getErrorMessage());
		}
		deployResult = mdConnection.checkDeployStatus(asyncResultId, true);
		
		return deployResult;
	}
	
	/**
	 * Calculate code coverage
	 * @param _deployResult
	 * @return coverageBeanList
	 */
	protected List<CoverageBean> calcCoverage(DeployResult _deployResult) {
		List<CoverageBean> coverageBeanList = new ArrayList<CoverageBean>();
		
		DeployDetails deployDetails = _deployResult.getDetails();
		RunTestsResult runTestResults = deployDetails.getRunTestResult();
		if (runTestResults.getCodeCoverageWarnings().length != 0) {
			System.out.println(runTestResults.getCodeCoverageWarnings()[0].getMessage());
		}
		
		CodeCoverageResult[] codeCoverageResult = runTestResults.getCodeCoverage();
		if (codeCoverageResult != null) {
			for (CodeCoverageResult ccr : codeCoverageResult) {
				if (ccr.getNumLocations() != 0) {
					CoverageBean coverageBean = new CoverageBean();
					coverageBean.setClassName(ccr.getName());
					coverageBean.setUnCoveredLine(ccr.getNumLocationsNotCovered());
					coverageBean.setShouldCoverLine(ccr.getNumLocations());
					coverageBean.setCoveredLine(ccr.getNumLocations() - ccr.getNumLocationsNotCovered());
					int coverage = (int)((1-ccr.getNumLocationsNotCovered()/(double)ccr.getNumLocations())*100);
					coverageBean.setCoverage(coverage);

					if (ccr.getLocationsNotCovered() != null) {
						coverageBean.setCodeLocation(ccr.getLocationsNotCovered());
					}
					coverageBeanList.add(coverageBean);
				}
			}
		}
		return coverageBeanList;
	}
	
	/**
	 * Show error messages
	 * @param result
	 * @param messageHeader
	 */
	protected void printErrors(DeployResult result, String messageHeader)	{
		System.out.println("\n");
		System.out.println("[Error]");
		
		DeployDetails deployDetails = result.getDetails();
		
		StringBuilder errorMessageBuilder = new StringBuilder();
		if (deployDetails != null) {
			DeployMessage[] componentFailures = deployDetails.getComponentFailures();
			for (DeployMessage message : componentFailures) {
				String loc = (message.getLineNumber() == 0 ? "" :
					("(" + message.getLineNumber() + "," +
							message.getColumnNumber() + ")"));
				if (loc.length() == 0
						&& !message.getFileName().equals(message.getFullName())) {
					loc = "(" + message.getFullName() + ")";
				}
				errorMessageBuilder.append(message.getFileName() + loc + ":" +
						message.getProblem()).append('\n');
			}
			RunTestsResult rtr = deployDetails.getRunTestResult();
			if (rtr.getFailures() != null) {
				int i = 1;
				for (RunTestFailure failure : rtr.getFailures()) {
					String n = (failure.getNamespace() == null ? "" :
						(failure.getNamespace() + ".")) + failure.getName();
					errorMessageBuilder.append("[" + i++ + "] " + 
							"Test failure, method: " + n + "." +
							failure.getMethodName() + " -- " +
							failure.getMessage() + " stack " +
							failure.getStackTrace() + "\n");
				}
			}
			if (rtr.getCodeCoverageWarnings() != null) {
				for (CodeCoverageWarning ccw : rtr.getCodeCoverageWarnings()) {
					errorMessageBuilder.append("\nCode coverage issue");
					if (ccw.getName() != null) {
						String n = (ccw.getNamespace() == null ? "" :
							(ccw.getNamespace() + ".")) + ccw.getName();
						errorMessageBuilder.append(", class: " + n);
					}
					errorMessageBuilder.append(" -- " + ccw.getMessage() + "\n");
				}
			}
		}
		
		if (errorMessageBuilder.length() > 0) {
			errorMessageBuilder.insert(0, messageHeader);
			System.out.println(errorMessageBuilder.toString());
		}
	}
}
