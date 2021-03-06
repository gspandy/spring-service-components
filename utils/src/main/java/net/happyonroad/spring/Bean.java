/**
 * @author XiongJie, Date: 13-10-28
 */
package net.happyonroad.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.PriorityOrdered;

/**
 * <h2>基本的Spring Bean</h2>
 * 支持Lifecycle相关接口
 */
public class Bean implements SmartLifecycle, PriorityOrdered {
    public static final int    DEFAULT_PHASE = 100;
    protected           Logger logger        = LoggerFactory.getLogger(getClass());

    protected int phase = DEFAULT_PHASE;
    protected boolean running;
    protected boolean autoStartup = false;
    private int order;

    @Override
    public void start() {
        starting();
        performStart();
        started();
    }

    /** 子类应该覆盖的启动工作方法 */
    protected void performStart() {
        logger.trace("performing start");
    }

    @Override
    public void stop() {
        stopping();
        performStop();
        stopped();
    }

    /** 子类应该覆盖的停止工作方法 */
    protected void performStop() {
        logger.trace("performing stop");
    }

    @Override
    public boolean isAutoStartup() {
        return autoStartup;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        if (callback != null) {
            try {
                callback.run();
            } catch (Exception ex) {
                logger.error("Failed to run stop callback", ex);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return phase;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPhase(int phase) {
        this.phase = phase;
    }

    /** 默认的 starting 方法，主要就是说明自身准备开始启动 */
    protected void starting() {
        logger.debug("{} Starting", this);
        running = true;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /** 默认的 started 方法，主要就是说明自身已经启动完成 */
    protected void started() {
        logger.debug("{} Started!", this);
    }


    /** 默认的 stopping 方法，主要就是说明自身准备停止 */
    protected void stopping() {
        logger.debug("{} Stopping", this);
        running = false;
    }

    /** 默认的 Stopped 方法，主要就是说明自身已经停止完成 */
    protected void stopped() {
        logger.debug("{} Stopped!", this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    //wait for max ms if nobody notify me
    protected void rest(int ms) {
        synchronized (this) {
            try {
                wait(ms);
            } catch (InterruptedException e) {
                //skip
            }
        }
    }

    protected void breakRest() {
        synchronized (this) {
            notifyAll();
        }
    }
}
