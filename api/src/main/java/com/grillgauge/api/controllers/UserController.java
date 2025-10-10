package com.grillgauge.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.services.UserService;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public User storeUser(@RequestBody final User user) {
        return userService.storeUser(user);
    }

    @DeleteMapping
    public User deleteUser(final User user) {
        return userService.deleteUser(user);
    }

    @GetMapping
    public User getUserByEmail(@RequestParam final String email) {
        return userService.getUserByEmail(email);
    }
}
