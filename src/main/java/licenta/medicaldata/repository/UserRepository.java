package licenta.medicaldata.repository;

import licenta.medicaldata.model.MedUser;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete


public interface UserRepository extends CrudRepository<MedUser, Integer> {
    MedUser findByEmail(String email);
    MedUser findByCnp(String cnp);
    List<MedUser> findAll();
    List<MedUser> findAllByRol(String rol);
}