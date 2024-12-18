# AI-Code-Review

## 获取通义千问 API-KEY

本地代码配置，使用的是通义千问系列的大预言模型。Qwen 大模型可以在[阿里云百炼](https://www.aliyun.com/product/bailian)上进行配置，并获取 Key。

## 设置通知邮箱

可以使用 QQ 邮箱进行短信通知，在 QQ 邮箱中进入"账号与安全"，在安全设置中开启**POP3/IMAP/SMTP/Exchange/CardDAV 服务**。注意记录**授权码**，发送邮件功能需要用到。修改邮件发送人和接收人，以及授权码。

## 设置 Git 密钥地址

如果是传入一个仓库地址，对指定地址的仓库进行代码评审，那么需要配置该 Git 仓库的密钥。将密钥文件 id_rsa 的地址配置到 yaml 文件的 config.ssh.key.url 变量上

## 其他操作

将根目录下 pre-push 文件，添加到 .git/hooks 目录下，提交代码后会调用通义千问大模型进行代码评审。
