package licenta.medicaldata.repository;

import licenta.medicaldata.model.MedUser;
import licenta.medicaldata.model.PacientMedic;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PacientMedicRepository extends CrudRepository<PacientMedic, Integer> {
    public List<PacientMedic> getPacientMedicByMedic(MedUser medic);
}
