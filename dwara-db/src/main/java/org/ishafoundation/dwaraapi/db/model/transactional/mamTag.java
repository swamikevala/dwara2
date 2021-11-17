package org.ishafoundation.dwaraapi.db.model.transactional;

import javax.persistence.*;

@Entity
@Table(name ="mamtag")
public class mamTag {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "t_file_sequence")
    @Column(name="id")
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
