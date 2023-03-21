package sk.stuba.fei.uim.vsa.pr1.entities;

import lombok.Data;
import sk.stuba.fei.uim.vsa.pr1.enums.Status;
import sk.stuba.fei.uim.vsa.pr1.enums.Typ;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

@Entity
@Data
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private String registracneCislo;
    private String nazov;
    private String popis;
    private String pracovisko;
    @ManyToOne(optional = false)
    private Teacher teacher;
    @OneToOne
    private Student student;
    private LocalDate datumZverejnenia;
    private LocalDate odovzdaniePrace;
    @Enumerated(EnumType.STRING)
    private Typ typ;
    @Enumerated(EnumType.STRING)
    private Status status;

    public Assignment(){}

    public Assignment(Teacher teacher, String title, String type, String description) {
        this.teacher = teacher;
        this.nazov = title;
        this.popis = description;
        this.typ = Typ.valueOf(type);
        this.status = Status.Free;
        this.datumZverejnenia = LocalDate.now();
        this.odovzdaniePrace = LocalDate.now().plusMonths(3);
        this.registracneCislo = generateRegCislo(teacher.getMeno());
    }

    private String generateRegCislo(String meno) {
        return "FEI-" + meno + "-" + UUID.randomUUID();
    }
}
