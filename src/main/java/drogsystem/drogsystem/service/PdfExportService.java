package drogsystem.drogsystem.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import drogsystem.drogsystem.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY);
    private static final Font SECAO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
    private static final Font CABEC = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

    public byte[] exportarVendas(RelatorioVendasDTO rel) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Título
            doc.add(new Paragraph("DROGSYSTEM — Relatório de Vendas", TITULO));
            doc.add(new Paragraph("Período: " + rel.dataInicio().format(FMT) + " a " + rel.dataFim().format(FMT),
                    NORMAL));
            doc.add(Chunk.NEWLINE);

            // Resumo
            doc.add(new Paragraph("Resumo", SECAO));
            PdfPTable resumo = tabela(2, 0.6f, 0.4f);
            addCabecalhos(resumo, "Indicador", "Valor");
            addLinhas(resumo, "Vendas concluídas", String.valueOf(rel.totalVendasConcluidas()));
            addLinhas(resumo, "Vendas canceladas", String.valueOf(rel.totalVendasCanceladas()));
            addLinhas(resumo, "Receita total", "R$ " + rel.receitaTotal());
            addLinhas(resumo, "Ticket médio", "R$ " + rel.ticketMedio());
            doc.add(resumo);
            doc.add(Chunk.NEWLINE);

            // Vendas por Dia
            doc.add(new Paragraph("Vendas por Dia", SECAO));
            PdfPTable porDia = tabela(3, 0.33f, 0.33f, 0.34f);
            addCabecalhos(porDia, "Data", "Nº Vendas", "Receita (R$)");
            for (ItemVendasPorDiaDTO d : rel.vendasPorDia()) {
                addLinhas(porDia, d.data().format(FMT), String.valueOf(d.totalVendas()), "R$ " + d.receita());
            }
            doc.add(porDia);
            doc.add(Chunk.NEWLINE);

            // Por Forma de Pagamento
            doc.add(new Paragraph("Por Forma de Pagamento", SECAO));
            PdfPTable porPag = tabela(3, 0.4f, 0.3f, 0.3f);
            addCabecalhos(porPag, "Forma", "Nº Vendas", "Receita (R$)");
            for (VendasPorFormaPagamentoDTO p : rel.vendasPorFormaPagamento()) {
                addLinhas(porPag, p.formaPagamento().name().replace("_", " "),
                        String.valueOf(p.totalVendas()), "R$ " + p.receita());
            }
            doc.add(porPag);
            doc.add(Chunk.NEWLINE);

            // Top Produtos
            doc.add(new Paragraph("Top 10 Produtos Mais Vendidos", SECAO));
            PdfPTable topProd = tabela(3, 0.5f, 0.25f, 0.25f);
            addCabecalhos(topProd, "Produto", "Qtd Vendida", "Receita (R$)");
            for (ProdutoMaisVendidoDTO p : rel.topProdutos()) {
                addLinhas(topProd, p.nomeProduto(), String.valueOf(p.quantidadeVendida()), "R$ " + p.receita());
            }
            doc.add(topProd);

        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF de vendas.", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    public byte[] exportarEstoque(RelatorioEstoqueDTO rel) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(new Paragraph("DROGSYSTEM — Relatório de Estoque", TITULO));
            doc.add(Chunk.NEWLINE);

            // Resumo
            doc.add(new Paragraph("Resumo", SECAO));
            PdfPTable resumo = tabela(2, 0.7f, 0.3f);
            addCabecalhos(resumo, "Indicador", "Qtd");
            addLinhas(resumo, "Produtos ativos", String.valueOf(rel.totalProdutosAtivos()));
            addLinhas(resumo, "Produtos abaixo do estoque mín.", String.valueOf(rel.totalProdutosAbaixoMinimo()));
            addLinhas(resumo, "Lotes vencidos", String.valueOf(rel.totalLotesVencidos()));
            addLinhas(resumo, "Lotes a vencer (30 dias)", String.valueOf(rel.totalLotesAVencer30Dias()));
            doc.add(resumo);
            doc.add(Chunk.NEWLINE);

            // Estoque Baixo
            if (!rel.produtosAbaixoMinimo().isEmpty()) {
                doc.add(new Paragraph("Produtos Abaixo do Estoque Mínimo", SECAO));
                PdfPTable t = tabela(4, 0.35f, 0.25f, 0.2f, 0.2f);
                addCabecalhos(t, "Produto", "Categoria", "Qtd Atual", "Qtd Mín.");
                for (ProdutoResponseDTO p : rel.produtosAbaixoMinimo()) {
                    addLinhas(t, p.nome(), p.categoria().name(), String.valueOf(p.quantidadeEstoque()),
                            String.valueOf(p.estoqueMinimo()));
                }
                doc.add(t);
                doc.add(Chunk.NEWLINE);
            }

            // Lotes Vencidos
            if (!rel.lotesVencidos().isEmpty()) {
                doc.add(new Paragraph("Lotes Vencidos", SECAO));
                doc.add(loteTabela(rel.lotesVencidos()));
                doc.add(Chunk.NEWLINE);
            }

            // Lotes a Vencer
            if (!rel.lotesAVencer30Dias().isEmpty()) {
                doc.add(new Paragraph("Lotes a Vencer nos Próximos 30 Dias", SECAO));
                doc.add(loteTabela(rel.lotesAVencer30Dias()));
            }

        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF de estoque.", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Element loteTabela(java.util.List<LoteResponseDTO> lotes) {
        PdfPTable t = tabela(4, 0.35f, 0.25f, 0.2f, 0.2f);
        addCabecalhos(t, "Produto", "Nº Lote", "Validade", "Qtd Atual");
        for (LoteResponseDTO l : lotes) {
            addLinhas(t, l.nomeProduto(), l.numeroLote(), l.dataValidade().format(FMT),
                    String.valueOf(l.quantidadeAtual()));
        }
        return t;
    }

    private PdfPTable tabela(int colunas, float... larguras) {
        PdfPTable t = new PdfPTable(colunas);
        t.setWidthPercentage(100);
        try {
            t.setWidths(larguras);
        } catch (DocumentException ignored) {
        }
        return t;
    }

    private void addCabecalhos(PdfPTable tabela, String... cabecalhos) {
        for (String texto : cabecalhos) {
            PdfPCell cell = new PdfPCell(new Phrase(texto, CABEC));
            cell.setBackgroundColor(new Color(70, 130, 180));
            cell.setPadding(5);
            tabela.addCell(cell);
        }
    }

    private void addLinhas(PdfPTable tabela, String... valores) {
        for (String v : valores) {
            PdfPCell cell = new PdfPCell(new Phrase(v, NORMAL));
            cell.setPadding(4);
            tabela.addCell(cell);
        }
    }
}
