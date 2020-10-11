# spiral

[![Build Status](https://travis-ci.com/shallowinggg/spiral.svg?branch=master)](https://travis-ci.com/shallowinggg/spiral)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/io.github.shallowinggg/spiral/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.shallowinggg/spiral)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/io.github.shallowinggg/spiral.svg)](https://oss.sonatype.org/content/repositories/snapshots/io/github/shallowinggg/spiral/)

The `Spiral` project make it easier to test `dubbo` service. When multiple `dubbo` services are modified, developer can use feature that `Sprial` provides to invoke specified `service providers` instead of [Explicit target](http://dubbo.apache.org/en-us/docs/user/demos/explicit-target.html) which is inconvenient.

This project should only be used in `alibaba dubbo 2.2 - 2.6`. If you use `apache dubbo 2.7+`, you can try [Tag routing rules](http://dubbo.apache.org/en-us/docs/user/demos/routing-rule.html).

## Quick start

The most common way to use `Dubbo` is to run it in `Spring Framework`, `Spiral` only supports `Spring Framework`.

### dependency

Add the following in your `maven pom` file:

```xml
    <dependencies>
        <!-- other dependencies -->

        <dependency>
            <groupId>io.github.shallowinggg</groupId>
            <artifactId>spiral</artifactId>
            <version>0.1.0</version>
        </dependency>
    </dependencies>
```

### config

Add `@EnableSpiral` annotation on any `Configuration` annotated class:

```java
import io.github.shallowinggg.spiral.spring.EnableSpiral;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableSpiral
public class XXXConfig {
    
    // other configs
}
```

The `dubbo` service providers and consumers which need to test are all should add this annotation.

### 启动

Add following system property when started:

`-Dspiral.enable=on -Dspiral.tag=red`

- Property `spiral.enable` represents whether `spiral` should be enabled or not, value `true`, `yes` are all acceptable.
- Property `spiral.tag` represents the value of route tag. When service started successfully, it will be marked with this tag. If the service consumer makes an invocation, it will check service provider list and find which has this tag and route to it. 

If multiple people are testing at the same time, you should set different tags.
