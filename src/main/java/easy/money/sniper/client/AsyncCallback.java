package easy.money.sniper.client;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/18 14:33
 */
public interface AsyncCallback {

    void success(Object result);

    void fail(Throwable error);
}
