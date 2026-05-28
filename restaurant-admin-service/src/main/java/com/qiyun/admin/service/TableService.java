package com.qiyun.admin.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiyun.admin.entity.DiningTable;
import com.qiyun.admin.mapper.DiningTableMapper;
import org.springframework.stereotype.Service;

/**
 * 餐桌服务
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@Service
public class TableService extends ServiceImpl<DiningTableMapper, DiningTable> {
}
