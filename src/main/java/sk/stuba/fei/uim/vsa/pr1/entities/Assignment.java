package sk.stuba.fei.uim.vsa.pr1.entities;

import lombok.Getter;
import lombok.Setter;
import sk.stuba.fei.uim.vsa.pr1.enums.Status;
import sk.stuba.fei.uim.vsa.pr1.enums.Typ;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
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
    @JoinColumn(nullable = false)
    private Teacher teacher;
    @OneToOne()
    @JoinColumn(nullable = true)
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
        this.pracovisko = teacher.getInstitut();
        this.nazov = title;
        this.popis = description;
        this.typ = Typ.valueOf(type);
        this.status = Status.VOLNA;
        this.datumZverejnenia = LocalDate.now();
        this.odovzdaniePrace = LocalDate.now().plusMonths(3);
        this.registracneCislo = generateRegCislo(teacher.getMeno());
    }

    public Assignment(Teacher teacher, String nazov, String popis, String bachelor, String free) {
        this.teacher = teacher;
        this.nazov = nazov;
        this.popis = popis;
        this.typ = Typ.valueOf(bachelor);
        this.status = Status.valueOf(free);
        this.datumZverejnenia = LocalDate.now();
        this.odovzdaniePrace = LocalDate.now().plusMonths(3);
        this.registracneCislo = generateRegCislo("test");
    }

    public Assignment(Assignment a) {
        this.teacher = a.getTeacher();
        this.nazov = a.getNazov();
        this.popis = a.getPopis();
        this.typ = a.getTyp();
        this.status = a.getStatus();
        this.student = a.getStudent();
        this.id = a.getId();
        this.datumZverejnenia = LocalDate.now();
        this.odovzdaniePrace = LocalDate.now().plusMonths(3);
        this.registracneCislo = a.getRegistracneCislo();
        this.pracovisko = a.getPracovisko();
    }

    private String generateRegCislo(String meno) {
        return "FEI-" + meno + "-" + UUID.randomUUID();
    }
}
