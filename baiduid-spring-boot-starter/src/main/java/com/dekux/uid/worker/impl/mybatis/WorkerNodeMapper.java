package com.dekux.uid.worker.impl.mybatis;

import com.dekux.uid.worker.entity.WorkerNodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Mybatis 实现
 *
 * @author yuan
 * @since 1.0
 */
@Mapper
public interface WorkerNodeMapper {

    /**
     * Get {@link WorkerNodeEntity} by node host
     *
     * @param host
     * @param port
     * @return
     */
    WorkerNodeEntity getWorkerNodeByHostPort(@Param("host") String host, @Param("port") String port);

    /**
     * Add {@link WorkerNodeEntity}
     *
     * @param workerNodeEntity
     */
    void addWorkerNode(WorkerNodeEntity workerNodeEntity);
}
