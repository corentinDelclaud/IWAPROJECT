package iwaproject.transaction.dto;

public record ProductDTO(
    Integer idService,
    String game,
    String serviceType,
    String description,
    Float price,
    Boolean unique,
    Boolean isAvailable,
    String idProvider  // String au lieu de Integer
) {}