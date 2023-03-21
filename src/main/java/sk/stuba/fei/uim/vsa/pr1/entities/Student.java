package sk.stuba.fei.uim.vsa.pr1.entities;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Entity
@Data
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
    @OneToOne(mappedBy = "student", orphanRemoval = true)
    private Assignment assignment;

    public Student(Long aisId, String name, String email) {
        this.aisId = aisId;
        this.meno = name;
        this.email = email;
    }

    public Student() {}
}
