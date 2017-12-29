package com.icexxx.icedeploy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.http.HttpException;
import com.xiaoleilu.hutool.http.HttpUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import com.xiaoleilu.hutool.util.ThreadUtil;

public class DeployService {
	public static String deploy(String host, String username, String password, int port, String localFileName,
			String remoteTomcat, String remoteTemp, String remoteBak, String jdkHome, String url,
			DeployCheck deployCheck) {

		localFileName = localFileName.replace("\\", "/");
		JSch jsch = new JSch();
		Session session = null;
		ChannelSftp openChannel = null;
		ChannelExec cmdChannel = null;
		remoteTomcat = StrUtil.removeSuffix(remoteTomcat, "/");
		String remoteWebapp = remoteTomcat + "/webapps";
		try {
			session = jsch.getSession(username, host, port);
			if (password != null) {
				session.setPassword(password);
			}
			Properties pro = new Properties();
			pro.put("StrictHostKeyChecking", "no");
			session.setConfig(pro);
			try {
				session.connect(10000);
			} catch (JSchException e) {
				if ("Auth fail".equals(e.getMessage())) {
					System.out.println("无法连接服务器,可能是密码错误,正在用ping命令检查");
					if (DeployUtil.ping(host)) {
						System.out.println("可以ping通:" + host + ",应该是密码错误");
					} else {
						System.out.println("无法ping通:" + host);
					}
					return null;
				} else {
					e.printStackTrace();
				}
			}
			if (StrUtil.isBlank(jdkHome)) {
				jdkHome = DeployUtil.javaHome(session);
			}
			openChannel = (ChannelSftp) session.openChannel("sftp");
			openChannel.connect();
			String simpleFileName = StrUtil.subAfter(localFileName, "/", true);
			if (remoteTemp.endsWith("/")) {
				remoteTemp += simpleFileName;
			}
			System.out.println("上传开始");
			openChannel.put(localFileName, remoteTemp);
			System.out.println("上传完成");
			String backup = null;
			if (StrUtil.isNotBlank(remoteBak)) {
				remoteBak = StrUtil.removeSuffix(remoteBak, "/");
				String time = DateUtil.format(new Date(), "yyyyMMdd_HHmmss");
				backup = remoteBak + "/" + time;
				openChannel.mkdir(backup);
			}
			if (openChannel != null) {
				openChannel.disconnect();
				openChannel = null;
			}
			String remoteWebappWar = remoteWebapp + "/" + simpleFileName;
			String remoteWebappDir = StrUtil.removeSuffix(remoteWebappWar, ".war");
			if (StrUtil.isNotBlank(remoteBak)) {
				cmdChannel = (ChannelExec) session.openChannel("exec");
				String command = "cp -r " + remoteWebappDir + " " + backup;
				System.out.println(command);
				cmdChannel.setCommand(command);
				cmdChannel.connect(5000);

				command = "cp " + remoteWebappWar + " " + backup;
				System.out.println(command);
				cmdChannel = (ChannelExec) session.openChannel("exec");
				cmdChannel.setCommand(command);
				cmdChannel.connect(5000);
				System.out.println("备份完成");
			}

			Session sessionShell = null;
			sessionShell = jsch.getSession(username, host, port);
			if (password != null) {
				sessionShell.setPassword(password);
			}
			Properties proShell = new Properties();
			proShell.put("StrictHostKeyChecking", "no");
			sessionShell.setConfig(proShell);
			try {
				sessionShell.connect(10000);
			} catch (JSchException e) {
				e.printStackTrace();
			}
			String command = null;
			String remoteBin = remoteTomcat + "/bin/";
			command = "export JAVA_HOME=" + jdkHome + ";" + remoteBin + "shutdown.sh";
			System.out.println("执行命令:" + command);
			cmdChannel = (ChannelExec) sessionShell.openChannel("exec");
			cmdChannel.setCommand(command);
			cmdChannel.connect(5000);
			int exitStatusShutdown = cmdChannel.getExitStatus();
			System.out.println("exitStatusShutdown=" + exitStatusShutdown);
			class MyThread extends Thread {
				public InputStream inputStreamShutdown = null;
				public InputStream errorStreamShutdown = null;

				public void initt(InputStream inputStreamShutdown, InputStream errorStreamShutdown) {
					this.inputStreamShutdown = inputStreamShutdown;
					this.errorStreamShutdown = errorStreamShutdown;
				}
			}
			MyThread thread = new MyThread() {
				@Override
				public void run() {
					String readShutdown = DeployUtil.read2(inputStreamShutdown);
					if (StrUtil.isNotBlank(readShutdown)) {
						System.out.println("readShutdown=" + readShutdown);
					}
					String errorShutdown = DeployUtil.read2(errorStreamShutdown);
					if (StrUtil.isNotBlank(errorShutdown)) {
						System.out.println("errorShutdown=" + errorShutdown);
					}
				}
			};
			try {
				thread.initt(cmdChannel.getInputStream(), cmdChannel.getErrStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			thread.start();
			ThreadUtil.sleep(3000);
			String work = remoteTomcat + "/work/*";
			command = "rm -rf " + work;
			System.out.println("执行命令:" + command);
			cmdChannel = (ChannelExec) session.openChannel("exec");
			cmdChannel.setCommand(command);
			cmdChannel.connect(5000);
			command = "rm -rf " + remoteWebappDir;
			System.out.println("执行命令:" + command);
			cmdChannel = (ChannelExec) session.openChannel("exec");
			cmdChannel.setCommand(command);
			cmdChannel.connect(5000);
			command = "rm " + remoteWebappWar;
			System.out.println("执行命令:" + command);
			cmdChannel = (ChannelExec) session.openChannel("exec");
			cmdChannel.setCommand(command);
			cmdChannel.connect(5000);
			System.out.println("删除完成");

			String remoteTempFile = remoteTemp + "/" + simpleFileName;
			cmdChannel = (ChannelExec) session.openChannel("exec");
			command = "mv " + remoteTempFile + " " + remoteWebappWar;
			System.out.println("执行命令:" + command);
			cmdChannel.setCommand(command);
			cmdChannel.connect(5000);
			command = "export JAVA_HOME=" + jdkHome + ";" + remoteBin + "startup.sh";
			System.out.println("执行命令:" + command);
			cmdChannel = (ChannelExec) session.openChannel("exec");
			cmdChannel.setCommand(command);
			cmdChannel.connect(5000);
			int exitStatus = cmdChannel.getExitStatus();
			System.out.println(exitStatus);
			InputStream inputStream = null;
			InputStream errorStream = null;
			try {
				inputStream = cmdChannel.getInputStream();
				errorStream = cmdChannel.getErrStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("读取startup.sh执行结果");
			String read = DeployUtil.read3(inputStream);
			String err = DeployUtil.read3(errorStream);
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (errorStream != null) {
				try {
					errorStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if ("Tomcat started.".equals(read.trim())) {
				System.out.println("<<tomcat启动完成>>");
			}
			if (StrUtil.isNotBlank(read)) {
				System.out.println(read);
			}
			if (StrUtil.isNotBlank(err)) {
				System.out.println(err);
			}
			if (cmdChannel != null && !cmdChannel.isClosed()) {
				cmdChannel.disconnect();
			}
			System.out.println("启动服务器完成");
			if (deployCheck == null) {
				deployCheck = new DeployCheckDefault();
			}
			String result = null;
			int delay = DeployContext.getDelay();
			String catalinaFileName = DeployContext.getCatalinaFileName();
			if (delay == 0) {
				delay = 5000;
			}
			if (StrUtil.isBlank(catalinaFileName)) {
				catalinaFileName = "catalina.out";
			}
			String tomcatLogs = remoteTomcat + "/logs/";
			command = "tail -n 100 -f " + tomcatLogs + catalinaFileName;
			System.out.println("执行命令:" + command);
			cmdChannel = (ChannelExec) session.openChannel("exec");
			cmdChannel.setCommand(command);
			cmdChannel.connect(5000);
			int exitStatus2 = exitStatus;
			System.out.println(exitStatus2);
			try {
				inputStream = cmdChannel.getInputStream();
				errorStream = cmdChannel.getErrStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("读取tail文件开始");
			read = DeployUtil.read3(inputStream);
			System.out.println("读取tail文件结束");
//			System.out.println(read);
			err = DeployUtil.read3(errorStream);
			if (StrUtil.isNotBlank(err)) {
				System.out.println(err);
			}
			System.out.println("开始检查请求");
			try {
				result = HttpUtil.get(url, 10000);
			} catch (HttpException e) {
				if ("Read timed out".equals(e.getMessage())) {
					try {
						result = HttpUtil.get(url, 10000);
					} catch (HttpException e1) {
						if ("Read timed out".equals(e1.getMessage())) {
							System.out.println("请求超时,部署失败");
							ThreadUtil.sleep(3000);
							if(StrUtil.isBlank(read)){
							    read = DeployUtil.read4(inputStream);
								System.out.println(read);
							}
							if (inputStream != null) {
								try {
									inputStream.close();
								} catch (IOException e2) {
									e2.printStackTrace();
								}
							}
							if (errorStream != null) {
								try {
									errorStream.close();
								} catch (IOException e2) {
									e2.printStackTrace();
								}
							}
							System.exit(0);
						}
					}
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (errorStream != null) {
				try {
					errorStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (result == null) {
				System.out.println("检查请求返回结果为null");
			} else if ("".equals(result)) {
				System.out.println("检查请求返回结果为空字符串");
			} else {
				boolean checkFlag = deployCheck.check(result);
				if (checkFlag) {
					System.out.println("部署成功");
				} else {
					System.out.println("部署失败");
				}
				ThreadUtil.sleep(3000);
				System.exit(0);
			}
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (SftpException e) {
			e.printStackTrace();
		} finally {
			if (openChannel != null) {
				openChannel.disconnect();
			}
			if (cmdChannel != null) {
				cmdChannel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}
		return null;
	}
}
