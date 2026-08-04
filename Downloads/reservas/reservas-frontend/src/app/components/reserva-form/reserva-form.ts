import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ReservaService } from '../../services/reserva.service';

import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-reserva-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule,
    MatDatepickerModule, MatNativeDateModule,
    MatButtonModule, MatCardModule,
    MatIconModule, MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './reserva-form.html',
  styleUrl: './reserva-form.scss'
})
export class ReservaFormComponent implements OnInit {

  form!: FormGroup;
  editando = false;
  reservaId?: number;
  salvando = false;
  hoje = new Date();

  constructor(
    private fb: FormBuilder,
    private reservaService: ReservaService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      nomeCliente: ['', [Validators.required]],
      emailCliente: ['', [Validators.required, Validators.email]],
      dataCheckIn: [null, Validators.required],
      dataCheckOut: [null, Validators.required],
      quantidadePessoas: [1, [Validators.required, Validators.min(1)]],
      observacoes: ['']
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editando = true;
      this.reservaId = +id;
      this.reservaService.buscarPorId(this.reservaId).subscribe({
        next: (r) => {
          this.form.patchValue({
            ...r,
            dataCheckIn: new Date(r.dataCheckIn + 'T12:00:00'),
            dataCheckOut: new Date(r.dataCheckOut + 'T12:00:00')
          });
        },
        error: () => this.snackBar.open('Reserva não encontrada', 'Fechar', { duration: 3000 })
      });
    }
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando = true;
    const val = this.form.value;
    const payload = {
      ...val,
      dataCheckIn: this.formatarData(val.dataCheckIn),
      dataCheckOut: this.formatarData(val.dataCheckOut)
    };

    const operacao = this.editando
      ? this.reservaService.atualizar(this.reservaId!, payload)
      : this.reservaService.criar(payload);

    operacao.subscribe({
      next: () => {
        this.snackBar.open(
          this.editando ? 'Reserva atualizada!' : 'Reserva criada!',
          'Fechar', { duration: 3000 }
        );
        this.router.navigate(['/reservas']);
      },
      error: (err) => {
        const msg = err.error?.message || 'Erro ao salvar reserva';
        this.snackBar.open(msg, 'Fechar', { duration: 4000 });
        this.salvando = false;
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/reservas']);
  }

  private formatarData(data: Date): string {
    const d = new Date(data);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
