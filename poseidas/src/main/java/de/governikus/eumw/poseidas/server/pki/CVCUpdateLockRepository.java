package de.governikus.eumw.poseidas.server.pki;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CVCUpdateLockRepository extends JpaRepository<CVCUpdateLock, String> {
}
