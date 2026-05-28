package com.qiyun.restaurant.controller;

import com.qiyun.restaurant.common.Result;
import com.qiyun.restaurant.entity.DiningTable;
import com.qiyun.restaurant.service.DiningTableService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 桌台Controller
 */
@RestController
@RequestMapping("/table")
@Tag(name = "桌台接口")
@Slf4j
public class DiningTableController {
    
    @Autowired
    private DiningTableService diningTableService;
    
    /**
     * 获取所有桌�?
     */
    @GetMapping("/list")
    @ApiOperation("获取所有桌�?)
    public Result<List<DiningTable>> list() {
        List<DiningTable> tables = diningTableService.getAllTables();
        return Result.success(tables);
    }
    
    /**
     * 根据ID获取桌台详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取桌台详情")
    public Result<DiningTable> getById(@PathVariable Long id) {
        DiningTable table = diningTableService.getTableById(id);
        return Result.success(table);
    }
    
    /**
     * 添加桌台
     */
    @PostMapping
    @Operation(summary = "添加桌台")
    public Result<String> add(@RequestBody DiningTable table) {
        diningTableService.addTable(table);
        return Result.success("添加成功");
    }
    
    /**
     * 更新桌台
     */
    @PutMapping
    @Operation(summary = "更新桌台")
    public Result<String> update(@RequestBody DiningTable table) {
        diningTableService.updateTable(table);
        return Result.success("更新成功");
    }
    
    /**
     * 删除桌台
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除桌台")
    public Result<String> delete(@PathVariable Long id) {
        diningTableService.deleteTable(id);
        return Result.success("删除成功");
    }
    
    /**
     * 更新桌台状�?
     */
    @PutMapping("/status/{id}")
    @ApiOperation("更新桌台状�?)
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam String status) {
        diningTableService.updateTableStatus(id, status);
        return Result.success("更新成功");
    }
}
