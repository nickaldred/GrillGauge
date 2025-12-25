package com.grillgauge.api.security;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.services.CertificateService;
import jakarta.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.Optional;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Translates an X.509 client certificate (from a PreAuthenticated token) into a Spring Security
 * UserDetails representing a Hub. Also performs an extra verification step that the certificate was
 * issued by the configured CA.
 */
@Service
public class CertificateUserDetailsService
    implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

  private final HubRepository hubRepository;
  private final CertificateService certificateService;

  public CertificateUserDetailsService(
      final HubRepository hubRepository, final CertificateService certificateService) {
    this.hubRepository = hubRepository;
    this.certificateService = certificateService;
  }

  /**
   * Loads UserDetails from a PreAuthenticatedAuthenticationToken containing an X.509 certificate
   *
   * @param token the pre-authenticated token
   * @return the UserDetails representing the hub
   * @throws UsernameNotFoundException if the certificate is invalid or no hub is found
   */
  @Override
  public UserDetails loadUserDetails(final PreAuthenticatedAuthenticationToken token)
      throws UsernameNotFoundException {
    X509Certificate cert =
        extractCertificateFromToken(token)
            .or(this::extractCertificateFromRequestContext)
            .orElseThrow(() -> new UsernameNotFoundException("No client certificate presented"));

    verifyCertificateSignedByTrustedCa(cert);

    long serial = cert.getSerialNumber().longValue();

    return hubRepository
        .findByCertificateSerial(serial)
        .map(this::toRegisteredHubUserDetails)
        .orElseGet(
            () ->
                createUserDetailsFromSubject(cert)
                    .orElseThrow(
                        () ->
                            new UsernameNotFoundException("No hub found for certificate serial")));
  }

  private Optional<X509Certificate> extractCertificateFromToken(
      final PreAuthenticatedAuthenticationToken token) {
    Object creds = token.getCredentials();

    if (creds instanceof X509Certificate) {
      return Optional.of((X509Certificate) creds);
    }

    if (creds instanceof X509Certificate[] certs && certs.length > 0) {
      return Optional.of(certs[0]);
    }

    return Optional.empty();
  }

  private Optional<X509Certificate> extractCertificateFromRequestContext() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      return Optional.empty();
    }

    HttpServletRequest req = attrs.getRequest();
    Object attr = req.getAttribute("javax.servlet.request.X509Certificate");
    if (attr instanceof X509Certificate[] certs && certs.length > 0) {
      return Optional.of(certs[0]);
    }

    return Optional.empty();
  }

  private void verifyCertificateSignedByTrustedCa(final X509Certificate cert) {
    try {
      X509Certificate ca = certificateService.getCaCertificate();
      cert.verify(ca.getPublicKey());
    } catch (Exception e) {
      throw new UsernameNotFoundException("Client certificate not signed by trusted CA", e);
    }
  }

  private UserDetails toRegisteredHubUserDetails(final Hub hub) {
    if (hub.getStatus() != Hub.HubStatus.REGISTERED) {
      throw new UsernameNotFoundException("Hub not in REGISTERED status");
    }
    return new HubUserDetails(hub.getId(), hub.getName());
  }

  private Optional<UserDetails> createUserDetailsFromSubject(final X509Certificate cert) {
    if (cert.getSubjectX500Principal() == null) {
      return Optional.empty();
    }

    String dn = cert.getSubjectX500Principal().getName();
    String[] parts = dn.split(",");

    for (String part : parts) {
      String p = part.trim();
      if (!p.startsWith("CN=")) {
        continue;
      }

      String cn = p.substring(3);
      if (!cn.startsWith("Hub-")) {
        continue;
      }

      String idStr = cn.substring(4);
      Optional<Long> hubId = parseHubId(idStr);
      if (hubId.isPresent()) {
        return Optional.of(new HubUserDetails(hubId.get(), cn));
      }
    }

    return Optional.empty();
  }

  private Optional<Long> parseHubId(final String idStr) {
    try {
      return Optional.of(Long.parseLong(idStr));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }
}
