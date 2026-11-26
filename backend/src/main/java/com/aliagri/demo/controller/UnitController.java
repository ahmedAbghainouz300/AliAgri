package com.aliagri.demo.controller;

import com.aliagri.demo.model.Unit;
import com.aliagri.demo.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @GetMapping
    public List<Unit> getAll() {
        return unitService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Unit create(@RequestBody Unit unit) {
        return unitService.create(unit);
    }

    @PutMapping("/{id}")
    public Unit update(@PathVariable Long id, @RequestBody Unit unit) {
        return unitService.update(id, unit);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        unitService.delete(id);
    }
}