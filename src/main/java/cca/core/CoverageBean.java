package cca.core;

import lombok.Data;
import com.sforce.soap.metadata.CodeLocation;

public @Data class CoverageBean {
	private String className;
	private int coveredLine;
	private int unCoveredLine;
	private int shouldCoverLine;
//	private double coverage;
	private int coverage;
	private CodeLocation[] codeLocation;
	
	public CoverageBean() {
	}
}
