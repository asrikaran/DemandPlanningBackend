package com.org.DemandPlanningBackend.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.org.DemandPlanningBackend.Model.SalesData;

@Service
public class SalesPredictionService {

    private List<SalesData> salesDataList = new ArrayList<>();

    // Method to load sales data from uploaded CSV file
    public void loadSalesDataFromFile(MultipartFile file) throws IOException {
        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                String[] nextLine;
                csvReader.readNext(); // Skip header

                while ((nextLine = csvReader.readNext()) != null) {
                    if (nextLine.length < 2) {
                        throw new IllegalArgumentException("Invalid CSV format: Missing required columns");
                    }

                    String yearMonthString = nextLine[0];
                    Double sales = nextLine[1].isEmpty() ? null : Double.parseDouble(nextLine[1]);

                    YearMonth yearMonth = YearMonth.parse(yearMonthString);
                    SalesData salesData = new SalesData();
                    salesData.setYearMonth(yearMonth);
                    salesData.setSales(sales);
                    salesDataList.add(salesData);
                }
            }
        } catch (CsvValidationException e) {
            throw new IOException("Error reading CSV data", e);
        } catch (Exception e) {
            throw new IOException("Error processing sales data: " + e.getMessage(), e);
        }
    }

    // Train regression model on available sales data
    public SimpleRegression trainModel() {
        SimpleRegression regression = new SimpleRegression();
        if (salesDataList.isEmpty()) {
            throw new IllegalStateException("No sales data available for training the model.");
        }

        for (SalesData data : salesDataList) {
            if (data.getYearMonth() != null && data.getSales() != null) {
                YearMonth ym = data.getYearMonth();
                double yearMonthValue = ym.getYear() + (ym.getMonthValue() / 12.0);
                regression.addData(yearMonthValue, data.getSales());
            }
        }

        return regression;
    }

    // Predict sales for a given month and year
    public double predictSales(YearMonth yearMonth) {
        SimpleRegression regression = trainModel();
        double yearMonthValue = yearMonth.getYear() + (yearMonth.getMonthValue() / 12.0);
        return regression.predict(yearMonthValue);
    }

    // Get actual sales for a specific month and year
    public Double getSalesForMonth(YearMonth yearMonth) {
        return salesDataList.stream()
                .filter(data -> data.getYearMonth().equals(yearMonth))
                .findFirst()
                .map(SalesData::getSales)
                .orElseThrow(() -> new IllegalArgumentException("Data Not Available for " + yearMonth));
    }

    // Analyze sales for a specific month and year
    public String analyzeSales(YearMonth yearMonth) {
        double predictedSales = predictSales(yearMonth);
        if (predictedSales > 1000) {
            return "Sales are high due to increased marketing and seasonal demand.";
        } else if (predictedSales < 500) {
            return "Sales are low due to market saturation and lack of promotions.";
        } else {
            return "Sales are average, with no significant contributing factors.";
        }
    }

    // Get the best sales month for a specific year
    public String getBestSalesMonth(int year) {
        if (salesDataList.isEmpty()) {
            throw new IllegalStateException("No sales data available to determine the best sales month.");
        }
    
        Optional<SalesData> bestSalesData = salesDataList.stream()
                .filter(data -> data.getSales() != null && data.getYearMonth().getYear() == year)
                .max((d1, d2) -> Double.compare(d1.getSales(), d2.getSales()));
    
        return bestSalesData
                .map(data -> 
                    "The best sales month in the year " + year + " is: " + data.getYearMonth().toString() + 
                    " with sales amounting to $" + data.getSales()
                )
                .orElse("No sales data available for the year " + year);
    }
    
}
