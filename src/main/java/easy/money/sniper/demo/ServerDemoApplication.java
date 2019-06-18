package easy.money.sniper.demo;

import easy.money.sniper.server.RPCServer;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/17 15:14
 */
@Configurable
@ComponentScan(basePackages = "easy.money.sniper.*")
public class ServerDemoApplication {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context
                = new AnnotationConfigApplicationContext(ServerDemoApplication.class);

        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
    }

    @Bean
    public RPCServer rpcServer() {
        return new RPCServer("127.0.0.1:9576");
    }
}
