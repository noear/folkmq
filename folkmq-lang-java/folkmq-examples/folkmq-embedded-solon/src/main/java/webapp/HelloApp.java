package webapp;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Mapping;

@Controller
public class HelloApp {
    public static void main(String[] args) {
        Solon.start(HelloApp.class, args);
    }

    @Get
    @Mapping("/")
    public String hello(String name){
        return "hello world: " + name;
    }

    @Get
    @Mapping("/hello2")
    public String hello2(String name) throws Exception{
        Thread.sleep(10);
        return "hello world: " + name;
    }
}
