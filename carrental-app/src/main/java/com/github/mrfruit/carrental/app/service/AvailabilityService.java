package com.github.mrfruit.carrental.app.service;

import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AvailabilityService {

    private static final String AVAILABLE_CLASSES = """
            select car_class, min(capacity - allocated) as available_count
            from daily_slot
            where branch_id = ?
              and slot_date between ? and ?
            group by car_class
            having count(*) = ? and min(capacity - allocated) > 0
            order by car_class
            """;

    private final JdbcTemplate jdbcTemplate;

    public AvailabilityService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AvailableCarClass> findAvailable(BranchId branchId, DateRange period) {
        return jdbcTemplate.query(AVAILABLE_CLASSES, (rs, rowNum) -> new AvailableCarClass(
                        CarClass.valueOf(rs.getString("car_class")),
                        rs.getInt("available_count")),
                branchId.value(), period.start(), period.end(), period.lengthInDays());
    }
}
