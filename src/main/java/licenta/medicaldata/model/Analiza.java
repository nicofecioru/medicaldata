package licenta.medicaldata.model;

import licenta.medicaldata.crypto.CryptoConverter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "analize")
public class
Analiza implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    private MedUser cadruMedical;

    @ManyToOne
    private MedUser pacient;

    private String fisier;

    @Convert(converter = CryptoConverter.class)
    private String tip;

    public Analiza() {
    }

    public Analiza(MedUser cadruMedical, MedUser pacient, String fisier, String tip) {
        this.cadruMedical = cadruMedical;
        this.pacient = pacient;
        this.fisier = fisier;
        this.tip = tip;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MedUser getCadruMedical() {
        return cadruMedical;
    }

    public void setCadruMedical(MedUser cadruMedical) {
        this.cadruMedical = cadruMedical;
    }

    public MedUser getPacient() {
        return pacient;
    }

    public void setPacient(MedUser pacient) {
        this.pacient = pacient;
    }

    public String getFisier() {
        return fisier;
    }

    public void setFisier(String fisier) {
        this.fisier = fisier;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }
}
