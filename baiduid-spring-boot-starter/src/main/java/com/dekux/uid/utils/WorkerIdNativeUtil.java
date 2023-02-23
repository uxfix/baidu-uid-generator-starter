package com.dekux.uid.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.*;

/**
 * 保持 WorkerId 到本地
 *
 * @author yuan
 * @since 1.0
 */
public abstract class WorkerIdNativeUtil {

    private final static String CLASS_PATH = "id/worker_id.txt";

    public static void save(Long workerId) {
        BufferedWriter write = null;
        try {
            ClassPathResource resource = new ClassPathResource(CLASS_PATH);
            File file = resource.getFile();
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

    public static Long read() {
        BufferedReader read = null;
        try {
            ClassPathResource resource = new ClassPathResource(CLASS_PATH);
            File file = resource.getFile();
            read = new BufferedReader(new FileReader(file));
            return Long.valueOf(read.readLine());
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
}
