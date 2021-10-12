package org.ishafoundation.dwaraapi.resource;

import java.util.List;

import org.ishafoundation.dwaraapi.api.req.report.RequestReportIngestSize;
import org.ishafoundation.dwaraapi.api.resp.report.RespondReportIngestSize;
import org.ishafoundation.dwaraapi.service.ReportService;
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
public class ReportController {
    @Autowired
    ReportService reportService;
    
    @PostMapping(value="/report/ingestSize", produces = "application/json")
    public ResponseEntity<List<RespondReportIngestSize>> getArtifactclass(@RequestBody RequestReportIngestSize requestReportIngestSize){
        List<RespondReportIngestSize> list = reportService.getReportIngestSize(requestReportIngestSize);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }
}
