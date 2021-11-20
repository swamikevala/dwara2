package org.ishafoundation.dwaraapi.db.model.transactional;

import javax.persistence.*;

@Entity
@Table(name ="clip_tag")
public class ClipTag {
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

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "t_file_sequence")
    @Column(name="id")
     int id;
    int clipId;
    int tagId;
}
