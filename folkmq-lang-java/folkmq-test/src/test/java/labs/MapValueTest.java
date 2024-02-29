package labs;

import org.noear.socketd.utils.RunUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapValueTest {
    public static void main(String[] args) throws Exception{
        Map<String,String> map = new ConcurrentHashMap<>();

        RunUtils.async(()->{
            while (true){
                for (String val : map.values()){
                    //System.out.println(val);
                }
            }
        });

        Thread.sleep(10);

        for (int i=0; i<100000; i++){
            map.put("a"+i, "a" +i );
        }
    }
}
