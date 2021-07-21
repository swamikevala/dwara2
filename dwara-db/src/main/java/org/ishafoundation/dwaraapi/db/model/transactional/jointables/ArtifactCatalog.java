package org.ishafoundation.dwaraapi.db.model.transactional.jointables;

import java.io.Serializable;
import java.util.List;

/*
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.SqlResultSetMapping;

@SqlResultSetMapping(
    name = "CatalogMapping",
    entities = {
        @EntityResult(
            entityClass = org.ishafoundation.dwaraapi.db.model.transactional.jointables.Catalog.class,
            fields = {
                @FieldResult(name="artifactId", column = "id"),
                @FieldResult(name="artifactClass", column = "artifactclass_id"),
                @FieldResult(name="artifactName", column = "name"),
                @FieldResult(name="size", column = "total_size"),
                @FieldResult(name="volumeId", column = "volume_id"),
                @FieldResult(name="volumeGroup", column = "group_ref_id")
            }
        )
    }
) */
public class ArtifactCatalog implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int artifactId;
    public int artifactRefId;
    public String artifactClass;
    public String artifactName;
    public long size;
    public String volumeId;
    public String ingestedDate;
    public String ingestedBy;
    public String format;
    public String oldName;
    public String proxyVolumeId;

    public ArtifactCatalog() {

    }

    public ArtifactCatalog(int artifactId, int artifactRefId, String artifactClass, String artifactName, long size, String volumeId,
            String ingestedDate, String ingestedBy, String oldName) {
        this.artifactId = artifactId;
        this.artifactRefId = artifactRefId;
        this.artifactClass = artifactClass;
        this.artifactName = artifactName;
        this.size = size;
        this.volumeId = volumeId;
        this.ingestedDate = ingestedDate;
        this.ingestedBy = ingestedBy;
        this.oldName = oldName;
    }
}
