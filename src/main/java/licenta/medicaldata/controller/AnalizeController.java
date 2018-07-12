package licenta.medicaldata.controller;

import licenta.medicaldata.crypto.AesEncryption;
import licenta.medicaldata.crypto.SignatureRSA;
import licenta.medicaldata.model.Analiza;
import licenta.medicaldata.model.MedUser;
import licenta.medicaldata.model.PacientMedic;
import licenta.medicaldata.repository.DiagnosticRepository;
import licenta.medicaldata.repository.PacientMedicRepository;
import licenta.medicaldata.repository.UserRepository;
import licenta.medicaldata.service.AnalizaService;
import licenta.medicaldata.service.UserDetailsServiceImpl;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.*;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.NoSuchFileException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

@Controller
public class AnalizeController {

    private static String UPLOADED_FOLDER = "D://temp//";

    public AesEncryption aesEncryption = new AesEncryption();

    public SignatureRSA signatureRSA = new SignatureRSA();

    @Autowired
    AnalizaService analizaService;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    UserRepository userRepo;

    @Autowired
    DiagnosticRepository diagnosticRepo;

    @Autowired
    PacientMedicRepository pacientMedicRepository;

    @GetMapping("/lab/upload")
    public String analizaForm() {
        return "upload";
    }

    @PostMapping("/lab/upload")
    public String addAnaliza(@RequestParam("file") MultipartFile file, @RequestParam("cnp") String cnp,
                             @RequestParam("tip") String tip, RedirectAttributes redirectAttributes, Principal principal) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Trebuie sa selectati un fisier");
            return "redirect:/lab/upload";
        }
        try {
            String path = UPLOADED_FOLDER + file.getOriginalFilename();
            OutputStream fileOutputStream = new FileOutputStream(path);
            byte[] data = file.getBytes(); // data for signing
            aesEncryption.encryptFile(file.getInputStream(), fileOutputStream);
            MedUser pacient = userDetailsService.getByCnp(cnp);
            if (pacient!=null&&pacient.getRol().equals("pacient")) {
                String userName = principal.getName();
                MedUser laborant = userDetailsService.getByEmail(userName);
                PrivateKey privateKey = signatureRSA.readPrivateKey("privateKey/"+laborant.getId().toString());
                String filename = file.getOriginalFilename();
                byte[] signature = signatureRSA.sign(privateKey, data);
                analizaService.addAnalize(filename, laborant, pacient, tip, signature);
                redirectAttributes.addFlashAttribute("message",
                        "Fisierul '" + file.getOriginalFilename() + "' a fost adaugat cu succes");
            } else {
                redirectAttributes.addFlashAttribute("error", "Pacientul nu exista");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "A aparut o eroare, va rugam reincercati");
        }

        return "redirect:/lab/upload";
    }

    @RequestMapping(value="analize/download/{id}", method = RequestMethod.GET)
    public void downloadAnaliza(HttpServletResponse response, @PathVariable("id") Integer id,
                                @PathParam("idPacient") Integer idPacient, Principal principal) throws IOException {
        Analiza analiza = analizaService.getAnalizaById(id);
        Integer idP=analiza.getPacient().getId();
        if ( idP.equals(idPacient)) {
            MedUser user = userRepo.findByEmail(principal.getName());
            if (user.getRol().equals("pacient")) {
                if (user.getId().equals( idPacient)) {
                    download(response, analiza);
                } else {
                    response.sendError(403, "Puteti avea acces doar la datele dumneavoastra");
                }
            }else if (user.getRol().equals("doctor")){
                List<PacientMedic> listPacientiMedic = pacientMedicRepository.getPacientMedicByMedic(user);
                if (listPacientiMedic.contains(new PacientMedic(new MedUser(idPacient), new MedUser(user.getId())))) {
                    download(response, analiza);
                } else {
                    response.sendError(403, "Puteti avea acces doar la datele pacientului dumneavoastra");
                }
            }
        } else {
            response.sendError(403, "Analiza nu corespunde pacientului");
        }
    }

    public void download(HttpServletResponse response, Analiza analiza) throws IOException {
        String filename = analiza.getFisier();
        byte[] publicKey = analiza.getCadruMedical().getPublicKey();
        File file = new File(UPLOADED_FOLDER + filename);

        if (!file.exists()) {
            String errorMessage = "Fisierul nu exista";
            System.out.println(errorMessage);
            OutputStream outputStream = null;
            try {
                outputStream = response.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
            outputStream.close();
            return;
        }

        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if (mimeType == null) {
            System.out.println("mimetype is not detectable, will take default");
            mimeType = "application/octet-stream";
        }

        System.out.println("mimetype : " + mimeType);


        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            File outfile = File.createTempFile("temp", "");
            byte[] signature = signatureRSA.getSignature(analiza.getId().toString());
            byte[] data = aesEncryption.decryptFile(inputStream, new FileOutputStream(outfile));
            System.out.println("decrypt done");
            if (signatureRSA.verify(publicKey, signature, data)) {

                response.setContentType(mimeType);
                response.setHeader("Content-Disposition", String.format("inline; filename=\"" + analiza.getFisier() + "\""));

                response.setContentLength((int) file.length());



                AesEncryption aesEncryption = new AesEncryption();
                IOUtils.copy(new FileInputStream(outfile), response.getOutputStream());
                response.flushBuffer();
                System.out.println("flush buffer done");
                return;
            } else {
                response.sendError(403, "Semnatura invalida");
            }
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            response.sendError(403, "Semnatura invalida");
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchFileException e) {
            response.sendError(403, "Semnatura invalida");
        }
    }

    @RequestMapping(value = "/analize", method = RequestMethod.GET)
    public ModelAndView getAllData() {
        List<Analiza> list = analizaService.getAnalize();
        ModelAndView model = new ModelAndView("analize");
        model.addObject("list", list);

        return model;
    }

    @RequestMapping(value = "/analize/{id}", method = RequestMethod.GET)
    public ModelAndView getPatientData(@PathVariable("id") int id, Principal principal) {
        MedUser user = userRepo.findByEmail(principal.getName());
        MedUser patient = userRepo.findById(id).get();
        List<Analiza> list = analizaService.getAnalizeByPacient(patient);
        if (user.getRol().equals("pacient")) {
            if (user.getId().equals(patient.getId())) {
                return getAnalize(user,patient,list);
            } else {
                throw new SecurityException("Puteti avea acces doar la datele dumneavoastra");
            }
        }else if (user.getRol().equals("doctor")){
            List<PacientMedic> listPacientiMedic = pacientMedicRepository.getPacientMedicByMedic(user);
            for (PacientMedic pm: listPacientiMedic) {
                if (pm.getPacient().equals(patient)) {
                    return getAnalize(user,patient,list);
                }
            }
            throw new SecurityException("Puteti avea acces doar la datele pacientului dumneavoastra");
        }
        return null;
    }

    public ModelAndView getAnalize(MedUser user , MedUser patient, List<Analiza> list) {
        ModelAndView model = new ModelAndView("analize");
        model.addObject("list", list);
        model.addObject("patient", patient);
        model.addObject("user", user);
        model.addObject("diagnosticRepo", diagnosticRepo);
        return model;
    }
}
