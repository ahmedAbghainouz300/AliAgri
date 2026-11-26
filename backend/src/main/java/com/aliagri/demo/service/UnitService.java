package com.aliagri.demo.service;

import com.aliagri.demo.model.Unit;
import com.aliagri.demo.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;

    public List<Unit> getAll() {
        return unitRepository.findAll();
    }

    public Unit create(Unit unit) {
        return unitRepository.save(unit);
    }

    public Unit update(Long id, Unit unit) {
        Unit existing = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found"));
        existing.setName(unit.getName());
        return unitRepository.save(existing);
    }

    public void delete(Long id) {
        unitRepository.deleteById(id);
    }
}