Good question — for your **Spring Boot backend** you’ll need dependencies that let you:

1. **Run normal Spring Boot services (Web, Data, Actuator)**.
2. **Add Post-Quantum Cryptography support** (liboqs-java or Bouncy Castle PQ builds).
3. **Optionally integrate with metrics/observability tools** (Prometheus, Grafana).

Here’s a structured breakdown:

---

# 1. Core Spring Boot Dependencies

Add the usual starters in your `pom.xml`:

```xml
<dependencies>
    <!-- Web layer -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- For database storage -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>

    <!-- For monitoring/metrics -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

---

# 2. PQC Cryptography Dependencies

### Option A — **liboqs-java** (JNI wrapper around Open Quantum Safe liboqs)

* Maven coordinates depend on the build you choose (liboqs-java is not yet on Maven Central in all versions, you may need to build from source and install into your local repo).
* Usage: provides Java bindings for **Kyber (ML-KEM)**, **Dilithium**, etc.

```xml
<dependency>
    <groupId>org.openquantumsafe</groupId>
    <artifactId>liboqs-java</artifactId>
    <version>0.9.0</version> <!-- or latest -->
</dependency>
```

⚠️ You’ll also need the **native liboqs** library installed on your server (JNI links to it).

---

### Option B — **Bouncy Castle with PQC support**

* The Bouncy Castle provider has introduced PQC algorithms (Kyber, Dilithium, Falcon, SPHINCS+).
* Easier to integrate than liboqs-java (pure Java, no JNI).
* Example dependency:

```xml
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.78</version> <!-- or latest -->
</dependency>
```

* Add the provider at runtime:

```java
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

public class PQConfig {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
}
```

Then you can use PQ algorithms through JCA (`KeyPairGenerator`, `Cipher`, `Signature`).

---

# 3. Optional — Hybrid TLS Support

If you want PQ **inside TLS** itself:

* You’ll need **OpenSSL 3 built with oqs-provider**.
* Spring Boot will then use this via Tomcat/Jetty/Undertow with custom SSLContext.
* This is infrastructure-level; not a Maven dependency inside Spring Boot.

---

# 4. Recommended Choice for Prototype

* Start with **Bouncy Castle PQC**: no JNI, simpler integration, good for a student/project environment.
* If you want maximum alignment with NIST PQ standards, add **liboqs-java** later (for Kyber/Dilithium official bindings).

---
