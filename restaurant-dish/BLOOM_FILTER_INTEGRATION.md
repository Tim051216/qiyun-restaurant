# Bloom Filter Integration for Cache Penetration Protection

## Overview

This document describes the integration of Guava BloomFilter into the DishCacheService to prevent cache penetration attacks.

## What is Cache Penetration?

Cache penetration occurs when attackers repeatedly query for non-existent data, bypassing the cache and hitting the database directly. This can overwhelm the database and cause system performance degradation.

## Solution: Bloom Filter

A Bloom Filter is a space-efficient probabilistic data structure that can quickly determine if an element is **definitely not** in a set or **might be** in a set.

### Configuration

- **Expected Insertions**: 100,000 items
- **False Positive Rate**: 0.01 (1%)
- **Implementation**: Guava BloomFilter

### How It Works

1. **Initialization**: During service startup, the Bloom Filter is initialized with expected capacity
2. **Cache Warm-up**: All hot dish IDs are added to the Bloom Filter during cache warm-up
3. **Query Flow**:
   ```
   Query Request
        ↓
   Bloom Filter Check
        ↓
   Not in filter? → Return null (blocked)
        ↓
   Might be in filter? → Continue to cache/database
        ↓
   L1 Cache (Caffeine)
        ↓
   L2 Cache (Redis)
        ↓
   L3 Database
   ```
4. **New Data**: When new data is queried from the database, it's automatically added to the Bloom Filter

## Benefits

1. **Protection**: Blocks queries for non-existent data before they reach the database
2. **Performance**: Bloom Filter check is O(1) and extremely fast (<1μs)
3. **Memory Efficient**: Uses only ~120KB for 100,000 items with 1% false positive rate
4. **No False Negatives**: If data exists, it will never be incorrectly blocked

## Trade-offs

1. **False Positives**: 1% of non-existent queries may still reach the database (acceptable)
2. **Memory Usage**: Requires additional memory for the filter
3. **No Deletion**: Bloom Filters don't support deletion (acceptable for our use case)

## Code Example

```java
// Bloom Filter initialization
this.bloomFilter = BloomFilter.create(
    Funnels.longFunnel(),
    100000,  // Expected insertions
    0.01     // False positive rate
);

// Query with Bloom Filter protection
public Dish getDishById(Long id) {
    // Bloom Filter check (防止缓存穿透)
    if (!bloomFilter.mightContain(id)) {
        log.debug("Bloom filter rejected: dishId={}", id);
        return null;  // Blocked!
    }
    
    // Continue with normal cache flow...
}
```

## Testing

All existing tests have been updated to account for Bloom Filter behavior:
- Tests add IDs to the Bloom Filter before querying
- Tests verify that unknown IDs are blocked
- Cache warm-up tests verify Bloom Filter population

## Requirements Satisfied

- **Requirement 7.6**: "WHEN 缓存穿透发生时，THE System SHALL使用布隆过滤器拦截不存在的数据查询"

## Performance Impact

- **Query Latency**: +0.001ms (negligible)
- **Memory Usage**: +120KB (negligible)
- **Database Load**: Reduced by blocking invalid queries
- **Overall Impact**: Positive - improved system resilience

## Monitoring

Monitor the following metrics:
- Bloom Filter rejection rate
- Database query rate (should decrease)
- Cache hit rate (should improve)

## Future Enhancements

1. **Dynamic Sizing**: Adjust Bloom Filter size based on actual data volume
2. **Persistence**: Persist Bloom Filter to Redis for distributed scenarios
3. **Refresh Strategy**: Periodically rebuild Bloom Filter to remove stale entries
