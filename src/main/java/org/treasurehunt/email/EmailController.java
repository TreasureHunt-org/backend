package org.treasurehunt.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/sendEmail")
    public String sendEmail(@RequestParam String to, @RequestParam String subject, @RequestParam String body) {
        emailService.sendSimpleEmail(to, subject, body);
        return "Email sent successfully!";
    }
}