package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.catalog.ArtifactCatalogRequest;
import org.ishafoundation.dwaraapi.api.req.catalog.TapeBulkChangeLocationRequest;
import org.ishafoundation.dwaraapi.api.req.catalog.TapeCatalogRequest;
import org.ishafoundation.dwaraapi.api.req.catalog.TapeChangeLocationRequest;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactCatalog;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TapeCatalog;
import org.ishafoundation.dwaraapi.enumreferences.VolumeHealthStatus;
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

    @GetMapping(value="/catalog/getArtifactclass", produces = "application/json")
    public ResponseEntity<List<Artifactclass>> getArtifactclass(){
        List<Artifactclass> list = catalogService.getAllArtifactclass();
        // List<String> result = new ArrayList<String>();
        // for(Artifactclass a: list) {
        //     result.add(a.getId());
        // }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }
    

    @GetMapping(value="/catalog/getVolumeHealthStatuses", produces = "application/json")
    public ResponseEntity<List<String>> getVolumeHealthStatuses(){
    	List<String> list = new ArrayList<String>();
    	for (VolumeHealthStatus vhs : VolumeHealthStatus.values()) {
    		list.add(vhs.name());
    	}
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping(value="/catalog/getLocations", produces = "application/json")
    public ResponseEntity<List<Location>> getLocations() {
        return ResponseEntity.status(HttpStatus.OK).body(catalogService.getAllLocations());
    }

    @PostMapping(value="/catalog/bulkChangeTapeLocation", produces = "application/json")
    public ResponseEntity<Location> bulkChangeTapeLocation(@RequestBody TapeBulkChangeLocationRequest request) {
        Location l = catalogService.bulkChangeTapeLocation(request.volumeIds, request.newLocation);
        return ResponseEntity.status(HttpStatus.OK).body(l);
    }

    @PostMapping(value="/catalog/changeTapeLocation", produces = "application/json")
    public ResponseEntity<Location> changeTapeLocation(@RequestBody TapeChangeLocationRequest request) {
        Location l = catalogService.changeTapeLocation(request.volumeId, request.newLocation);
        return ResponseEntity.status(HttpStatus.OK).body(l);
    }

    @PostMapping(value="/catalog/updateUsedSpace", produces = "application/json")
    public ResponseEntity<String> updateUsedSpace() {
        catalogService.updateUsedSpace();
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @PostMapping(value="/catalog/updateFinalizedDate", produces = "application/json")
    public ResponseEntity<String> updateFinalizedDate() {
        catalogService.updateFinalizedDate();
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @PostMapping(value="/catalog/artifacts", produces = "application/json")
    public ResponseEntity<List<ArtifactCatalog>> loadArtifactsCatalog(@RequestBody ArtifactCatalogRequest catalogRequest) {
        List<ArtifactCatalog> list = catalogService.findArtifactsCatalog(catalogRequest.artifactClass, catalogRequest.volumeGroup, catalogRequest.copyNumber, catalogRequest.volumeId, 
            catalogRequest.startDate, catalogRequest.endDate, catalogRequest.artifactName, catalogRequest.deleted, catalogRequest.softRenamed, catalogRequest.status);
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
            tapeCatalogRequest.format, tapeCatalogRequest.location, tapeCatalogRequest.startDate, tapeCatalogRequest.endDate, tapeCatalogRequest.healthStatus);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }
}
