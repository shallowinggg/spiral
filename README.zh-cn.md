# spiral

[![Build Status](https://travis-ci.com/shallowinggg/spiral.svg?branch=master)](https://travis-ci.com/shallowinggg/spiral)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/io.github.shallowinggg/spiral/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.shallowinggg/spiral)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/io.github.shallowinggg/spiral.svg)](https://oss.sonatype.org/content/repositories/snapshots/io/github/shallowinggg/spiral/)


`spiral`项目用于简化测试时`dubbo`服务调用的复杂配置。当开发跨越多个`dubbo`服务时，开发者可以借用`spiral`提供的特性调用指定的服务提供者
（当测试环境被占用时可以直接在本地进行测试），无需进行较为麻烦的`dubbo`框架内置的点对点调用配置。

本项目主要适用于`alibaba dubbo 2.2 - 2.6`。如果你使用`apache dubbo 2.7+`，那么你可以直接使用其提供的`tag route`特性。

## 快速开始

`spiral`基于`Dubbo Spring`配置，对应用无任何API侵入。

注意：本项目只支持使用`Dubbo Spring`配置工作。

### 依赖引入

在`maven pom`中引入`spiral`依赖，如下所示：

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

### 配置

在任意一个`Configuration`配置类上加上`@EnableSpiral`注解，如下所示：

```java
import io.github.shallowinggg.spiral.spring.EnableSpiral;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableSpiral
public class XXXConfig {
    
    // other configs
}
```

需要进行测试的`dubbo`服务提供者以及消费者都需要增加此配置。

### 启动

服务启动时需加入如下系统配置：

`-Dspiral.enable=on -Dspiral.tag=red`

其中`spiral.enable`表示是否启用`spiral`，可以使用`true`, `yes`, `on`表示启用。`spiral.tag`表示路由标志，服务启动后将会被打上给定
的标志，当服务消费者进行调用时将会在服务提供者列表中查询是否存在此标志，以进行合适的路由。在一次独立的测试中，服务消费者以及提供者必须拥有
相同的标志才能路由成功。如果多人同时测试，那么需要各自指定不同的标志即可避免冲突。