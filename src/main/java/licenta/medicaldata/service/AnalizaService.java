package licenta.medicaldata.service;

import licenta.medicaldata.crypto.SignatureRSA;
import licenta.medicaldata.model.Analiza;
import licenta.medicaldata.model.MedUser;
import licenta.medicaldata.repository.AnalizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalizaService {
    @Autowired
    private AnalizeRepository analizeRepository;

    SignatureRSA signatureRSA = new SignatureRSA();

    public void addAnalize(String file, MedUser user, MedUser pacient, String tip, byte[] signature){

        Analiza a = new Analiza(user, pacient, file, tip);
        analizeRepository.save(a);
        Analiza analiza = analizeRepository.findAnalizaByFisier(file);
        signatureRSA.storeSignature(signature, analiza.getId().toString());
    }

    public List<Analiza> getAnalize (){
        return (List<Analiza>) analizeRepository.findAll();
    }

    public List<Analiza> getAnalizeByPacient (MedUser pacient){
        return (List<Analiza>) analizeRepository.findAllByPacient(pacient);
    }

    public Analiza getAnalizaById(Integer id){
        return analizeRepository.findAnalizaById(id);
    }
}
