package io.snowdrop.narayana;

import java.util.Collections;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;

import org.apache.camel.CamelContext;
import org.apache.camel.component.kubernetes.ha.KubernetesClusterService;
import org.apache.camel.ha.CamelClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jta.narayana.NarayanaRecoveryManagerBean;
import org.springframework.context.annotation.Bean;

/**
 * Main Spring Boot application class.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@SpringBootApplication
public class ExampleApplication {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleApplication.class);

    public static void main(String... args) {
        String nodeIdentifier = System.getenv("NARAYANA_NODE_IDENTIFIER");
        if (nodeIdentifier != null) {
            LOG.info("Narayana application using CoreEnvironmentBean.nodeIdentifier={}", nodeIdentifier);
            System.setProperty("CoreEnvironmentBean.nodeIdentifier", nodeIdentifier);
        }
        SpringApplication.run(ExampleApplication.class, args);
    }

    /**
     * Override NarayanaRecoveryManagerBean. Currently NarayanaRecoveryManagerBean provided by Spring Boot cannot be
     * replaces, so a change in Spring Boot is needed.
     *
     * @param recoveryManagerService Recovery manager service which should be started.
     * @return
     */
    @Bean
    public NarayanaRecoveryManagerBean narayanaRecoveryManager(RecoveryManagerService recoveryManagerService, CamelClusterService camelClusterService) throws Exception {
        RecoveryManager.delayRecoveryManagerThread();
        CustomNarayanaRecoveryManagerBean narayanaBean = new CustomNarayanaRecoveryManagerBean(recoveryManagerService);
        camelClusterService.getView("narayana").addEventListener(narayanaBean);
        return narayanaBean;
    }

    @Bean
    public CamelClusterService clusterService(CamelContext context) throws Exception {
        KubernetesClusterService kubernetes = new KubernetesClusterService();
        kubernetes.setClusterLabels(Collections.singletonMap("deploymentconfig", "spring-boot-narayana-leader-example"));
        context.addService(kubernetes);

        return kubernetes;
    }

}
