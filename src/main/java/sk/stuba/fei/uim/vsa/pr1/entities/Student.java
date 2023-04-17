package sk.stuba.fei.uim.vsa.pr1.entities;


import lombok.Getter;
import lombok.Setter;
import sk.stuba.fei.uim.vsa.pr1.entities.Assignment;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
public class Student implements Serializable {
    @Id
    @Column(nullable = false, unique = true)
    protected Long aisId;
    private String meno;
    @Column(unique = true)
    private String email;
    private Integer rocnikStudia;
    private Integer semesterStudia;
    private String programStudia;
    @OneToOne(mappedBy = "student")
    private Assignment assignment;

    public Student(Long aisId, String name, String email) {
        this.aisId = aisId;
        this.meno = name;
        this.email = email;
    }

    public Student() {}
}
