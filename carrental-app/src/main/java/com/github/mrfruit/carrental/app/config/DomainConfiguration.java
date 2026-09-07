package com.github.mrfruit.carrental.app.config;

import com.github.mrfruit.carrental.domain.assignment.policy.CarSelectionPolicy;
import com.github.mrfruit.carrental.domain.availability.repository.BlockadeRepository;
import com.github.mrfruit.carrental.domain.availability.repository.DailySlotRepository;
import com.github.mrfruit.carrental.domain.availability.service.BlockadeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DomainConfiguration {

    @Bean
    BlockadeService blockadeService(DailySlotRepository dailySlotRepository, BlockadeRepository blockadeRepository) {
        return new BlockadeService(dailySlotRepository, blockadeRepository);
    }

    @Bean
    CarSelectionPolicy carSelectionPolicy() {
        return new CarSelectionPolicy();
    }
}
