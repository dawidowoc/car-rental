package com.github.mrfruit.carrental.domain.availability.service;

import com.github.mrfruit.carrental.domain.availability.entity.DailySlot;
import com.github.mrfruit.carrental.domain.availability.exception.SlotUnavailableException;
import com.github.mrfruit.carrental.domain.availability.repository.BlockadeRepository;
import com.github.mrfruit.carrental.domain.availability.repository.DailySlotRepository;
import com.github.mrfruit.carrental.domain.availability.values.BlockadeId;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.mrfruit.carrental.domain.availability.entity.Blockade.activeBlockade;

public class BlockadeService {

    private final DailySlotRepository dailySlotRepository;
    private final BlockadeRepository blockadeRepository;

    public BlockadeService(DailySlotRepository dailySlotRepository, BlockadeRepository blockadeRepository) {
        this.dailySlotRepository = dailySlotRepository;
        this.blockadeRepository = blockadeRepository;
    }

    public BlockadeId block(BranchId branchId, CarClass carClass, DateRange period) {
        var slots = loadSlots(branchId, carClass, period);

        slots.forEach(DailySlot::allocate);
        dailySlotRepository.saveAll(slots);

        var blockade = activeBlockade(branchId, carClass, period);
        blockadeRepository.save(blockade);
        return blockade.id();
    }

    private List<DailySlot> loadSlots(BranchId branchId, CarClass carClass, DateRange period) {
        var slotsByDate = dailySlotRepository.findForRangeForUpdate(branchId, carClass, period).stream()
                .collect(Collectors.toMap(DailySlot::date, Function.identity()));

        return period.dates().stream()
                .map(date -> requireSlot(slotsByDate, branchId, carClass, date))
                .toList();
    }

    private DailySlot requireSlot(Map<LocalDate, DailySlot> slotsByDate, BranchId branchId, CarClass carClass, LocalDate date) {
        var slot = slotsByDate.get(date);
        if (slot == null) {
            throw new SlotUnavailableException("no slot for " + carClass + " at branch " + branchId.value() + " on " + date);
        }
        return slot;
    }
}
