# baiduid-spring-boot-starter

## 简介

`baiduid-spring-boot-starter` 是将百度的 **[uid-generator](https://github.com/baidu/uid-generator)** 项目封装成了 Spring-Boot-Starter，方便在 SpringBoot 项目中更加易于使用，遵循最少依赖配置，开箱即用的原则，另外还在原项目的基础之上增加了 `workerId` 复用的机制，以及整合了 Spring-Data-Jpa。

## 如何使用

### 引入依赖

在你的项目中引入以下依赖即可

```xml
<dependency>
    <groupId>com.dekux</groupId>
    <artifactId>baiduid-spring-boot-starter</artifactId>
    <version>1.0</version>
</dependency>
```

由于 `uid-generator`内部分配 `workerId` 需要借助数据库的自增主键 ID 来分配，所以还需要数据库连接的依赖，在 `baiduid-spring-boot-starter` 内部已经整合了 Mybatis 和 Spring-Data-Jpa，会根据你当前项目所使用的的数据库连接依赖来自动配置选择使用其中某种。

### 建数据库表

如果你使用的 Mybatis，那么你需要你自己创建一张 worker_node 表：

```sql
DROP TABLE IF EXISTS worker_node;
CREATE TABLE worker_node
(
id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'auto increment id',
host_name VARCHAR(64) NOT NULL COMMENT 'host name',
port VARCHAR(64) NOT NULL COMMENT 'port',
type INT NOT NULL COMMENT 'node type: ACTUAL or CONTAINER',
launch_date DATE NOT NULL COMMENT 'launch date',
modified TIMESTAMP NOT NULL COMMENT 'modified time',
created TIMESTAMP NOT NULL COMMENT 'created time',
PRIMARY KEY(ID)
)
 COMMENT='DB WorkerID Assigner for UID Generator',ENGINE = INNODB;
```

如果你使用的 Spring-Data-Jpa，那么通常不需要自己创建表。

## 自定义分配 WorkerId 策略

如果你的项目既不使用 Mybatis 也不是使用 Spring-Data-Jpa，也许你可能需要自定义分配 workerId 策略，比方说你可能想要使用 Redis 来进行 workerId 分配，那么你只需要实现 `WorkerIdAssigner` 接口即可：

```java
public class RedisWorkerIdAssigner implements WorkerIdAssigner{
    @Override
    public long assignWorkerId() {
        // 实现自己的 workerId 分配策略
        // 你需要保证每次分配的唯一性
        return 0;
    }
}
```

然后将其注册到 Spring 容器即可

```java
@Bean
public WorkerIdAssigner redisWorkerIdAssigner(){
    return new RedisWorkerIdAssigner();
}
```

这样就完成了自定义分配 WorkerId 策略。

## WorkerId 复用机制

通过配置 `baiduid.reusable` 配置来控制是否开启 WorkerId 复用机制，该参数配置默认是 true。

```yml
baiduid:
  reusable: true
```

workerId 复用机制的实现原理是借助于 AOP，对 `WorkerIdAssigner#assignWorkerId` 进行拦截切面，然后将返回的 workerId 缓存到本地，这样下次服务重启的时候，会先去本地读取缓存的 workerId ，如果读取到了，那么就不会再去调用 `assignWorkerId` 方法去再次分配 workerId ，以此来达到复用的目的，减少服务多次重启造成的浪费。

## 项目配置

```yml
baiduid:
  reusable: true #是否可复用 workerId  
  time-bits: 28 #时间比特位数
  worker-bits: 22 #机器id比特位数
  seq-bits: 13 #每秒下的并发序列比特位
  epoch-str: 2023-02-25 #时间基点
  boost-power: 3 #RingBuffer size扩容参数
  padding-factor: 50 #指定何时向RingBuffer中填充UID
```

以上是一些你可以自定义修改的配置，对这些配置更详细的定义解析，可以参见 `BaiduidProperties` 类，或者去看 `https://github.com/baidu/uid-generator` 原项目的文档说明。