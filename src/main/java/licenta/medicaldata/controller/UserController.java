package licenta.medicaldata.controller;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import licenta.medicaldata.model.MedUser;
import licenta.medicaldata.model.PacientMedic;
import licenta.medicaldata.repository.PacientMedicRepository;
import licenta.medicaldata.repository.UserRepository;
import licenta.medicaldata.service.UserDetailsServiceImpl;
import licenta.medicaldata.utils.CnpValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
    @RequestMapping("/admin")
    public class UserController {

        @Autowired
        UserDetailsServiceImpl userDetailsService;

        @Autowired
        UserRepository userRepo;

        @Autowired
        PacientMedicRepository pacientMedicRepo;

        @GetMapping("/addUser")
        public String userForm(Model model) {
            model.addAttribute("user", new MedUser());
            return "userform";
        }

        @PostMapping("/addUser")
        public String addUser(@ModelAttribute MedUser medUser, RedirectAttributes redirectAttributes) {
            if (CnpValidator.isValid(medUser.getCnp())) {
                userDetailsService.saveUser(medUser);

                redirectAttributes.addFlashAttribute("message", "Userul a fost adaugat");
            } else {
                redirectAttributes.addFlashAttribute("error", "Cnp invalid");
            }
            return "redirect:/admin/addUser";
        }

        @GetMapping("/addPatient")
        public String patientMedicForm(Model model) {
            model.addAttribute("medici", userRepo.findAllByRol("doctor"));
            model.addAttribute("pacientMedic", new PacientMedic());
            return "pacientMedic";
        }

        @PostMapping("/addPatient")
        public String addPatient(@ModelAttribute PacientMedic pacientMedic, @RequestParam("cnp") String cnp,
                                 RedirectAttributes redirectAttributes) {

            MedUser pacient = userDetailsService.getByCnp(cnp);
            if (pacient != null && pacient.getRol().equals("pacient")) {
                pacientMedic.setPacient(pacient);
                try {
                pacientMedicRepo.save(pacientMedic);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Asocierea deja exista");
                    return "redirect:/admin/addPatient";
                }
                redirectAttributes.addFlashAttribute("message", "Pacientul a fost asociat cu doctorul");
            } else {
                redirectAttributes.addFlashAttribute("error", "Pacientul nu exista");
            }

            return "redirect:/admin/addPatient";

        }
    }

