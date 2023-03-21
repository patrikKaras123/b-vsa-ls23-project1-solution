package sk.stuba.fei.uim.vsa.pr1;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@MappedSuperclass
public class Person implements Serializable {
    @Id
    @Column(nullable = false, unique = true)
    private Long aisId;
    private String meno;
    @Column(unique = true)
    private String email;

    public Person(Long aisId, String name, String email) {
        this.aisId = aisId;
        this.meno = name;
        this.email = email;
    }

    public Person() {}
}
