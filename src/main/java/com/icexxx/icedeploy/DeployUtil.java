package com.icexxx.icedeploy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.xiaoleilu.hutool.collection.CollUtil;
import com.xiaoleilu.hutool.util.RuntimeUtil;
import com.xiaoleilu.hutool.util.StrUtil;

public class DeployUtil {
	public static boolean ping(String host) {
		String execForStr = RuntimeUtil.execForStr("ping " + host);
		System.out.println(execForStr);
		if (execForStr != null) {
			if (execForStr.contains("请求超时")) {
				return false;
			} else if (execForStr.contains("(0% 丢失)")) {
				return true;
			}
		}
		return false;
	}

	public static String read(InputStream inputStream) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String br = null;
		String newLine = "\n";
		int index = 0;
		try {
			while ((br = reader.readLine()) != null) {
				sb.append(br);
				sb.append(newLine);
				System.out.println("br=" + br + "<");
				if (index++ > 1000) {
					break;
				}
			}
			return sb.toString();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static String read3(InputStream inputStream) {
		StringBuilder sb = new StringBuilder();
		int available = -1;
		try {
			available = inputStream.available();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] bys = new byte[1024];
		while (available > 0) {
			int length = -1;
			try {
				length = inputStream.read(bys, 0, 1024);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (length < 0) {
				break;
			} else {
				sb.append(new String(bys, 0, length));
			}
			try {
				available = inputStream.available();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static String read2(InputStream inputStream) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String br = null;
		String newLine = "\n";
		int index = 0;
		try {
			while (reader.ready() && (br = reader.readLine()) != null) {
				sb.append(br);
				sb.append(newLine);
				System.out.println(br);
				if (br.contains("Server startup in ")) {
					break;
				}
				if (br.contains("Address already in use (Bind failed) <null>")) {
					System.out.println("端口被占用");
					break;
				}
				if (index++ > 1000) {
					break;
				}
			}
			return sb.toString();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static String read4(InputStream inputStream) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String br = null;
		String newLine = "\n";
		int index = 0;
		try {
			reader.reset();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			while ((br = reader.readLine()) != null) {
				sb.append(br);
				sb.append(newLine);
				System.out.println(br);
				if (br.contains("Server startup in ")) {
					break;
				}
				if (br.contains("Address already in use (Bind failed) <null>")) {
					System.out.println("端口被占用");
					break;
				}
				if (index++ > 1000) {
					break;
				}
			}
			return sb.toString();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static List<String> readLines(InputStream inputStream) {
		StringBuilder sb = new StringBuilder();
		InputStreamReader isr = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(isr);
		String br = null;
		List<String> lines = new ArrayList<String>();
		int count = 0;
		System.out.println("开始分析文件");
		try {
			while ((br = reader.readLine()) != null) {
				sb.append(br);
				lines.add(br);
				if (count++ > 1000) {
					break;
				}
			}
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
					reader = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (isr != null) {
				try {
					isr.close();
					isr = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static List<String> cmd(Session session, String command) {
		ChannelExec cmdChannel = null;
		try {
			cmdChannel = (ChannelExec) session.openChannel("exec");
		} catch (JSchException e) {
			e.printStackTrace();
		}
		cmdChannel.setCommand(command);
		System.out.println("执行命令:" + command);
		try {
			cmdChannel.connect(5000);
		} catch (JSchException e) {
			e.printStackTrace();
		}
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
		List<String> read = DeployUtil.readLines(inputStream);
		if (inputStream != null) {
			try {
				inputStream.close();
				inputStream = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (errorStream != null) {
			try {
				errorStream.close();
				errorStream = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (cmdChannel != null && cmdChannel.isConnected()) {
			cmdChannel.disconnect();
			cmdChannel = null;
		}
		return read;
	}

	public static String javaHome(Session session, String command) {
		List<String> cmd = cmd(session, command);
		String javaHomePath = extract(cmd);
		return javaHomePath;
	}

	public static String javaHome(Session session) {
		String command = "cat ~/.bashrc";
		String javaHomePath = javaHome(session, command);
		if (StrUtil.isBlank(javaHomePath)) {
			command = "cat /etc/profile";
			javaHomePath = javaHome(session, command);
		}
		return javaHomePath;
	}

	private static String extract(List<String> list) {
		if (CollUtil.isEmpty(list)) {
			return null;
		}
		for (int i = list.size() - 1; i >= 0; i--) {
			String line = list.get(i);
			if (StrUtil.isNotBlank(line)) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				if (line.contains("JAVA_HOME")) {
					String lineLeft = line;
					if (line.contains("#")) {
						lineLeft = StrUtil.subBefore(line, "#", false);
					}
					lineLeft = StrUtil.removePrefix(lineLeft, "export");
					lineLeft = lineLeft.trim();
					String left = StrUtil.subBefore(lineLeft, "=", false);
					left = left.trim();
					if ("JAVA_HOME".equals(left)) {
						String right = StrUtil.subAfter(lineLeft, "=", false);
						right = right.trim();
						return right;
					}
				}

			}
		}
		return null;
	}
}
