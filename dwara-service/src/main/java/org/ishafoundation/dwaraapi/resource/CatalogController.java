package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.catalog.ArtifactCatalogRequest;
import org.ishafoundation.dwaraapi.api.req.catalog.TapeCatalogRequest;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact1;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactCatalog;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TapeCatalog;
import org.ishafoundation.dwaraapi.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class CatalogController {
    private static final Logger logger = LoggerFactory.getLogger(CatalogController.class);

    @Autowired
    CatalogService catalogService;

    @PostMapping(value="/catalog/updateFinalizedDate", produces = "application/json")
    public ResponseEntity<String> updateFinalizedDate() {
        catalogService.updateFinalizedDate();
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @PostMapping(value="/catalog/artifacts", produces = "application/json")
    public ResponseEntity<List<ArtifactCatalog>> loadArtifactsCatalog(@RequestBody ArtifactCatalogRequest catalogRequest) {
        List<ArtifactCatalog> list = catalogService.findArtifactsCatalog(catalogRequest.artifactClass, catalogRequest.volumeGroup, catalogRequest.copyNumber, catalogRequest.volumeId, 
            catalogRequest.startDate, catalogRequest.endDate, catalogRequest.artifactName);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PostMapping(value="/catalog/artifactsbyvolumeid", produces = "application/json")
    public ResponseEntity<List<ArtifactCatalog>> loadArtifactsCatalogByVolumeIds(@RequestBody String[] volumeIds) {
        List<ArtifactCatalog> list = catalogService.findArtifactsCatalogByVolumeIds(volumeIds);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PostMapping(value="/catalog/tapes", produces = "application/json")
    public ResponseEntity<List<TapeCatalog>> loadTapesCatalog(@RequestBody TapeCatalogRequest tapeCatalogRequest) {
        List<TapeCatalog> list = catalogService.findTapesCatalog(tapeCatalogRequest.volumeId, tapeCatalogRequest.volumeGroup, tapeCatalogRequest.copyNumber,
            tapeCatalogRequest.format, tapeCatalogRequest.location, tapeCatalogRequest.startDate, tapeCatalogRequest.endDate);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }
}
