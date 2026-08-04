import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';

import { Reserva } from '../../models/reserva.model';
import { ReservaService } from '../../services/reserva.service';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-reserva-lista',
  standalone: true,
  imports: [
    CommonModule, RouterLink,
    MatTableModule, MatButtonModule, MatIconModule,
    MatCardModule, MatProgressSpinnerModule,
    MatSnackBarModule, MatTooltipModule,
    MatDialogModule, MatChipsModule, MatBadgeModule
  ],
  templateUrl: './reserva-lista.html',
  styleUrl: './reserva-lista.scss'
})
export class ReservaListaComponent implements OnInit {

  reservas: Reserva[] = [];
  carregando = true;
  colunas = ['id', 'nomeCliente', 'emailCliente', 'dataCheckIn', 'dataCheckOut', 'quantidadePessoas', 'acoes'];

  constructor(
    private reservaService: ReservaService,
    private snackBar: MatSnackBar,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.carregarReservas();
  }

  carregarReservas(): void {
    this.carregando = true;
    this.reservaService.listarTodas().subscribe({
      next: (dados) => {
        this.reservas = dados;
        this.carregando = false;
      },
      error: () => {
        this.snackBar.open('Erro ao carregar reservas', 'Fechar', { duration: 3000 });
        this.carregando = false;
      }
    });
  }

  editar(id: number): void {
    this.router.navigate(['/reservas/editar', id]);
  }

  remover(id: number, nome: string): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '380px',
      data: {
        titulo: 'Remover Reserva',
        mensagem: `Deseja remover a reserva de "${nome}"? Esta ação não pode ser desfeita.`,
        confirmLabel: 'Remover',
        cancelLabel: 'Cancelar'
      }
    });

    ref.afterClosed().subscribe(confirmado => {
      if (!confirmado) return;
      this.reservaService.remover(id).subscribe({
        next: () => {
          this.snackBar.open('Reserva removida com sucesso!', 'Fechar', { duration: 3000 });
          this.carregarReservas();
        },
        error: () => {
          this.snackBar.open('Erro ao remover reserva', 'Fechar', { duration: 3000 });
        }
      });
    });
  }
}
