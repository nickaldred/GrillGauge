package com.grillgauge.api.services;

import jakarta.annotation.PostConstruct;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for handling certificate operations such as loading CA certificates/keys and signing
 * CSRs.
 */
@Service
public class CertificateService {

  @Value("${certificate.validity-days:365}")
  private long validityDays;

  @Value("${certificate.ca-cert}")
  private String caCertPath;

  @Value("${certificate.ca-key}")
  private String caKeyPath;

  @Value("${certificate.ca-key-passphrase:}")
  private String caKeyPassphrase;

  private X509Certificate caCertificate;
  private PrivateKey caPrivateKey;

  @PostConstruct
  private void init() throws CertificateServiceException {
    this.caCertificate = loadCaCertificate(caCertPath);
    this.caPrivateKey = loadCaPrivateKey(caKeyPath);
  }

  /** Custom runtime exception for CertificateService errors. */
  public class CertificateServiceRuntimeException extends RuntimeException {
    public CertificateServiceRuntimeException(String message, Throwable cause) {
      super(message, cause);
    }

    public CertificateServiceRuntimeException(String message) {
      super(message);
    }
  }

  /** Custom checked exception for CertificateService errors. */
  public class CertificateServiceException extends Exception {
    public CertificateServiceException(String message, Throwable cause) {
      super(message, cause);
    }

    public CertificateServiceException(String message) {
      super(message);
    }
  }

  /**
   * Load a CA certificate from a PEM file.
   *
   * @param path The file path to the PEM-encoded CA certificate.
   * @return The loaded X509Certificate.
   */
  private X509Certificate loadCaCertificate(final String path) throws CertificateServiceException {
    try (Reader reader = new FileReader(path);
        PEMParser pemParser = new PEMParser(reader)) {

      Object obj = pemParser.readObject();
      if (obj == null) {
        throw new CertificateServiceException("No PEM object found in " + path);
      }

      X509CertificateHolder holder;
      if (obj instanceof X509CertificateHolder) {
        holder = (X509CertificateHolder) obj;
      } else if (obj instanceof org.bouncycastle.asn1.x509.Certificate) {
        holder = new X509CertificateHolder((org.bouncycastle.asn1.x509.Certificate) obj);
      } else {
        throw new CertificateServiceException(
            "Unsupported PEM object: " + obj.getClass().getName(), null);
      }

      return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);

    } catch (Exception e) {
      throw new CertificateServiceException("Failed to load CA certificate from " + path, e);
    }
  }

  /**
   * Load a CA private key from a PEM file.
   *
   * @param path The file path to the PEM-encoded CA private key.
   * @return The loaded PrivateKey.
   */
  private PrivateKey loadCaPrivateKey(final String path) throws CertificateServiceException {
    try (Reader reader = new FileReader(path);
        PEMParser pemParser = new PEMParser(reader)) {

      Object obj = pemParser.readObject();
      if (obj == null) {
        throw new CertificateServiceException("No PEM object found in " + path);
      }

      JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
      // Unencrypted key pair
      if (obj instanceof PEMKeyPair) {
        java.security.KeyPair kp = converter.getKeyPair((PEMKeyPair) obj);
        return kp.getPrivate();
      }

      // Encrypted PEM key pair
      if (obj instanceof PEMEncryptedKeyPair) {
        if (caKeyPassphrase == null || caKeyPassphrase.isEmpty()) {
          throw new CertificateServiceException("Encrypted private key found; passphrase required");
        }
        PEMDecryptorProvider decProv =
            new JcePEMDecryptorProviderBuilder().build(caKeyPassphrase.toCharArray());
        PEMKeyPair decrypted = ((PEMEncryptedKeyPair) obj).decryptKeyPair(decProv);
        return converter.getKeyPair(decrypted).getPrivate();
      }

      // PKCS#8 encrypted private key
      if (obj instanceof PKCS8EncryptedPrivateKeyInfo) {
        if (caKeyPassphrase == null || caKeyPassphrase.isEmpty()) {
          throw new CertificateServiceException(
              "Encrypted PKCS#8 private key found; passphrase required");
        }
        PKCS8EncryptedPrivateKeyInfo encInfo = (PKCS8EncryptedPrivateKeyInfo) obj;
        InputDecryptorProvider pkcs8Prov =
            new JceOpenSSLPKCS8DecryptorProviderBuilder().build(caKeyPassphrase.toCharArray());
        PrivateKeyInfo pki = encInfo.decryptPrivateKeyInfo(pkcs8Prov);
        return converter.getPrivateKey(pki);
      }

      if (obj instanceof PrivateKeyInfo) {
        return converter.getPrivateKey((PrivateKeyInfo) obj);
      }

      throw new CertificateServiceException("Unsupported PEM object: " + obj.getClass().getName());

    } catch (Exception e) {
      throw new CertificateServiceException("Failed to load CA private key from " + path, e);
    }
  }

  /**
   * Convert an X509Certificate to PEM format.
   *
   * @param csrPem The PEM-encoded CSR string.
   * @return The PEM-encoded certificate as a String.
   */
  public PKCS10CertificationRequest loadCsrFromPem(String csrPem) {
    try (PemReader pemReader = new PemReader(new StringReader(csrPem))) {
      byte[] content = pemReader.readPemObject().getContent();
      return new PKCS10CertificationRequest(content);
    } catch (final Exception e) {
      throw new CertificateServiceRuntimeException("Failed to load CSR from PEM", e);
    }
  }

  /**
   * Convert an X509Certificate to PEM format.
   *
   * @param certificate The X509Certificate to convert.
   * @return The PEM-encoded certificate as a String.
   * @throws Exception if an error occurs during conversion.
   */
  public String convertToPem(X509Certificate certificate) {
    StringWriter stringWriter = new StringWriter();
    try (PemWriter pemWriter = new PemWriter(stringWriter)) {
      pemWriter.writeObject(new PemObject("CERTIFICATE", certificate.getEncoded()));
    } catch (final Exception e) {
      throw new CertificateServiceRuntimeException("Failed to convert certificate to PEM", e);
    }
    return stringWriter.toString();
  }

  /**
   * Convert a public key to PEM format.
   *
   * @param publicKey The public key to convert.
   * @return The PEM-encoded public key as a String.
   */
  public String convertPublicKeyToPem(final PublicKey publicKey) {
    StringWriter stringWriter = new StringWriter();
    try (PemWriter pemWriter = new PemWriter(stringWriter)) {
      pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey.getEncoded()));
    } catch (final Exception e) {
      throw new CertificateServiceRuntimeException("Failed to convert public key to PEM", e);
    }
    return stringWriter.toString();
  }

  /**
   * Extract a public key from a CSR PEM string and return it as PEM.
   *
   * @param csrPem PEM-encoded CSR string
   * @return PEM-encoded public key
   */
  public String extractPublicKeyFromCsrPem(final String csrPem) {
    try {
      PKCS10CertificationRequest csr = loadCsrFromPem(csrPem);
      JcaPKCS10CertificationRequest jcaReq = new JcaPKCS10CertificationRequest(csr);
      PublicKey pub = jcaReq.getPublicKey();
      return convertPublicKeyToPem(pub);
    } catch (final Exception e) {
      throw new CertificateServiceRuntimeException("Failed to extract public key from CSR PEM", e);
    }
  }

  /**
   * Extract a public key from a certificate PEM string and return it as PEM.
   *
   * @param certPem PEM-encoded certificate string
   * @return PEM-encoded public key
   */
  public String extractPublicKeyFromCertPem(final String certPem) {
    try (PemReader pemReader = new PemReader(new StringReader(certPem))) {
      byte[] content = pemReader.readPemObject().getContent();
      X509CertificateHolder holder = new X509CertificateHolder(content);
      X509Certificate cert =
          new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
      return convertPublicKeyToPem(cert.getPublicKey());
    } catch (final Exception e) {
      throw new CertificateServiceRuntimeException(
          "Failed to extract public key from certificate PEM", e);
    }
  }

  /**
   * Sign a CSR to issue a certificate.
   *
   * @param csr The CSR to sign.
   * @return The signed X509Certificate.
   */
  public X509Certificate sign(PKCS10CertificationRequest csr) {

    try {
      // --- Validate CSR ---
      JcaPKCS10CertificationRequest jcaRequest = new JcaPKCS10CertificationRequest(csr);
      boolean csrValid =
          jcaRequest.isSignatureValid(
              new JcaContentVerifierProviderBuilder()
                  .setProvider("BC")
                  .build(jcaRequest.getPublicKey()));

      if (!csrValid) {
        throw new CertificateServiceRuntimeException("Invalid CSR signature");
      }

      // --- Certificate Metadata ---
      long now = System.currentTimeMillis();
      Date notBefore = new Date(now);
      Date notAfter = new Date(now + (validityDays * 24L * 60 * 60 * 1000));
      BigInteger serial = new BigInteger(160, new SecureRandom());

      // Issuer DN from CA cert
      X500Name issuer = new X500Name(caCertificate.getSubjectX500Principal().getName());

      // Subject DN and public key from CSR
      X500Name subject = csr.getSubject();
      SubjectPublicKeyInfo publicKeyInfo = csr.getSubjectPublicKeyInfo();

      // --- Build certificate ---
      X509v3CertificateBuilder certBuilder =
          new X509v3CertificateBuilder(issuer, serial, notBefore, notAfter, subject, publicKeyInfo);

      // Extensions (Required for most browsers + TLS stacks)
      JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

      certBuilder.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));

      certBuilder.addExtension(
          Extension.keyUsage,
          true,
          new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

      certBuilder.addExtension(
          Extension.authorityKeyIdentifier,
          false,
          extUtils.createAuthorityKeyIdentifier(
              new X509CertificateHolder(caCertificate.getEncoded())));

      certBuilder.addExtension(
          Extension.subjectKeyIdentifier,
          false,
          extUtils.createSubjectKeyIdentifier(publicKeyInfo));

      // Copy SAN from CSR if present
      Attribute[] attrs = csr.getAttributes(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest);
      if (attrs != null && attrs.length > 0) {
        ASN1Set attSet = attrs[0].getAttrValues();
        Extensions extensions = Extensions.getInstance(attSet.getObjectAt(0));

        Extension sanExt = extensions.getExtension(Extension.subjectAlternativeName);
        if (sanExt != null) {
          certBuilder.addExtension(
              Extension.subjectAlternativeName, false, sanExt.getParsedValue());
        }
      }

      // --- Sign certificate ---
      AsymmetricKeyParameter caKeyParam = PrivateKeyFactory.createKey(caPrivateKey.getEncoded());
      AlgorithmIdentifier sigAlgId =
          new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
      AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

      ContentSigner signer = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(caKeyParam);

      X509CertificateHolder holder = certBuilder.build(signer);

      return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);

    } catch (Exception e) {
      throw new CertificateServiceRuntimeException("Failed to sign certificate", e);
    }
  }

  /**
   * Revoke a signed certificate (convenience method). Uses the current time as revocation date and
   * `cessationOfOperation` as reason.
   *
   * @param certificate the issued certificate to revoke
   * @return the generated X509CRL containing the revoked certificate
   */
  public X509CRL revokeSignedCertificate(final X509Certificate certificate) {
    return revokeBySerial(certificate.getSerialNumber(), new Date(), 5); // 5 = cessationOfOperation
  }

  /**
   * Revoke a certificate by serial number. Returns a signed CRL containing the revocation.
   *
   * @param serial serial number of the certificate to revoke
   * @param revocationDate date of revocation
   * @param reason numeric CRL reason code (see RFC5280)
   * @return signed X509CRL
   */
  public X509CRL revokeBySerial(
      final BigInteger serial, final Date revocationDate, final int reason) {
    try {
      // Issuer must be the CA
      X500Name issuer = new X500Name(caCertificate.getSubjectX500Principal().getName());

      // Build CRL
      X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(issuer, new Date());
      crlBuilder.addCRLEntry(serial, revocationDate, reason);

      // Add authority key identifier
      JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
      crlBuilder.addExtension(
          Extension.authorityKeyIdentifier,
          false,
          extUtils.createAuthorityKeyIdentifier(
              new X509CertificateHolder(caCertificate.getEncoded())));

      // Add a CRL number (use timestamp-based value)
      BigInteger crlNumber = BigInteger.valueOf(System.currentTimeMillis());
      crlBuilder.addExtension(Extension.cRLNumber, false, new ASN1Integer(crlNumber));

      // Sign CRL with CA private key
      AsymmetricKeyParameter caKeyParam = PrivateKeyFactory.createKey(caPrivateKey.getEncoded());
      AlgorithmIdentifier sigAlgId =
          new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
      AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

      ContentSigner signer = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(caKeyParam);

      X509CRLHolder holder = crlBuilder.build(signer);

      return new JcaX509CRLConverter().setProvider("BC").getCRL(holder);

    } catch (Exception e) {
      throw new CertificateServiceRuntimeException("Failed to revoke certificate", e);
    }
  }

  /**
   * Convert an X509CRL to PEM format.
   *
   * @param crl the CRL to convert
   * @return PEM-encoded CRL as String
   */
  public String convertCrlToPem(final X509CRL crl) {
    StringWriter stringWriter = new StringWriter();
    try (PemWriter pemWriter = new PemWriter(stringWriter)) {
      pemWriter.writeObject(new PemObject("X509 CRL", crl.getEncoded()));
    } catch (final Exception e) {
      throw new CertificateServiceRuntimeException("Failed to convert CRL to PEM", e);
    }
    return stringWriter.toString();
  }
}
