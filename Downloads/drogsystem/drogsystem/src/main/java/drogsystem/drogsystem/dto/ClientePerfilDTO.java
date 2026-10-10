package drogsystem.drogsystem.dto;

import java.time.LocalDate;

public record ClientePerfilDTO(
        String cpf,
        String telefone,
        LocalDate dataNascimento,
        String endereco
) {}
