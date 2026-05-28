package com.qiyun.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.admin.common.Result;
import com.qiyun.admin.entity.FlashSale;
import com.qiyun.admin.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/flash-sale")
@RequiredArgsConstructor
public class FlashSaleController {
    
    private final FlashSaleService flashSaleService;
    
    /**
     * 获取秒杀列表
     */
    @GetMapping("/list")
    public Result<List<FlashSale>> list() {
        log.info("获取秒杀列表");
        List<FlashSale> list = flashSaleService.list();
        return Result.success(list);
    }
    
    /**
     * 分页查询秒杀
     */
    @GetMapping("/page")
    public Result<Page<FlashSale>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String sessionTime,
            @RequestParam(required = false) String status) {
        log.info("分页查询秒杀: page={}, pageSize={}, sessionTime={}, status={}", 
                page, pageSize, sessionTime, status);
        Page<FlashSale> result = flashSaleService.page(page, pageSize, sessionTime, status);
        return Result.success(result);
    }
    
    /**
     * 根据场次时间查询秒杀商品
     */
    @GetMapping("/session/{sessionTime}")
    public Result<List<FlashSale>> listBySession(@PathVariable String sessionTime) {
        log.info("根据场次查询秒杀商品: sessionTime={}", sessionTime);
        List<FlashSale> list = flashSaleService.listBySession(sessionTime);
        return Result.success(list);
    }
    
    /**
     * 获取秒杀详情
     */
    @GetMapping("/{id}")
    public Result<FlashSale> getById(@PathVariable Long id) {
        log.info("获取秒杀详情: id={}", id);
        FlashSale flashSale = flashSaleService.getById(id);
        if (flashSale == null) {
            return Result.error("秒杀不存在");
        }
        return Result.success(flashSale);
    }
    
    /**
     * 添加秒杀
     */
    @PostMapping
    public Result<String> save(@RequestBody FlashSale flashSale) {
        log.info("添加秒杀: {}", flashSale);
        boolean success = flashSaleService.save(flashSale);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }
    
    /**
     * 更新秒杀
     */
    @PutMapping
    public Result<String> update(@RequestBody FlashSale flashSale) {
        log.info("更新秒杀: {}", flashSale);
        boolean success = flashSaleService.update(flashSale);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    /**
     * 删除秒杀
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除秒杀: id={}", id);
        boolean success = flashSaleService.delete(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
