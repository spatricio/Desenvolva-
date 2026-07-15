# Projeto Reservas

API REST inicial para gerenciamento de reservas, criada com Spring Boot, Spring Web e Spring Data JPA.

## Tecnologias

- Java 17
- Spring Boot 3.5.3
- Spring Data JPA
- H2 (ambiente local)
- PostgreSQL (runtime, para ambiente real)

## Como executar

No Windows:

```bat
mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

Aplicacao sobe em `http://localhost:8080`.

Console do H2 (local):

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:reservasdb`
- User: `sa`
- Password: (vazio)

## Endpoints

Base: `/api/reservas`

- `GET /api/reservas` lista todas
- `GET /api/reservas/{id}` busca por id
- `POST /api/reservas` cria reserva
- `PUT /api/reservas/{id}` atualiza reserva
- `DELETE /api/reservas/{id}` remove reserva

### Exemplo de payload

```json
{
	"nomeCliente": "Sergio Silva",
	"emailCliente": "sergio@email.com",
	"dataCheckIn": "2026-07-20",
	"dataCheckOut": "2026-07-25",
	"quantidadePessoas": 2,
	"observacoes": "Quarto com vista para o mar"
}
```

## Regras basicas

- `dataCheckIn` e `dataCheckOut` nao podem estar no passado
- `dataCheckOut` deve ser posterior ao `dataCheckIn`
- `quantidadePessoas` deve ser no minimo 1

