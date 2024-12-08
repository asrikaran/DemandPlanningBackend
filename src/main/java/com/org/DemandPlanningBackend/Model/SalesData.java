package com.org.DemandPlanningBackend.Model;

import java.time.YearMonth;

public class SalesData {
    private YearMonth yearMonth;
    private Double sales;

    // Getters and Setters
    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(YearMonth yearMonth) {
        this.yearMonth = yearMonth;
    }

    public Double getSales() {
        return sales;
    }

    public void setSales(Double sales) {
        this.sales = sales;
    }
}
