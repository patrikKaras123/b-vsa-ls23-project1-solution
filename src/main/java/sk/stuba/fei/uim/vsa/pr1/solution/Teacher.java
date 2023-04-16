package sk.stuba.fei.uim.vsa.pr1.solution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Entity
@Table(name = "TEACHER")
@NamedQuery(name = Teacher.FIND_ALL_QUERY, query = "select t from Teacher t")
public class Teacher implements Serializable {
    private static final long serialVersionUID = -3294165768183131788L;

    public static final String FIND_ALL_QUERY = "Teacher.findAll";

    @Id
    private Long aisId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;
    private String institute;

    @Column(nullable = false)
    private String department;

    @OneToMany(mappedBy = "supervisor", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private List<Thesis> supervisedTheses;

    public Teacher() {
    }
}
