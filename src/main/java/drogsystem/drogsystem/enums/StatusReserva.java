package drogsystem.drogsystem.enums;

public enum StatusReserva {
    PENDENTE, // aguardando estoque
    DISPONIVEL, // produto disponível, cliente notificado
    CONFIRMADA, // cliente confirmou retirada
    EXPIRADA, // prazo de retirada vencido
    CANCELADA
}
