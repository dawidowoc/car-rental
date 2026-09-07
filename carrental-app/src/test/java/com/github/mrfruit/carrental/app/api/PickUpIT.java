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
class PickUpIT extends PostgresIntegrationTest {

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
        jdbcTemplate.execute("truncate table reservation, blockade, daily_slot, car, car_assignment");
        seedSlots();
    }

    @Test
    void assignsAnAvailableCarAndMarksItRented() throws Exception {
        // given
        var carId = seedCar("WBA1111111111AAAA", BRANCH, CAR_CLASS);
        var reservationId = makeReservation();

        // when
        mockMvc.perform(post("/reservations/{reservationId}/pick-up", reservationId))
                .andExpect(status().isCreated());

        // then
        var assignment = jdbcTemplate.queryForMap("select * from car_assignment");
        assertThat(assignment.get("reservation_id")).hasToString(reservationId);
        assertThat(assignment.get("car_id")).hasToString(carId.toString());
        assertThat(assignment.get("period_start")).hasToString(START.toString());
        assertThat(assignment.get("period_end")).hasToString(END.toString());
        assertThat(statusOf(carId)).isEqualTo("RENTED");
    }

    @Test
    void picksOnlyCarsMatchingTheBranchAndClassOfTheReservation() throws Exception {
        // given
        var wrongClass = seedCar("WBA1111111111AAAA", BRANCH, "SEDAN");
        var wrongBranch = seedCar("WBA2222222222BBBB", "krakow-1", CAR_CLASS);
        var matching = seedCar("WBA3333333333CCCC", BRANCH, CAR_CLASS);
        var reservationId = makeReservation();

        // when
        mockMvc.perform(post("/reservations/{reservationId}/pick-up", reservationId))
                .andExpect(status().isCreated());

        // then
        assertThat(statusOf(matching)).isEqualTo("RENTED");
        assertThat(statusOf(wrongClass)).isEqualTo("AVAILABLE");
        assertThat(statusOf(wrongBranch)).isEqualTo("AVAILABLE");
    }

    @Test
    void rejectsPickUpWhenNoCarOfTheReservedClassIsFree() throws Exception {
        // given
        seedCar("WBA1111111111AAAA", BRANCH, "SEDAN");
        var reservationId = makeReservation();

        // then
        mockMvc.perform(post("/reservations/{reservationId}/pick-up", reservationId))
                .andExpect(status().isConflict());
        assertThat(countOf("car_assignment")).isZero();
    }

    @Test
    void rejectsPickingUpTheSameReservationTwice() throws Exception {
        // given
        seedCar("WBA1111111111AAAA", BRANCH, CAR_CLASS);
        seedCar("WBA2222222222BBBB", BRANCH, CAR_CLASS);
        var reservationId = makeReservation();
        mockMvc.perform(post("/reservations/{reservationId}/pick-up", reservationId))
                .andExpect(status().isCreated());

        // when
        mockMvc.perform(post("/reservations/{reservationId}/pick-up", reservationId))
                .andExpect(status().isConflict());

        // then
        assertThat(countOf("car_assignment")).isEqualTo(1);
        assertThat(countOf("car where status = 'RENTED'")).isEqualTo(1);
    }

    @Test
    void rejectsPickUpForUnknownReservation() throws Exception {
        // given
        seedCar("WBA1111111111AAAA", BRANCH, CAR_CLASS);

        // then
        mockMvc.perform(post("/reservations/{reservationId}/pick-up", UUID.randomUUID()))
                .andExpect(status().isNotFound());
        assertThat(countOf("car_assignment")).isZero();
    }

    private String makeReservation() throws Exception {
        var body = """
                {"customerId":"%s","branchId":"%s","carClass":"%s","startDate":"%s","endDate":"%s","pickUpTime":"10:00"}
                """.formatted(UUID.randomUUID(), BRANCH, CAR_CLASS, START, END);

        var location = mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        return location.substring(location.lastIndexOf('/') + 1);
    }

    private void seedSlots() {
        START.datesUntil(END.plusDays(1)).forEach(date -> jdbcTemplate.update(
                "insert into daily_slot (branch_id, car_class, slot_date, capacity, allocated) values (?, ?, ?, 5, 0)",
                BRANCH, CAR_CLASS, date));
    }

    private UUID seedCar(String vin, String branchId, String carClass) {
        var carId = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into car (id, vin, car_class, branch_id, status) values (?, ?, ?, ?, 'AVAILABLE')",
                carId, vin, carClass, branchId);
        return carId;
    }

    private String statusOf(UUID carId) {
        return jdbcTemplate.queryForObject("select status from car where id = ?", String.class, carId);
    }

    private int countOf(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Integer.class);
    }
}
