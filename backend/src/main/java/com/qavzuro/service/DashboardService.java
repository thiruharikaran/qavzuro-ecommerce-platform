package com.qavzuro.service;

import com.qavzuro.domain.*;
import com.qavzuro.dto.response.DashboardStatsResponse;
import com.qavzuro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ProductService productService;

    public DashboardStatsResponse getStats() {
        List<Order> allOrders = orderRepository.findAll();

        double totalRevenue = allOrders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .mapToDouble(Order::getGrandTotal)
                .sum();

        long totalOrders = allOrders.size();
        long totalCustomers = userRepository.findAll().stream()
                .filter(u -> u.getRoleCodes().contains("CUSTOMER"))
                .count();
        long pendingReturns = returnRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReturnStatus.REQUESTED)
                .count();
        long lowStock = productService.lowStock().size();

        Pageable recentPage = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Map<String, Object>> recentOrders = orderRepository.findAll(recentPage).stream()
                .map(o -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("orderNumber", o.getOrderNumber());
                    m.put("status", o.getStatus());
                    m.put("grandTotal", o.getGrandTotal());
                    m.put("createdAt", o.getCreatedAt());
                    return m;
                }).toList();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);
        Map<String, Double> revenueTrend = new LinkedHashMap<>();
        for (Order o : allOrders) {
            if (o.getPaymentStatus() != PaymentStatus.PAID || o.getCreatedAt() == null) continue;
            String day = fmt.format(o.getCreatedAt());
            revenueTrend.merge(day, o.getGrandTotal(), Double::sum);
        }

        return DashboardStatsResponse.builder()
                .totalRevenue(Math.round(totalRevenue * 100.0) / 100.0)
                .totalOrders(totalOrders)
                .totalCustomers(totalCustomers)
                .pendingReturns(pendingReturns)
                .lowStockProductCount(lowStock)
                .recentOrders(recentOrders)
                .revenueTrend(revenueTrend)
                .build();
    }
}
