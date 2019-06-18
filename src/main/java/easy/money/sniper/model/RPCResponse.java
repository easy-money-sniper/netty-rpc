package easy.money.sniper.model;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/12 16:41
 */
public class RPCResponse {
    private String requestId;
    private Object result;
    private Throwable error;

    // metrics
    private long serverStartTime;
    private long serverEndTime;
    private long clientStartTime;
    private long clientEndTime;

    public long getServerStartTime() {
        return serverStartTime;
    }

    public void setServerStartTime(long serverStartTime) {
        this.serverStartTime = serverStartTime;
    }

    public long getServerEndTime() {
        return serverEndTime;
    }

    public void setServerEndTime(long serverEndTime) {
        this.serverEndTime = serverEndTime;
    }

    public long getClientStartTime() {
        return clientStartTime;
    }

    public void setClientStartTime(long clientStartTime) {
        this.clientStartTime = clientStartTime;
    }

    public long getClientEndTime() {
        return clientEndTime;
    }

    public void setClientEndTime(long clientEndTime) {
        this.clientEndTime = clientEndTime;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "RPCResponse{" +
                "requestId='" + requestId + '\'' +
                ", result=" + result +
                ", error=" + error +
                ", serverStartTime=" + serverStartTime +
                ", serverEndTime=" + serverEndTime +
                ", clientStartTime=" + clientStartTime +
                ", clientEndTime=" + clientEndTime +
                '}';
    }
}
