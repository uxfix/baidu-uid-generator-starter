package com.dekux.uid.worker.impl.jpa;

import com.dekux.uid.worker.entity.WorkerNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Jpa 实现
 *
 * @author yuan
 * @since 1.0
 */
public interface WorkerNodeRepository extends JpaRepository<WorkerNodeEntity,Long> {
}
