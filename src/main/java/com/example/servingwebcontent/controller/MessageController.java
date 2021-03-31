package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.domain.Message;
import com.example.servingwebcontent.domain.User;
import com.example.servingwebcontent.domain.dto.MessageDto;
import com.example.servingwebcontent.repository.MessageRepository;
import com.example.servingwebcontent.service.MessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Controller
public class MessageController {

    private final MessageRepository messageRepository;

    private final MessageService messageService;

    @Value("${upload.path}")
    private String uploadPath;

    public MessageController(MessageRepository messageRepository, MessageService messageService) {
        this.messageRepository = messageRepository;
        this.messageService = messageService;
    }

    @GetMapping("/")
    public String greeting() {
        return "greeting";
    }

    @GetMapping("/main")
    public String main(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String filter,
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<MessageDto> page = messageService.messageList(pageable, filter, user);

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
        Page<MessageDto> page = messageRepository.findAll(pageable, user);

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

    @GetMapping("/user-messages/{currentId}")
    public String userMessages(
            @AuthenticationPrincipal User user,
            @PathVariable("currentId") User author,
            @RequestParam(required = false) Message message,
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<MessageDto> page = messageService.messageListForUser(pageable, user, author);

        int totalPages = page.getTotalPages();

        Integer[] body = ControllerUtils.getArraySizePage(page, totalPages);

        int[] sizeArray = {5, 10, 25, 50};

        model.addAttribute("size", sizeArray);
        model.addAttribute("body", body);
        model.addAttribute("userChannel", author);
        model.addAttribute("subscriptionsCount", author.getSubscriptions().size());
        model.addAttribute("subscribersCount", author.getSubscribers().size());
        model.addAttribute("page", page);
        model.addAttribute("isCurrentUser", user.equals(author));
        model.addAttribute("isSubscriber", author.getSubscribers().contains(user));
        model.addAttribute("message", message);
        model.addAttribute("url", "/user-messages/" + author.getId());

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

    @GetMapping("messages/{message}/like")
    public String like(
            @AuthenticationPrincipal User user,
            @PathVariable Message message,
            RedirectAttributes redirectAttributes,
            @RequestHeader(required = false) String referer
    ) {
        Set<User> likes = message.getLikes();
        if (likes.contains(user)) {
            likes.remove(user);
        } else {
            likes.add(user);
        }

        UriComponents components = UriComponentsBuilder.fromHttpUrl(referer).build();
        components.getQueryParams()
                .entrySet()
                .forEach(pair -> redirectAttributes.addAttribute(pair.getKey(), pair.getValue()));
        return "redirect:" + components.getPath();
    }
}
