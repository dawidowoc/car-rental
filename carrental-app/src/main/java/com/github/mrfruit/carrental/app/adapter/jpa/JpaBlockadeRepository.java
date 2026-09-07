package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.availability.entity.Blockade;
import com.github.mrfruit.carrental.domain.availability.repository.BlockadeRepository;
import org.springframework.stereotype.Repository;

@Repository
class JpaBlockadeRepository implements BlockadeRepository {

    private final BlockadeCrudRepository crudRepository;

    JpaBlockadeRepository(BlockadeCrudRepository crudRepository) {
        this.crudRepository = crudRepository;
    }

    @Override
    public void save(Blockade blockade) {
        crudRepository.save(BlockadeEntity.from(blockade));
    }
}
