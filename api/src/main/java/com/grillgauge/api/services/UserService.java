package com.grillgauge.api.services;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.repositorys.UserRepository;

/**
 * Service class for managing users.
 */
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
        userRepository.save(user);
        return user;
    }

    /**
     * Delete the user with the given email.
     * 
     * @param user the User entity containing the email to delete
     * @return the deleted User entity
     * @throws ResponseStatusException with status 404 if no user is found for the
     *                                 given email
     */
    @Transactional
    public User deleteUser(final User user) {
        Optional<User> deletedUser = userRepository.deleteByEmail(user.getEmail());
        if (deletedUser.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No user found with for email: %s".formatted(user.getEmail()));
        }
        return user;
    }

}
