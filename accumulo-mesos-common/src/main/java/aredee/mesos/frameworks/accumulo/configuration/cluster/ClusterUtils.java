package aredee.mesos.frameworks.accumulo.configuration.cluster;

import org.apache.commons.lang3.StringUtils;

public class ClusterUtils {

    /**
     * verify the cluster config has what it needs.
     * 
     * @param cluster
     * @throws RuntimeException if not valid
     */
    public static void verifyCluster(ClusterConfiguration cluster) {
        // Most everything is defaulted except the tarball.
        if (StringUtils.isEmpty(cluster.getTarballUri())) {
            throw new RuntimeException("Failed to set framework tarball parameter");
        } 
    }
}
