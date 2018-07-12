package licenta.medicaldata.repository;

import licenta.medicaldata.model.Analiza;
import licenta.medicaldata.model.MedUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AnalizeRepository  extends CrudRepository<Analiza, Integer> {
    public List<Analiza> findAllByPacient(MedUser pacient);
    public Analiza findAnalizaById(Integer id);
    public Analiza findAnalizaByFisier(String filename);
}
