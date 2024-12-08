package com.org.DemandPlanningBackend.Controller;

import com.org.DemandPlanningBackend.Service.SalesPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.YearMonth;

@RestController
@RequestMapping("/sales")
public class SalesController {

    private final SalesPredictionService salesPredictionService;

    @Autowired
    public SalesController(SalesPredictionService salesPredictionService) {
        this.salesPredictionService = salesPredictionService;
    }

    // Endpoint to upload CSV file containing sales data
    @PostMapping("/upload")
    public ResponseEntity<String> uploadSalesData(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Received file: " + file.getOriginalFilename());
            salesPredictionService.loadSalesDataFromFile(file);
            return ResponseEntity.ok("Sales data loaded successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Data validation error: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("File processing error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Endpoint to predict sales for a specific month and year
    @GetMapping("/predict")
    public ResponseEntity<Double> predictSales(@RequestParam int year, @RequestParam int month) {
        try {
            YearMonth yearMonth = YearMonth.of(year, month);
            double predictedSales = salesPredictionService.predictSales(yearMonth);
            return ResponseEntity.ok(predictedSales);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Endpoint to fetch sales for a specific month and year
    @GetMapping("/salesForMonth")
    public ResponseEntity<String> getSalesForMonth(@RequestParam int year, @RequestParam int month) {
        try {
            YearMonth yearMonth = YearMonth.of(year, month);
            Double sales = salesPredictionService.getSalesForMonth(yearMonth);

            if (sales == null) {
                return ResponseEntity.status(404).body("Data Not Available for " + yearMonth);  // Return 404 with message
            }
            return ResponseEntity.ok("Sales for " + yearMonth + ": " + sales);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Endpoint to analyze sales for a specific month and year
    @GetMapping("/analyze")
    public ResponseEntity<String> analyzeSales(@RequestParam int year, @RequestParam int month) {
        try {
            YearMonth yearMonth = YearMonth.of(year, month);
            String analysis = salesPredictionService.analyzeSales(yearMonth);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Endpoint to fetch the best sales month for a specific year
    @GetMapping("/bestSalesMonth")
public ResponseEntity<String> getBestSalesMonth(@RequestParam int year) {
    try {
        String bestMonth = salesPredictionService.getBestSalesMonth(year);
        return ResponseEntity.ok(bestMonth);
    } catch (Exception e) {
        // Return an error message as a plain string
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
}

}
