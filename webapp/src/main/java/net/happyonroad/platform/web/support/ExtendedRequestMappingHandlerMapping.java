/**
 * Developer: Kadvin Date: 14-7-16 上午9:50
 */
package net.happyonroad.platform.web.support;

import net.happyonroad.component.container.LaunchEnvironment;
import net.happyonroad.component.container.event.ContainerStartedEvent;
import net.happyonroad.component.core.Component;
import net.happyonroad.event.ExtensionLoadedEvent;
import net.happyonroad.platform.web.SpringMvcConfig;
import net.happyonroad.platform.web.controller.ApplicationController;
import net.happyonroad.platform.web.model.RouteItem;
import net.happyonroad.util.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.HandlerMethodSelector;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * <h1>被扩展的Spring Request Mapping</h1>
 *
 * 主要目的是
 * 1. 让在platform之前或者之后加载的controller的request mapping 方法能注册上来
 * 2. 让插入的服务scan出来的controller的request mapping 方法能注册上来
 *
 * <pre>
 * 目的1的实现方法是：
 *   在容器启动完毕之后，检查所有的应用组件
 * 目的2实现方法是：
 *   监听service package加载事件，
 *   收到事件后，从事件找找到扩展包的application context
 *   从相应的application context中找到新建的所有application controller
 *   而后把所有的RequestMapping/Method注册上来
 * 注意：这里并没有按照spring的标准规范，找到所有的 @Controller 标记的bean
 * </pre>
 */
public class ExtendedRequestMappingHandlerMapping extends RequestMappingHandlerMapping
        implements ApplicationListener<ApplicationEvent> {

    private ApplicationContext theApplicationContext;
    private Set<Class> handledTypes = new HashSet<Class>();

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContainerStartedEvent) {
            //解决第一个问题
            LaunchEnvironment environment = (LaunchEnvironment) event.getSource();
            List<ApplicationContext> applications = environment.getApplications();
            for (ApplicationContext application : applications) {
                detectApplicationContext(application);
            }
        } else if (event instanceof ExtensionLoadedEvent) {
            //解决第二个问题
            Component component = (Component) event.getSource();
            ApplicationContext application = component.getApplication();
            detectApplicationContext(application);
        }
    }

    private void detectApplicationContext(ApplicationContext application) {
        theApplicationContext = application;
        if (application == null) return;// it's not a component with application
        // controllers 是注册在parent中的
        // 发现一个bug，当controller实习了额外的接口，由于控制器又会被proxy，所以以下代码的判断将会错误
        String[] controllerNames = application.getBeanNamesForType(ApplicationController.class);
        Set<String> names = new HashSet<String>();
        Collections.addAll(names, controllerNames);
        controllerNames = application.getBeanNamesForAnnotation(Controller.class);
        Collections.addAll(names, controllerNames);
        for (String name : names) {
            detectHandlerMethods(name);
        }
        theApplicationContext = null;
    }

    ApplicationContext theApplicationContext() {
        return theApplicationContext == null ? getApplicationContext() : theApplicationContext;
    }

    // 与下面 detectHandlerMethods ，基于当前实例同步化
    @Override
    protected synchronized HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
        return super.getHandlerInternal(request);
    }

    /**
     * Look for handler methods in a handler.
     * @param handler the bean name of a handler or a handler instance
     */
    protected synchronized void detectHandlerMethods(final Object handler) {
        Object real;
        if(handler instanceof String){
            real = theApplicationContext().getBean((String) handler);
        }else{
            real = handler;
        }
        Class<?> handlerType;
        if(Proxy.isProxyClass(real.getClass())){
            String className = StringUtils.substringBefore(real.toString(), "@");
            try {
                handlerType = ClassUtils.forName(className, theApplicationContext().getClassLoader());
            }catch (Exception ex ){
                logger.warn("Can't reflect " + className + " by " + theApplicationContext().getClassLoader());
                return;
            }
        }else{
            handlerType = real.getClass();
        }
        //在许多情况下，会重复对某个控制器进行方法解析
        if (handled(handlerType)) return;
        else handle(handlerType);
   		// Avoid repeated calls to getMappingForMethod which would rebuild RequestMappingInfo instances
   		final Map<Method, RequestMappingInfo> mappings = new IdentityHashMap<Method, RequestMappingInfo>();
   		final Class<?> userType = ClassUtils.getUserClass(handlerType);

   		Set<Method> methods = HandlerMethodSelector.selectMethods(userType, new ReflectionUtils.MethodFilter() {
               @Override
               public boolean matches(Method method) {
                   RequestMappingInfo mapping = getMappingForMethod(method, userType);
                   if (mapping != null) {
                       mappings.put(method, mapping);
                       return true;
                   } else {
                       return false;
                   }
               }
           });

   		for (Method method : methods) {
            RequestMappingInfo mapping = mappings.get(method);
            registerHandlerMethod(handler, method, mapping);
            HandlerMethod handlerMethod = getHandlerMethods().get(mapping);
            RouteItem routeItem = RouteItem.fromMapping(mapping, handlerMethod);
            //Extended Request Mapping Handler Mapping's logger/info has been disabled
            SpringMvcConfig.logger.info(routeItem);
        }
   	}


    private boolean handled(Class<?> handlerType) {
        return handledTypes.contains(handlerType);
    }

    private void handle(Class<?> handlerType) {
        handledTypes.add(handlerType);
    }

    /**
   	 * Create the HandlerMethod instance.
   	 * @param handler either a bean name or an actual handler instance
   	 * @param method the target method
   	 * @return the created HandlerMethod
   	 */
   	protected HandlerMethod createHandlerMethod(Object handler, Method method) {
   		HandlerMethod handlerMethod;
   		if (handler instanceof String) {
   			String beanName = (String) handler;
   			handlerMethod = new HandlerMethod(beanName, theApplicationContext(), method);
   		}
   		else {
   			handlerMethod = new HandlerMethod(handler, method);
   		}
   		return handlerMethod;
   	}


}
