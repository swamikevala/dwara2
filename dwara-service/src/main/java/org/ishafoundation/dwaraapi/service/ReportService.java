package org.ishafoundation.dwaraapi.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.ishafoundation.dwaraapi.api.req.report.RequestReportSize;
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
}
