package org.ishafoundation.dwaraapi.db.model.transactional;

import javax.persistence.*;

@Entity
@Table(name ="clip_cliplist")
public class ClipClipList {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    @Column(name="id")
    private int id;

    int clipId;
   int cliplistId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClipId() {
        return clipId;
    }

    public void setClipId(int clipId) {
        this.clipId = clipId;
    }

    public int getCliplistId() {
        return cliplistId;
    }

    public void setCliplistId(int cliplistId) {
        this.cliplistId = cliplistId;
    }
}
