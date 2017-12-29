# icedeploy
icedeploy
自动部署工具

部署是一个非常频繁的操作.
icedeploy是一个基于无入侵原则设计的Tomcat部署工具.
该工具基于以下设计原则:
1. 服务端不可安装额外的软件
2. 客户端不可安装额外的软件
3. 服务端Tomcat不能为部署做额外的修改
4. 要部署的源代码不能为部署做额外的修改
5. 服务端不可运行额外的程序

程序中用的参数解释:
localFileName -- 本地war文件路径,完整的文件名(war结尾)
remoteTomcat -- 远程Tomcat的位置,路径名
remoteTemp -- 远程临时war文件位置,路径名
remoteBak -- 远程备份文件位置,路径名(可缺省,使用null)
jdkHome -- jdk环境变量JAVA_HOME对应的路径,路径名
url -- 在客户端浏览器中监测部署是否正确的url

icedeploy
Automated deployment tools

Deployment is a very frequent operation.
icedeploy is a Tomcat deployment tool based on the principle of non-intrusive.
The tool is based on the following design principles:
1. The server can not install additional software
2. Client can not install additional software
3. Server Tomcat can not make additional changes to the deployment
4. The source code to be deployed can not make additional modifications to the deployment
5. The server can not run additional programs

Parameters used in the program explained:
localFileName - local war file path, full filename (end of war)
remoteTomcat - the location of the remote Tomcat, the path name
remoteTemp - temporary temporary war file location, path name
remoteBak - remote backup file location, path name (can be used by default, null)
jdkHome - Jdk environment variable JAVA_HOME corresponding path, path name
url - The URL for monitoring the correct deployment in the client browser


