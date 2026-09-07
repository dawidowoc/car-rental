package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.common.CarClass;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

interface DailySlotCrudRepository extends JpaRepository<DailySlotEntity, DailySlotId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s from DailySlotEntity s
            where s.id.branchId = :branchId
              and s.id.carClass = :carClass
              and s.id.date between :start and :end
            order by s.id.date
            """)
    List<DailySlotEntity> findForRangeForUpdate(@Param("branchId") String branchId,
                                                @Param("carClass") CarClass carClass,
                                                @Param("start") LocalDate start,
                                                @Param("end") LocalDate end);
}
