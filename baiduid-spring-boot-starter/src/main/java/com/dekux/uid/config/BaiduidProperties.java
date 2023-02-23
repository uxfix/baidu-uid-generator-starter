package com.dekux.uid.config;

import com.dekux.uid.buffer.RingBuffer;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 对于并发数要求不高、期望长期使用的应用, 可增加timeBits位数, 减少seqBits位数.
 * 例如节点采取用完即弃的WorkerIdAssigner策略, 重启频率为12次/天,
 * 那么配置成{"workerBits":23,"timeBits":31,"seqBits":9}时, 可支持28个节点以整体并发量14400 UID/s的速度持续运行68年.
 * 对于节点重启频率频繁、期望长期使用的应用, 可增加workerBits和timeBits位数, 减少seqBits位数.
 * 例如节点采取用完即弃的WorkerIdAssigner策略, 重启频率为24*12次/天, 那么配置成{"workerBits":27,"timeBits":30,"seqBits":6}时,
 * 可支持37个节点以整体并发量2400 UID/s的速度持续运行34年.
 *
 * @author yuan
 * @since 1.0
 **/
@ConfigurationProperties(prefix = "baiduid")
public class BaiduidProperties {

    /**
     * 是否可复用 workerId
     */
    private boolean reusable = true;

    /**
     * 时间比特位,单位：秒，最多可支持约8.7年
     */
    private int timeBits = 28;

    /**
     * 机器id比特位数，最多可支持约420w次机器启动。
     * 内置实现为在启动时由数据库分配，默认分配策略为用后即弃，后续可提供复用策略
     */
    private int workerBits = 22;

    /**
     * 每秒下的并发序列比特位，13 bits可支持每秒8192个并发
     */
    private int seqBits = 13;

    /**
     * 时间基点
     */
    private String epochStr = "2023-02-23";

    /**
     * RingBuffer size扩容参数, 可提高UID生成的吞吐量.
     * 默认:3， 原bufferSize=8192, 扩容后bufferSize= 8192 << 3 = 65536
     */
    private int boostPower = 3;

    /**
     * 指定何时向RingBuffer中填充UID, 取值为百分比(0, 100), 默认为50
     * 举例: bufferSize=1024, paddingFactor=50 -> threshold=1024 * 50 / 100 = 512.
     * 当环上可用UID数量 < 512时, 将自动对RingBuffer进行填充补全
     */
    private int paddingFactor = RingBuffer.DEFAULT_PADDING_PERCENT;

    public int getTimeBits() {
        return timeBits;
    }

    public void setTimeBits(int timeBits) {
        this.timeBits = timeBits;
    }

    public int getWorkerBits() {
        return workerBits;
    }

    public void setWorkerBits(int workerBits) {
        this.workerBits = workerBits;
    }

    public int getSeqBits() {
        return seqBits;
    }

    public void setSeqBits(int seqBits) {
        this.seqBits = seqBits;
    }

    public String getEpochStr() {
        return epochStr;
    }

    public void setEpochStr(String epochStr) {
        this.epochStr = epochStr;
    }

    public int getBoostPower() {
        return boostPower;
    }

    public void setBoostPower(int boostPower) {
        this.boostPower = boostPower;
    }

    public int getPaddingFactor() {
        return paddingFactor;
    }

    public void setPaddingFactor(int paddingFactor) {
        this.paddingFactor = paddingFactor;
    }

    public boolean isReusable() {
        return reusable;
    }

    public void setReusable(boolean reusable) {
        this.reusable = reusable;
    }
}
