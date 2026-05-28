package com.qiyun.admin.controller;

import com.qiyun.admin.common.Result;
import com.qiyun.admin.entity.DiningTable;
import com.qiyun.admin.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 餐桌控制器
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@Slf4j
@RestController
@RequestMapping("/table")
public class TableController {
    
    @Autowired
    private TableService tableService;
    
    @GetMapping("/list")
    public Result<List<DiningTable>> listTables() {
        log.info("查询餐桌列表");
        List<DiningTable> tables = tableService.lambdaQuery()
                .eq(DiningTable::getDeleted, 0)
                .orderByAsc(DiningTable::getTableNo)
                .list();
        return Result.success(tables);
    }
    
    @GetMapping("/{id}")
    public Result<DiningTable> getTableById(@PathVariable Long id) {
        log.info("查询餐桌详情: tableId={}", id);
        DiningTable table = tableService.getById(id);
        return Result.success(table);
    }
    
    @PostMapping
    public Result<Void> addTable(@RequestBody DiningTable table) {
        log.info("添加餐桌: {}", table);
        tableService.save(table);
        return Result.success(null);
    }
    
    @PutMapping
    public Result<Void> updateTable(@RequestBody DiningTable table) {
        log.info("更新餐桌: {}", table);
        tableService.updateById(table);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteTable(@PathVariable Long id) {
        log.info("删除餐桌: tableId={}", id);
        tableService.removeById(id);
        return Result.success(null);
    }
    
    @PutMapping("/status/{id}")
    public Result<Void> updateTableStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("更新餐桌状态: tableId={}, status={}", id, status);
        DiningTable table = new DiningTable();
        table.setId(id);
        table.setStatus(status);
        tableService.updateById(table);
        return Result.success(null);
    }
}
