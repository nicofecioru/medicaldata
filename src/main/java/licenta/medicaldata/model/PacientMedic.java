package licenta.medicaldata.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pacient_medic")
public class PacientMedic implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    int id;

    @ManyToOne
    MedUser pacient;

    @ManyToOne
    MedUser medic;

    public PacientMedic() {

    }

    public PacientMedic(MedUser pacient, MedUser medic) {
        this.pacient = pacient;
        this.medic = medic;
    }

    public int getId() {
        return id;
    }

    public MedUser getPacient() {
        return pacient;
    }

    public void setPacient(MedUser pacient) {
        this.pacient = pacient;
    }

    public MedUser getMedic() {
        return medic;
    }

    public void setMedic(MedUser medic) {
        this.medic = medic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PacientMedic that = (PacientMedic) o;
        return Objects.equals(pacient, that.pacient) &&
                Objects.equals(medic, that.medic);
    }
}
