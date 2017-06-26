package io.snowdrop.narayana;

import java.net.InetAddress;
import java.util.Collections;

import io.atomix.catalyst.transport.Address;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;

import org.apache.camel.CamelContext;
import org.apache.camel.component.atomix.ha.AtomixClusterClientService;
import org.apache.camel.ha.CamelClusterService;
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

    public static void main(String... args) {
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
        AtomixClusterClientService service = new AtomixClusterClientService();
        service.setId(InetAddress.getLocalHost().getHostName());
        service.setNodes(Collections.singletonList(new Address("atomix-boot-node", 8700)));

        context.addService(service);

        return service;
    }

}
