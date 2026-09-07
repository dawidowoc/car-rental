package com.github.mrfruit.carrental.app.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface BlockadeCrudRepository extends JpaRepository<BlockadeEntity, UUID> {
}
