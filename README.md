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

### 注入 UidGenerator

当你完成上面两步后，那么在 Spring 容器内会有一个 `UidGenerator` 类型 Bean 对象，然后在你需要生成 ID 的地方里面注入这个 Bean 对象即可：

```java
// 使用示例
@Service
public class UserService {
    // 注入 UidGenerator Bean 对象
    private final UidGenerator uidGenerator;

    public UserService(UidGenerator uidGenerator) {
        this.uidGenerator = uidGenerator;
    }
    
    public void saveUser(User user){
        // 生成ID
        long uid = uidGenerator.getUID();
        user.setId(uid);
        // ...
    }
}
```

默认注册的是一个带缓存的 `CachedUidGenerator`  实现，在内部总共有两种 `UidGenerator` 的实现，另一种是 `DefaultUidGenerator` 不带缓存的实现，如果你想使用 `DefaultUidGenerator` 的实现，那么只需要将这个对象注册成为 Bean 即可：

```java
@Bean
public UidGenerator defaultUidGenerator(BaiduidProperties baiduidProperties) {
    DefaultUidGenerator defaultUidGenerator = new DefaultUidGenerator();
    defaultUidGenerator.setTimeBits(baiduidProperties.getTimeBits());
    defaultUidGenerator.setWorkerBits(baiduidProperties.getWorkerBits());
    defaultUidGenerator.setSeqBits(baiduidProperties.getSeqBits());
    defaultUidGenerator.setEpochStr(baiduidProperties.getEpochStr());
    return defaultUidGenerator;
}
```

关于这两种实现的区别可见[百度 uid 的文档](https://github.com/baidu/uid-generator/blob/master/README.zh_cn.md#cacheduidgenerator)。

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
  # 以下三个参数配置仅在使用 CachedUidGenerator 实现时才需要配置
  boost-power: 3 #RingBuffer size扩容参数
  schedule-interval: 10 #填充RingBuffer的Schedule线程时间间隔, 单位:秒
  padding-factor: 50 #指定RingBuffer小于多少百分比数量时进行填充
```

以上是一些你可以自定义修改的配置，对这些配置更详细的定义解析，可以参见 `BaiduidProperties` 类，或者去看 `https://github.com/baidu/uid-generator` 原项目的文档说明。

如果你使用的是 `CachedUidGenerator` 实现类，那么你还可以往 Spring 容器注册：

- `RejectedPutBufferHandler`：（当环已满, 无法继续填充时，默认实现为将丢弃Put操作, 仅日志记录）
- `RejectedTakeBufferHandler`：（当环已空, 无法继续获取时， 默认实现记录日志, 并抛出 UidGenerateException 异常）

来自定义你的拒绝策略。

