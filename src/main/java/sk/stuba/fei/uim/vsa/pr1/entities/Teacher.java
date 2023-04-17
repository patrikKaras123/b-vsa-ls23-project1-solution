package sk.stuba.fei.uim.vsa.pr1.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Teacher implements Serializable {
    @Id
    @Column(nullable = false, unique = true)
    protected Long aisId;
    private String meno;
    @Column(unique = true)
    private String email;
    private String institut;
    private String oddelenie;
    @OneToMany(mappedBy = "teacher", orphanRemoval = true)
    private List<Assignment> assignmentList;

    public Teacher() {
        this.assignmentList = new ArrayList<>();
    }

    public Teacher(Long aisId, String name, String email, String department) {
        this.aisId = aisId;
        this.meno = name;
        this.email = email;
        this.oddelenie = department;
        this.institut = department;
        this.assignmentList = new ArrayList<>();
    }

    public Teacher(Long aisId, String meno, String email, String oddelenie, String institut, List<Assignment> o) {
        this.aisId = aisId;
        this.meno = meno;
        this.email = email;
        this.oddelenie = oddelenie;
        this.institut = institut;
        this.assignmentList = o;
    }

    public void addAssignment(Assignment assignment) {
        this.assignmentList.add(assignment);
    }
}
