package com.github.mrfruit.carrental.app.api;

import com.github.mrfruit.carrental.app.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class MakeReservationIT extends PostgresIntegrationTest {

    private static final String BRANCH = "wroclaw-1";
    private static final String CAR_CLASS = "SUV";
    private static final LocalDate START = LocalDate.of(2026, 9, 8);
    private static final LocalDate END = START.plusDays(2);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.execute("truncate table reservation, blockade, daily_slot");
    }

    @Test
    void persistsReservationWithBlockadeAndAllocatesEveryDayInThePeriod() throws Exception {
        // given
        seedSlots(START, END, 1);
        var customerId = UUID.randomUUID();

        // when
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(customerId, START, END)))
                .andExpect(status().isCreated());

        // then
        var reservation = jdbcTemplate.queryForMap("select * from reservation");
        assertThat(reservation.get("customer_id")).hasToString(customerId.toString());
        assertThat(reservation.get("branch_id")).isEqualTo(BRANCH);
        assertThat(reservation.get("car_class")).isEqualTo(CAR_CLASS);

        var blockade = jdbcTemplate.queryForMap("select * from blockade");
        assertThat(blockade.get("id")).hasToString(reservation.get("blockade_id").toString());

        assertThat(allocatedPerDay()).containsExactly(1, 1, 1);
    }

    @Test
    void rejectsReservationAndAllocatesNothingWhenOneDayOfThePeriodIsFull() throws Exception {
        // given
        seedSlots(START, START.plusDays(1), 1);
        seedSlots(END, END, 0);

        // when
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(UUID.randomUUID(), START, END)))
                .andExpect(status().isConflict());

        // then
        assertThat(countOf("reservation")).isZero();
        assertThat(countOf("blockade")).isZero();
        assertThat(allocatedPerDay()).containsExactly(0, 0, 0);
    }

    @Test
    void rejectsSecondReservationOnceCapacityIsExhausted() throws Exception {
        // given
        seedSlots(START, END, 1);
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(UUID.randomUUID(), START, END)))
                .andExpect(status().isCreated());

        // when
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(UUID.randomUUID(), START, END)))
                .andExpect(status().isConflict());

        // then
        assertThat(countOf("reservation")).isEqualTo(1);
        assertThat(countOf("blockade")).isEqualTo(1);
        assertThat(allocatedPerDay()).containsExactly(1, 1, 1);
    }

    @Test
    void rejectsReservationForADayThatHasNoConfiguredCapacity() throws Exception {
        // given
        seedSlots(START, START.plusDays(1), 1);

        // then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(UUID.randomUUID(), START, END)))
                .andExpect(status().isConflict());
        assertThat(countOf("reservation")).isZero();
    }

    private void seedSlots(LocalDate from, LocalDate to, int capacity) {
        from.datesUntil(to.plusDays(1)).forEach(date -> jdbcTemplate.update(
                "insert into daily_slot (branch_id, car_class, slot_date, capacity, allocated) values (?, ?, ?, ?, 0)",
                BRANCH, CAR_CLASS, date, capacity));
    }

    private java.util.List<Integer> allocatedPerDay() {
        return jdbcTemplate.queryForList("select allocated from daily_slot order by slot_date", Integer.class);
    }

    private int countOf(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Integer.class);
    }

    private static String requestBody(UUID customerId, LocalDate startDate, LocalDate endDate) {
        return """
                {"customerId":"%s","branchId":"%s","carClass":"%s","startDate":"%s","endDate":"%s","pickUpTime":"10:00"}
                """.formatted(customerId, BRANCH, CAR_CLASS, startDate, endDate);
    }
}
