package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.domain.Message;
import com.example.servingwebcontent.domain.dto.MessageDto;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControllerUtils {

    static Map<String, String> getErrors(BindingResult bindingResult) {
        Collector<FieldError, ?, Map<String, String>> collector = Collectors.toMap(
                fieldError -> fieldError.getField() + "Error",
//                FieldError::getField,
                FieldError::getDefaultMessage
        );
        return bindingResult.getFieldErrors().stream().collect(collector);
    }

    static void saveFile(Message message, MultipartFile file, String uploadPath) throws IOException {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            File uploadDir = new File(uploadPath);

            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFilename = uuidFile + "." + file.getOriginalFilename();

            file.transferTo(new File(uploadPath + "/" + resultFilename));

            message.setFilename(resultFilename);
        }
    }

    static Integer[] getArraySizePage(Page<MessageDto> page, int totalPages) {
        Integer[] body;
        if (page.getTotalPages() > 7) {
            int pageNum = page.getNumber() + 1;
            Integer[] head = pageNum > 4 ? new Integer[]{1, -1} : new Integer[]{1, 2, 3};
            Integer[] tail = pageNum < (totalPages - 3) ? new Integer[]{-1, totalPages}
                    : new Integer[]{totalPages - 2, totalPages - 1, totalPages};
            Integer[] bodyBefore = pageNum > 4 && pageNum < totalPages - 1
                    ? new Integer[]{pageNum - 2, pageNum - 1}
                    : new Integer[]{};
            Integer[] bodyAfter = pageNum > 2 && pageNum < totalPages - 3
                    ? new Integer[]{pageNum + 1, pageNum + 2}
                    : new Integer[]{};

            body = concatArrays(head, bodyBefore,
                    pageNum > 3 && pageNum < (totalPages - 2) ? (new Integer[]{pageNum}) : (new Integer[]{}),
                    bodyAfter, tail);
        } else {
            body = Stream.iterate(1, i -> i = i + 1).limit(totalPages).toArray(Integer[]::new);
        }
        return body;
    }

    static Integer[] concatArrays(final Integer[]... arrays) {
        return Arrays.stream(arrays)
                .flatMap(Arrays::stream)
                .toArray(Integer[]::new);
    }
}
