/**
 * @author XiongJie, Date: 13-11-1
 */
package net.happyonroad.test.provider;

import net.happyonroad.test.DemoRemoteService;
import org.springframework.stereotype.Service;

/** 演示的远程服务实现 */
@Service
public class DemoRemoteServiceProvider implements DemoRemoteService {
    @Override
    public String sayHello(String name) {
        return "Hello {" + name + "}";
    }
}
