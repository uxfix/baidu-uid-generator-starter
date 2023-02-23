package com.dekux.uid.config;

import com.dekux.uid.utils.WorkerIdNativeUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 对 WorkerIdAssigner 进行 Aop 通过 reusable 属性来决定是否复用 workerId
 *
 * @author yuan
 * @since 1.0
 */
@Aspect
public class ReusableWorkerIdAop {

    private final boolean reusable;

    public ReusableWorkerIdAop(boolean reusable) {
        this.reusable = reusable;
    }

    @Around("execution(public long com.dekux.uid.worker.WorkerIdAssigner.assignWorkerId())")
    public Object reusable(ProceedingJoinPoint pdj) throws Throwable {
        if (reusable) {
            Long workerId = WorkerIdNativeUtil.read();
            if (workerId != null && workerId > 0L) {
                return workerId;
            }
        }
        Object result = pdj.proceed();
        if (reusable) {
            WorkerIdNativeUtil.save((Long) result);
        }
        return result;
    }
}
