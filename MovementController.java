package com.inventaire;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MovementController {

    private final ProductController productController;

    public MovementController(ProductController productController) {
        this.productController = productController;
    }

    public List<Movement> getAllMovements() {
        return productController.getAllMovements();
    }

    public List<Movement> getMovementsFiltered(Movement.Type type, String productSearch,
                                                LocalDateTime from, LocalDateTime to) {
        return getAllMovements().stream()
            .filter(m -> type == null || m.getType() == type)
            .filter(m -> productSearch == null || productSearch.isBlank()
                || m.getProductName().toLowerCase().contains(productSearch.toLowerCase())
                || m.getProductReference().toLowerCase().contains(productSearch.toLowerCase()))
            .filter(m -> m.getDateTime() != null && !m.getDateTime().isBefore(from))
            .filter(m -> m.getDateTime() != null && !m.getDateTime().isAfter(to))
            .collect(Collectors.toList());
    }
}
