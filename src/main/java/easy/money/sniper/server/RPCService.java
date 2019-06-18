package easy.money.sniper.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/12 17:00
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface RPCService {
    Class<?> value();
}
