package sk.stuba.fei.uim.vsa.pr1;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Entity
@Data
public class Student extends Person implements Serializable {

    private Integer rocnikStudia;
    private Integer semesterStudia;
    private String programStudia;
    @OneToOne(mappedBy = "student")
    private Assignment assignment;

    public Student(Long aisId, String name, String email) {
        super(aisId, name, email);
    }

    public Student() {}
}
