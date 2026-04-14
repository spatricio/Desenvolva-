package compra.demo;

import compra.demo.service.CarrinhoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CarrinhoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarrinhoService carrinhoService;

    @BeforeEach
    void limparCarrinho() {
        carrinhoService.listarItens().clear();
    }

    @Test
    void deveCriarProdutoComSucesso() throws Exception {
        String body = """
                {
                  \"id\": 1,
                  \"nome\": \"Mouse\",
                  \"preco\": 99.90,
                  \"quantidade\": 2
                }
                """;

        mockMvc.perform(post("/carrinho/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mouse"))
                .andExpect(jsonPath("$.preco").value(99.9))
                .andExpect(jsonPath("$.quantidade").value(2));
    }

    @Test
    void deveRetornarBadRequestQuandoPayloadForInvalido() throws Exception {
        String bodyInvalido = """
                {
                  \"id\": 1,
                  \"nome\": \"\",
                  \"preco\": -10,
                  \"quantidade\": 0
                }
                """;

        mockMvc.perform(post("/carrinho/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados de entrada invalidos"))
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.preco").exists())
                .andExpect(jsonPath("$.erros.quantidade").exists());
    }

    @Test
    void deveRetornarBadRequestQuandoIdForInvalido() throws Exception {
        mockMvc.perform(get("/carrinho/produtos/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Parametro invalido"))
                .andExpect(jsonPath("$.erros").exists());
    }

    @Test
    void deveRetornarNotFoundQuandoProdutoNaoExistir() throws Exception {
        mockMvc.perform(get("/carrinho/produtos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRemoverProdutoComSucesso() throws Exception {
        String body = """
                {
                  \"id\": 7,
                  \"nome\": \"Teclado\",
                  \"preco\": 120.00,
                  \"quantidade\": 1
                }
                """;

        mockMvc.perform(post("/carrinho/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/carrinho/produtos/7"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/carrinho/produtos/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveCalcularTotalCorretamente() throws Exception {
        String produtoA = """
                {
                  \"id\": 1,
                  \"nome\": \"Mouse\",
                  \"preco\": 100.00,
                  \"quantidade\": 2
                }
                """;

        String produtoB = """
                {
                  \"id\": 2,
                  \"nome\": \"Headset\",
                  \"preco\": 50.00,
                  \"quantidade\": 1
                }
                """;

        mockMvc.perform(post("/carrinho/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoA))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/carrinho/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoB))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/carrinho/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(250.0));
    }

    @Test
    void deveManterCompatibilidadeComEndpointLegadoAdd() throws Exception {
        String body = """
                {
                  \"id\": 10,
                  \"nome\": \"Monitor\",
                  \"preco\": 900.00,
                  \"quantidade\": 1
                }
                """;

        mockMvc.perform(post("/carrinho/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/carrinho/produtos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Monitor"))
                .andExpect(jsonPath("$.preco").value(900.0))
                .andExpect(jsonPath("$.quantidade").value(1));
    }

    @Test
    void deveRetornarNotFoundAoRemoverProdutoInexistente() throws Exception {
        mockMvc.perform(delete("/carrinho/produtos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarStatusDoCarrinho() throws Exception {
        String body = """
                {
                  \"id\": 3,
                  \"nome\": \"Webcam\",
                  \"preco\": 200.00,
                  \"quantidade\": 1
                }
                """;

        mockMvc.perform(post("/carrinho/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/carrinho/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Carrinho online"))
                .andExpect(jsonPath("$.quantidadeItens").value(1))
                .andExpect(jsonPath("$.total").value(200.0));
    }
}
