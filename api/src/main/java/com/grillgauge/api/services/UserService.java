package com.grillgauge.api.services;

import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.repositorys.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Service class for managing users. */
@Service
public class UserService {

  private UserRepository userRepository;

  public UserService(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Store a new user.
   *
   * @param user the User entity to store
   * @return the stored User entity
   */
  @Transactional
  public User storeUser(final User user) {
    if (user.getRoles() == null || user.getRoles().isEmpty()) {
      user.setRoles(List.of(User.UserRole.USER));
    }
    userRepository.save(user);
    return user;
  }

  /**
   * Update the temperature unit preference for the given user.
   *
   * @param email the email of the user to update
   * @param temperatureUnit the desired temperature unit
   * @return the updated User entity
   * @throws ResponseStatusException with status 404 if no user is found for the given email
   */
  @Transactional
  public User updateTemperatureUnit(
      final String email, final User.UserTemperatureUnit temperatureUnit) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No user found with for email: %s".formatted(email)));

    User.UserTemperatureUnit resolvedUnit =
        temperatureUnit == null ? User.UserTemperatureUnit.CELSIUS : temperatureUnit;
    user.setTemperatureUnit(resolvedUnit);
    userRepository.save(user);
    return user;
  }

  /**
   * Delete the user with the given email.
   *
   * @param user the User entity containing the email to delete
   * @return the deleted User entity
   * @throws ResponseStatusException with status 404 if no user is found for the given email
   */
  @Transactional
  public User deleteUser(final User user) {
    Optional<User> deletedUser = userRepository.deleteByEmail(user.getEmail());
    if (deletedUser.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "No user found with for email: %s".formatted(user.getEmail()));
    }
    return user;
  }

  /**
   * Get the user with the given email.
   *
   * @param email the email of the user to retrieve
   * @return the User entity with the given email
   * @throws ResponseStatusException with status 404 if no user is found for the given email
   */
  @Transactional(readOnly = true)
  public User getUserByEmail(final String email) {
    Optional<User> user = userRepository.findByEmail(email);
    if (user.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "No user found with for email: %s".formatted(email));
    }
    return user.get();
  }
}
