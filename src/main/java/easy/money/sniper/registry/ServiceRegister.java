package easy.money.sniper.registry;

import easy.money.sniper.common.Const;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/12 15:51
 * 服务注册
 */
public class ServiceRegister {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegister.class);

    // zookeeper地址
    private final String zkAddress;
    private final String serverAddress;
    private final CountDownLatch latch;
    private ZooKeeper zk;

    public ServiceRegister(String serverAddress) {
        this(serverAddress, Const.DEFAULT_ZK_ADDRESS);
    }

    public ServiceRegister(String serverAddress, String zkAddress) {
        this.serverAddress = serverAddress;
        this.zkAddress = zkAddress;
        this.latch = new CountDownLatch(1);
    }

    public void start() {
        connectZooKeeper();
    }

    /**
     * 注册服务
     *
     * @return true | false
     */
    public boolean registerService() {

        // TODO: 2019/6/12 校验格式

        boolean flag = createRoot() && createServerNode(serverAddress);

        // TODO: 2019/6/17 关闭后需要验证路径是否存在
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        return flag;
    }

    /**
     * 创建永久根节点
     *
     * @return true | false
     */
    private boolean createRoot() {
        try {
            Stat stat = zk.exists(Const.DEFAULT_REGISTRY_PATH, false);

            if (stat != null) {
                return true;
            }

            String rootPath = zk.create(Const.DEFAULT_REGISTRY_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            return !StringUtils.isEmpty(rootPath);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("创建根节点失败", e);
            return false;
        }
    }

    /**
     * 创建瞬时服务节点
     *
     * @param serverAddress 服务地址，格式：ip:port
     * @return true | false
     */
    private boolean createServerNode(String serverAddress) {
        String path = Const.DEFAULT_REGISTRY_PATH + "/" + serverAddress;

        try {
            Stat stat = zk.exists(Const.DEFAULT_REGISTRY_PATH + "/" + serverAddress, false);

            if (stat != null) {
                return true;
            }

            String nodePath = zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            return !StringUtils.isEmpty(nodePath);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("注册服务节点失败", e);
            return false;
        }
    }

    /**
     * 连接ZK
     */
    private void connectZooKeeper() {
        try {
            zk = new ZooKeeper(zkAddress, Const.DEFAULT_ZK_SESSION_TIMEOUT, watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown();
                    LOGGER.info("ZK连接成功，{}", zkAddress);
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("尝试连接ZooKeeper失败：{}", zkAddress, e);
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        if (null == zk) {
            return;
        }

        String ephemeralPath = Const.DEFAULT_REGISTRY_PATH + "/" + serverAddress;
        try {
            Stat stat = zk.exists(ephemeralPath, false);

            if (stat != null) {
                zk.delete(ephemeralPath, -1);
            }

            zk.close();
        } catch (InterruptedException | KeeperException e) {
            LOGGER.error("关闭ZK连接异常", e);
        }
        LOGGER.info("关闭注册中心成功，{}", zkAddress);
    }

    public String getServerAddress() {
        return serverAddress;
    }
}
