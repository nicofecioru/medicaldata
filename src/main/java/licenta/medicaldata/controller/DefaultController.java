package licenta.medicaldata.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {
    @RequestMapping("/")
    public String indexAction() {
        return "redirect:/login";
    }

}
