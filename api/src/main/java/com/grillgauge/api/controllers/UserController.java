package com.grillgauge.api.controllers;

import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Controller for managing user-related operations. */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

  private UserService userService;

  public UserController(final UserService userService) {
    this.userService = userService;
  }

  /**
   * Store a new user.
   *
   * @param user the User entity to store.
   * @return the stored User entity.
   */
  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  public User storeUser(@RequestBody final User user) {
    return userService.storeUser(user);
  }

  /**
   * Delete a user.
   *
   * @param user the User entity to delete.
   * @return the deleted User entity.
   */
  @DeleteMapping
  public User deleteUser(final User user) {
    return userService.deleteUser(user);
  }

  /**
   * Get a user by their email.
   *
   * @param email the email to get the user for.
   * @return the User entity.
   */
  @GetMapping
  public User getUserByEmail(@RequestParam final String email) {
    return userService.getUserByEmail(email);
  }
}
