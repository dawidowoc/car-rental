package com.github.mrfruit.carrental.domain.availability.repository;

import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.availability.entity.DailySlot;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;

import java.util.List;

public interface DailySlotRepository {

    List<DailySlot> findForRangeForUpdate(BranchId branchId, CarClass carClass, DateRange range);

    void saveAll(List<DailySlot> slots);
}
