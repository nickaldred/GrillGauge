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

  @Override
  public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token)
      throws UsernameNotFoundException {
    Object creds = token.getCredentials();
    X509Certificate cert = null;

    if (creds instanceof X509Certificate[]) {
      X509Certificate[] certs = (X509Certificate[]) creds;
      if (certs.length > 0) {
        cert = certs[0];
      }
    } else if (creds instanceof X509Certificate) {
      cert = (X509Certificate) creds;
    }

    if (cert == null) {
      try {
        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
          HttpServletRequest req = attrs.getRequest();
          Object attr = req.getAttribute("javax.servlet.request.X509Certificate");
          if (attr instanceof X509Certificate[]) {
            X509Certificate[] certs = (X509Certificate[]) attr;
            if (certs.length > 0) {
              cert = certs[0];
            }
          }
        }
      } catch (Exception ignored) {
        // continue to throw below
      }
    }

    if (cert == null) {
      throw new UsernameNotFoundException("No client certificate presented");
    }

    try {
      X509Certificate ca = certificateService.getCaCertificate();
      cert.verify(ca.getPublicKey());
    } catch (Exception e) {
      throw new UsernameNotFoundException("Client certificate not signed by trusted CA", e);
    }

    Long serial = cert.getSerialNumber().longValue();

    Optional<Hub> hubOpt = hubRepository.findByCertificateSerial(serial);
    if (hubOpt.isPresent()) {
      Hub hub = hubOpt.get();
      if (hub.getStatus() != Hub.HubStatus.REGISTERED) {
        throw new UsernameNotFoundException("Hub not in REGISTERED status");
      }
      return new HubUserDetails(hub.getId(), hub.getName());
    }

    // No DB entry for this certificate serial. Try to extract a hub id from
    // the certificate subject CN (e.g. CN=Hub-6) so controller/service can
    // return a 404 when appropriate instead of a 403.
    try {
      String dn = cert.getSubjectX500Principal().getName();
      String[] parts = dn.split(",");
      for (String p : parts) {
        p = p.trim();
        if (p.startsWith("CN=")) {
          String cn = p.substring(3);
          if (cn.startsWith("Hub-")) {
            String idStr = cn.substring(4);
            try {
              Long parsedId = Long.parseLong(idStr);
              return new HubUserDetails(parsedId, cn);
            } catch (NumberFormatException ignored) {
              // fall through
            }
          }
        }
      }
    } catch (Exception ignored) {
      // ignore and throw below
    }

    throw new UsernameNotFoundException("No hub found for certificate serial");
  }
}
