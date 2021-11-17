package org.ishafoundation.dwaraapi.db.model.transactional;

import javax.persistence.*;

@Entity
@Table(name ="clip")
public class Clip {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "t_file_sequence")
    @Column(name="id")
    private int id;

    String name;
    Integer file_id;
    Integer fps;
    Integer in;
    Integer out;
    String notes;
    Integer duration;
   Integer clip_ref_id;

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

    public Integer getFile_id() {
        return file_id;
    }

    public void setFile_id(Integer file_id) {
        this.file_id = file_id;
    }

    public Integer getFps() {
        return fps;
    }

    public void setFps(Integer fps) {
        this.fps = fps;
    }

    public Integer getIn() {
        return in;
    }

    public void setIn(Integer in) {
        this.in = in;
    }

    public Integer getOut() {
        return out;
    }

    public void setOut(Integer out) {
        this.out = out;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getClip_ref_id() {
        return clip_ref_id;
    }

    public void setClip_ref_id(Integer clip_ref_id) {
        this.clip_ref_id = clip_ref_id;
    }
}
