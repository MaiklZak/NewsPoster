package com.example.servingwebcontent.service;

import com.example.servingwebcontent.domain.Role;
import com.example.servingwebcontent.domain.User;
import com.example.servingwebcontent.repository.UserRepository;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MailSender mailSender;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void addUser() {
        User user = new User();
        user.setEmail("some@mail.com");
        boolean isUserCreated = userService.addUser(user);

        Assert.assertTrue(isUserCreated);
        Assert.assertNotNull(user.getActivationCode());
        Assert.assertTrue(CoreMatchers.is(user.getRoles()).matches(Collections.singleton(Role.USER)));

        Mockito.verify(userRepository, Mockito.times(1)).save(user);
        Mockito.verify(mailSender, Mockito.times(1)).send(
                ArgumentMatchers.eq(user.getEmail()),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString());
    }

    @Test
    public void addUserFailTest() {
        User user = new User();
        user.setUsername("Unknown");

        Mockito.doReturn(new User())
                .when(userRepository)
                .findByUsername("Unknown");

        boolean isUserCreated = userService.addUser(user);

        Assert.assertFalse(isUserCreated);

        Mockito.verify(userRepository, Mockito.times(0)).save(ArgumentMatchers.any(User.class));
        Mockito.verify(mailSender, Mockito.times(0)).send(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString());
    }

    @Test
    void activateUser() {
        User user = new User();
        user.setActivationCode("simple");

        Mockito.doReturn(new User())
                .when(userRepository)
                .findByActivationCode("activate");

        boolean isUserActivated = userService.activateUser("activate");
        Assert.assertTrue(isUserActivated);
        Assert.assertNotNull(user.getActivationCode());

        Mockito.verify(userRepository, Mockito.times(1)).save(user);
    }

    @Test
    public void activateUserFailTest() {
        boolean isUserActivated = userService.activateUser("activate");
        Assert.assertFalse(isUserActivated);

        Mockito.verify(userRepository, Mockito.times(0)).save(ArgumentMatchers.any(User.class));
    }
}