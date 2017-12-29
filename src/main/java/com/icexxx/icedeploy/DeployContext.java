package com.icexxx.icedeploy;

public class DeployContext {
	private static int delay;
	private static String catalinaFileName;

	public static int getDelay() {
		return delay;
	}

	public static void setDelay(int delay) {
		DeployContext.delay = delay;
	}

	public static String getCatalinaFileName() {
		return catalinaFileName;
	}

	public static void setCatalinaFileName(String catalinaFileName) {
		DeployContext.catalinaFileName = catalinaFileName;
	}

}
