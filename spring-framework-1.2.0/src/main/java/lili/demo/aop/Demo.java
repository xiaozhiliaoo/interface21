package lili.demo.aop;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.DebugInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.RegexpMethodPointcut;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.lang.reflect.Method;

/**
 * @author lili
 * @date 2022/3/20 18:59
 */
public class Demo {
    public static void main(String[] args) {
        BussinessObject bo = new MyBussinessObject();
        TransactionInterceptor ti = new TransactionInterceptor();
        DebugInterceptor di = new DebugInterceptor();
        Pointcut pointcut = new RegexpMethodPointcut();
        Advice advice = new MethodBeforeAdvice() {
            @Override
            public void before(Method m, Object[] args, Object target) throws Throwable {

            }
        };
        Advisor advisor = new DefaultPointcutAdvisor(pointcut, advice);

        ProxyFactory factory = new ProxyFactory(bo);
        factory.addInterceptor(ti);
        factory.addInterceptor(di);
        factory.addAdvisor(advisor);
        BussinessObject proxy = (BussinessObject) factory.getProxy();
        proxy.sayHello();
    }
}
