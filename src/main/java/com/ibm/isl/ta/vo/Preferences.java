package com.ibm.isl.ta.vo;

public class Preferences {
	String useFlexibleEnv;
    String cloudChoice;
    int devEffort;
    int overhead;
    
	public String getUseFlexibleEnv() {
		return useFlexibleEnv;
	}
	public void setUseFlexibleEnv(String useFlexibleEnv) {
		this.useFlexibleEnv = useFlexibleEnv;
	}
	public String getCloudChoice() {
		return cloudChoice;
	}
	public void setCloudChoice(String cloudChoice) {
		this.cloudChoice = cloudChoice;
	}
	public int getDevEffort() {
		return devEffort;
	}
	public void setDevEffort(int devEffort) {
		this.devEffort = devEffort;
	}
	public int getOverhead() {
		return overhead;
	}
	public void setOverhead(int overhead) {
		this.overhead = overhead;
	}
}
