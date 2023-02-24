package com.dekux.uid.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.io.*;

/**
 * 对 WorkerIdAssigner 进行 Aop 通过 reusable 属性来决定是否复用 workerId
 *
 * @author yuan
 * @since 1.0
 */
@Aspect
public class ReusableWorkerIdAop implements EnvironmentAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReusableWorkerIdAop.class);
    private final boolean reusable;
    private final String ROOT_PATH;
    private int port = -1;

    {
        ROOT_PATH = System.getProperty("user.dir") + File.separator + "WORKER_ID" + File.separator;
    }

    public ReusableWorkerIdAop(boolean reusable) {
        this.reusable = reusable;
    }

    @Around("execution(public long com.dekux.uid.worker.WorkerIdAssigner.assignWorkerId())")
    public Object reusable(ProceedingJoinPoint pdj) throws Throwable {
        if (reusable) {
            Long workerId = read();
            if (workerId != null && workerId > 0L) {
                LOGGER.info("Reuse workerId:{} success, FileName:{}", workerId, getFileName());
                return workerId;
            }
        }
        Object result = pdj.proceed();
        if (reusable) {
            save((Long) result);
            LOGGER.info("Save workerId:{} success, FileName:{}", result, getFileName());
        }
        return result;
    }

    private void save(Long workerId) {
        if (port <= 0) {
            LOGGER.error("port:{} error, save workerId fail", port);
            return;
        }
        BufferedWriter write = null;
        try {
            File file = new File(getFileName());
            if (!file.exists()) {
                FileUtils.createParentDirectories(file);
            }
            write = new BufferedWriter(new FileWriter(file));
            write.write(String.valueOf(workerId));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (write != null) {
                try {
                    write.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Long read() {
        if (port <= 0) {
            LOGGER.error("port:{} error, read workerId fail", port);
            return null;
        }
        BufferedReader read = null;
        try {
            File file = new File(getFileName());
            if (file.exists()) {
                read = new BufferedReader(new FileReader(file));
                return Long.valueOf(read.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (read != null) {
                try {
                    read.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private String getFileName() {
        return ROOT_PATH + port + ".txt";
    }

    @Override
    public void setEnvironment(Environment environment) {
        String serverPort = environment.getProperty("server.port");
        if (StringUtils.isNotBlank(serverPort)) {
            this.port = Integer.parseInt(serverPort);
        } else {
            // 如果无法从环境变量获取 server.port, 说明未主动设置则采取默认端口号设置
            this.port = 8080;
        }
    }
}
