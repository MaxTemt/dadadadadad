package com.airport.server.service;

import com.airport.common.dto.ChartData;
import com.airport.common.dto.DashboardStats;
import com.airport.common.exception.DataAccessException;
import com.airport.server.dao.AnalyticsDao;

/**
 * Аналитическая информация для построения графиков на клиенте.
 */
public class AnalyticsService {

    private final AnalyticsDao analyticsDao;

    public AnalyticsService(AnalyticsDao analyticsDao) {
        this.analyticsDao = analyticsDao;
    }

    public DashboardStats dashboard() throws DataAccessException {
        return analyticsDao.dashboard();
    }

    public ChartData occupancyChart() throws DataAccessException {
        return analyticsDao.occupancyByFlight();
    }

    public ChartData statusChart() throws DataAccessException {
        return analyticsDao.bookingStatuses();
    }

    public ChartData revenueChart() throws DataAccessException {
        return analyticsDao.revenueByAirline();
    }
}
