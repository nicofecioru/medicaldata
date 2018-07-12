package licenta.medicaldata.model;

import licenta.medicaldata.crypto.CryptoConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "users")
public class MedUser implements Serializable  {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    @Convert(converter = CryptoConverter.class)
    private String email;

    @Convert(converter = CryptoConverter.class)
    private String nume;

    @Convert(converter = CryptoConverter.class)
    private String prenume;

    @Convert(converter = CryptoConverter.class)
    private String parola;

    @Column(unique = true)
    @Convert(converter = CryptoConverter.class)
    private String cnp;

    @Convert(converter = CryptoConverter.class)
    private String telefon;

    private byte[] publicKey;

    @Convert(converter = CryptoConverter.class)
    private String rol;

    public MedUser() {
    }

    public MedUser(String email, String nume, String prenume, String parola, String cnp, String telefon, String rol) {
        this.email = email;
        this.nume = nume;
        this.prenume = prenume;
        this.parola = parola;
        this.cnp = cnp;
        this.telefon = telefon;
        this.rol = rol;
    }

    public MedUser(Integer id){
        this.id=id;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getParola() {
        return parola;
    }

    public void setParola(String parola) {
        this.parola = parola;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getPrenume() {
        return prenume;
    }

    public void setPrenume(String prenume) {
        this.prenume = prenume;
    }

    @Override
    public String toString() {
        return this.nume + " " + this.prenume;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedUser medUser = (MedUser) o;
        return Objects.equals(id, medUser.id);
    }

}
