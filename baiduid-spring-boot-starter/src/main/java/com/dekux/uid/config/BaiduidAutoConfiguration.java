package com.dekux.uid.config;

import com.dekux.uid.UidGenerator;
import com.dekux.uid.buffer.RejectedPutBufferHandler;
import com.dekux.uid.buffer.RejectedTakeBufferHandler;
import com.dekux.uid.impl.CachedUidGenerator;
import com.dekux.uid.worker.WorkerIdAssigner;
import com.dekux.uid.worker.impl.jpa.JpaDisposableWorkerIdAssigner;
import com.dekux.uid.worker.impl.mybatis.MybatisDisposableWorkerIdAssigner;
import org.apache.ibatis.session.SqlSessionFactory;
import org.hibernate.engine.spi.SessionImplementor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManager;

/**
 * 自动配置类
 *
 * @author yuan
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties(BaiduidProperties.class)
public class BaiduidAutoConfiguration {

    private final BaiduidProperties baiduidProperties;
    private final WorkerIdAssigner workerIdAssigner;
    /**
     * 拒绝策略: 当环已满, 无法继续填充时
     * 默认无需指定, 将丢弃Put操作, 仅日志记录. 如有特殊需求,
     * 请实现RejectedPutBufferHandler接口(支持Lambda表达式)
     */
    private final RejectedPutBufferHandler rejectedPutBufferHandler;

    /**
     * 拒绝策略: 当环已空, 无法继续获取时
     * 默认无需指定, 将记录日志, 并抛出UidGenerateException异常.
     * 如有特殊需求, 请实现RejectedTakeBufferHandler接口(支持Lambda表达式)
     */
    private final RejectedTakeBufferHandler rejectedTakeBufferHandler;

    public BaiduidAutoConfiguration(BaiduidProperties baiduidProperties,
                                    ObjectProvider<RejectedPutBufferHandler> rejectedPutBufferHandlerProvider,
                                    ObjectProvider<RejectedTakeBufferHandler> rejectedTakeBufferHandlerProvider,
                                    WorkerIdAssigner workerIdAssigner) {
        this.baiduidProperties = baiduidProperties;
        this.workerIdAssigner = workerIdAssigner;
        this.rejectedPutBufferHandler = rejectedPutBufferHandlerProvider.getIfAvailable();
        this.rejectedTakeBufferHandler = rejectedTakeBufferHandlerProvider.getIfAvailable();
    }

    @Bean
    @ConditionalOnMissingBean
    public UidGenerator cachedUidGenerator() {
        CachedUidGenerator cachedUidGenerator = new CachedUidGenerator();
        cachedUidGenerator.setTimeBits(baiduidProperties.getTimeBits());
        cachedUidGenerator.setWorkerBits(baiduidProperties.getWorkerBits());
        cachedUidGenerator.setSeqBits(baiduidProperties.getSeqBits());
        cachedUidGenerator.setEpochStr(baiduidProperties.getEpochStr());
        cachedUidGenerator.setBoostPower(baiduidProperties.getBoostPower());
        Long scheduleInterval = baiduidProperties.getScheduleInterval();
        if (scheduleInterval != null && scheduleInterval > 0) {
            cachedUidGenerator.setScheduleInterval(scheduleInterval);
        }
        cachedUidGenerator.setPaddingFactor(baiduidProperties.getPaddingFactor());
        cachedUidGenerator.setWorkerIdAssigner(workerIdAssigner);
        cachedUidGenerator.setRejectedPutBufferHandler(rejectedPutBufferHandler);
        cachedUidGenerator.setRejectedTakeBufferHandler(rejectedTakeBufferHandler);
        return cachedUidGenerator;
    }

    @Bean
    public ReusableWorkerIdAop reusableWorkerIdAop() {
        return new ReusableWorkerIdAop(baiduidProperties.isReusable());
    }

    @ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
    static class MybatisDisposableWorkerIdAssignerAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public WorkerIdAssigner mybatisWorkerIdAssigner() {
            return new MybatisDisposableWorkerIdAssigner();
        }
    }

    @ConditionalOnClass({LocalContainerEntityManagerFactoryBean.class,
            EntityManager.class, SessionImplementor.class})
    static class JpaDisposableWorkerIdAssignerAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public WorkerIdAssigner mybatisWorkerIdAssigner() {
            return new JpaDisposableWorkerIdAssigner();
        }
    }
}
