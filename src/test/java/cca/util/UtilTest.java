package cca.util;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cca.core.CoverageBean;


public class UtilTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testReadZipFile() throws IOException {
		// SetUp
		String fileName = "src/test/resources/deploy.zip";
		
		// Exercise
		int expected = (int) new File(fileName).length();
		byte[] actual = Util.readZipFile(fileName);
		
		// Verify
		assertThat(actual.length, is(expected));
		
		// TearDown
	}
	
	@Test
	public void testShowResultAsTable() throws IOException {
		// SetUp
		List<CoverageBean> coverageBeanList = new ArrayList<CoverageBean>();
		CoverageBean coverageBean = new CoverageBean();
		coverageBean.setClassName("TestClass01");
		coverageBean.setShouldCoverLine(100);
		coverageBean.setCoveredLine(80);
		coverageBean.setUnCoveredLine(20);
		coverageBean.setCoverage(80);
		coverageBeanList.add(coverageBean);
		
		coverageBean = new CoverageBean();
		coverageBean.setClassName("TestClass02");
		coverageBean.setShouldCoverLine(20);
		coverageBean.setCoveredLine(1);
		coverageBean.setUnCoveredLine(19);
		coverageBean.setCoverage(5);
		coverageBeanList.add(coverageBean);
		
		// Exercise
		Util.showResultAsTable(coverageBeanList);
		
		// Verify
		
		// TearDown
	}
}