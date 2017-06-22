package io.snowdrop.narayana;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.transaction.xa.XAResource;
import java.util.List;

/**
 * Service to store entries in the database.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Service
@Transactional
public class EntriesService {

    private final EntriesRepository entriesRepository;

    private final TransactionManager transactionManager;

    @Autowired
    public EntriesService(EntriesRepository entriesRepository, TransactionManager transactionManager) {
        this.entriesRepository = entriesRepository;
        this.transactionManager = transactionManager;
    }

    public Entry create(String value) throws RollbackException, SystemException {
        // Enlisting extra XAResource to have a 2 phase commit and to be able to simulate system crash
        enlistDummyResource(value);
        return entriesRepository.save(new Entry(value));
    }

    public List<Entry> getAll() {
        return entriesRepository.findAll();
    }

    private void enlistDummyResource(String value) throws SystemException, RollbackException {
        // If value == 'kill' tell XAResource to crash the system before commit
        XAResource xaResource = new DummyXAResource("kill".equals(value));
        transactionManager
                .getTransaction()
                .enlistResource(xaResource);
    }

}
