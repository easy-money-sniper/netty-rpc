package easy.money.sniper.demo.service.impl;

import easy.money.sniper.demo.service.HelloService;
import easy.money.sniper.server.RPCService;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/12 17:24
 */
@RPCService(HelloService.class)
public class HelloServieImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name + ", I am Kevin Durant.";
    }

    public String knock() {
        return "pong";
    }
}
