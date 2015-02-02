/**
 * Developer: Kadvin Date: 15/2/2 下午5:17
 */
package net.happyonroad;

import net.happyonroad.component.container.ServiceExporter;
import net.happyonroad.config.ApplyToSystemProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * <h1>Util App Config</h1>
 */
@Configuration
@PropertySource("file://${app.home}/config/*.properties")
public class UtilAppConfig implements InitializingBean{
    @Autowired
    ServiceExporter exporter;

    @Bean
    ApplyToSystemProperties applyToSystemProperties(){
        return new ApplyToSystemProperties();
    }

    @Bean
    TaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadGroupName("SystemScheduler");
        scheduler.setThreadNamePrefix("SystemScheduler-");
        return scheduler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        exporter.exports(TaskScheduler.class, taskScheduler());
    }
}
