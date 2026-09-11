package com.egjarabo.agenteia.inventario;

public record ConsultaStockResponse(
    String producto, int unidadesDisponibles, double precioUnitario, double costeTotal) {}
