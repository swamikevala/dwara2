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
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.ishafoundation.dwaraapi.api.req.report.RequestReportSize;
import org.ishafoundation.dwaraapi.api.resp.report.RespondReportRestoreTime;
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

    public HashMap<String, HashMap<String, List<String>>> getPipelineReport(String requestedFrom, String requestedTo) {
        HashMap<String, List<String>> ingestPipelineReport = new HashMap<String, List<String>>();
        String condition = "";
        if(requestedFrom != "")
            condition += " and r.requested_at >= '" + requestedFrom + "'";
        if(requestedTo != "")
            condition += " and r.requested_at <= '" + requestedTo + "'";
        
        String ingestedArtifactQuery = "select distinct(details->>'$.staged_filename') from request r join artifact1 a on a.q_latest_request_id=r.id where a.deleted=0 and action_id = 'ingest' and type='system'"
        + condition;
        ingestPipelineReport.put("a.Ingested Artifacts", entityManager.createNativeQuery(ingestedArtifactQuery).getResultList());

        String queuedQuery = "select distinct(details->>'$.staged_filename') from request r join artifact1 a on a.q_latest_request_id=r.id where a.deleted=0 and action_id = 'ingest' and status = 'queued' and type='system'"
        + condition;
        ingestPipelineReport.put("b.Queued", entityManager.createNativeQuery(queuedQuery).getResultList());

        String inProgressQuery = "select distinct(details->>'$.staged_filename') from request r join artifact1 a on a.q_latest_request_id=r.id where a.deleted=0 and action_id = 'ingest' and status = 'in_progress' and type='system'"
        + condition;
        ingestPipelineReport.put("c.In Progress", entityManager.createNativeQuery(inProgressQuery).getResultList());

        String inProgressBeforeYesterdayQuery = "select distinct(details->>'$.staged_filename') from request r join artifact1 a on a.q_latest_request_id=r.id where a.deleted=0 and action_id = 'ingest' and status = 'in_progress' and type='system'"
        + " and requested_at >= subdate(current_date,1)"
        + condition;
        ingestPipelineReport.put("d.In Progress > 24h", entityManager.createNativeQuery(inProgressBeforeYesterdayQuery).getResultList());

        String completedQuery = "select distinct(details->>'$.staged_filename') from request r join artifact1 a on a.q_latest_request_id=r.id where a.deleted=0 and action_id = 'ingest' and (status = 'completed' or status='marked_completed') and type='system'"
        + condition;
        ingestPipelineReport.put("e.Ingest Completed", entityManager.createNativeQuery(completedQuery).getResultList());

        String completedFailuresQuery = "select distinct(details->>'$.staged_filename') from request r join artifact1 a on a.q_latest_request_id=r.id where a.deleted=0 and action_id = 'ingest' and (status = 'completed_failures' or status = 'failed') and type='system'"
        + condition;
        ingestPipelineReport.put("f.Ingest Failed", entityManager.createNativeQuery(completedFailuresQuery).getResultList());

        String copy1WriteFailedQuery = "SELECT distinct(r.details->>'$.staged_filename') FROM job j join request r on j.request_id=r.id join artifact1 a on a.q_latest_request_id=r.id where a.deleted=0 and j.storagetask_action_id='write' and j.group_volume_id like '%1' and (j.status='failed' or j.status='completed_failures')"
        + condition;
        ingestPipelineReport.put("g.Copy1 Write Failed", entityManager.createNativeQuery(copy1WriteFailedQuery).getResultList());

        String copy2WriteFailedQuery = "SELECT distinct(r.details->>'$.staged_filename') FROM job j join request r on j.request_id=r.id join artifact1 a on a.q_latest_request_id=r.id where a.deleted=0 and j.storagetask_action_id='write' and j.group_volume_id like '%2' and (j.status='failed' or j.status='completed_failures')"
        + condition;
        ingestPipelineReport.put("h.Copy2 Write Failed", entityManager.createNativeQuery(copy2WriteFailedQuery).getResultList());

        String copy3WriteFailedQuery = "SELECT distinct(r.details->>'$.staged_filename') FROM job j join request r on j.request_id=r.id join artifact1 a on a.q_latest_request_id=r.id where a.deleted=0 and j.storagetask_action_id='write' and j.group_volume_id like '%3' and (j.status='failed' or j.status='completed_failures')"
        + condition;
        ingestPipelineReport.put("i.Copy3 Write Failed", entityManager.createNativeQuery(copy3WriteFailedQuery).getResultList());

        String proxyGenFailedQuery = "SELECT distinct(r.details->>'$.staged_filename') FROM job j join request r on j.request_id=r.id join artifact1 a on a.q_latest_request_id=r.id where a.deleted=0 and j.processingtask_id='video-proxy-low-gen' and (j.status='failed' or j.status='completed_failures')"
        + condition;
        ingestPipelineReport.put("j.Proxy Gen Failed", entityManager.createNativeQuery(proxyGenFailedQuery).getResultList());

        String mamUpdateFailedQuery = "SELECT distinct(r.details->>'$.staged_filename') FROM job j join request r on j.request_id=r.id join artifact1 a on a.q_latest_request_id=r.id where a.deleted=0 and j.processingtask_id='video-mam-update' and (j.status='failed' or j.status='completed_failures')"
        + condition;
        ingestPipelineReport.put("k.Mam Update Failed", entityManager.createNativeQuery(mamUpdateFailedQuery).getResultList());

        List<String> inStaged3Days = new ArrayList<String>();
        File stagedFile = new File("/data/dwara/staged");
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        for(String p: stagedFile.list()) {
            try {
                Path path = Paths.get(stagedFile.getAbsolutePath() + "/" + p);
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                // System.out.println(p + ": " + attr.creationTime().toString() + ", " + attr.lastAccessTime().toString() + ", " + attr.lastModifiedTime().toString());
                LocalDateTime movedTime = LocalDateTime.ofInstant(attr.lastAccessTime().toInstant(), ZoneId.systemDefault());
                if(movedTime.isBefore(threeDaysAgo)) {
                    inStaged3Days.add(p);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        ingestPipelineReport.put("l.In Staged > 3Days", inStaged3Days);

        HashMap<String, List<String>> restorePipelineReport = new HashMap<String, List<String>>();
        String restoreQuery = "select f.pathname from request r join file1 f on r.file_id=f.id where r.action_id like 'restore%' and r.type='system'"
        + condition;
        restorePipelineReport.put("a.Restore Request", entityManager.createNativeQuery(restoreQuery).getResultList());

        String restoreQueuedQuery = "select f.pathname from request r join file1 f on r.file_id=f.id where r.action_id like 'restore%' and r.type='system' and r.status='queued'"
        + condition;
        restorePipelineReport.put("b.Queued", entityManager.createNativeQuery(restoreQueuedQuery).getResultList());

        String restoreInProgressQuery = "select f.pathname from request r join file1 f on r.file_id=f.id where r.action_id like 'restore%' and r.type='system' and r.status='in_progress'"
        + condition;
        restorePipelineReport.put("c.In Progress", entityManager.createNativeQuery(restoreInProgressQuery).getResultList());

        String restoreCompletedQuery = "select f.pathname from request r join file1 f on r.file_id=f.id where r.action_id like 'restore%' and r.type='system' and (r.status='completed' or r.status='marked_completed')"
        + condition;
        restorePipelineReport.put("d.Restore Completed", entityManager.createNativeQuery(restoreCompletedQuery).getResultList());

        String restoreFailedQuery = "SELECT f.pathname FROM job j join request r on r.id = j.request_id join file1 f on r.file_id=f.id where j.processingtask_id = 'restore' and (j.status='failed' or j.status='completed_failures')"
        + condition;
        restorePipelineReport.put("f.Restore Failed", entityManager.createNativeQuery(restoreFailedQuery).getResultList());

        String restoreMovConversionFailedQuery = "SELECT f.pathname FROM job j join request r on r.id = j.request_id join file1 f on r.file_id=f.id where j.processingtask_id = 'video-digi-2020-mkv-mov-gen' and (j.status='failed' or j.status='completed_failures')"
        + condition;
        restorePipelineReport.put("g.Mov Conversion Failed", entityManager.createNativeQuery(restoreMovConversionFailedQuery).getResultList());

        HashMap<String, HashMap<String, List<String>>> pipelineReport = new HashMap<String, HashMap<String, List<String>>>();
        pipelineReport.put("1.Ingest", ingestPipelineReport);
        pipelineReport.put("2.Restore", restorePipelineReport);

        return pipelineReport;
    }

    public List<RespondReportRestoreTime> getReportRestoreSize(String requestedFrom, String requestedTo) {
        String condition = "";
        if(requestedFrom != "")
            condition += " and r.requested_at >= '" + requestedFrom + "'";
        if(requestedTo != "")
            condition += " and r.requested_at <= '" + requestedTo + "'";
        
        String query = "select f.pathname, f.size, date_format(r.requested_at, '%Y-%m-%d %T'), date_format(j.started_at, '%Y-%m-%d %T'), date_format(r.completed_at, '%Y-%m-%d %T'), time_to_sec(timediff(r.completed_at, j.started_at)) as time_taken, time_to_sec(timediff(r.completed_at, r.requested_at)) as total_time"
        + " from request r join file1 f on r.file_id = f.id join job j on j.request_id = r.id"
        + " where r.action_id like 'restore%' and r.status = 'completed' and r.type='system' and j.storagetask_action_id='restore'"
        + condition;
        Query q = entityManager.createNativeQuery(query);
        List<Object[]> results = q.getResultList();
        List<RespondReportRestoreTime> list = new ArrayList<RespondReportRestoreTime>();
        results.stream().forEach((record) -> {
            int i = 0;
            String pathName = (String) record[i++];
            long size = 0;
            if(record[i] != null)
                size = ((BigInteger)record[i]).longValue();
            i++;
            String requestedAt = (String)record[i++];
            String startedAt = (String)record[i++];
            String completedAt = (String)record[i++];
            long timeTaken = ((BigInteger)record[i++]).longValue();
            long totalTime = ((BigInteger)record[i++]).longValue();

            RespondReportRestoreTime report = new RespondReportRestoreTime(pathName, size, requestedAt, startedAt, completedAt, timeTaken, totalTime);
            list.add(report);
        });
        return list;
    }
}
