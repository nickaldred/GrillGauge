package com.grillgauge.api.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Minimal UserDetails representing an authenticated Hub. */
public class HubUserDetails implements UserDetails {

  private final Long hubId;
  private final String hubName;

  public HubUserDetails(Long hubId, String hubName) {
    this.hubId = hubId;
    this.hubName = hubName == null ? "hub-" + hubId : hubName;
  }

  public Long getHubId() {
    return hubId;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_HUB"));
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return hubName;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
