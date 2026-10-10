package drogsystem.drogsystem.controller;

import drogsystem.drogsystem.dto.RelatorioEstoqueDTO;
import drogsystem.drogsystem.dto.RelatorioVendasDTO;
import drogsystem.drogsystem.service.ExcelExportService;
import drogsystem.drogsystem.service.PdfExportService;
import drogsystem.drogsystem.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
public class RelatorioController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private static final MediaType PDF = MediaType.APPLICATION_PDF;

    private final RelatorioService relatorioService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    // ── Relatório de Vendas ───────────────────────────────────────────────────

    @GetMapping("/vendas")
    public ResponseEntity<RelatorioVendasDTO> relatorioVendas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return ResponseEntity.ok(relatorioService.gerarRelatorioVendas(dataInicio, dataFim));
    }

    @GetMapping("/vendas/excel")
    public ResponseEntity<byte[]> exportarVendasExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        RelatorioVendasDTO rel = relatorioService.gerarRelatorioVendas(dataInicio, dataFim);
        return download(excelExportService.exportarVendas(rel), XLSX,
                "relatorio-vendas-" + dataInicio + ".xlsx");
    }

    @GetMapping("/vendas/pdf")
    public ResponseEntity<byte[]> exportarVendasPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        RelatorioVendasDTO rel = relatorioService.gerarRelatorioVendas(dataInicio, dataFim);
        return download(pdfExportService.exportarVendas(rel), PDF,
                "relatorio-vendas-" + dataInicio + ".pdf");
    }

    // ── Relatório de Estoque ──────────────────────────────────────────────────

    @GetMapping("/estoque")
    public ResponseEntity<RelatorioEstoqueDTO> relatorioEstoque() {
        return ResponseEntity.ok(relatorioService.gerarRelatorioEstoque());
    }

    @GetMapping("/estoque/excel")
    public ResponseEntity<byte[]> exportarEstoqueExcel() {
        RelatorioEstoqueDTO rel = relatorioService.gerarRelatorioEstoque();
        return download(excelExportService.exportarEstoque(rel), XLSX, "relatorio-estoque.xlsx");
    }

    @GetMapping("/estoque/pdf")
    public ResponseEntity<byte[]> exportarEstoquePdf() {
        RelatorioEstoqueDTO rel = relatorioService.gerarRelatorioEstoque();
        return download(pdfExportService.exportarEstoque(rel), PDF, "relatorio-estoque.pdf");
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private ResponseEntity<byte[]> download(byte[] content, MediaType type, String filename) {
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(content);
    }
}
