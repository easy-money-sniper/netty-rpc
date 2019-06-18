package easy.money.sniper.demo;

import easy.money.sniper.client.RPCProxyManager;
import easy.money.sniper.demo.service.HelloService;
import easy.money.sniper.registry.ServiceDiscover;

import java.util.stream.IntStream;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/17 15:19
 */
public class ClientDemoApplication {
    public static void main(String[] args) throws InterruptedException {
        new ServiceDiscover();

        HelloService service = RPCProxyManager.getClient(HelloService.class);

        // 预热
        service.knock();

        IntStream.range(0, 50).forEach(i -> new Thread(
                () -> System.out.println(service.sayHello(i + ""))
        ).start());
    }
}
