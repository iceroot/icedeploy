package com.icexxx.icedeploy;

public class Main {

	public static void main(String[] args) {
		String host = "192.168.1.123";
		String username = "root";
		String password = "root";
		int port = 22;
		String localFileName = "C:/Users/Administrator/Desktop/web.war";
		String remoteTomcat = "/root/apache-tomcat-7.0.82";
		String remoteTemp = "/root/temp";
		String remoteBak = "/root/bak";
		String jdkHome = "/usr/lib/jvm/jdk1.8.0_151";
		String url = "http://192.168.1.123:8080/web";
		DeployCheck deployCheck = null;
		DeployService.deploy(host, username, password, port, localFileName, remoteTomcat, remoteTemp, remoteBak,
				jdkHome, url, deployCheck);

	}

}
