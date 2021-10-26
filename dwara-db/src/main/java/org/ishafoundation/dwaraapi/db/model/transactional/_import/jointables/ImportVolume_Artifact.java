package org.ishafoundation.dwaraapi.db.model.transactional._import.jointables;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;
import org.ishafoundation.dwaraapi.enumreferences.Status;

import com.vladmihalcea.hibernate.type.json.JsonStringType;



@Entity
@Table(name="importvolume_artifact")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class ImportVolume_Artifact {

	
	/*
	 * we want to generate the sequence per table, so we are going for TableGenerator, but when the jvm restarts it jumps values
	 * 
	 * see here
	 * 
	 * https://stackoverflow.com/questions/2895242/whats-the-reason-behind-the-jumping-generatedvaluestrategy-generationtype-tabl
	 * https://forum.hibernate.org/viewtopic.php?f=9&t=980566&start=0
	 * 
	 * To avoid this we are using allocationSize = 1, which has a performance hit, but solves the business usecase.
	 * 
	 * Also check out this, but we are not worried about the performace.
	 * https://vladmihalcea.com/why-you-should-never-use-the-table-identifier-generator-with-jpa-and-hibernate/
	 */
		
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

    @Column(name = "volume_id")
    private String volumeId;
    
    @Column(name = "artifact_id")
    private String artifactId;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status")
	private Status status;
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	

}