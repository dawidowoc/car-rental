package com.github.mrfruit.carrental.app.api;

import com.github.mrfruit.carrental.app.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AvailabilityIT extends PostgresIntegrationTest {

    private static final String BRANCH = "wroclaw-1";
    private static final LocalDate START = LocalDate.of(2026, 9, 8);
    private static final LocalDate END = START.plusDays(2);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.execute("truncate table reservation, blockade, daily_slot, car, car_assignment");
    }

    @Test
    void listsEveryClassThatHasFreeCapacityOnEveryDayOfThePeriod() throws Exception {
        // given
        seedSlots(BRANCH, "SUV", START, END, 3);
        seedSlots(BRANCH, "SEDAN", START, END, 1);

        // then
        availability(START, END)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available.length()").value(2))
                .andExpect(jsonPath("$.available[0].carClass").value("SEDAN"))
                .andExpect(jsonPath("$.available[0].availableCount").value(1))
                .andExpect(jsonPath("$.available[1].carClass").value("SUV"))
                .andExpect(jsonPath("$.available[1].availableCount").value(3));
    }

    @Test
    void reportsNothingAvailableWhenThePeriodIsNotCovered() throws Exception {
        // given
        seedSlots(BRANCH, "SUV", START, START.plusDays(1), 3);

        // then
        availability(START, END)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").isEmpty());
    }

    @Test
    void reportsNothingAvailableWhenOneDayOfThePeriodIsFullyBooked() throws Exception {
        // given
        seedSlots(BRANCH, "SUV", START, START.plusDays(1), 3);
        seedSlots(BRANCH, "SUV", END, END, 0);

        // then
        availability(START, END)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").isEmpty());
    }

    @Test
    void countGoesDownAfterAReservationIsMade() throws Exception {
        // given
        seedSlots(BRANCH, "SUV", START, END, 2);
        availability(START, END).andExpect(jsonPath("$.available[0].availableCount").value(2));

        // when
        makeReservation();

        // then
        availability(START, END).andExpect(jsonPath("$.available[0].availableCount").value(1));
    }

    @Test
    void classDisappearsOnceItsCapacityIsExhausted() throws Exception {
        // given
        seedSlots(BRANCH, "SUV", START, END, 1);

        // when
        makeReservation();

        // then
        availability(START, END)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").isEmpty());
    }

    @Test
    void ignoresCapacityOfOtherBranches() throws Exception {
        // given
        seedSlots(BRANCH, "SUV", START, END, 1);
        seedSlots("krakow-1", "SEDAN", START, END, 2);

        // then
        availability(START, END)
                .andExpect(jsonPath("$.available.length()").value(1))
                .andExpect(jsonPath("$.available[0].carClass").value("SUV"))
                .andExpect(jsonPath("$.available[0].availableCount").value(1));
    }

    @Test
    void shorterPeriodInsideABookedOneStaysAvailable() throws Exception {
        // given
        seedSlots(BRANCH, "SUV", START, END, 1);
        seedSlots(BRANCH, "SUV", END.plusDays(1), END.plusDays(1), 1);

        // when
        makeReservation();

        // then
        availability(END.plusDays(1), END.plusDays(1))
                .andExpect(jsonPath("$.available.length()").value(1))
                .andExpect(jsonPath("$.available[0].availableCount").value(1));
    }

    private ResultActions availability(LocalDate startDate, LocalDate endDate) throws Exception {
        return mockMvc.perform(get("/availability")
                .param("branchId", BRANCH)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()));
    }

    private void makeReservation() throws Exception {
        var body = """
                {"customerId":"%s","branchId":"%s","carClass":"SUV","startDate":"%s","endDate":"%s","pickUpTime":"10:00"}
                """.formatted(UUID.randomUUID(), BRANCH, START, END);

        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    private void seedSlots(String branchId, String carClass, LocalDate from, LocalDate to, int capacity) {
        from.datesUntil(to.plusDays(1)).forEach(date -> jdbcTemplate.update(
                "insert into daily_slot (branch_id, car_class, slot_date, capacity, allocated) values (?, ?, ?, ?, 0)",
                branchId, carClass, date, capacity));
    }
}
