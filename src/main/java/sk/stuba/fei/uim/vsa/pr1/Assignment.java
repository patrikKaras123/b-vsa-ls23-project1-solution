package sk.stuba.fei.uim.vsa.pr1;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Random;

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
    private Date datumZverejnenia;
    private Date odovzdaniePrace;
    @Enumerated(EnumType.STRING)
    private Typ typ;
    @Enumerated(EnumType.STRING)
    private Status status;

    private String generateRegCislo() {
        // Metoda na vygenerovanie registračného čísla práce, začína na reťazec "FEI-"
        // a následuje náhodných 6 číslic.
        Random random = new Random();
        int cislo = random.nextInt(900000) + 100000;
        return "FEI-" + cislo;
    }
}
