# Getting Started

### Useful command
```shell
lsof -i :8080
COMMAND   PID      USER   FD   TYPE            DEVICE SIZE/OFF NODE NAME
java    26112 jerryshao   77u  IPv6 0x2cc65a4a2bc4128      0t0  TCP *:http-alt (LISTEN)
sudo kill -9 26112
```

### Create a new repository on the command line
```shell
echo "# fraud_detection_demo_java" >> README.md
git init
git add .
git commit -m "Add fraud detection demo"
git branch -M main
git remote add origin https://github.com/jerryshao2012/fraud_detection_demo_java.git
git push -u origin main
```

### Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.0-SNAPSHOT/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.0-SNAPSHOT/gradle-plugin/packaging-oci-image.html)

### Additional Links

These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

