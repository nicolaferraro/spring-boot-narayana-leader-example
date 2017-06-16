package io.snowdrop.narayana;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(BMUnitRunner.class)
public class RecoveryTests {

    @Mock
    private XAResource xaResource;

    @Mock
    private XAResourceRecoveryHelper xaResourceRecoveryHelper;

    private ApplicationContext context;

    private EntriesService entriesService;

    private TransactionManager transactionManager;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        context = SpringApplication.run(ExampleApplication.class);
        entriesService = context.getBean(EntriesService.class);
        transactionManager = context.getBean(TransactionManager.class);
    }

    @After
    public void after() throws IOException {
        ((Closeable) context).close();
    }

    @Test
    @BMRule(name = "Fail before commit",
            targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction",
            targetMethod = "phase2Commit",
            targetLocation = "ENTRY",
            helper = "io.snowdrop.narayana.BytemanHelper",
            action = "incrementCommitsCounter(); failFirstCommit($0.get_uid());")
    public void testCrashBeforeCommit() throws Exception {
        // Setup dummy XAResource and its recovery helper
        setupXaMocks();

        transactionManager.begin();
        transactionManager.getTransaction().enlistResource(xaResource);
        entriesService.create("test");
        try {
            // Byteman rule will cause commit to fail
            transactionManager.commit();
            fail("Exception was expected");
        } catch (Throwable ignored) {
        }

        assertTrue(entriesService.getAll().isEmpty());

        // Wait for the recovery to happen
        Thread.sleep(30000);

        assertEquals(1, entriesService.getAll().size());
    }

    private void setupXaMocks() throws Exception {
        List<Xid> xids = new ArrayList<>();
        // Save Xid provided during prepare
        when(xaResource.prepare(anyObject()))
                .then(i -> {
                    xids.add((Xid) i.getArguments()[0]);
                    return XAResource.XA_OK;
                });
        // Return Xids when recovering
        when(xaResource.recover(anyInt()))
                .then(i -> xids.toArray(new Xid[xids.size()]));
        // Return XAResource when recovering
        when(xaResourceRecoveryHelper.getXAResources())
                .thenReturn(new XAResource[] { xaResource });
    }

}
