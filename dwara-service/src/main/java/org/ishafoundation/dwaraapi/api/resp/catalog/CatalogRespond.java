package org.ishafoundation.dwaraapi.api.resp.catalog;

import java.io.Serializable;

public class CatalogRespond implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 8740685509765929136L;
    public String tapeNumber;
    public String artifactClass;
    public String artifactName;
    public String finalizedDate;
    public long size;
}
