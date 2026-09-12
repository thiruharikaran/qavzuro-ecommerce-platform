package com.qavzuro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private double totalRevenue;
    private long totalOrders;
    private long totalCustomers;
    private long pendingReturns;
    private long lowStockProductCount;
    private List<Map<String, Object>> recentOrders;
    private Map<String, Double> revenueTrend; // date label -> revenue
}
