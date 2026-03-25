package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.request.CreateRecordRequest;
import ro.unibuc.prodeng.response.ReportResponse;
import ro.unibuc.prodeng.service.ReportService;

import java.util.Arrays;
import java.util.List;
import java.util.*;


import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
class ReportsControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportsController reportsController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    private ReportResponse report1;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportsController).build();
        objectMapper.findAndRegisterModules();

        report1 = new ReportResponse(
                "1",
                "user1",
                2026,
                3,
                300f,
                Map.of("cat1", 100.0, "cat2", 200.0)
        );
    }

    @Test
    void shouldReturnReportById() throws Exception {
        when(reportService.getReportById("1")).thenReturn(report1);

        mockMvc.perform(get("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.totalAmount").value(300.0));

        verify(reportService).getReportById("1");
    }

    @Test
    void shouldReturnReportsByUser() throws Exception {
        
        List<ReportResponse> reports = List.of(report1);
        when(reportService.getReportsByUser("user1")).thenReturn(reports);

        mockMvc.perform(get("/api/reports/user/user1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].assignedUserId").value("user1"));

        verify(reportService).getReportsByUser("user1");
    }

}