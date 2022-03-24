package lili.demo.aop;

/**
 * @author lili
 * @date 2022/3/20 18:59
 */
public class MyBussinessObject implements BussinessObject {
    @Override
    public void sayHello() {
        System.out.println("Say Hello");
    }
}
