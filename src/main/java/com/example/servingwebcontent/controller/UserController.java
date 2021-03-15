package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.domain.Role;
import com.example.servingwebcontent.domain.User;
import com.example.servingwebcontent.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String userList(Model model) {
        List<User> userList = userRepository.findAll();
        model.addAttribute("users", userList);
        return "userList";
    }

    @GetMapping("{id}")
    public String userEditForm(@PathVariable("id") User user, Model model) {
//        model.addAttribute("user", userRepository.getOne(id));
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "userEdit";
    }

    @PostMapping
    public String userSave(@ModelAttribute User user,
                           @RequestParam Map<String, String> form) {

        user.setRoles(userRepository.getOne(user.getId()).getRoles());

        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());

        user.getRoles().clear();
        for (String key : form.keySet()) {
            if (roles.contains(key)) {
                user.getRoles().add(Role.valueOf(key));
            }
        }
        userRepository.save(user);
        return "redirect:/user";
    }
}
