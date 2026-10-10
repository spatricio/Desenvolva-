package drogsystem.drogsystem.service;

import drogsystem.drogsystem.dto.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] exportarVendas(RelatorioVendasDTO rel) {
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle headerStyle = criarEstiloCabecalho(wb);
            CellStyle moedaStyle = criarEstiloMoeda(wb);

            // ── Resumo ──────────────────────────────────────────────────────
            Sheet resumo = wb.createSheet("Resumo");
            int r = 0;
            cabecalhoLinha(resumo, r++, headerStyle,
                    "Relatório de Vendas: " + rel.dataInicio().format(FMT_DATA)
                            + " a " + rel.dataFim().format(FMT_DATA));
            resumo.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
            r++;
            parLinha(resumo, r++, "Vendas concluídas", String.valueOf(rel.totalVendasConcluidas()));
            parLinha(resumo, r++, "Vendas canceladas", String.valueOf(rel.totalVendasCanceladas()));
            parLinhaMoeda(resumo, r++, "Receita total", rel.receitaTotal(), moedaStyle);
            parLinhaMoeda(resumo, r++, "Ticket médio", rel.ticketMedio(), moedaStyle);
            resumo.autoSizeColumn(0);
            resumo.autoSizeColumn(1);

            // ── Vendas por Dia ───────────────────────────────────────────────
            Sheet porDia = wb.createSheet("Vendas por Dia");
            Row hd = porDia.createRow(0);
            celula(hd, 0, "Data", headerStyle);
            celula(hd, 1, "Nº Vendas", headerStyle);
            celula(hd, 2, "Receita (R$)", headerStyle);
            int rd = 1;
            for (ItemVendasPorDiaDTO d : rel.vendasPorDia()) {
                Row row = porDia.createRow(rd++);
                row.createCell(0).setCellValue(d.data().format(FMT_DATA));
                row.createCell(1).setCellValue(d.totalVendas());
                Cell c = row.createCell(2);
                c.setCellValue(d.receita().doubleValue());
                c.setCellStyle(moedaStyle);
            }
            autoSize(porDia, 3);

            // ── Por Forma de Pagamento ────────────────────────────────────────
            Sheet porPag = wb.createSheet("Por Pagamento");
            Row hp = porPag.createRow(0);
            celula(hp, 0, "Forma de Pagamento", headerStyle);
            celula(hp, 1, "Nº Vendas", headerStyle);
            celula(hp, 2, "Receita (R$)", headerStyle);
            int rp = 1;
            for (VendasPorFormaPagamentoDTO p : rel.vendasPorFormaPagamento()) {
                Row row = porPag.createRow(rp++);
                row.createCell(0).setCellValue(p.formaPagamento().name().replace("_", " "));
                row.createCell(1).setCellValue(p.totalVendas());
                Cell c = row.createCell(2);
                c.setCellValue(p.receita().doubleValue());
                c.setCellStyle(moedaStyle);
            }
            autoSize(porPag, 3);

            // ── Top Produtos ──────────────────────────────────────────────────
            Sheet topProd = wb.createSheet("Top Produtos");
            Row ht = topProd.createRow(0);
            celula(ht, 0, "Produto", headerStyle);
            celula(ht, 1, "Qtd Vendida", headerStyle);
            celula(ht, 2, "Receita (R$)", headerStyle);
            int rt = 1;
            for (ProdutoMaisVendidoDTO p : rel.topProdutos()) {
                Row row = topProd.createRow(rt++);
                row.createCell(0).setCellValue(p.nomeProduto());
                row.createCell(1).setCellValue(p.quantidadeVendida());
                Cell c = row.createCell(2);
                c.setCellValue(p.receita().doubleValue());
                c.setCellStyle(moedaStyle);
            }
            autoSize(topProd, 3);

            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar Excel de vendas.", e);
        }
    }

    public byte[] exportarEstoque(RelatorioEstoqueDTO rel) {
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle headerStyle = criarEstiloCabecalho(wb);
            CellStyle moedaStyle = criarEstiloMoeda(wb);

            // ── Estoque Baixo ─────────────────────────────────────────────────
            Sheet baixo = wb.createSheet("Estoque Baixo");
            Row hb = baixo.createRow(0);
            celula(hb, 0, "Produto", headerStyle);
            celula(hb, 1, "Categoria", headerStyle);
            celula(hb, 2, "Qtd Atual", headerStyle);
            celula(hb, 3, "Qtd Mínima", headerStyle);
            celula(hb, 4, "Preço (R$)", headerStyle);
            int rb = 1;
            for (ProdutoResponseDTO p : rel.produtosAbaixoMinimo()) {
                Row row = baixo.createRow(rb++);
                row.createCell(0).setCellValue(p.nome());
                row.createCell(1).setCellValue(p.categoria().name());
                row.createCell(2).setCellValue(p.quantidadeEstoque());
                row.createCell(3).setCellValue(p.estoqueMinimo());
                Cell c = row.createCell(4);
                c.setCellValue(p.preco().doubleValue());
                c.setCellStyle(moedaStyle);
            }
            autoSize(baixo, 5);

            // ── Lotes Vencidos ────────────────────────────────────────────────
            loteSheet(wb, "Lotes Vencidos", rel.lotesVencidos(), headerStyle);

            // ── Lotes a Vencer ────────────────────────────────────────────────
            loteSheet(wb, "A Vencer 30 dias", rel.lotesAVencer30Dias(), headerStyle);

            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar Excel de estoque.", e);
        }
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private void loteSheet(Workbook wb, String nome, java.util.List<LoteResponseDTO> lotes, CellStyle hs) {
        Sheet sheet = wb.createSheet(nome);
        Row h = sheet.createRow(0);
        celula(h, 0, "Produto", hs);
        celula(h, 1, "Nº Lote", hs);
        celula(h, 2, "Validade", hs);
        celula(h, 3, "Qtd Atual", hs);
        int r = 1;
        for (LoteResponseDTO l : lotes) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(l.nomeProduto());
            row.createCell(1).setCellValue(l.numeroLote());
            row.createCell(2).setCellValue(l.dataValidade().format(FMT_DATA));
            row.createCell(3).setCellValue(l.quantidadeAtual());
        }
        autoSize(sheet, 4);
    }

    private CellStyle criarEstiloCabecalho(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    private CellStyle criarEstiloMoeda(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("R$ #,##0.00"));
        return s;
    }

    private void celula(Row row, int col, String valor, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(valor);
        c.setCellStyle(style);
    }

    private void cabecalhoLinha(Sheet sheet, int rowNum, CellStyle style, String titulo) {
        Row row = sheet.createRow(rowNum);
        celula(row, 0, titulo, style);
    }

    private void parLinha(Sheet sheet, int rowNum, String chave, String valor) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(chave);
        row.createCell(1).setCellValue(valor);
    }

    private void parLinhaMoeda(Sheet sheet, int rowNum, String chave, BigDecimal valor, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(chave);
        Cell c = row.createCell(1);
        c.setCellValue(valor.doubleValue());
        c.setCellStyle(style);
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private byte[] toBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }
}
