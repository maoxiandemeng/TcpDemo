# TcpDemo
init
使用MQTT 协议 
下载地址： http://activemq.apache.org/apollo/download.html 
命令行进入安装目录bin目录下
输入apollo create XXX（xxx为创建的服务器实例名称，例：apollo create mybroker），之后会在bin目录下创建名称为XXX的文件夹。XXX文件夹下etc\apollo.xml文件下是配置服务器信息的文件。
etc\users.properties文件包含连接MQTT服务器时用到的用户名和密码，默认为admin=password，即账号为admin，密码为password，可自行更改

进入XXX/bin目录，输入apollo-broker.cmd run开启服务器

