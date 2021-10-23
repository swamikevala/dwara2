package org.ishafoundation.dwaraapi.service;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.ishafoundation.dwaraapi.api.req.report.RequestReportSize;
import org.ishafoundation.dwaraapi.api.resp.report.RespondPipelineReport;
import org.ishafoundation.dwaraapi.api.resp.report.RespondReportSize;
import org.springframework.stereotype.Component;

@Component
public class ReportService extends DwaraService {
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<RespondReportSize> getReportIngestSize(RequestReportSize request) {
        String condition = "";
        if(request.startDate != "")
            condition += " and r.requested_at >= '" + request.startDate + "'";
        if(request.endDate != "")
            condition += " and r.requested_at <= '" + request.endDate + "'";
        if(request.users != null && request.users.length > 0 && !request.users[0].equals("all")) {
            condition += " and u.name in (";
            for (String user: request.users) {
                condition += "'" + user + "',";
            } 
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }

        if(request.artifactClasses != null && request.artifactClasses.length > 0 && !request.artifactClasses[0].equals("all")) {
            condition += " and a.artifactclass_id in (";
            for (String artifactClass: request.artifactClasses) {
                condition += "'" + artifactClass + "',";
            } 
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }

        String query = "SELECT sum(a.total_size), a.artifactclass_id, date_format(r.requested_at, '" + request.formatDate + "') as timeStone" 
        + " FROM artifact1 a join request r on a.write_request_id=r.id join user u on r.requested_by_id=u.id"
        + " where r.status='completed' and a.artifactclass_id not like '%proxy%'"
        + condition
        + " group by timeStone, a.artifactclass_id"
        + " order by timeStone asc, a.artifactclass_id asc;";

        // System.out.println("query: " + query);
        Query q = entityManager.createNativeQuery(query);
        List<Object[]> results = q.getResultList();
        List<RespondReportSize> list = new ArrayList<RespondReportSize>();
        results.stream().forEach((record) -> {
            int i = 0;
            long _size = 0;
            if(record[i] != null)
                _size = ((BigDecimal)record[i]).longValue();
            i++;
            String _artifactClass = (String) record[i++];
            String _time = (String) record[i++];

            RespondReportSize report = new RespondReportSize(_artifactClass, _time, _size);
            list.add(report);
        });
        return list;

    }

    public List<RespondReportSize> getReportRestoreSize(RequestReportSize request) {
        String condition = "";
        if(request.startDate != "")
            condition += " and r.requested_at >= '" + request.startDate + "'";
        if(request.endDate != "")
            condition += " and r.requested_at <= '" + request.endDate + "'";
        if(request.users != null && request.users.length > 0 && !request.users[0].equals("all")) {
            condition += " and u.name in (";
            for (String user: request.users) {
                condition += "'" + user + "',";
            } 
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }

        if(request.artifactClasses != null && request.artifactClasses.length > 0 && !request.artifactClasses[0].equals("all")) {
            condition += " and a.artifactclass_id in (";
            for (String artifactClass: request.artifactClasses) {
                condition += "'" + artifactClass + "',";
            } 
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }

        String query = "SELECT sum(a.total_size), a.artifactclass_id, date_format(r.requested_at, '" + request.formatDate + "') as timeStone" 
        + " FROM request r join file1 f on r.file_id=f.id join artifact1 a on a.id=f.artifact_id join user u on r.requested_by_id=u.id"
        + " where r.status='completed' and a.artifactclass_id not like '%proxy%'"
        + condition
        + " group by timeStone, a.artifactclass_id"
        + " order by timeStone asc, a.artifactclass_id asc;";

        // System.out.println("query: " + query);
        Query q = entityManager.createNativeQuery(query);
        List<Object[]> results = q.getResultList();
        List<RespondReportSize> list = new ArrayList<RespondReportSize>();
        results.stream().forEach((record) -> {
            int i = 0;
            long _size = 0;
            if(record[i] != null)
                _size = ((BigDecimal)record[i]).longValue();
            i++;
            String _artifactClass = (String) record[i++];
            String _time = (String) record[i++];

            RespondReportSize report = new RespondReportSize(_artifactClass, _time, _size);
            list.add(report);
        });
        return list;

    }

    public RespondPipelineReport getPipelineReport(String requestedFrom, String requestedTo) {
        RespondPipelineReport report = new RespondPipelineReport();
        String condition = "";
        if(requestedFrom != "")
            condition += " and r.requested_at >= '" + requestedFrom + "'";
        if(requestedTo != "")
            condition += " and r.requested_at <= '" + requestedTo + "'";
        
        String ingestedArtifactQuery = "select details->>'$.staged_filename' from request r where action_id = 'ingest' and status = 'completed' and type='system'"
        + condition;
        report.ingestedArtifacts = entityManager.createNativeQuery(ingestedArtifactQuery).getResultList();
        // System.out.println("ingested query: " + ingestedArtifactQuery);

        String inProgressQuery = "select details->>'$.staged_filename' from request r where action_id = 'ingest' and status = 'in_progress' and type='system'"
        + condition;
        report.inprogress = entityManager.createNativeQuery(inProgressQuery).getResultList();

        String inProgressBeforeYesterdayQuery = "select details->>'$.staged_filename' from request r where action_id = 'ingest' and status = 'in_progress' and type='system'"
        + " and requested_at >= subdate(current_date,1)"
        + condition;
        report.inprogressBeforeYesterday = entityManager.createNativeQuery(inProgressBeforeYesterdayQuery).getResultList();

        String completedQuery = "select details->>'$.staged_filename' from request r where action_id = 'ingest' and status = 'completed' and type='system'"
        + condition;
        report.completed = entityManager.createNativeQuery(completedQuery).getResultList();

        String copy1WriteFailedQuery = "SELECT distinct(r.details->>'$.staged_filename') FROM job j join request r on j.request_id=r.id where j.storagetask_action_id='write' and j.group_volume_id like '%1' and j.status='failed'"
        + condition;
        report.copy1WriteFailed = entityManager.createNativeQuery(copy1WriteFailedQuery).getResultList();

        String copy2WriteFailedQuery = "SELECT distinct(r.details->>'$.staged_filename') FROM job j join request r on j.request_id=r.id where j.storagetask_action_id='write' and j.group_volume_id like '%2' and j.status='failed'"
        + condition;
        report.copy2WriteFailed = entityManager.createNativeQuery(copy2WriteFailedQuery).getResultList();

        String copy3WriteFailedQuery = "SELECT distinct(r.details->>'$.staged_filename') FROM job j join request r on j.request_id=r.id where j.storagetask_action_id='write' and j.group_volume_id like '%3' and j.status='failed'"
        + condition;
        report.copy3WriteFailed = entityManager.createNativeQuery(copy3WriteFailedQuery).getResultList();

        String proxyGenFailedQuery = "SELECT distinct(r.details->>'$.staged_filename') FROM job j join request r on j.request_id=r.id where j.processingtask_id='video-proxy-low-gen' and j.status='failed'"
        + condition;
        report.proxyGenFailed = entityManager.createNativeQuery(proxyGenFailedQuery).getResultList();

        String mamUpdateFailedQuery = "SELECT distinct(r.details->>'$.staged_filename') FROM job j join request r on j.request_id=r.id where j.processingtask_id='video-mam-update' and j.status='failed'"
        + condition;
        report.mapUpdateFailed = entityManager.createNativeQuery(mamUpdateFailedQuery).getResultList();

        report.inStaged3Days = new ArrayList<String>();
        File stagedFile = new File("/data/dwara/staged");
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        for(String p: stagedFile.list()) {
            try {
                Path path = Paths.get(stagedFile.getAbsolutePath() + "/" + p);
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                // System.out.println(p + ": " + attr.creationTime().toString() + ", " + attr.lastAccessTime().toString() + ", " + attr.lastModifiedTime().toString());
                LocalDateTime movedTime = LocalDateTime.ofInstant(attr.lastAccessTime().toInstant(), ZoneId.systemDefault());
                if(movedTime.isBefore(threeDaysAgo)) {
                    report.inStaged3Days.add(p);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return report;
    }
}
