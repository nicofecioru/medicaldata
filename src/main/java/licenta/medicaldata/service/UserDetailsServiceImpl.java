package licenta.medicaldata.service;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;

import licenta.medicaldata.crypto.SignatureRSA;
import licenta.medicaldata.model.MedUser;
import licenta.medicaldata.repository.PacientMedicRepository;
import licenta.medicaldata.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component(value = "userDetailService")
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    private SignatureRSA signatureRSA = new SignatureRSA();

    @Autowired
    private PacientMedicRepository pacientMedicRepository;


    @Override
    public UserDetails loadUserByUsername(String email) {
        MedUser user = userRepo.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }
        return User.withUsername(user.getEmail())
                .password(user.getParola())
                .roles(user.getRol())
                .build();
    }

    public MedUser getByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public MedUser getByCnp(String cnp) {
        return userRepo.findByCnp(cnp);
    }

    private List getAuthority(String rol) {
        return Arrays.asList(new SimpleGrantedAuthority(rol));
    }

    public List getUsers() {
        return userRepo.findAll();
    }

//    public void saveUser(String email, String nume, String prenume, String parola, String rol){
//        parola=bCryptPasswordEncoder.encode(parola);
//        userRepo.save(new MedUser(email,nume, prenume, parola,rol));
//    }

    public void saveUser(MedUser user) {
        user.setParola(bCryptPasswordEncoder.encode(user.getParola()));
        try {
            if (user.getRol().equals("lab")) {
                KeyPair pair = signatureRSA.generateKeyPair();
                byte[] publicKey = pair.getPublic().getEncoded();
                user.setPublicKey(publicKey);
                userRepo.save(user);
                signatureRSA.storePrivateKey(pair.getPrivate(), "privateKey/" + userRepo.findByCnp(user.getCnp()).getId().toString());
            } else {
                userRepo.save(user);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
