package licenta.medicaldata.repository;

import licenta.medicaldata.model.Analiza;
import licenta.medicaldata.model.Diagnostic;
import org.springframework.data.repository.CrudRepository;

public interface DiagnosticRepository extends CrudRepository<Diagnostic, Integer> {
    public Diagnostic findFirstByAnalizaOrderByIdDesc(Analiza analiza);
}
