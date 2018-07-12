package licenta.medicaldata.controller;

import licenta.medicaldata.model.Analiza;
import licenta.medicaldata.model.Diagnostic;
import licenta.medicaldata.model.MedUser;
import licenta.medicaldata.model.PacientMedic;
import licenta.medicaldata.repository.AnalizeRepository;
import licenta.medicaldata.repository.DiagnosticRepository;
import licenta.medicaldata.repository.PacientMedicRepository;
import licenta.medicaldata.repository.UserRepository;
import licenta.medicaldata.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@RequestMapping("/doctor")
@Controller
public class MedicController {

    @Autowired
    PacientMedicRepository pacientMedicRepo;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    DiagnosticRepository diagnosticRepo;

    @Autowired
    AnalizeRepository analizeRepo;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PacientMedicRepository pacientMedicRepository;

    @GetMapping("/pacienti")
    public ModelAndView getPatients(Principal principal) {
        String userName = principal.getName();
        MedUser doctor = userDetailsService.getByEmail(userName);
        List<PacientMedic> list = pacientMedicRepo.getPacientMedicByMedic(doctor);
        ModelAndView model = new ModelAndView("pacienti");
        model.addObject("list", list);
        model.addObject("doctor", doctor);

        return model;
    }

    @GetMapping("/diagnostic/{id}")
    public String diagnostic(@PathVariable("id") int idAnaliza, Model model, Principal principal) {
        Analiza analiza = analizeRepo.findById(idAnaliza).get();
        MedUser user = userRepository.findByEmail(principal.getName());
        List<PacientMedic> listPacientiMedic = pacientMedicRepository.getPacientMedicByMedic(user);
        if (listPacientiMedic.contains(new PacientMedic(new MedUser(analiza.getPacient().getId()), new MedUser(user.getId())))) {
            model.addAttribute("diagnostic", new Diagnostic());
            model.addAttribute("idAnaliza", idAnaliza);
            return "diagnostic";
        } else {
            throw new SecurityException("Puteti avea acces doar la datele pacientului  dumneavoastra");
        }

    }

    @PostMapping("/diagnostic/{id}")
    public String addDiagnostic(@PathVariable("id") int idAnaliza, Diagnostic diagnostic, RedirectAttributes redirectAttributes, Principal principal) {
        Analiza analiza = analizeRepo.findById(idAnaliza).get();
        if (analiza != null) {

                MedUser doctor = userDetailsService.getByEmail(principal.getName());

                diagnostic.setDoctor(doctor);
                diagnostic.setAnaliza(analiza);

                diagnosticRepo.save(diagnostic);

                redirectAttributes.addFlashAttribute("message", "Diagnosticul a fost adaugat");

        } else {
            redirectAttributes.addFlashAttribute("error", "Analiza nu exista");
        }


        return "redirect:/doctor/pacienti";
    }
}
