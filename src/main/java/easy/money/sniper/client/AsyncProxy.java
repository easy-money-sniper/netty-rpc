package easy.money.sniper.client;

import easy.money.sniper.model.RPCFuture;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/18 14:23
 */
public interface AsyncProxy {

    RPCFuture call(String methodName, Object... args);
}
