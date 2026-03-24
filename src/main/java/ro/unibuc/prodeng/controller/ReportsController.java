package ro.unibuc.prodeng.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import com.mongodb.lang.NonNull;

import ro.unibuc.prodeng.response.ReportResponse;
import ro.unibuc.prodeng.service.ReportService;

@RestController
@RequestMapping("/api/reports")

public class ReportsController {

    
    @Autowired
    private ReportService reportService;

    @GetMapping("/{id}")
    public ReportResponse getById(@NonNull String id){
        return reportService.getReportById(id);
    }

    @GetMapping("/user/{userId}")
    public List<ReportResponse> getByUser(@NonNull String userId){
        return reportService.getReportsByUser(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@NonNull String id){
        reportService.deleteReport(id);
    }

    @PostMapping("/generate")
    public ReportResponse generateReport(
            @RequestParam @NonNull String userId,
            @RequestParam int year,
            @RequestParam int month) {

        return reportService.generateReport(userId, year, month);
    }


    
}
