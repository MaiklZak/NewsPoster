package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.domain.Message;
import com.example.servingwebcontent.domain.User;
import com.example.servingwebcontent.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Controller
public class MessageController {

    @Value("${upload.path}")
    private String uploadPath;

    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/user-messages/{currentId}")
    public String userMessages(
            @AuthenticationPrincipal User user,
            @PathVariable("currentId") User currentUser,
            @RequestParam(required = false) Message message,
            Model model
    ) {
        Set<Message> messages = currentUser.getMessages();
        model.addAttribute("userChannel", currentUser);
        model.addAttribute("subscriptionsCount", currentUser.getSubscriptions().size());
        model.addAttribute("subscribersCount", currentUser.getSubscribers().size());
        model.addAttribute("messages", messages);
        model.addAttribute("isCurrentUser", user.equals(currentUser));
        model.addAttribute("isSubscriber", currentUser.getSubscribers().contains(user));
        model.addAttribute("message", message);
        return "userMessages";
    }

    @PostMapping("/user-messages/{currentId}")
    public String updateMessage(
            @AuthenticationPrincipal User user,
            @PathVariable("currentId") User currentUser,
            @RequestParam("id") Message message,
            @RequestParam("text") String text,
            @RequestParam("tag") String tag,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (message.getAuthor().equals(currentUser)) {
            if (!StringUtils.isEmpty(text)) {
                message.setText(text);
            }
            if (!StringUtils.isEmpty(tag)) {
                message.setTag(tag);
            }
            ControllerUtils.saveFile(message, file, uploadPath);
            messageRepository.save(message);
        }

        return "redirect:/user-messages/" + user.getId();
    }
}
