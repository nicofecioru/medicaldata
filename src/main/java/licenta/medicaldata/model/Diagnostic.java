package licenta.medicaldata.model;

import licenta.medicaldata.crypto.CryptoConverter;

import javax.persistence.*;

@Entity
@Table(name = "diagnostice")
public class Diagnostic {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    @Convert(converter = CryptoConverter.class)
    String tip;

    @Convert(converter = CryptoConverter.class)
    String descriere;

    @ManyToOne
    MedUser doctor;
    
    @ManyToOne
    private Analiza analiza;

    public Diagnostic(String tip, String descriere, MedUser doctor, Analiza analiza) {
        this.tip = tip;
        this.descriere = descriere;
        this.doctor = doctor;
        this.analiza = analiza;
    }

    public Diagnostic() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getDescriere() {
        return descriere;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }

    public MedUser getDoctor() {
        return doctor;
    }

    public void setDoctor(MedUser doctor) {
        this.doctor = doctor;
    }

    public Analiza getAnaliza() {
        return analiza;
    }

    public void setAnaliza(Analiza analiza) {
        this.analiza = analiza;
    }
}
