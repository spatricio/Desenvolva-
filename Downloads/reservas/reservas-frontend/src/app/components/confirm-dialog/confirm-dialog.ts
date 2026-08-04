import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogData {
  titulo: string;
  mensagem: string;
  confirmLabel?: string;
  cancelLabel?: string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title class="dialog-titulo">
      <mat-icon color="warn">warning_amber</mat-icon>
      {{ data.titulo }}
    </h2>
    <mat-dialog-content>
      <p class="dialog-mensagem">{{ data.mensagem }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button [mat-dialog-close]="false">
        {{ data.cancelLabel || 'Cancelar' }}
      </button>
      <button mat-raised-button color="warn" [mat-dialog-close]="true" cdkFocusInitial>
        {{ data.confirmLabel || 'Confirmar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-titulo {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 1.1rem;
    }
    .dialog-mensagem {
      margin: 0;
      font-size: 0.95rem;
      color: var(--mat-sys-on-surface-variant);
    }
    mat-dialog-actions {
      padding: 8px 16px 16px;
      gap: 8px;
    }
  `]
})
export class ConfirmDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData
  ) {}
}
