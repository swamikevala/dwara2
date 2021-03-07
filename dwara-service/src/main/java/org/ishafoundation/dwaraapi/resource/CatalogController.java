package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.catalog.CatalogRequest;
import org.ishafoundation.dwaraapi.api.resp.catalog.CatalogRespond;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact1;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.Catalog;
import org.ishafoundation.dwaraapi.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class CatalogController {
    private static final Logger logger = LoggerFactory.getLogger(CatalogController.class);

    @Autowired
    CatalogService catalogService;

    @PostMapping(value="/catalog", produces = "application/json")
    public ResponseEntity<List<Catalog>> findCatalogs(@RequestBody CatalogRequest catalogRequest) {
        logger.info("request: " + catalogRequest.tapeNumber + catalogRequest.artifactClass + catalogRequest.volumeGroup);
        List<Catalog> list = catalogService.searchCatalogs(catalogRequest.artifactClass, catalogRequest.volumeGroup, catalogRequest.copyNumber, catalogRequest.tapeNumber, 
            catalogRequest.startDate, catalogRequest.endDate, catalogRequest.artifactName, catalogRequest.tags);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }
}
