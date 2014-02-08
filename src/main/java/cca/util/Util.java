package cca.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import cca.core.CoverageBean;

public class Util {
	/**
	 * Read zip file
	 * @param _zipFile
	 * @return byte[]
	 */
	public static byte[] readZipFile(String _zipFile) {
		File deployZip = new File(_zipFile);
		if (!deployZip.exists() || !deployZip.isFile()) {
			try {
				throw new Exception("Cannot find the zip file to deploy. Looking for " + deployZip.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try {
			fis = new FileInputStream(deployZip);
			bos = new ByteArrayOutputStream();
			int readbyte = -1;
			while ((readbyte = fis.read()) != -1)  {
				bos.write(readbyte);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
		return bos.toByteArray();
	}
	
	/**
	 * Show code coverage of each Apex as table format
	 * @param coverageBeanList
	 */
	public static void showResultAsTable(List<CoverageBean> coverageBeanList) {
		int totalCoveredLine = 0;
		int totalShouldCoverLine = 0;
		
		System.out.println("\n");
		System.out.println("[Code Coverage]");
		System.out.printf("%-18s  %16s  %16s  %9s%n", "ClassName", "#CoveredLine", "#ShouldCoverLine", "%Coverage");
		System.out.printf("%-18s  %16s  %16s  %9s%n", "------------------", "------------", "----------------", "---------");
		for (CoverageBean coverageBean : coverageBeanList) {
			System.out.printf("%-18s", coverageBean.getClassName());
			System.out.printf("%18d", coverageBean.getCoveredLine());
			System.out.printf("%18d", coverageBean.getShouldCoverLine());
			System.out.printf("%11d", coverageBean.getCoverage());
			System.out.println("");
			
			totalCoveredLine += coverageBean.getCoveredLine();
			totalShouldCoverLine += coverageBean.getShouldCoverLine();
		}
		System.out.printf("%-18s  %16s  %16s  %9s%n", "------------------", "------------", "----------------", "---------");
		System.out.printf("%-18s", "Overall coverage");
		System.out.printf("%18d", totalCoveredLine);
		System.out.printf("%18d", totalShouldCoverLine);
		System.out.printf("%11d", (int)((totalCoveredLine/(double)totalShouldCoverLine)*100));
		System.out.println("\n");
	}
}
