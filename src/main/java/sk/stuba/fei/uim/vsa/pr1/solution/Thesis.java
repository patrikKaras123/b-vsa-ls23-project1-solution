package sk.stuba.fei.uim.vsa.pr1.solution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@Entity
@Table(name = "THESIS")
@NamedQuery(name = Thesis.FIND_ALL_QUERY, query = "select t from Thesis t")
@NamedQuery(name = Thesis.FIND_ALL_BY_AUTHOR, query = "select t from Thesis t where t.author.aisId = :studentId")
@NamedQuery(name = Thesis.FIND_ALL_BY_SUPERVISOR, query = "select t from Thesis t where t.supervisor.aisId = :teacherId")
public class Thesis implements Serializable {
    private static final long serialVersionUID = -2566031798277360984L;

    public static final String FIND_ALL_QUERY = "Thesis.findAll";
    public static final String FIND_ALL_BY_AUTHOR = "Thesis.findAllByAuthor";
    public static final String FIND_ALL_BY_SUPERVISOR = "Thesis.findAllBySupervisor";

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String registrationNumber;

    @Column(nullable = false)
    private String title;
    private String description;
    private String department;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Teacher supervisor;

    @OneToOne(fetch = FetchType.EAGER)
    private Student author;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date publishedOn;

    @Temporal(TemporalType.DATE)
    private Date deadline;

    @Enumerated(EnumType.STRING)
    private ThesisType type;

    @Enumerated(EnumType.STRING)
    private ThesisStatus status;

    public Thesis() {
    }

}
