package sk.stuba.fei.uim.vsa.pr1;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Teacher extends Person implements Serializable {

    private String institut;
    private String oddelenie;
    @OneToMany(mappedBy = "pedagog", orphanRemoval = true)
    private List<Assignment> assignmentList;

    public Teacher() {
        super();
        this.assignmentList = new ArrayList<>();
    }

    public Teacher(Long aisId, String name, String email, String department) {
        super(aisId, name, email);
        this.oddelenie = department;
        this.assignmentList = new ArrayList<>();
    }
}
