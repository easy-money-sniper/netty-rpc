package easy.money.sniper.registry;

import easy.money.sniper.common.Const;
import easy.money.sniper.connection.ConnectionManager;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/17 11:04
 */
public class ServiceDiscover {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscover.class);
    private final CountDownLatch latch;
    private String zkAddress;
    private ZooKeeper zk;

    public ServiceDiscover() {
        this(Const.DEFAULT_ZK_ADDRESS);
    }

    public ServiceDiscover(String zkAddress) {
        this.zkAddress = zkAddress;
        this.latch = new CountDownLatch(1);
        connectZooKeeper();
        watchNode();
    }

    private void connectZooKeeper() {
        try {
            zk = new ZooKeeper(zkAddress, Const.DEFAULT_ZK_SESSION_TIMEOUT, watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("连接ZK失败，{}", zkAddress, e);
        }
    }

    private void watchNode() {
        if (null == zk) {
            return;
        }

        try {
            List<String> nodes = zk.getChildren(Const.DEFAULT_REGISTRY_PATH, watchedEvent -> {
                if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    watchNode();
                }
            });
            // 连接管理更新
            ConnectionManager.getInstance().updateServerNodes(nodes);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
