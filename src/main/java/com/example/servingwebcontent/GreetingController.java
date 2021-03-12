package com.example.servingwebcontent;

import com.example.servingwebcontent.domain.Message;
import com.example.servingwebcontent.repository.MessageRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GreetingController {

    private final MessageRepository messageRepository;

    public GreetingController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/")
    public String greeting(Model model) {
        return "greeting";
    }

    @GetMapping("/main")
    public String main(Model model) {
        Iterable<Message> messages = messageRepository.findAll();
        model.addAttribute("messages", messages);
        model.addAttribute("message", new Message());
        return "main";
    }

    @PostMapping("/main")
    public String add(@ModelAttribute("message") Message message, Model model) {
        messageRepository.save(message);
        Iterable<Message> messages = messageRepository.findAll();
        model.addAttribute("messages", messages);
        return "main";
    }

    @PostMapping("/filter")
    public String filter(@ModelAttribute("message") Message message, Model model) {
        Iterable<Message> messages;
        if (message.getTag() != null && !message.getTag().isEmpty()) {
            messages = messageRepository.findByTag(message.getTag());
        } else {
            messages = messageRepository.findAll();
        }
        model.addAttribute("messages", messages);
        return "main";
    }
}
