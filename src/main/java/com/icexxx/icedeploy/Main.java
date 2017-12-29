package com.icexxx.icedeploy;

public class Main {

	public static void main(String[] args) {
		String host = "118.190.205.147";
		String username = "root";
		String password = "Hutool12";
		int port = 22;
		String localFileName = "C:/Users/Administrator/Desktop/tp/ssm.war";
		String remoteTomcat = "/root/apache-tomcat-7.0.82";
		String remoteTemp = "/root/temp";
		String remoteBak = "/root/bak";
		String jdkHome = "/usr/lib/jvm/jdk1.8.0_151";
		String url = "http://118.190.205.147:8080/ssm/s";
		DeployCheck deployCheck = null;
		DeployService.deploy(host, username, password, port, localFileName, remoteTomcat, remoteTemp, remoteBak,
				jdkHome, url, deployCheck);

	}

}
