package de.governikus.eumw.poseidas.server.pki;

import java.security.cert.X509Certificate;


/**
 * This interface is used to manage the {@link RequestSignerCertificate}.
 */
public interface RequestSignerCertificateService
{

  /**
   * Generates a new {@link RequestSignerCertificateService} for the given entityId
   *
   * @param entityId entityId
   * @param lifespan Validity in months
   * @param rscChr CHR for private provider
   * @return boolean if {@link RequestSignerCertificate} was created
   */
  boolean generateNewPendingRequestSignerCertificate(String entityId, String rscChr, int lifespan);

  /**
   * Gets the most recent request signer certificate for the given entityId
   * 
   * @param entityId entityId
   * @return pending certificate if present, otherwise current certificate if present, otherwise
   *         <code>null</code>
   */
  X509Certificate getRequestSignerCertificate(String entityId);

  /**
   * Checks which terminals have request signer certificates due to expire and generates new certificates for
   * these.
   */
  void renewOutdated();
}
