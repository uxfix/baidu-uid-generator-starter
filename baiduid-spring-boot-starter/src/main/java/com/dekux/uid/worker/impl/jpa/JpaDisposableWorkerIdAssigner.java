/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dekux.uid.worker.impl.jpa;

import com.dekux.uid.worker.WorkerIdAssigner;
import com.dekux.uid.worker.entity.WorkerNodeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Represents an implementation of {@link WorkerIdAssigner},
 * the worker id will be discarded after assigned to the UidGenerator
 *
 * @author yutianbao
 */
public class JpaDisposableWorkerIdAssigner implements WorkerIdAssigner {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaDisposableWorkerIdAssigner.class);

    @Resource
    private WorkerNodeRepository workerNodeRepository;

    /**
     * Assign worker id base on database.<p>
     * If there is host name & port in the environment, we considered that the node runs in Docker container<br>
     * Otherwise, the node runs on an actual machine.
     *
     * @return assigned worker id
     */
    @Transactional
    public long assignWorkerId() {
        // build worker node entity
        WorkerNodeEntity workerNodeEntity = WorkerNodeEntity.buildWorkerNode();

        // add worker node for new (ignore the same IP + PORT)
        workerNodeRepository.save(workerNodeEntity);
        LOGGER.info("Add worker node:" + workerNodeEntity);
        return workerNodeEntity.getId();
    }

}
