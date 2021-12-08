package org.ishafoundation.dwaraapi.db.model.transactional;

import javax.persistence.*;

@Entity
@Table(name ="clip_mamtag")
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
        return mamtagId;
    }

    public void setTagId(int tagId) {
        this.mamtagId = tagId;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    @Column(name="id")
     int id;
    int clipId;
    int mamtagId;
}
