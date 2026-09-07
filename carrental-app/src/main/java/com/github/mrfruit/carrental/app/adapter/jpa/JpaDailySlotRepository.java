package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.availability.entity.DailySlot;
import com.github.mrfruit.carrental.domain.availability.repository.DailySlotRepository;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class JpaDailySlotRepository implements DailySlotRepository {

    private final DailySlotCrudRepository crudRepository;

    JpaDailySlotRepository(DailySlotCrudRepository crudRepository) {
        this.crudRepository = crudRepository;
    }

    @Override
    public List<DailySlot> findForRangeForUpdate(BranchId branchId, CarClass carClass, DateRange range) {
        return crudRepository.findForRangeForUpdate(branchId.value(), carClass, range.start(), range.end()).stream()
                .map(DailySlotEntity::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<DailySlot> slots) {
        crudRepository.saveAll(slots.stream().map(DailySlotEntity::from).toList());
    }
}
