package de.governikus.eumw.poseidas.cardserver;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class CertificateUtil
{

  private CertificateUtil()
  {
    // No Instance needed
  }

  /**
   * Creates a self signed certificate from given key pair.
   *
   * @param keyPair key pair
   * @param subject subject for the certificate as X.500 name
   * @param lifespan how many month the certificate will be valid
   * @param providerName name of security provider to use
   * @return self-signed certificate or null when no certificate could created
   */
  public static Certificate createSelfSignedCert(KeyPair keyPair,
                                                 String subject,
                                                 int lifespan,
                                                 String providerName)
  {
    return createSignedCert(keyPair.getPublic(),
                            keyPair.getPrivate(),
                            subject,
                            subject,
                            lifespan,
                            providerName);
  }

  /**
   * Creates a signed certificate from given keys.
   *
   * @param publicKey key to be included in certificate
   * @param privateKey key to sign the certificate
   * @param subject subject for the certificate as X.500 name
   * @param issuer issuer for the certificate as X.500 name
   * @param lifespan how many month the certificate will be valid
   * @param providerName name of security provider to use
   * @return signed certificate or null when no certificate could created
   */
  public static Certificate createSignedCert(PublicKey publicKey,
                                             PrivateKey privateKey,
                                             String subject,
                                             String issuer,
                                             int lifespan,
                                             String providerName)
  {
    String algo = getAlgorithm(privateKey);

    Date startDate = new Date();
    Date endDate = getExpirationDate(lifespan, startDate);
    X500Name subjectDnName = new X500Name(subject);
    X500Name issuerDnName = new X500Name(issuer);
    BigInteger certSerialNumber = new BigInteger(Long.toString(startDate.getTime()));

    try
    {
      ContentSigner contentSigner = new JcaContentSignerBuilder(algo).setProvider(providerName)
                                                                     .build(privateKey);
      JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuerDnName,
                                                                                certSerialNumber, startDate,
                                                                                endDate, subjectDnName,
                                                                                publicKey);
      return new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                                              .getCertificate(certBuilder.build(contentSigner));
    }
    catch (OperatorCreationException e)
    {
      log.error("Can not build Content Signer. No Certificate was created.", e);
    }
    catch (CertificateException e)
    {
      log.error("Can not create certificate", e);
    }
    return null;
  }

  private static Date getExpirationDate(int lifespan, Date startDate)
  {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    calendar.add(Calendar.MONTH, lifespan);
    return calendar.getTime();
  }

  private static String getAlgorithm(PrivateKey key)
  {
    if (key instanceof ECPrivateKey)
    {
      return getAlgorithm(((ECPrivateKey)key).getParams());
    }
    if ("EC".equals(key.getAlgorithm()))
    {
      return "SHA256withECDSA";
    }
    return "SHA256withRSA";
  }

  private static String getAlgorithm(AlgorithmParameterSpec spec)
  {
    String algo = "SHA256withRSA";
    if (spec instanceof ECParameterSpec)
    {
      ECParameterSpec ecSpec = (ECParameterSpec)spec;
      int fieldSize = ecSpec.getCurve().getField().getFieldSize();
      switch (fieldSize)
      {
        case 192:
        case 224:
          algo = "SHA224withECDSA";
          break;
        case 256:
          algo = "SHA256withECDSA";
          break;
        case 320:
        case 384:
          algo = "SHA384withECDSA";
          break;
        case 512:
        default:
          algo = "SHA512withECDSA";
          break;
      }
    }
    return algo;
  }
}
