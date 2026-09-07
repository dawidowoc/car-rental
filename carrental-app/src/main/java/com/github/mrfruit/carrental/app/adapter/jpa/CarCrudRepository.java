package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.fleet.values.CarStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface CarCrudRepository extends JpaRepository<CarEntity, UUID> {

    @Query("select c from CarEntity c where c.id = :id")
    Optional<CarEntity> findById(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c from CarEntity c
            where c.branchId = :branchId
              and c.carClass = :carClass
              and c.status = :status
            order by c.vin
            """)
    List<CarEntity> findAvailableForUpdate(@Param("branchId") String branchId,
                                           @Param("carClass") CarClass carClass,
                                           @Param("status") CarStatus status);
}
