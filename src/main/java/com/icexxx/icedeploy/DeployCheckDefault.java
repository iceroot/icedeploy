package com.icexxx.icedeploy;

public class DeployCheckDefault implements DeployCheck {

	public boolean check(String result) {
		if (result == null) {
			return false;
		}
		if ("".equals(result)) {
			return false;
		}
		// System.out.println(result);
		if (result.contains("<h1>HTTP Status 404 - </h1>")) {
			return false;
		}
		if (result.contains("<h1>HTTP Status 500 - </h1>")) {
			return false;
		}
		return true;
	}

}
