package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.domain.Message;
import com.example.servingwebcontent.domain.User;
import com.example.servingwebcontent.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@Controller
public class MainController {

    private final MessageRepository messageRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public MainController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/")
    public String greeting(Model model) {
        return "greeting";
    }

    @GetMapping("/main")
    public String main(@RequestParam(required = false) String filter,
                       Model model,
                       @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Message> page;
        if (filter != null && !filter.isEmpty()) {
            page = messageRepository.findByTag(filter, pageable);
        } else {
            page = messageRepository.findAll(pageable);
        }
        int totalPages = page.getTotalPages();

        Integer[] body = ControllerUtils.getArraySizePage(page, totalPages);

        int[] sizeArray = {5, 10, 25, 50};
        model.addAttribute("body", body);
        model.addAttribute("page", page);
        model.addAttribute("url", "/main");
        model.addAttribute("message", new Message());
        model.addAttribute("filter", filter);
        model.addAttribute("size", sizeArray);
        return "main";
    }

    @PostMapping("/main")
    public String add(
            @AuthenticationPrincipal User user,
            @ModelAttribute @Valid Message message,
            BindingResult bindingResult,
            @RequestParam("file") MultipartFile file,
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) throws IOException {

        message.setAuthor(user);
        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errorMap);
            model.addAttribute("message", message);
//            return main(model, user);
        } else {
            ControllerUtils.saveFile(message, file, uploadPath);
            model.addAttribute("message", new Message());
            messageRepository.save(message);
        }
        Page<Message> page = messageRepository.findAll(pageable);

        int totalPages = page.getTotalPages();
        Integer[] body = ControllerUtils.getArraySizePage(page, totalPages);
        int[] sizeArray = {5, 10, 25, 50};
        model.addAttribute("body", body);
        model.addAttribute("url", "/main");
        model.addAttribute("size", sizeArray);

        model.addAttribute("page", page);
        return "/main";
//        redirect
    }


}
