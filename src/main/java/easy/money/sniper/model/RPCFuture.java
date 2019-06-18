package easy.money.sniper.model;

import easy.money.sniper.client.AsyncCallback;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/17 15:08
 */
public class RPCFuture implements Future<RPCResponse> {
    private RPCRequest request;
    private RPCResponse response;
    private Sync sync;
    private List<AsyncCallback> pendingCallbacks;

    public RPCFuture(RPCRequest request) {
        this.request = request;
        this.sync = new Sync();
        this.pendingCallbacks = new ArrayList<>();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public RPCResponse get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);
        return response;
    }

    @Override
    public RPCResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (!success) {
            throw new TimeoutException("获取结果超时：" + request);
        }
        return response;
    }

    /**
     * 执行所有回调
     */
    public void invokeAllCallbacks() {
        for (AsyncCallback callback : pendingCallbacks) {
            runCallback(callback);
        }
    }

    /**
     * 添加回调接口
     *
     * @param callback 回调接口
     */
    public void addCallback(AsyncCallback callback) {
        if (!isDone()) {
            pendingCallbacks.add(callback);
            return;
        }
        runCallback(callback);
    }

    /**
     * 执行回调接口
     *
     * @param callback 回调接口
     */
    public void runCallback(AsyncCallback callback) {
        // TODO: 2019/6/18 异步处理
        if (response.getError() != null) {
            callback.fail(response.getError());
            return;
        }
        callback.success(response.getResult());
    }

    /**
     * 服务器处理完成
     *
     * @param response 服务器响应
     */
    public void done(RPCResponse response) {
        response.setClientEndTime(Instant.now().toEpochMilli());
        this.response = response;
        sync.release(-1);
    }

    static class Sync extends AbstractQueuedSynchronizer {

        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return isDone();
        }

        private boolean isDone() {
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int arg) {
            return getState() == done || compareAndSetState(pending, done);
        }
    }
}
