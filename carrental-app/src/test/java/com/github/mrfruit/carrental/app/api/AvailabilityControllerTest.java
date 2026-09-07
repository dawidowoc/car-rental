package com.github.mrfruit.carrental.app.api;

import com.github.mrfruit.carrental.app.service.AvailabilityService;
import com.github.mrfruit.carrental.app.service.AvailableCarClass;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvailabilityController.class)
class AvailabilityControllerTest {

    private static final BranchId BRANCH = new BranchId("wroclaw-1");
    private static final LocalDate START = LocalDate.of(2026, 9, 8);
    private static final LocalDate END = START.plusDays(2);
    private static final DateRange PERIOD = new DateRange(START, END);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;

    @Test
    void listsAvailableCarClassesForThePeriod() throws Exception {
        // given
        when(availabilityService.findAvailable(BRANCH, PERIOD)).thenReturn(List.of(
                new AvailableCarClass(CarClass.SEDAN, 1),
                new AvailableCarClass(CarClass.SUV, 3)));

        // then
        availability().andExpect(status().isOk())
                .andExpect(jsonPath("$.available.length()").value(2))
                .andExpect(jsonPath("$.available[0].carClass").value("SEDAN"))
                .andExpect(jsonPath("$.available[0].availableCount").value(1))
                .andExpect(jsonPath("$.available[1].carClass").value("SUV"))
                .andExpect(jsonPath("$.available[1].availableCount").value(3));
    }

    @Test
    void returnsEmptyListWhenNothingIsAvailable() throws Exception {
        // given
        when(availabilityService.findAvailable(BRANCH, PERIOD)).thenReturn(List.of());

        // then
        availability().andExpect(status().isOk())
                .andExpect(jsonPath("$.available").isArray())
                .andExpect(jsonPath("$.available").isEmpty());
    }

    @Test
    void returnsBadRequestWhenBranchIsMissing() throws Exception {
        // then
        mockMvc.perform(get("/availability")
                        .param("startDate", START.toString())
                        .param("endDate", END.toString()))
                .andExpect(status().isBadRequest());
        verify(availabilityService, never()).findAvailable(any(), any());
    }

    @Test
    void returnsBadRequestWhenPeriodEndsBeforeItStarts() throws Exception {
        // then
        mockMvc.perform(get("/availability")
                        .param("branchId", BRANCH.value())
                        .param("startDate", END.toString())
                        .param("endDate", START.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsBadRequestWhenDatesAreMissing() throws Exception {
        // then
        mockMvc.perform(get("/availability")
                        .param("branchId", BRANCH.value())
                        .param("startDate", START.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsBadRequestForMalformedDate() throws Exception {
        // then
        mockMvc.perform(get("/availability")
                        .param("branchId", BRANCH.value())
                        .param("startDate", "08-09-2026")
                        .param("endDate", END.toString()))
                .andExpect(status().isBadRequest());
    }

    private ResultActions availability() throws Exception {
        return mockMvc.perform(get("/availability")
                .param("branchId", BRANCH.value())
                .param("startDate", START.toString())
                .param("endDate", END.toString()));
    }
}
