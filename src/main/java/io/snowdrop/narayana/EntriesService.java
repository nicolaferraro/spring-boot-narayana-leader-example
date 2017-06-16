package io.snowdrop.narayana;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.transaction.xa.XAResource;
import java.net.InetAddress;
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

    public Entry create(String value) throws Exception {
        transactionManager
                .getTransaction()
                .enlistResource(getXaResource(value));
        return  entriesRepository.save(new Entry(value));
    }

    public List<Entry> getAll() {
        return entriesRepository.findAll();
    }

    private XAResource getXaResource(String value) {
        return new DummyXAResource("kill".equals(value));
    }

}
