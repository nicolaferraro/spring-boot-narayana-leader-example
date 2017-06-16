package io.snowdrop.narayana;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public interface EntriesRepository extends JpaRepository<Entry, Long> {

}
