package io.snowdrop.narayana;

import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main Spring Boot application class.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@SpringBootApplication
public class ExampleApplication {

    public static void main(String... args) {
        ConfigurableApplicationContext context = SpringApplication.run(ExampleApplication.class, args);
        RecoveryManagerService recoveryManagerService = context.getBean(RecoveryManagerService.class);
        recoveryManagerService.addXAResourceRecovery(new DummyXAResourceRecovery());
    }

}
